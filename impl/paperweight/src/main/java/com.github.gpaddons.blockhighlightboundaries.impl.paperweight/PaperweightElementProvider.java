package com.github.gpaddons.blockhighlightboundaries.impl.paperweight;

import com.github.gpaddons.blockhighlightboundaries.BlockHighlightElementProvider;
import com.github.gpaddons.blockhighlightboundaries.HighlightConfiguration;
import com.github.gpaddons.blockhighlightboundaries.TeamManager;
import com.github.gpaddons.blockhighlightboundaries.type.DebugBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.EntityBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.ItemDisplayBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.VisualizationElementType;
import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.Boundary;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;


public class PaperweightElementProvider extends BlockHighlightElementProvider {

  public PaperweightElementProvider(HighlightConfiguration configuration, TeamManager teamManager) {
    super(configuration, teamManager);
  }

  @Override
  public boolean isCapable(@NotNull Server server, @NotNull HighlightConfiguration configuration) {
    return true;
  }

  @Override
  protected @NotNull EntityBlockHighlight getEntityHighlight(
      @NotNull IntVector coordinate,
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType) {
    return new EntityHighlight(coordinate, configuration, teamManager, boundary, visualizationElementType);
  }

  @Override
  protected @NotNull ItemDisplayBlockHighlight getDisplayHighlight(
          @NotNull IntVector coordinate,
          @NotNull IntVector scale,
          @NotNull Boundary boundary,
          @NotNull VisualizationElementType visualizationElementType) {
    return new ItemDisplayHighlight(coordinate, scale, configuration, boundary, visualizationElementType);
  }

  @Override
  protected @NotNull DebugBlockHighlight getDebugHighlight(
      @NotNull IntVector coordinate,
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType) {
    return new DebugHighlight(coordinate, configuration, boundary, visualizationElementType);
  }
}
