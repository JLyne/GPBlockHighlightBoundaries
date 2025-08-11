package com.github.gpaddons.blockhighlightboundaries;

import com.github.gpaddons.blockhighlightboundaries.compat.FloodgateCompat;
import com.github.gpaddons.blockhighlightboundaries.impl.packetevents1.PacketEvents1Provider;
import com.github.gpaddons.blockhighlightboundaries.impl.packetevents2.PacketEvents2Provider;
import com.github.gpaddons.blockhighlightboundaries.impl.protocollib.ProtocolLibProvider;
import com.github.gpaddons.blockhighlightboundaries.impl.paperweight.PaperweightProvider;
import com.griefprevention.events.BoundaryVisualizationEvent;
import com.griefprevention.visualization.VisualizationProvider;
import java.io.File;
import java.util.List;
import java.util.function.Supplier;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import uk.co.notnull.messageshelper.Message;
import uk.co.notnull.messageshelper.MessagesHelper;

import static io.papermc.paper.command.brigadier.Commands.literal;

public class GPBlockHighlightBoundaries extends JavaPlugin implements Listener
{
  private final @NotNull PluginHighlightConfiguration configuration = new PluginHighlightConfiguration(this);
  private final @NotNull PluginTeamManager teamManager = new PluginTeamManager(this, configuration);
  private FloodgateCompat floodgateCompat;
  private @Nullable VisualizationProvider provider;
  MessagesHelper messagesHelper = MessagesHelper.getInstance(this);

  @Override
  public void onEnable() {
    saveDefaultConfig();
    provider = getProvider();
    floodgateCompat = new FloodgateCompat(this);
    if (provider == null) {
      getLogger().warning("No eligible provider found!");
      getLogger().warning("Please install ProtocolLib or PacketEvents and restart your server.");
    }
    getServer().getPluginManager().registerEvents(this, this);

    initMessages();

    LifecycleEventManager<@NotNull Plugin> manager = getLifecycleManager();
    manager.registerEventHandler(LifecycleEvents.COMMANDS,
        event -> registerCommands(event.registrar()));
  }

  @Override
  public void onDisable() {
    teamManager.cleanUp();
    configuration.saveOptOuts();
  }

  @EventHandler
  private void onVisualize(@NotNull BoundaryVisualizationEvent event)
  {
    if (provider != null && !floodgateCompat.isBedrock(event.getPlayer())
        && !configuration.isOptedOut(event.getPlayer().getUniqueId())) {
      event.setProvider(provider);
    }
  }

  private void registerCommands(Commands commands) {
    LiteralCommandNode<CommandSourceStack> reloadCommand = literal("gpbhbreload")
        .requires(source -> source.getSender().hasPermission("gpbhb.reload"))
        .executes(ctx -> {
          configuration.saveOptOuts();
          this.reloadConfig();
          initMessages();
          configuration.reload();
          teamManager.reload();
          provider = getProvider();
          messagesHelper.send(ctx.getSource().getSender(), Message.builder("message.config-reloaded").build());

          return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }).build();

    LiteralCommandNode<CommandSourceStack> optOutCommand = literal("basicvisualizations")
        .requires(source -> source.getSender() instanceof Player player
            && player.hasPermission("gpbhb.toggle"))
        .executes(ctx -> {
          if (!(ctx.getSource().getSender() instanceof Player player)) {
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
          }

          if (floodgateCompat.isBedrock(player)) {
            messagesHelper.send(ctx.getSource().getSender(), Message.builder("message.cant-switch-bedrock").build());
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
          }

          if (configuration.optOut(player.getUniqueId())) {
            messagesHelper.send(ctx.getSource().getSender(), Message.builder("message.switched-basic").build());
          } else {
            messagesHelper.send(ctx.getSource().getSender(), Message.builder("message.already-basic").build());
          }

          return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }).build();

    LiteralCommandNode<CommandSourceStack> optInCommand = literal("enhancedvisualizations")
        .requires(source -> source.getSender() instanceof Player player
            && player.hasPermission("gpbhb.toggle"))
        .executes(ctx -> {
          if (!(ctx.getSource().getSender() instanceof Player player)) {
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
          }

          if (floodgateCompat.isBedrock(player)) {
            messagesHelper.send(ctx.getSource().getSender(), Message.builder("message.cant-switch-bedrock").build());
            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
          }

          if (configuration.optIn(player.getUniqueId())) {
            messagesHelper.send(ctx.getSource().getSender(), Message.builder("message.switched-enhanced").build());
          } else {
            messagesHelper.send(ctx.getSource().getSender(), Message.builder("message.already-enhanced").build());
          }

          return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }).build();

    commands.register(reloadCommand, "Reload the GPBlockHighlightBoundaries configuration.");
    commands.register(optOutCommand, "Switch to basic claim visualizations.");
    commands.register(optInCommand, "Switch to enhanced claim visualizations.");
  }

  private @Nullable VisualizationProvider getProvider() {
    List<Supplier<BoundaryProvider>> providers = List.of(
        // Prefer ProtocolLib, it's more reliable/stable.
        ProtocolLibProvider::new,
        PacketEvents2Provider::new,
        PacketEvents1Provider::new,
        PaperweightProvider::new
    );

    return providers.stream().map(Supplier::get)
        .filter(provider -> provider.isCapable(getServer(), configuration))
        .map(provider -> provider.getProvider(configuration, teamManager))
        .findFirst()
        .orElse(null);
  }

  private void initMessages() {
    File messagesFile = new File(getDataFolder(), "messages.yml");

    if(!messagesFile.exists()) {
      saveResource("messages.yml", false);
    }

    messagesHelper.loadMessages(messagesFile);
  }
}
