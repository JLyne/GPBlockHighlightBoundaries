package com.github.gpaddons.blockhighlightboundaries;

import com.github.gpaddons.blockhighlightboundaries.style.HighlightStyle;
import com.github.gpaddons.blockhighlightboundaries.type.HighlightType;
import com.github.gpaddons.blockhighlightboundaries.type.VisualizationElementType;
import com.griefprevention.visualization.Boundary;
import java.util.Set;
import java.util.UUID;
import com.griefprevention.visualization.VisualizationType;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Interface defining configuration details.
 */
public interface HighlightConfiguration {

  /**
   * Get the spacing between highlight elements.
   *
   * @return the spacing
   */
  @Range(from = 1, to = 500) int getSpacing();

  /**
   * Get the view distance of highlight elements. Elements outside this range will not be sent to
   * clients.
   *
   * @return the view distance of elements
   */
  @Range(from = 25, to = 500) int getViewDistance();

  /**
   * Get the number of milliseconds that elements will display for.
   *
   * @return the number of milliseconds elements will be reverted after
   */
  @Range(from = 5_000, to = 300_000) int getDisplayMillis();

  /**
   * Get the next fake entity ID.
   *
   * @return the next fake entity ID
   */
  int getNextEntityId();

  /**
   * Get the active {@link HighlightType}.
   *
   * @return the type of highlight active
   */
  @NotNull HighlightType getType();

  /**
   * Get the active {@link HighlightStyle}.
   *
   * @return the style of highlight active
   */
  @NotNull HighlightStyle getStyle();

  /**
   * Get the set of players who have opted out of enhanced visualisations
   *
   * @return the set of players who have opted out
   */
  @NotNull Set<UUID> getOptedOut();

  /**
   * Returns whether a player is currently opted out from enhanced visualisations
   */
  boolean isOptedOut(UUID uuid);

  /**
   * Opt out a player from enhanced visualisations
   */
  boolean optOut(UUID uuid);

  /**
   * Opt in a player to enhanced visualisations
   */
  boolean optIn(UUID uuid);

  /**
   * Get the name of an element in a {@link Boundary}.
   *
   * @param boundary the {@code Boundary}
   * @param element the element type
   * @return the name of the element
   */
  @NotNull String getName(@NotNull Boundary boundary, @NotNull VisualizationElementType element);

  /**
   * Get the closest legacy {@link NamedTextColor} to a configured {@link Color} for an element.
   *
   * @param type the type of visualization
   * @param element the element of the visualization
   * @return the closest legacy chat color
   */
  @NotNull NamedTextColor getClosestChatColor(@NotNull VisualizationType type, @NotNull VisualizationElementType element);

  /**
   * Get a {@link Color} for an element.
   *
   * @param type the type of visualization
   * @param element the element of the visualization
   * @return the color
   */
  @NotNull Color getColor(@NotNull VisualizationType type, @NotNull VisualizationElementType element);

  /**
   * Get the {@link ItemStack} for an element to be used in an item display.
   *
   * @param type the type of visualization
   * @param element the element of the visualization
   * @return the item model
   */
  @NotNull ItemStack getItemStack(@NotNull VisualizationType type, @NotNull VisualizationElementType element);

}
