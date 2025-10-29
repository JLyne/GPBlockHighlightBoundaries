package com.github.gpaddons.blockhighlightboundaries.style;

import com.github.gpaddons.blockhighlightboundaries.BlockHighlightElementProvider;
import com.github.gpaddons.blockhighlightboundaries.HighlightConfiguration;
import com.github.gpaddons.blockhighlightboundaries.type.VisualizationElementType;
import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.Boundary;
import me.ryanhamshire.GriefPrevention.util.BoundingBox;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A {@link BlockHighlightVisualization} abstraction that attempts to snap elements to the nearest
 * surface.
 */
public class SurfaceBlockHighlightVisualisation extends BlockHighlightVisualization {
  private int lastLoadedDisplayHeight = Integer.MIN_VALUE;

  public SurfaceBlockHighlightVisualisation(
      @NotNull World world,
      @NotNull IntVector visualizeFrom,
      int height,
      @NotNull HighlightConfiguration config,
      @NotNull BlockHighlightElementProvider elementProvider) {
    super(world, visualizeFrom, height, config, elementProvider);
  }

  @Override
  protected void drawBoundary(@NotNull Boundary boundary) {
    // Elements can be scaled to optimise element count if step is 1
    if (!config.getType().isScalable() || step != 1) {
      super.drawBoundary(boundary);
      return;
    }

    BoundingBox area = boundary.bounds();

    // Trim to area - allows for simplified display containment check later.
    BoundingBox displayZone = displayZoneArea.intersection(area);

    // If area is not inside display zone, there is nothing to display.
    if (displayZone == null) return;

    Consumer<@NotNull IntVector> addCorner = addElement(boundary, VisualizationElementType.CORNER);
    @NotNull BiConsumer<@NotNull IntVector, @NotNull IntVector> addScaledSide =
        addScaledElement(boundary, VisualizationElementType.SIDE);

    // Only check side blocks in display zone
    int startX = Math.max(area.getMinX() + 1, displayZone.getMinX());
    int endX = Math.min(area.getMaxX(), displayZone.getMaxX());
    int startZ = Math.max(area.getMinZ() + 1, displayZone.getMinZ());
    int endZ = Math.min(area.getMaxZ(), displayZone.getMaxZ());
    int x = startX;
    int z = startZ;

    do {
      int scale = 1;
      int currentY = findDisplayY(x, height, displayZone.getMaxZ(), displayZone.getMinY());
      while(++x < endX &&
          findDisplayY(x, height, displayZone.getMaxZ(), displayZone.getMinY()) == currentY) {
        scale++;
      }

      addScaledDisplayed(displayZone, new IntVector(x - scale, currentY, area.getMaxZ()),
        new IntVector(scale, 1, 1), addScaledSide);
    } while(x < endX);

    x = startX;

    do {
      int scale = 1;
      int currentY = findDisplayY(x, height, displayZone.getMinZ(), displayZone.getMinY());
      while(++x < endX &&
          findDisplayY(x, height, displayZone.getMinZ(), displayZone.getMinY()) == currentY) {
        scale++;
      }

      addScaledDisplayed(displayZone, new IntVector(x - scale, currentY, area.getMinZ()),
        new IntVector(scale, 1, 1), addScaledSide);
    } while(x < endX);

    do {
      int scale = 1;
      int currentY = findDisplayY(displayZone.getMaxX(), height, z, displayZone.getMinY());
      while(++z < endZ &&
          findDisplayY(displayZone.getMaxX(), height, z, displayZone.getMinY()) == currentY) {
        scale++;
      }

      addScaledDisplayed(displayZone, new IntVector(displayZone.getMaxX(), currentY, z - scale),
        new IntVector(1, 1, scale), addScaledSide);
    } while(z < endZ);

    z = startZ;

    do {
      int scale = 1;
      int currentY = findDisplayY(displayZone.getMinX(), height, z, displayZone.getMinY());
      while(++z < endZ &&
          findDisplayY(displayZone.getMinX(), height, z, displayZone.getMinY()) == currentY) {
        scale++;
      }

      addScaledDisplayed(displayZone, new IntVector(displayZone.getMinX(), currentY, z - scale),
        new IntVector(1, 1, scale), addScaledSide);
    } while(z < endZ);

    // North
    addScaledDisplayed(displayZone, new IntVector(startX, displayZone.getMinY(), area.getMaxZ()),
          new IntVector(endX - startX, 1, 1), addScaledSide);
    // South
    addScaledDisplayed(displayZone, new IntVector(startX, displayZone.getMinY(), area.getMinZ()),
          new IntVector(endX - startX, 1, 1), addScaledSide);
    // East
    addScaledDisplayed(displayZone, new IntVector(area.getMaxX(), displayZone.getMinY(), startZ),
          new IntVector(1, 1, endZ - startZ), addScaledSide);
    // West
    addScaledDisplayed(displayZone, new IntVector(area.getMinX(), displayZone.getMinY(), startZ),
          new IntVector(1, 1, endZ - startZ), addScaledSide);

    // Add corners last to override any other elements created by very small claims.
    addAdjustedDisplayed(area.getMinX(), height, area.getMaxZ(), displayZone, addCorner);
    addAdjustedDisplayed(area.getMaxX(), height, area.getMaxZ(), displayZone, addCorner);
    addAdjustedDisplayed(area.getMinX(), height, area.getMinZ(), displayZone, addCorner);
    addAdjustedDisplayed(area.getMaxX(), height, area.getMinZ(), displayZone, addCorner);
  }

