package com.github.gpaddons.blockhighlightboundaries.impl.protocollib;

import com.github.gpaddons.blockhighlightboundaries.BlockHighlightElementProvider;
import com.github.gpaddons.blockhighlightboundaries.HighlightConfiguration;
import com.github.gpaddons.blockhighlightboundaries.TeamManager;
import com.github.gpaddons.blockhighlightboundaries.type.DebugBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.EntityBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.HighlightType;
import com.github.gpaddons.blockhighlightboundaries.type.VisualizationElementType;
import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.Boundary;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

public class ProtocolLibElementProvider extends BlockHighlightElementProvider {
  public ProtocolLibElementProvider(HighlightConfiguration configuration, TeamManager teamManager) {
    super(configuration, teamManager);
  }

  @Override
  public boolean isCapable(@NotNull Server server, @NotNull HighlightConfiguration configuration) {
    if (!server.getPluginManager().isPluginEnabled("ProtocolLib")) {
      return false;
    }

    try {
      Class.forName("com.comphenix.protocol.ProtocolLibrary");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  @Override
  protected @NotNull DebugBlockHighlight getDebugHighlight(
      @NotNull IntVector coordinate,
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType) {
    return new DebugHighlight(
        coordinate,
        configuration,
        boundary,
        visualizationElementType);
  }

  @Override
  protected @NotNull EntityBlockHighlight getEntityHighlight(
      @NotNull IntVector coordinate,
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType) {
    return new EntityHighlightDataValueList(
        coordinate,
        configuration,
        teamManager,
        boundary,
        visualizationElementType);
  }

}
