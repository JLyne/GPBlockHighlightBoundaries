package com.github.gpaddons.blockhighlightboundaries;

import com.github.gpaddons.blockhighlightboundaries.type.BlockHighlightElement;
import com.github.gpaddons.blockhighlightboundaries.type.DebugBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.EntityBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.FallThroughElement;
import com.github.gpaddons.blockhighlightboundaries.type.HighlightType;
import com.github.gpaddons.blockhighlightboundaries.type.ItemDisplayBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.VisualizationElementType;
import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.Boundary;
import com.griefprevention.visualization.VisualizationProvider;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

/**
 * An interface defining behavior for a {@link VisualizationProvider} implementation wrapper.
 */
public abstract class BlockHighlightElementProvider {
  protected final HighlightConfiguration configuration;
  protected final TeamManager teamManager;
  protected final HighlightType type;

  protected BlockHighlightElementProvider(HighlightConfiguration configuration, TeamManager teamManager) {
    this.configuration = configuration;
    this.teamManager = teamManager;
    this.type = configuration.getType();
  }

  /**
   * Get whether the {@link VisualizationProvider} is capable of functioning properly.
   *
   * @param server the active server implementation
   * @param configuration the highlight configuration
   * @return whether the provider is capable of functioning
   */
  abstract public boolean isCapable(@NotNull Server server, @NotNull HighlightConfiguration configuration);

  public final BlockHighlightElement getElement(@NotNull IntVector coordinate,
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType) {
    return switch(type) {
      case DEBUG_BLOCK -> getDebugHighlight(coordinate, boundary, visualizationElementType);
      case GLOWING_ENTITY -> getEntityHighlight(coordinate, boundary, visualizationElementType);
      case ITEM_DISPLAY -> getDisplayHighlight(coordinate, boundary, visualizationElementType);
    };
  }

  public final FallThroughElement getFallthroughElement(@NotNull IntVector coordinate,
      @NotNull NamedTextColor color) {
    return new FallThroughElement(coordinate, color);
  }

  /**
   * Method for obtaining a
   * {@link com.github.gpaddons.blockhighlightboundaries.type.HighlightType#DEBUG_BLOCK DEBUG_BLOCK}
   * highlight implementation for the given parameters.
   *
   * @param coordinate the location of the element
   * @param boundary the boundary being visualized
   * @param visualizationElementType the type of element in the boundary being visualized
   * @return the {@link DebugBlockHighlight} created
   */
  abstract protected @NotNull DebugBlockHighlight getDebugHighlight(
      @NotNull IntVector coordinate,
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType);

  /**
   * Method for obtaining a
   * {@link com.github.gpaddons.blockhighlightboundaries.type.HighlightType#GLOWING_ENTITY GLOWING_ENTITY}
   * highlight implementation for the given parameters.
   *
   * @param coordinate the location of the element
   * @param boundary the boundary being visualized
   * @param visualizationElementType the type of element in the boundary being visualized
   * @return the {@link EntityBlockHighlight} created
   */
  abstract protected @NotNull EntityBlockHighlight getEntityHighlight(
      @NotNull IntVector coordinate,
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType);

  /**
   * Method for obtaining a
   * {@link com.github.gpaddons.blockhighlightboundaries.type.HighlightType#ITEM_DISPLAY ITEM_DISPLAY}
   * highlight implementation for the given parameters.
   *
   * @param coordinate the location of the element
   * @param boundary the boundary being visualized
   * @param visualizationElementType the type of element in the boundary being visualized
   * @return the {@link ItemDisplayBlockHighlight} created
   */
   abstract protected @NotNull ItemDisplayBlockHighlight getDisplayHighlight(
      @NotNull IntVector coordinate,
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType);

}
