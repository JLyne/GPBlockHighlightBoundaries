package com.github.gpaddons.blockhighlightboundaries.style;

import com.github.gpaddons.blockhighlightboundaries.BlockHighlightElementProvider;
import com.github.gpaddons.blockhighlightboundaries.HighlightConfiguration;
import com.github.gpaddons.blockhighlightboundaries.type.BlockHighlightElement;
import com.github.gpaddons.blockhighlightboundaries.type.VisualizationElementType;
import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.BlockBoundaryVisualization;
import com.griefprevention.visualization.BlockElement;
import com.griefprevention.visualization.Boundary;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.util.BoundingBox;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * A {@link BlockBoundaryVisualization} that always displays the actual depth of the boundary in
 * addition to the visualized depth.
 */
public class BlockHighlightVisualization extends BlockBoundaryVisualization {

  protected final @NotNull HighlightConfiguration config;
  protected final @NotNull BlockHighlightElementProvider elementProvider;
  protected final @Range(from = 1, to = 500) int step;
  protected final BoundingBox displayZoneArea;
  private long lastSend = 0;

  /**
   * Construct a new {@code RealCornerVisualization} with the given configuration.
   *
   * @param world the {@link World} being visualized in
   * @param visualizeFrom the {@link IntVector} representing the world coordinate being visualized from
   * @param height the height of the visualization
   * @param config the {@link HighlightConfiguration} containing additional settings
   */
  public BlockHighlightVisualization(
      @NotNull World world,
      @NotNull IntVector visualizeFrom,
      int height,
      @NotNull HighlightConfiguration config,
      @NotNull BlockHighlightElementProvider elementProvider) {
    super(world, visualizeFrom, height);

    this.step = config.getSpacing();
    this.displayZoneArea = new BoundingBox(
        new IntVector(
            visualizeFrom.x() - config.getViewDistance(),
            world.getMinHeight(),
            visualizeFrom.z() - config.getViewDistance()),
        new IntVector(
            visualizeFrom.x() + config.getViewDistance(),
            world.getMaxHeight(),
            visualizeFrom.z() + config.getViewDistance()));
    this.elementProvider = elementProvider;
    this.config = config;
  }

  @Override
  protected void draw(@NotNull Player player,
      @NotNull Boundary boundary) {
    this.lastSend = System.currentTimeMillis();
    try {
      drawBoundary(boundary);
    } catch (Exception e) {
      Collection<BlockElement> fallthroughElements = new ArrayList<>();
      for (BlockElement element : this.elements) {
        if (element instanceof BlockHighlightElement highlightElement) {
          NamedTextColor color = config.getClosestChatColor(boundary.type(), highlightElement.getElementType());
          fallthroughElements.add(elementProvider.getFallthroughElement(element.getCoordinate(), color));
        } else {
          // Somehow not our element? Someone else's problem if it fails again.
          fallthroughElements.add(element);
        }
      }

      this.elements.clear();
      this.elements.addAll(fallthroughElements);

      // Re-try the draw.
      drawBoundary(boundary);

      // TODO get logger instance in a better way (via config? Seems hacky, but it's an impl dao)
      Logger logger = Logger.getLogger("GPBlockHighlightBoundaries");
      logger.log(Level.SEVERE, "Caught exception while visualizing a claim! Please report this:");
      logger.log(Level.SEVERE, e, () -> "Visualization error");
    }
  }

