package com.github.gpaddons.blockhighlightboundaries.impl.packetevents2;

import com.github.gpaddons.blockhighlightboundaries.BlockHighlightElementProvider;
import com.github.gpaddons.blockhighlightboundaries.HighlightConfiguration;
import com.github.gpaddons.blockhighlightboundaries.TeamManager;
import com.github.gpaddons.blockhighlightboundaries.type.EntityBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.HighlightType;
import com.github.gpaddons.blockhighlightboundaries.type.ItemDisplayBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.VisualizationElementType;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.PEVersion;
import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.Boundary;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

public class PacketEvents2ElementProvider extends BlockHighlightElementProvider {
  public PacketEvents2ElementProvider(HighlightConfiguration configuration, TeamManager teamManager) {
    super(configuration, teamManager);
  }

  @Override
  public boolean isCapable(@NotNull Server server, @NotNull HighlightConfiguration configuration) {
    if (!server.getPluginManager().isPluginEnabled("PacketEvents")) {
      return false;
    }

    try {
      Class.forName("io.github.retrooper.packetevents.PacketEvents");
    } catch (ClassNotFoundException e) {
      return false;
    }

    return !PacketEvents.getAPI().getVersion().isOlderThan(new PEVersion(2, 11, 1));
  }

  @Override
  protected @NotNull EntityBlockHighlight getEntityHighlight(
      @NotNull IntVector coordinate,
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType) {
    return new PacketEventsEntityHighlight(coordinate, configuration, teamManager, boundary, visualizationElementType);
  }

  @Override
  protected @NotNull ItemDisplayBlockHighlight getDisplayHighlight(
          @NotNull IntVector coordinate,
          @NotNull IntVector scale,
          @NotNull Boundary boundary,
          @NotNull VisualizationElementType visualizationElementType) {
    throw new UnsupportedOperationException("PacketEvents 2.0-SNAPSHOT does not currently support ITEM_DISPLAY.");
  }

}
