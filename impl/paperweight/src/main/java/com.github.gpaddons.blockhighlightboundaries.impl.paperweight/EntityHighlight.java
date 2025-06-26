package com.github.gpaddons.blockhighlightboundaries.impl.paperweight;

import com.github.gpaddons.blockhighlightboundaries.HighlightConfiguration;
import com.github.gpaddons.blockhighlightboundaries.TeamManager;
import com.github.gpaddons.blockhighlightboundaries.type.EntityBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.VisualizationElementType;
import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.Boundary;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

class EntityHighlight extends EntityBlockHighlight {

  public EntityHighlight(
      @NotNull IntVector coordinate,
      @NotNull HighlightConfiguration configuration,
      @NotNull TeamManager teamManager,
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType) {
    super(coordinate, configuration, teamManager, boundary, visualizationElementType);
  }

  @Override
  protected void spawn(@NotNull Player player, @NotNull FakeEntity fakeEntity) {
    Connection channel = ((CraftPlayer) player).getHandle().connection.connection;
    ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(
        fakeEntity.entityId(), fakeEntity.uuid(),
        getCoordinate().x() + fakeEntity.localPosition().getX(),
        getCoordinate().y() + fakeEntity.localPosition().getY(),
        getCoordinate().z() + fakeEntity.localPosition().getZ(),
        0.0f, 0.0f, EntityType.MAGMA_CUBE, 0, Vec3.ZERO, 0.0);

    List<DataValue<?>> packedItems = List.of(
      new DataValue<>(0, EntityDataSerializers.BYTE, (byte) (0x20 | 0x40)), // Invisible (0x20) and glowing (0x40).
      new DataValue<>(4, EntityDataSerializers.BOOLEAN, Boolean.TRUE), // Silent
      new DataValue<>(5, EntityDataSerializers.BOOLEAN, Boolean.TRUE), // No gravity
      new DataValue<>(15, EntityDataSerializers.BYTE, (byte) 0x01), // No AI
      new DataValue<>(16, EntityDataSerializers.INT, getSlimeSize()) // Size
    );

    ClientboundSetEntityDataPacket packet2 = new ClientboundSetEntityDataPacket(
        fakeEntity.entityId(), packedItems);

    ClientboundBundlePacket bundle = new ClientboundBundlePacket(List.of(packet, packet2));
    channel.send(bundle);
  }

  @Override
  protected void remove(
      @NotNull Player player,
      @NotNull @Unmodifiable Collection<@NotNull FakeEntity> entities) {
    Connection channel = ((CraftPlayer) player).getHandle().connection.connection;
    int[] ids = entities.stream().mapToInt(FakeEntity::entityId).toArray();

    ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(ids);
    channel.send(packet);
  }
}
