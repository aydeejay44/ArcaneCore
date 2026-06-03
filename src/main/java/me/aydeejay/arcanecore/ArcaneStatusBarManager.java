package me.aydeejay.arcanecore;

import me.aydeejay.arcanecore.arcanes.BreezeArcane;
import me.aydeejay.arcanecore.arcanes.EmberArcane;
import me.aydeejay.arcanecore.arcanes.FrostArcane;
import me.aydeejay.arcanecore.arcanes.LuckArcane;
import me.aydeejay.arcanecore.arcanes.VoidArcane;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class ArcaneStatusBarManager {

    private static final String FRAME = "\u25C7";
    private static final String DIVIDER = "\u2503";
    private static final String MINI_DIVIDER = "|";
    private static final String NONE_ICON = "\u25CB";
    private static final String HOURGLASS_ICON = "\u23F3";
    private static final String DEFAULT_RESOURCE_PACK_FONT = "arcanesmp:status";

    private final ArcaneCore plugin;
    private BukkitTask task;

    public ArcaneStatusBarManager(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) {
            return;
        }

        long interval = getUpdateTicks();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateAllPlayers, 20L, interval);
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void reload() {
        shutdown();
        start();
    }

    public void sendResourcePackIconTest(Player player) {
        Component defaultFontTest = Component.text("Default font icon test: \uE101 \uE102 \uE103 \uE104 \uE105", NamedTextColor.GRAY);
        Component configuredFontTest = Component.text("Configured font icon test: ", NamedTextColor.GRAY)
                .append(Component.text("\uE101 \uE102 \uE103 \uE104 \uE105")
                        .font(getIconFontKey())
                        .color(NamedTextColor.WHITE))
                .append(Component.text(" | fallback: \u2739 \u2744 \u2726 \u2618 \u2727", NamedTextColor.GRAY));

        player.sendMessage(defaultFontTest);
        player.sendMessage(configuredFontTest);
        player.sendActionBar(defaultFontTest);
    }

    private void updateAllPlayers() {
        if (!plugin.getConfig().getBoolean("status-bar.enabled", true)) {
            return;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            updatePlayer(player);
        }
    }

    private void updatePlayer(Player player) {
        ArcaneBar normalBar = getNormalArcaneBar(player);
        ArcaneBar voidBar = getVoidArcaneBar(player);

        if (normalBar == null && voidBar == null) {
            sendStatusBar(player, buildNoArcaneBar());
            return;
        }

        if (normalBar != null && voidBar != null) {
            sendStatusBar(player, buildDualArcaneBar(normalBar, voidBar));
            return;
        }

        ArcaneBar onlyBar = normalBar != null ? normalBar : voidBar;
        if (plugin.getConfig().getBoolean("status-bar.single-arcane-compact", true)) {
            sendStatusBar(player, buildCompactBar(onlyBar));
            return;
        }

        sendStatusBar(player, buildFullBar(onlyBar));
    }

    private ArcaneBar getNormalArcaneBar(Player player) {
        String arcaneKey = getNormalArcaneKey(player);
        if (arcaneKey == null) {
            return null;
        }

        ArcaneTheme theme = ArcaneTheme.fromKey(arcaneKey);
        if (theme == null) {
            return null;
        }

        Status passive = getNormalPassiveStatus(player, arcaneKey);
        Status abilityOne = getAbilityStatus(player, arcaneKey);

        return new ArcaneBar(
                theme,
                renderStatus(theme, passiveIcon(theme), passive),
                renderStatus(theme, abilityOneIcon(theme), abilityOne),
                renderStatus(theme, NONE_ICON, Status.NONE)
        );
    }

    private ArcaneBar getVoidArcaneBar(Player player) {
        if (!hasVoidArcane(player)) {
            return null;
        }

        ArcaneTheme theme = ArcaneTheme.VOID;

        return new ArcaneBar(
                theme,
                renderStatus(theme, passiveIcon(theme), getVoidPassiveStatus(player)),
                renderStatus(theme, abilityOneIcon(theme), getAbilityStatus(player, "void_flight")),
                renderStatus(theme, abilityTwoIcon(theme), getAbilityStatus(player, "void_breath"))
        );
    }

    private void sendStatusBar(Player player, String message) {
        if (!useResourcePackIcons()) {
            player.sendActionBar(message);
            return;
        }

        player.sendActionBar(toActionBarComponent(message));
    }

    private String buildFullBar(ArcaneBar bar) {
        return ChatColor.DARK_GRAY + FRAME + " "
                + bar.passive().full()
                + ChatColor.DARK_GRAY + " " + DIVIDER + " "
                + bar.abilityOne().full()
                + ChatColor.DARK_GRAY + " " + DIVIDER + " "
                + bar.abilityTwo().full()
                + ChatColor.DARK_GRAY + " " + FRAME;
    }

    private String buildDualArcaneBar(ArcaneBar normalBar, ArcaneBar voidBar) {
        if (!plugin.getConfig().getBoolean("status-bar.compact-dual-style", true)) {
            return buildFullBar(normalBar) + ChatColor.DARK_GRAY + " || " + buildFullBar(voidBar);
        }

        return buildCompactBar(normalBar)
                + ChatColor.DARK_GRAY + " || "
                + buildCompactBar(voidBar);
    }

    private String buildCompactBar(ArcaneBar bar) {
        return ChatColor.DARK_GRAY + "["
                + bar.passive().compact()
                + ChatColor.DARK_GRAY + " " + MINI_DIVIDER + " "
                + bar.abilityOne().compact()
                + ChatColor.DARK_GRAY + " " + MINI_DIVIDER + " "
                + bar.abilityTwo().compact()
                + ChatColor.DARK_GRAY + "]";
    }

    private String buildNoArcaneBar() {
        RenderedStatus passive = renderNeutralStatus(NONE_ICON, Status.INACTIVE);
        RenderedStatus abilityOne = renderNeutralStatus(NONE_ICON, Status.NONE);
        RenderedStatus abilityTwo = renderNeutralStatus(NONE_ICON, Status.NONE);

        return ChatColor.DARK_GRAY + FRAME + " "
                + passive.full()
                + ChatColor.DARK_GRAY + " " + DIVIDER + " "
                + abilityOne.full()
                + ChatColor.DARK_GRAY + " " + DIVIDER + " "
                + abilityTwo.full()
                + ChatColor.DARK_GRAY + " " + FRAME;
    }

    private Status getNormalPassiveStatus(Player player, String arcaneKey) {
        if (plugin.getLevelManager().getLevel(player) <= 0) {
            return Status.LOCKED;
        }

        return isNormalPassiveEnabled(player, arcaneKey) ? Status.ACTIVE : Status.INACTIVE;
    }

    private Status getVoidPassiveStatus(Player player) {
        if (plugin.getLevelManager().getLevel(player) <= 0) {
            return Status.LOCKED;
        }

        return ConfigValues.getInt(plugin, "void.passive.extra-hearts", 5, 0, 100) > 0
                ? Status.ACTIVE
                : Status.INACTIVE;
    }

    private Status getAbilityStatus(Player player, String abilityKey) {
        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            return Status.LOCKED;
        }

        long millis = plugin.getCooldownManager().getRemainingMillis(player.getUniqueId(), abilityKey);
        if (millis <= 0) {
            return Status.READY;
        }

        long seconds = Math.max(1L, (long) Math.ceil(millis / 1000.0));
        return Status.cooldown(seconds, getCooldownColor(abilityKey, seconds));
    }

    private RenderedStatus renderStatus(ArcaneTheme theme, String icon, Status status) {
        return switch (status.type()) {
            case ACTIVE -> new RenderedStatus(
                    theme.passiveColor() + icon + statusLabel("ACTIVE", "A"),
                    theme.passiveColor() + icon + " A"
            );
            case INACTIVE -> new RenderedStatus(
                    ChatColor.GRAY + icon + statusLabel("INACTIVE", "I"),
                    ChatColor.GRAY + icon + " I"
            );
            case LOCKED -> new RenderedStatus(
                    ChatColor.RED + icon + statusLabel("LOCKED", "L"),
                    ChatColor.RED + icon + " L"
            );
            case READY -> new RenderedStatus(
                    ChatColor.GREEN + icon + statusLabel("READY", "R"),
                    ChatColor.GREEN + icon + " R"
            );
            case NONE -> new RenderedStatus(
                    ChatColor.GRAY + NONE_ICON + noneLabel(),
                    ChatColor.GRAY.toString() + NONE_ICON
            );
            case COOLDOWN -> new RenderedStatus(
                    status.color() + getCooldownIcon(theme, icon) + " " + status.seconds() + "s",
                    status.color() + getCooldownIcon(theme, icon) + status.seconds() + "s"
            );
        };
    }

    private RenderedStatus renderNeutralStatus(String icon, Status status) {
        return switch (status.type()) {
            case INACTIVE -> new RenderedStatus(ChatColor.GRAY + icon + statusLabel("INACTIVE", "I"), ChatColor.GRAY + icon + " I");
            case NONE -> new RenderedStatus(ChatColor.GRAY + icon + noneLabel(), ChatColor.GRAY.toString() + NONE_ICON);
            default -> new RenderedStatus(ChatColor.GRAY + icon + noneLabel(), ChatColor.GRAY.toString() + NONE_ICON);
        };
    }

    private String statusLabel(String fullText, String shortText) {
        return " " + (plugin.getConfig().getBoolean("status-bar.short-labels", true) ? shortText : fullText);
    }

    private String noneLabel() {
        return plugin.getConfig().getBoolean("status-bar.short-labels", true) ? "" : " NONE";
    }

    private String passiveIcon(ArcaneTheme theme) {
        return useResourcePackIcons() ? theme.resourcePackIcon() : theme.passiveIcon();
    }

    private String abilityOneIcon(ArcaneTheme theme) {
        return useResourcePackIcons() ? theme.resourcePackIcon() : theme.abilityOneIcon();
    }

    private String abilityTwoIcon(ArcaneTheme theme) {
        return useResourcePackIcons() ? theme.resourcePackIcon() : theme.abilityTwoIcon();
    }

    private boolean useResourcePackIcons() {
        return plugin.getConfig().getBoolean("status-bar.resource-pack-icons", true);
    }

    private Component toActionBarComponent(String legacyMessage) {
        TextComponent.Builder builder = Component.text();
        NamedTextColor currentColor = NamedTextColor.WHITE;

        for (int index = 0; index < legacyMessage.length(); index++) {
            char character = legacyMessage.charAt(index);

            if (character == ChatColor.COLOR_CHAR && index + 1 < legacyMessage.length()) {
                ChatColor chatColor = ChatColor.getByChar(legacyMessage.charAt(index + 1));
                NamedTextColor namedColor = toNamedTextColor(chatColor);

                if (namedColor != null) {
                    currentColor = namedColor;
                } else if (chatColor == ChatColor.RESET) {
                    currentColor = NamedTextColor.WHITE;
                }

                index++;
                continue;
            }

            Component characterComponent = Component.text(String.valueOf(character)).color(currentColor);
            builder.append(characterComponent);
        }

        return builder.build();
    }

    private Key getIconFontKey() {
        String configuredFont = plugin.getConfig().getString("status-bar.resource-pack-font", DEFAULT_RESOURCE_PACK_FONT);
        if (configuredFont == null || !configuredFont.contains(":")) {
            return Key.key("arcanesmp", "status");
        }

        String[] parts = configuredFont.split(":", 2);
        try {
            return Key.key(parts[0], parts[1]);
        } catch (IllegalArgumentException ignored) {
            return Key.key("arcanesmp", "status");
        }
    }

    private NamedTextColor toNamedTextColor(ChatColor chatColor) {
        if (chatColor == null) {
            return null;
        }

        return switch (chatColor) {
            case BLACK -> NamedTextColor.BLACK;
            case DARK_BLUE -> NamedTextColor.DARK_BLUE;
            case DARK_GREEN -> NamedTextColor.DARK_GREEN;
            case DARK_AQUA -> NamedTextColor.DARK_AQUA;
            case DARK_RED -> NamedTextColor.DARK_RED;
            case DARK_PURPLE -> NamedTextColor.DARK_PURPLE;
            case GOLD -> NamedTextColor.GOLD;
            case GRAY -> NamedTextColor.GRAY;
            case DARK_GRAY -> NamedTextColor.DARK_GRAY;
            case BLUE -> NamedTextColor.BLUE;
            case GREEN -> NamedTextColor.GREEN;
            case AQUA -> NamedTextColor.AQUA;
            case RED -> NamedTextColor.RED;
            case LIGHT_PURPLE -> NamedTextColor.LIGHT_PURPLE;
            case YELLOW -> NamedTextColor.YELLOW;
            case WHITE -> NamedTextColor.WHITE;
            default -> null;
        };
    }

    private String getCooldownIcon(ArcaneTheme theme, String abilityIcon) {
        if (theme == ArcaneTheme.VOID) {
            return abilityIcon;
        }

        return HOURGLASS_ICON;
    }

    private boolean isNormalPassiveEnabled(Player player, String arcaneKey) {
        int level = plugin.getLevelManager().getLevel(player);

        return switch (arcaneKey) {
            case "breeze" -> true;
            case "frost" -> ConfigValues.isPotionEnabled(
                    plugin,
                    level >= 4 ? "frost.passive.speed-level.level-4" : "frost.passive.speed-level.level-3",
                    level >= 4 ? 2 : 1
            );
            case "ember" -> ConfigValues.isPotionEnabled(plugin, "ember.passive.fire-resistance-level", 1);
            case "luck" -> ConfigValues.isPotionEnabled(plugin, "luck.passive.hero-level", 10);
            default -> false;
        };
    }

    private ChatColor getCooldownColor(String abilityKey, long remainingSeconds) {
        int maxSeconds = getMaxCooldownSeconds(abilityKey);
        if (maxSeconds <= 0) {
            return ChatColor.YELLOW;
        }

        double percentLeft = remainingSeconds / (double) maxSeconds;
        if (percentLeft > 0.50) {
            return ChatColor.RED;
        }
        if (percentLeft > 0.20) {
            return ChatColor.GOLD;
        }
        return ChatColor.YELLOW;
    }

    private int getMaxCooldownSeconds(String abilityKey) {
        return switch (abilityKey) {
            case "breeze" -> ConfigValues.getInt(plugin, "breeze.cooldown-seconds", 25, 0, 86400);
            case "frost" -> ConfigValues.getInt(plugin, "frost.cooldown-seconds", 75, 0, 86400);
            case "ember" -> ConfigValues.getInt(plugin, "ember.cooldown-seconds", 120, 0, 86400);
            case "luck" -> ConfigValues.getInt(plugin, "luck.cooldown-seconds", 60, 0, 86400);
            case "void_flight" -> ConfigValues.getInt(plugin, "void.flight.cooldown-seconds", 30, 0, 86400);
            case "void_breath" -> ConfigValues.getInt(plugin, "void.breath.cooldown-seconds", 60, 0, 86400);
            default -> 0;
        };
    }

    private long getUpdateTicks() {
        if (plugin.getConfig().contains("status-bar.update-ticks")) {
            return ConfigValues.getLong(plugin, "status-bar.update-ticks", 10L, 1L, 200L);
        }

        return ConfigValues.getLong(plugin, "status-bar.update-interval-ticks", 10L, 1L, 200L);
    }

    private boolean hasVoidArcane(Player player) {
        if (VoidArcane.isVoidArcane(player.getInventory().getItemInMainHand())
                || VoidArcane.isVoidArcane(player.getInventory().getItemInOffHand())) {
            return true;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (VoidArcane.isVoidArcane(item)) {
                return true;
            }
        }

        return false;
    }

    private String getNormalArcaneKey(Player player) {
        String heldArcane = getNormalArcaneKey(player.getInventory().getItemInMainHand());
        if (heldArcane != null) {
            return heldArcane;
        }

        heldArcane = getNormalArcaneKey(player.getInventory().getItemInOffHand());
        if (heldArcane != null) {
            return heldArcane;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            String arcane = getNormalArcaneKey(item);
            if (arcane != null) {
                return arcane;
            }
        }

        return null;
    }

    private String getNormalArcaneKey(ItemStack item) {
        if (BreezeArcane.isBreezeArcane(item)) {
            return "breeze";
        }
        if (FrostArcane.isFrostArcane(item)) {
            return "frost";
        }
        if (EmberArcane.isEmberArcane(item)) {
            return "ember";
        }
        if (LuckArcane.isLuckArcane(item)) {
            return "luck";
        }
        return null;
    }

    private enum ArcaneTheme {
        BREEZE("breeze", "\u2726", "\u2601", NONE_ICON, "\uE103", ChatColor.WHITE),
        FROST("frost", "\u2744", "\u2744", NONE_ICON, "\uE102", ChatColor.AQUA),
        EMBER("ember", "\u2739", "\u2600", NONE_ICON, "\uE101", ChatColor.GOLD),
        LUCK("luck", "\u2618", "\u2726", NONE_ICON, "\uE104", ChatColor.YELLOW),
        VOID("void", "\u2727", "\u2604", "\u2620", "\uE105", ChatColor.DARK_PURPLE);

        private final String key;
        private final String passiveIcon;
        private final String abilityOneIcon;
        private final String abilityTwoIcon;
        private final String resourcePackIcon;
        private final ChatColor passiveColor;

        ArcaneTheme(
                String key,
                String passiveIcon,
                String abilityOneIcon,
                String abilityTwoIcon,
                String resourcePackIcon,
                ChatColor passiveColor
        ) {
            this.key = key;
            this.passiveIcon = passiveIcon;
            this.abilityOneIcon = abilityOneIcon;
            this.abilityTwoIcon = abilityTwoIcon;
            this.resourcePackIcon = resourcePackIcon;
            this.passiveColor = passiveColor;
        }

        private static ArcaneTheme fromKey(String key) {
            for (ArcaneTheme theme : values()) {
                if (theme.key.equals(key)) {
                    return theme;
                }
            }
            return null;
        }

        private String passiveIcon() {
            return passiveIcon;
        }

        private String abilityOneIcon() {
            return abilityOneIcon;
        }

        private String abilityTwoIcon() {
            return abilityTwoIcon;
        }

        private String resourcePackIcon() {
            return resourcePackIcon;
        }

        private ChatColor passiveColor() {
            return passiveColor;
        }
    }

    private enum StatusType {
        ACTIVE,
        INACTIVE,
        LOCKED,
        READY,
        COOLDOWN,
        NONE
    }

    private record ArcaneBar(
            ArcaneTheme theme,
            RenderedStatus passive,
            RenderedStatus abilityOne,
            RenderedStatus abilityTwo
    ) {
    }

    private record RenderedStatus(String full, String compact) {
    }

    private record Status(StatusType type, long seconds, ChatColor color) {
        private static final Status ACTIVE = new Status(StatusType.ACTIVE, 0L, ChatColor.GREEN);
        private static final Status INACTIVE = new Status(StatusType.INACTIVE, 0L, ChatColor.GRAY);
        private static final Status LOCKED = new Status(StatusType.LOCKED, 0L, ChatColor.RED);
        private static final Status READY = new Status(StatusType.READY, 0L, ChatColor.GREEN);
        private static final Status NONE = new Status(StatusType.NONE, 0L, ChatColor.GRAY);

        private static Status cooldown(long seconds, ChatColor color) {
            return new Status(StatusType.COOLDOWN, seconds, color);
        }
    }
}
