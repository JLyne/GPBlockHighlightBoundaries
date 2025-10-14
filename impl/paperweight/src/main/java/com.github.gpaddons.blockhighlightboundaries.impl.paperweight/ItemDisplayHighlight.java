package com.github.gpaddons.blockhighlightboundaries.impl.paperweight;

import com.github.gpaddons.blockhighlightboundaries.HighlightConfiguration;
import com.github.gpaddons.blockhighlightboundaries.type.ItemDisplayBlockHighlight;
import com.github.gpaddons.blockhighlightboundaries.type.VisualizationElementType;
import com.griefprevention.util.IntVector;
import com.griefprevention.visualization.Boundary;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

class ItemDisplayHighlight extends ItemDisplayBlockHighlight {

  public ItemDisplayHighlight(
      @NotNull IntVector coordinate,
      @NotNull HighlightConfiguration configuration,
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType visualizationElementType) {
    super(coordinate, configuration, boundary, visualizationElementType);
  }

  @Override
  protected void spawn(@NotNull Player player, @NotNull FakeEntity fakeEntity) {
    Connection channel = ((CraftPlayer) player).getHandle().connection.connection;
    ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(
        fakeEntity.entityId(), fakeEntity.uuid(),
        getCoordinate().x() + fakeEntity.localPosition().getX(),
        getCoordinate().y() + fakeEntity.localPosition().getY(),
        getCoordinate().z() + fakeEntity.localPosition().getZ(),
        0.0f, 0.0f, EntityType.ITEM_DISPLAY, 0, Vec3.ZERO, 0.0);

    ItemStack item = CraftItemStack.unwrap(getItemStack());

    List<DataValue<?>> packedItems = List.of(
            new DataValue<>(5, EntityDataSerializers.BOOLEAN, Boolean.TRUE), // No gravity
            new DataValue<>(12, EntityDataSerializers.VECTOR3,
                new Vector3f(fakeEntity.xScale() + 0.01f, fakeEntity.yScale() + 0.01f,
                    fakeEntity.zScale() + 0.01f)), // Scale
            new DataValue<>(16, EntityDataSerializers.INT, Brightness.FULL_BRIGHT.pack()), // Fullbright
            new DataValue<>(17, EntityDataSerializers.FLOAT, ((float) configuration.getViewDistance()) / 64), // View distance
            new DataValue<>(20, EntityDataSerializers.FLOAT, 1.0f), // View distance
            new DataValue<>(21, EntityDataSerializers.FLOAT, 1.0f), // View distance
            new DataValue<>(23, EntityDataSerializers.ITEM_STACK, item) // Item
    );

    ClientboundSetEntityDataPacket packet2 = new ClientboundSetEntityDataPacket(
        fakeEntity.entityId(), packedItems);

    ClientboundBundlePacket bundle = new ClientboundBundlePacket(List.of(packet, packet2));
    channel.send(bundle);
  }

  @Override
  protected void remove(
      @NotNull Player player,
      @NotNull FakeEntity entity) {
    Connection channel = ((CraftPlayer) player).getHandle().connection.connection;
    ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(entity.entityId());
    channel.send(packet);
  }
}
