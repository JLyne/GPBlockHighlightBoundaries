package com.github.gpaddons.blockhighlightboundaries;

import com.github.gpaddons.blockhighlightboundaries.style.HighlightStyle;
import com.github.gpaddons.blockhighlightboundaries.type.HighlightType;
import com.github.gpaddons.blockhighlightboundaries.type.VisualizationElementType;
import com.griefprevention.visualization.Boundary;
import com.griefprevention.visualization.VisualizationType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@SuppressWarnings("UnstableApiUsage")
public class PluginHighlightConfiguration implements HighlightConfiguration {

  private final Plugin plugin;
  private @Nullable Integer spacing;
  private @Nullable Integer viewDistance;
  private @Nullable Integer displayMillis;
  private AtomicInteger nextEntityId;
  private @Nullable HighlightType highlightType;
  private @Nullable HighlightStyle highlightStyle;
  private final Map<ElementPath, Color> colorCache = new HashMap<>();
  private final Map<ElementPath, NamedTextColor> chatColorCache = new HashMap<>();
  private final Map<ElementPath, ItemStack> itemCache = new HashMap<>();
  private @Nullable Set<UUID> optedOut = null;

  PluginHighlightConfiguration(Plugin plugin) {
    this.plugin = plugin;
  }

  void reload() {
    spacing = null;
    viewDistance = null;
    displayMillis = null;
    highlightType = null;
    highlightStyle = null;
    colorCache.clear();
    chatColorCache.clear();
    itemCache.clear();
    getOptedOut();
  }

  void saveOptOuts() {
    plugin.reloadConfig();
    plugin.getConfig().set("opted-out", optedOut.stream().map(UUID::toString).toList());
    plugin.saveConfig();
  }

  @Override
  public @Range(from = 1, to = 500) int getSpacing() {
    if (spacing == null) {
      spacing = limit(1, 500, plugin.getConfig().getInt("spacing", 10));
    }

    return spacing;
  }

  @Override
  public @Range(from = 25, to = 500) int getViewDistance() {
    if (viewDistance == null) {
      viewDistance = limit(25, 500, plugin.getConfig().getInt("viewDistance", 150));
    }

    return viewDistance;
  }

  @Override
  public @Range(from = 5_000, to = 300_000) int getDisplayMillis() {
    if (displayMillis == null) {
      displayMillis = limit(5_000, 300_000, plugin.getConfig().getInt("displaySeconds", 60) * 1_000);
    }

    return displayMillis;
  }

  @Override
  public int getNextEntityId() {
    if (nextEntityId == null) {
      nextEntityId = new AtomicInteger(plugin.getConfig().getInt("advanced.startEid", -1));
    }

    return nextEntityId.getAndDecrement();
  }

  @Override
  public @NotNull HighlightType getType() {
    if (highlightType == null) {
      highlightType = parseEnum(plugin.getConfig().getString("type", "GLOWING_ENTITY"), HighlightType.GLOWING_ENTITY);
    }

    return highlightType;
  }

  @Override
  public @NotNull HighlightStyle getStyle() {
    if (highlightStyle == null) {
      highlightStyle = parseEnum(plugin.getConfig().getString("style", "FLAT"), HighlightStyle.FLAT);
    }

    return highlightStyle;
  }

  @Override
  public @NotNull Set<UUID> getOptedOut() {
    if (optedOut == null) {
      optedOut = plugin.getConfig().getStringList("opted-out").stream()
          .map(UUID::fromString)
          .collect(Collectors.toSet());
    }

    return optedOut;
  }

  @Override
  public boolean isOptedOut(UUID uuid) {
    if (optedOut == null) {
      getOptedOut();
    }

    return optedOut.contains(uuid);
  }

  @Override
  public boolean optOut(UUID uuid) {
    if (optedOut == null) {
      getOptedOut();
    }

    return optedOut.add(uuid);
  }

  @Override
  public boolean optIn(UUID uuid) {
    if (optedOut == null) {
      getOptedOut();
    }

    return optedOut.remove(uuid);
  }

  @Override
  public @NotNull String getName(
      @NotNull Boundary boundary,
      @NotNull VisualizationElementType element) {
    // FEATURE support ClaimslistClassifier
    return "";
  }

  @Override
  public @NotNull NamedTextColor getClosestChatColor(
      @NotNull VisualizationType type,
      @NotNull VisualizationElementType element) {
    ElementPath elementPath = new ElementPath(type, element);

    return chatColorCache.computeIfAbsent(elementPath,
        key -> asChatColor(getColor(elementPath)));
  }

  @Override
  public @NotNull Color getColor(
      @NotNull VisualizationType type,
      @NotNull VisualizationElementType element) {
    ElementPath elementPath = new ElementPath(type, element);

    return getColor(elementPath);
  }

  @Override
  public @NotNull ItemStack getItemStack(
      @NotNull VisualizationType type,
      @NotNull VisualizationElementType element) {
    ElementPath elementPath = new ElementPath(type, element);

    return getItemStack(elementPath);
  }

  private @NotNull Color getColor(@NotNull ElementPath elementPath) {
    return colorCache.computeIfAbsent(elementPath, key -> Color.fromARGB(
        sanitizeColor(key.colorPath("alpha")),
        sanitizeColor(key.colorPath("red")),
        sanitizeColor(key.colorPath("green")),
        sanitizeColor(key.colorPath("blue"))));
  }

  private @NotNull ItemStack getItemStack(@NotNull ElementPath elementPath) {
    return itemCache.computeIfAbsent(elementPath, key -> {
      ItemStack item = ItemStack.of(Material.WHITE_STAINED_GLASS);
      NamespacedKey model = NamespacedKey.fromString(
          plugin.getConfig().getString(key.displayPath("itemModel"), ""));

      if(plugin.getConfig().getBoolean(elementPath.displayPath("tint"))) {
        item.setData(DataComponentTypes.CUSTOM_MODEL_DATA,
            CustomModelData.customModelData().addColor(getColor(elementPath)));
      }

      if(model != null) {
        item.setData(DataComponentTypes.ITEM_MODEL, model);
      }

      return item;
    });
  }

  private int sanitizeColor(String path) {
    return limit(0, 255, plugin.getConfig().getInt(path, 255));
  }

  private static int limit(int min, int max, int actual) {
    return Math.max(min, Math.min(max, actual));
  }

  private static <T extends Enum<T>> @NotNull T parseEnum(
      @NotNull String value,
      @NotNull T defaultValue) {
    for (T enumConstant : defaultValue.getDeclaringClass().getEnumConstants()) {
      if (enumConstant.name().equalsIgnoreCase(value)) {
        return enumConstant;
      }
    }

    return defaultValue;
  }

  private static @NotNull NamedTextColor asChatColor(@NotNull Color color) {
    return NamedTextColor.nearestTo(TextColor.color(color.asRGB()));
  }

  private record ElementPath(@NotNull VisualizationType visualization, @NotNull VisualizationElementType element) {
    public String colorPath(@NotNull String element) {
      return String.format("colors.%s.%s.%s", visualization().name(), element().name(), element);
    }

    public String displayPath(@NotNull String element) {
      return String.format("displays.%s.%s.%s", visualization().name(), element().name(), element);
    }
  }
}
