package com.github.gpaddons.blockhighlightboundaries;

import com.github.gpaddons.blockhighlightboundaries.compat.FloodgateCompat;
import com.github.gpaddons.blockhighlightboundaries.impl.packetevents1.PacketEvents1Provider;
import com.github.gpaddons.blockhighlightboundaries.impl.packetevents2.PacketEvents2Provider;
import com.github.gpaddons.blockhighlightboundaries.impl.protocollib.ProtocolLibProvider;
import com.github.gpaddons.blockhighlightboundaries.impl.paperweight.PaperweightProvider;
import com.griefprevention.events.BoundaryVisualizationEvent;
import com.griefprevention.visualization.VisualizationProvider;
import java.util.List;
import java.util.function.Supplier;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.papermc.paper.command.brigadier.Commands.literal;

@SuppressWarnings("UnstableApiUsage")
public class GPBlockHighlightBoundaries extends JavaPlugin implements Listener
{
  private final @NotNull PluginHighlightConfiguration configuration = new PluginHighlightConfiguration(this);
  private final @NotNull PluginTeamManager teamManager = new PluginTeamManager(this, configuration);
  private FloodgateCompat floodgateCompat;
  private @Nullable VisualizationProvider provider;

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

    LifecycleEventManager<@NotNull Plugin> manager = getLifecycleManager();
    manager.registerEventHandler(LifecycleEvents.COMMANDS,
        event -> registerCommands(event.registrar()));
  }

  @Override
  public void onDisable() {
    teamManager.cleanUp();
  }

  @EventHandler
  private void onVisualize(@NotNull BoundaryVisualizationEvent event)
  {
    if (!floodgateCompat.isBedrock(event.getPlayer()) && provider != null) {
      event.setProvider(provider);
    }
  }

  private void registerCommands(Commands commands) {
    LiteralCommandNode<CommandSourceStack> reloadCommand = literal("gpbhbreload")
        .requires(source -> source.getSender().hasPermission("gpbhb.reload"))
        .executes(ctx -> {
          this.reloadConfig();
          configuration.reload();
          teamManager.reload();
          provider = getProvider();
          ctx.getSource().getSender()
              .sendMessage("GPBlockHighlightBoundaries configuration reloaded.");
          ctx.getSource().getSender()
              .sendMessage("Note that for values in the advanced section a full restart is required.");
          return com.mojang.brigadier.Command.SINGLE_SUCCESS;
        }).build();

    commands.register(reloadCommand, "Reload the GPBlockHighlightBoundaries configuration.");
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

}