  protected void drawBoundary(@NotNull Boundary boundary) {
    BoundingBox area = boundary.bounds();

    // Trim to area - allows for simplified display containment check later.
    BoundingBox displayZone = displayZoneArea.intersection(area);

    // If area is not inside display zone, there is nothing to display.
    if (displayZone == null) return;

    Consumer<@NotNull IntVector> addCorner = addElement(boundary, VisualizationElementType.CORNER);
    Consumer<@NotNull IntVector> addSide = addElement(boundary, VisualizationElementType.SIDE);
    @NotNull BiConsumer<@NotNull IntVector, @NotNull IntVector> addScaledSide =
        addScaledElement(boundary, VisualizationElementType.SIDE);
    @NotNull BiConsumer<@NotNull IntVector, @NotNull IntVector> addScaledCorner =
        addScaledElement(boundary, VisualizationElementType.SIDE);

    // Only check side blocks in display zone
    int startX = Math.max(area.getMinX() + step, displayZone.getMinX());
    int endX = Math.min(area.getMaxX() - step / 2, displayZone.getMaxX());
    int startZ = Math.max(area.getMinZ() + step, displayZone.getMinZ());
    int endZ = Math.min(area.getMaxZ() - step / 2, displayZone.getMaxZ());
    int x = startX;
    int z = startZ;

    // Elements can be scaled to optimise element count if step is 1
    if (config.getType().isScalable() && step == 1) {
      // Can use the same Y for all elements
      int y = findDisplayY(startX, height, startZ, displayZone.getMinY());

      // North
      addScaledDisplayed(displayZone, new IntVector(startX, y, area.getMaxZ()),
          new IntVector(endX - startX, 1, 1), addScaledSide);
      // South
      addScaledDisplayed(displayZone, new IntVector(startX, y, area.getMinZ()),
          new IntVector(endX - startX, 1, 1), addScaledSide);
      // East
      addScaledDisplayed(displayZone, new IntVector(area.getMaxX(), y, startZ),
          new IntVector(1, 1, endZ - startZ), addScaledSide);
      // West
      addScaledDisplayed(displayZone, new IntVector(area.getMinX(), y, startZ),
          new IntVector(1, 1, endZ - startZ), addScaledSide);
    } else {
      // North and south boundaries
      while ((x += step) < endX)
      {
        addAdjustedDisplayed(x, height, area.getMaxZ(), displayZone, addSide);
        addAdjustedDisplayed(x, height, area.getMinZ(), displayZone, addSide);
      }

      // East and west boundaries
      while ((z += step) < endZ)
      {
        addAdjustedDisplayed(area.getMinX(), height, z, displayZone, addSide);
        addAdjustedDisplayed(area.getMaxX(), height, z, displayZone, addSide);
      }

      // First and last step are always directly adjacent to corners
      // Not needed when step == 1 as every block will already be highlighted
      if (step > 1) {
        if (area.getLength() > 2)
        {
          addAdjustedDisplayed(area.getMinX() + 1, height, area.getMaxZ(), displayZone, addSide);
          addAdjustedDisplayed(area.getMinX() + 1, height, area.getMinZ(), displayZone, addSide);
          addAdjustedDisplayed(area.getMaxX() - 1, height, area.getMaxZ(), displayZone, addSide);
          addAdjustedDisplayed(area.getMaxX() - 1, height, area.getMinZ(), displayZone, addSide);
        }
        if (area.getWidth() > 2)
        {
          addAdjustedDisplayed(area.getMinX(), height, area.getMinZ() + 1, displayZone, addSide);
          addAdjustedDisplayed(area.getMaxX(), height, area.getMinZ() + 1, displayZone, addSide);
          addAdjustedDisplayed(area.getMinX(), height, area.getMaxZ() - 1, displayZone, addSide);
          addAdjustedDisplayed(area.getMaxX(), height, area.getMaxZ() - 1, displayZone, addSide);
        }
      }
    }

    // Add corners last to override any other elements created by very small claims.
    addAdjustedDisplayed(area.getMinX(), height, area.getMaxZ(), displayZone, addCorner);
    addAdjustedDisplayed(area.getMaxX(), height, area.getMaxZ(), displayZone, addCorner);
    addAdjustedDisplayed(area.getMinX(), height, area.getMinZ(), displayZone, addCorner);
    addAdjustedDisplayed(area.getMaxX(), height, area.getMinZ(), displayZone, addCorner);
  }

