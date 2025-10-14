package com.github.gpaddons.blockhighlightboundaries.type;

import com.github.gpaddons.blockhighlightboundaries.HighlightConfiguration;
import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.Boundary;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A {@link BlockHighlightElement} abstraction for using item display with custom models to highlight blocks.
 *
 */
public abstract class ItemDisplayBlockHighlight extends BlockHighlightElement {

  private final FakeEntity entity;

  /**
   * Construct a new {@code ItemDisplayBlockHighlight} with the given coordinate.
   *
   * @param coordinate the in-world coordinate of the element
   * @param configuration the configuration for highlights
   * @param boundary the boundary the element belongs to
   * @param visualizationElementType the type of element being visualized
   */
  public ItemDisplayBlockHighlight(
      @NotNull IntVector coordinate,
      @NotNull HighlightConfiguration configuration,
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType) {
    super(coordinate, configuration, boundary, visualizationElementType);
    this.entity = new FakeEntity(configuration.getNextEntityId(),
                                 new Vector(0.5, 0.5, 0.5));
  }

  @Override
  protected void draw(@NotNull Player player, @NotNull World world) {
    spawn(player, entity);
  }

  /**
   * Send the {@link Player} the packets required to display an entity with the correct data.
   *
   * @param player the recipient
   * @param fakeEntity the {@link FakeEntity} to spawn
   */
  protected abstract void spawn(@NotNull Player player, @NotNull FakeEntity fakeEntity);

  @Override
  protected void erase(@NotNull Player player, @NotNull World world) {
    remove(player, entity);
  }

  /**
   * Send the {@link Player} the packet required to remove an entity.
   *
   * @param player the recipient
   * @param entity the {@link FakeEntity FakeEntity} to despawn
   */
  protected abstract void remove(
      @NotNull Player player,
      @NotNull FakeEntity entity);

  public ItemStack getItemStack() {
    return configuration.getItemStack(boundary.type(), visualizationElementType);
  }

  /** Container for fake entity data. */
  protected record FakeEntity(int entityId, UUID uuid, Vector localPosition, int xScale, int yScale, int zScale) {

    private FakeEntity(int entityId, Vector localPosition) {
      this(entityId, UUID.randomUUID(), localPosition, 1, 1, 1);
    }

    private FakeEntity(int entityId, Vector localPosition, int xScale, int yScale, int zScale) {
      this(entityId, UUID.randomUUID(), localPosition, xScale, yScale, zScale);
    }
  }
}
