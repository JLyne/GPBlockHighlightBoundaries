package com.github.gpaddons.blockhighlightboundaries.impl.packetevents2;

import com.github.gpaddons.blockhighlightboundaries.BlockHighlightElementProvider;
import com.github.gpaddons.blockhighlightboundaries.HighlightConfiguration;
import com.github.gpaddons.blockhighlightboundaries.TeamManager;
import com.github.gpaddons.blockhighlightboundaries.type.DebugBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.EntityBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.HighlightType;
import com.github.gpaddons.blockhighlightboundaries.type.ItemDisplayBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.VisualizationElementType;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.PEVersion;
import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.Boundary;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import java.util.logging.Logger;

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

    if (PacketEvents.getAPI().getVersion().isOlderThan(new PEVersion(2))) {
      return false;
    }

    if (configuration.getType() == HighlightType.DEBUG_BLOCK) {
      Plugin plugin = server.getPluginManager().getPlugin("GPBlockHighlightBoundaries");
      Logger logger = plugin != null ? plugin.getLogger() : server.getLogger();
      logger.warning("PacketEvents 2 does not (or did not yet) support custom payloads.");
      logger.warning("This means that GPBHB cannot display DEBUG_BLOCK type boundaries.");
      logger.warning("Please edit your configuration to use GLOWING_ENTITY instead.");
      return false;
    }

    if (configuration.getType() == HighlightType.ITEM_DISPLAY) {
      Plugin plugin = server.getPluginManager().getPlugin("GPBlockHighlightBoundaries");
      Logger logger = plugin != null ? plugin.getLogger() : server.getLogger();
      logger.warning("PacketEvents 2 does not currently support ITEM_DISPLAY.");
      logger.warning("Please edit your configuration to use GLOWING_ENTITY instead.");
      return false;
    }

    return true;
  }

  @Override
  protected @NotNull DebugBlockHighlight getDebugHighlight(
      @NotNull IntVector coordinate,
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType) {
    throw new UnsupportedOperationException("PacketEvents 2.0-SNAPSHOT does not support custom payloads.");
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
          @NotNull Boundary boundary,
          @NotNull VisualizationElementType visualizationElementType) {
    throw new UnsupportedOperationException("PacketEvents 2.0-SNAPSHOT does not currently support ITEM_DISPLAY.");
  }

}
