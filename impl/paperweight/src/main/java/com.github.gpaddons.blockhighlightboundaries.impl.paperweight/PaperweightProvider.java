package com.github.gpaddons.blockhighlightboundaries.impl.paperweight;

import com.github.gpaddons.blockhighlightboundaries.BoundaryProvider;
import com.github.gpaddons.blockhighlightboundaries.HighlightConfiguration;
import com.github.gpaddons.blockhighlightboundaries.TeamManager;
import com.github.gpaddons.blockhighlightboundaries.type.DebugBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.EntityBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.VisualizationElementType;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;


public class PaperweightProvider implements BoundaryProvider {

  @Override
  public boolean isCapable(@NotNull Server server, @NotNull HighlightConfiguration configuration) {
    return true;
  }

  @Override
  public com.griefprevention.visualization.@NotNull VisualizationProvider getProvider(
      @NotNull HighlightConfiguration configuration, @NotNull TeamManager teamManager) {
    return BoundaryProvider.super.getProvider(configuration, teamManager);
  }

  @Override
  public @NotNull EntityBlockHighlight getEntityHighlight(
      com.griefprevention.util.@NotNull IntVector coordinate,
      @NotNull HighlightConfiguration configuration,
      @NotNull TeamManager teamManager,
      com.griefprevention.visualization.@NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType) {
    return new EntityHighlight(coordinate, configuration, teamManager, boundary, visualizationElementType);
  }

  @Override
  public @NotNull DebugBlockHighlight getDebugHighlight(
      com.griefprevention.util.@NotNull IntVector coordinate,
      @NotNull HighlightConfiguration configuration,
      com.griefprevention.visualization.@NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType) {
    return new DebugHighlight(coordinate, configuration, boundary, visualizationElementType);
  }
}
