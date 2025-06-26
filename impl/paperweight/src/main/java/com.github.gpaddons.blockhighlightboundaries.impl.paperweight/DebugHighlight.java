package com.github.gpaddons.blockhighlightboundaries.impl.paperweight;

import com.github.gpaddons.blockhighlightboundaries.HighlightConfiguration;
import com.github.gpaddons.blockhighlightboundaries.type.DebugBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.VisualizationElementType;
import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.Boundary;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.function.UnaryOperator;

class DebugHighlight extends DebugBlockHighlight {
  /**
   * Construct a new {@code ProtocolLibDebugHighlight}.
   *
   * @param coordinate the in-world coordinate of the element
   * @param configuration the configuration for highlights
   * @param boundary the boundary the element belongs to
   * @param visualizationElementType the type of element being visualized
   */
  public DebugHighlight(
      @NotNull IntVector coordinate,
      @NotNull HighlightConfiguration configuration,
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType) {
    super(coordinate, configuration, boundary, visualizationElementType);
  }

  @Override
  protected void draw(@NotNull Player player, @NotNull World world) {
    BlockPos blockPos = new BlockPos(getCoordinate().x(), getCoordinate().y(), getCoordinate().z());
    ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(
        new GameTestAddMarkerDebugPayload(blockPos, getColorInt(), getName(), getDisplayMillis()));
    ((CraftPlayer) player).getHandle().connection.connection.send(packet);
  }

  @Override
  protected void sendPacket(@NotNull Player player, @NotNull UnaryOperator<@NotNull ByteBuf> write) {
    throw new NotImplementedException("sendPacket");
  }

  @Override
  protected void erase(@NotNull Player player, @NotNull World world) {
    BlockPos blockPos = new BlockPos(getCoordinate().x(), getCoordinate().y(), getCoordinate().z());
    ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(
        new GameTestAddMarkerDebugPayload(blockPos, getColorInt(), "", 0));
    ((CraftPlayer) player).getHandle().connection.connection.send(packet);
  }
}