  protected void addAdjustedDisplayed(
      int x, int y, int z,
      @NotNull BoundingBox displayZone,
      @NotNull Consumer<@NotNull IntVector> addElement) {
    IntVector coordinate = new IntVector(x, findDisplayY(x, y, z, displayZone.getMinY()), z);

    if (isAccessible(displayZone, coordinate)) {
      addElement.accept(coordinate);
    }
  }

  protected void addScaledDisplayed(
      @NotNull BoundingBox displayZone,
      @NotNull IntVector coordinate,
      @NotNull IntVector scale,
      @NotNull BiConsumer<@NotNull IntVector, @NotNull IntVector> addScaledElement) {
    if (isAccessible(displayZone, coordinate)) {
      addScaledElement.accept(coordinate, scale);
    }
  }

  @Override
  protected @NotNull Consumer<@NotNull IntVector> addCornerElements(@NotNull Boundary boundary) {
    return addElement(boundary, VisualizationElementType.CORNER);
  }

  @Override
  protected @NotNull Consumer<@NotNull IntVector> addSideElements(@NotNull Boundary boundary) {
    return addElement(boundary, VisualizationElementType.SIDE);
  }

  protected @NotNull Consumer<@NotNull IntVector> addElement(@NotNull Boundary boundary, VisualizationElementType type) {
    return coordinate -> {
      int minY = boundary.bounds().getMinY();
      if (minY != coordinate.y()) {
        elements.add(elementProvider.getElement(coordinate, boundary, type));
      }
      // Always display actual bottom corners as well.
      elements.add(
          elementProvider.getElement(
              new IntVector(coordinate.x(), minY, coordinate.z()),
              boundary,
              type));
    };
  }

  protected @NotNull BiConsumer<@NotNull IntVector, @NotNull IntVector> addScaledElement(@NotNull Boundary boundary, VisualizationElementType type) {
    return (coordinate, scale) -> {
      int minY = boundary.bounds().getMinY();
      if (minY != coordinate.y()) {
        elements.add(elementProvider.getScaledElement(coordinate, scale, boundary, type));
      }
      // Always display actual bottom corners as well.
      elements.add(
          elementProvider.getScaledElement(
              new IntVector(coordinate.x(), minY, coordinate.z()),
              scale,
              boundary,
              type));
    };
  }

  @Override
  protected void scheduleRevert(@NotNull Player player, @NotNull PlayerData playerData) {
    // Use GP to schedule the revert - we don't have a concept of a managing plugin here, the plugin
    // is the implementation. Basically the same as super method but with a configurable delay.
    GriefPrevention.instance.getServer().getScheduler().scheduleSyncDelayedTask(
        GriefPrevention.instance,
        () -> {
          // Only revert if this is the active visualization.
          if (playerData.getVisibleBoundaries() == this) playerData.setVisibleBoundaries(null);
        },
        config.getDisplayMillis() / 50);
  }

  @Override
  public void revert(@Nullable Player player) {
    if (!config.getType().requiresErase()
        && (lastSend + config.getDisplayMillis()) < System.currentTimeMillis()) {
      // If already erased, do not send more packets.
      return;
    }

    super.revert(player);
  }

  /**
   * Find an eligible display coordinate in the same column as the given coordinate.
   *
   * @param displayCoord the provided coordinate
   * @param minY the minimum Y value of the resulting coordinate
   * @return the display coordinate
   */
  protected int findDisplayY(int x, int y, int z, int minY) {
    return defaultDisplayY(x, y, z, minY);
  }

  /**
   * Get the default display coordinate for a given coordinate.
   *
   * @param vector the provided coordinate
   * @param minY the minimum Y value of the resulting coordinate
   * @return the default display coordinate
   */
  protected int defaultDisplayY(int x, int y, int z, int minY) {
    // As most displays are triggered at eye level, moving 2 blocks deeper by default
    // makes the display far less obstructive while still being clearly visible.
    return Math.max(minY, y - 2);
  }
}