  @Override
  protected @NotNull BiConsumer<@NotNull IntVector, @NotNull IntVector> addScaledElement(
      @NotNull Boundary boundary, VisualizationElementType type) {
    return (coordinate, scale) -> {
      elements.add(elementProvider.getScaledElement(coordinate, scale, boundary, type));
    };
  }

  @Override
  protected int findDisplayY(int x, int y, int z, int minY) {
    if (!world.isChunkLoaded(x / 16, z / 16)) {
      // Don't load chunks to find a display coordinate.
      return defaultDisplayY(x, y, z, minY);
    }

    // Highest block + 1 air
    int maxY = world.getHighestBlockAt(x, z).getY() + 1;
    Block startBlock = world.getBlockAt(x, y, z);

    // Find suitable surface block (solid block below a transparent block)
    // Check down first to show blocks on the ground when there is a low roof/in a cave etc
    // Check up if no suitable block found to handle e.g mountains
    Integer result = findSuitableBlockDown(startBlock, minY);

    if (result == null) {
      result = findSuitableBlockUp(startBlock, maxY);
    }

    // Fallback to default position if nothing found
    if (result == null) {
      // Display at real boundary edge because it will be shown.
      result = defaultDisplayY(x, y, z, minY);
    }

    return result;
  }

  /**
   * Attempt to find a suitable block above the starting position
   * A suitable block is a solid block directly below a transparent block
   * @param start - Starting block
   * @param maxY - The y level to stop searching at
   * @return The first suitable block found, if any
   */
  private Integer findSuitableBlockUp(Block start, int maxY) {
    int y = Math.min(start.getY(), maxY);
    Boolean previousSolid = null;

    do {
      boolean solid = !isTransparent(start.getWorld().getBlockAt(start.getX(), y, start.getZ()));

      // Valid spot
      if (!solid && Boolean.TRUE.equals(previousSolid)) {
        lastLoadedDisplayHeight = y;
        return y - 1;
      }

      previousSolid = solid;
      y += 1;
    } while (y <= maxY);

    return null;
  }

  /**
   * Attempt to find a suitable block below the starting position
   * A suitable block is a solid block directly below a transparent block
   * @param start - Starting block
   * @param minY - The y level to stop searching at
   * @return The first suitable block found, if any
   */
  private Integer findSuitableBlockDown(Block start, int minY) {
    int y = Math.max(minY, start.getY());
    Boolean previousSolid = null;

    do {
      boolean solid = !isTransparent(start.getWorld().getBlockAt(start.getX(), y, start.getZ()));

      // Stop checking blocks if we encounter 2 solid blocks in a row, to avoid
      // selecting a block inaccessible to the player
      if (solid && Boolean.TRUE.equals(previousSolid)) {
        break;
      }

      // Valid spot
      if (solid && Boolean.FALSE.equals(previousSolid)) {
        lastLoadedDisplayHeight = y;
        return y;
      }

      previousSolid = solid;
      y -= 1;
    } while (y >= minY);

    return null;
  }

  private boolean isTransparent(Block block) {
    Material blockMaterial = block.getType();

    // Custom per-material definitions.
    switch (blockMaterial)
    {
      case WATER, SNOW:
        return false;
    }

    if (blockMaterial.isAir()
        || Tag.FENCES.isTagged(blockMaterial)
        || Tag.FENCE_GATES.isTagged(blockMaterial)
        || Tag.SIGNS.isTagged(blockMaterial)
        || Tag.WALLS.isTagged(blockMaterial)
        || Tag.WALL_SIGNS.isTagged(blockMaterial))
      return true;

    return block.getType().isTransparent();
  }

  @Override
  protected int defaultDisplayY(int x, int y, int z, int minY) {
    // If an element has been successfully displayed, display in line with it in unloaded chunks.
    if (lastLoadedDisplayHeight != Integer.MIN_VALUE) {
      return Math.max(minY, lastLoadedDisplayHeight);
    }

    // Fall through to default.
    return super.defaultDisplayY(x, y, z, minY);
  }
}
