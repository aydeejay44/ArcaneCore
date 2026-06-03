package me.aydeejay.arcanecore.commands;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.ConfigValues;
import me.aydeejay.arcanecore.arcanes.VoidArcane;
import me.aydeejay.arcanecore.items.HasteLeggings;
import me.aydeejay.arcanecore.items.HeartHelmet;
import me.aydeejay.arcanecore.items.ResistanceChestplate;
import me.aydeejay.arcanecore.items.SpeedBoots;
import me.aydeejay.arcanecore.items.StrengthAxe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ArcaneAdminCommand {

    private static final String PERMISSION = "arcanecore.admin";
    private static final String PREFIX = ChatColor.DARK_PURPLE + "[ArcaneCore] " + ChatColor.GRAY;

    private static final List<String> SUBCOMMANDS = List.of(
            "setlevel",
            "givearcane",
            "giveitem",
            "reloadconfig",
            "statuscompact",
            "statusshort",
            "statuspack",
            "statustest",
            "resetplayer"
    );

    private static final List<String> ARCANES = List.of("breeze", "frost", "ember", "luck", "void");
    private static final List<String> ITEMS = List.of(
            "hearthelmet",
            "resistancechestplate",
            "hasteleggings",
            "speedboots",
            "strengthaxe"
    );

    private final ArcaneCore plugin;

    public ArcaneAdminCommand(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use Arcane admin commands.");
            return true;
        }

        if (args.length < 2) {
            sendUsage(sender);
            return true;
        }

        String subcommand = args[1].toLowerCase(Locale.ROOT);

        return switch (subcommand) {
            case "setlevel" -> setLevel(sender, args);
            case "givearcane" -> giveArcane(sender, args);
            case "giveitem" -> giveItem(sender, args);
            case "reloadconfig" -> reloadConfig(sender, args);
            case "statuscompact" -> statusCompact(sender, args);
            case "statusshort" -> statusShort(sender, args);
            case "statuspack" -> statusPack(sender, args);
            case "statustest" -> statusTest(sender, args);
            case "resetplayer" -> resetPlayer(sender, args);
            default -> {
                sender.sendMessage(ChatColor.RED + "Invalid admin subcommand: " + args[1]);
                sendUsage(sender);
                yield true;
            }
        };
    }

    public List<String> tabComplete(String[] args) {
        if (args.length == 1) {
            return match(args[0], List.of("admin"));
        }

        if (args.length == 2) {
            return match(args[1], SUBCOMMANDS);
        }

        String subcommand = args[1].toLowerCase(Locale.ROOT);

        if (args.length == 3 && requiresPlayer(subcommand)) {
            return match(args[2], onlinePlayerNames());
        }

        if (args.length == 3 && subcommand.equals("statuscompact")) {
            return match(args[2], List.of("true", "false"));
        }

        if (args.length == 3 && subcommand.equals("statusshort")) {
            return match(args[2], List.of("true", "false"));
        }

        if (args.length == 3 && subcommand.equals("statuspack")) {
            return match(args[2], List.of("true", "false"));
        }

        if (args.length == 4) {
            return switch (subcommand) {
                case "setlevel" -> match(args[3], levelSuggestions());
                case "givearcane" -> match(args[3], ARCANES);
                case "giveitem" -> match(args[3], ITEMS);
                default -> List.of();
            };
        }

        return List.of();
    }

    private boolean setLevel(CommandSender sender, String[] args) {
        if (args.length != 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /arcane admin setlevel <player> <level>");
            return true;
        }

        Player target = findOnlinePlayer(sender, args[2]);
        if (target == null) {
            return true;
        }

        int level;
        try {
            level = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid level. Use a whole number.");
            return true;
        }

        int maxLevel = ConfigValues.getInt(plugin, "levels.max-level", 4, 0, 100);
        if (level < 0 || level > maxLevel) {
            sender.sendMessage(ChatColor.RED + "Invalid level. Use a value from 0 to " + maxLevel + ".");
            return true;
        }

        plugin.getLevelManager().setLevel(target, level);
        refreshPlayerState(target);

        sender.sendMessage(PREFIX + "Set " + target.getName() + "'s Arcane level to " + ChatColor.AQUA + level + ChatColor.GRAY + ".");
        target.sendMessage(PREFIX + "Your Arcane level was set to " + ChatColor.AQUA + level + ChatColor.GRAY + ".");
        return true;
    }

    private boolean giveArcane(CommandSender sender, String[] args) {
        if (args.length != 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /arcane admin givearcane <player> <arcane>");
            return true;
        }

        Player target = findOnlinePlayer(sender, args[2]);
        if (target == null) {
            return true;
        }

        String arcane = normalize(args[3]);
        if (arcane.equals("void")) {
            giveOrDrop(target, VoidArcane.createItem());
            refreshPlayerState(target);
            sender.sendMessage(PREFIX + "Gave Void Arcane to " + target.getName() + ".");
            return true;
        }

        ItemStack normalArcane = plugin.getPlayerArcaneManager().createNormalArcane(arcane);
        if (normalArcane == null) {
            sender.sendMessage(ChatColor.RED + "Invalid arcane. Use: breeze, frost, ember, luck, or void.");
            return true;
        }

        plugin.getPlayerArcaneManager().replaceOwnedArcane(target, normalArcane);
        refreshPlayerState(target);

        sender.sendMessage(PREFIX + "Gave " + capitalize(arcane) + " Arcane to " + target.getName() + ".");
        return true;
    }

    private boolean giveItem(CommandSender sender, String[] args) {
        if (args.length != 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /arcane admin giveitem <player> <item>");
            return true;
        }

        Player target = findOnlinePlayer(sender, args[2]);
        if (target == null) {
            return true;
        }

        ItemStack item = createCustomItem(normalize(args[3]));
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "Invalid item. Use: hearthelmet, resistancechestplate, hasteleggings, speedboots, or strengthaxe.");
            return true;
        }

        giveOrDrop(target, item);
        sender.sendMessage(PREFIX + "Gave " + args[3] + " to " + target.getName() + ".");
        return true;
    }

    private boolean reloadConfig(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /arcane admin reloadconfig");
            return true;
        }

        plugin.reloadConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
        plugin.reloadConfig();
        plugin.getCooldownManager().clearAllCooldowns();
        plugin.getStatusBarManager().reload();

        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshPlayerState(player);
        }

        sender.sendMessage(PREFIX + "Config reloaded. Active Arcane cooldowns were cleared so new values apply now.");
        return true;
    }

    private boolean statusCompact(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /arcane admin statuscompact <true|false>");
            return true;
        }

        Boolean compact = parseBoolean(args[2]);
        if (compact == null) {
            sender.sendMessage(ChatColor.RED + "Invalid value. Use true for small bars or false for full bars.");
            return true;
        }

        plugin.getConfig().set("status-bar.single-arcane-compact", compact);
        plugin.saveConfig();
        plugin.reloadConfig();
        plugin.getStatusBarManager().reload();

        sender.sendMessage(PREFIX + "Single-Arcane status bar compact mode is now "
                + (compact ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled")
                + ChatColor.GRAY + ".");
        return true;
    }

    private boolean statusShort(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /arcane admin statusshort <true|false>");
            return true;
        }

        Boolean shortLabels = parseBoolean(args[2]);
        if (shortLabels == null) {
            sender.sendMessage(ChatColor.RED + "Invalid value. Use true for short labels or false for full words.");
            return true;
        }

        plugin.getConfig().set("status-bar.short-labels", shortLabels);
        plugin.saveConfig();
        plugin.reloadConfig();
        plugin.getStatusBarManager().reload();

        sender.sendMessage(PREFIX + "Status bar short labels are now "
                + (shortLabels ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled")
                + ChatColor.GRAY + ".");
        return true;
    }

    private boolean statusPack(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /arcane admin statuspack <true|false>");
            return true;
        }

        Boolean resourcePackIcons = parseBoolean(args[2]);
        if (resourcePackIcons == null) {
            sender.sendMessage(ChatColor.RED + "Invalid value. Use true for resource-pack icons or false for normal symbols.");
            return true;
        }

        plugin.getConfig().set("status-bar.resource-pack-icons", resourcePackIcons);
        plugin.saveConfig();
        plugin.reloadConfig();
        plugin.getStatusBarManager().reload();

        sender.sendMessage(PREFIX + "Resource-pack status icons are now "
                + (resourcePackIcons ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled")
                + ChatColor.GRAY + ".");
        if (!resourcePackIcons) {
            sender.sendMessage(PREFIX + "The status bar will use normal Minecraft symbols instead.");
        }
        return true;
    }

    private boolean statusTest(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /arcane admin statustest");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only an in-game player can run the status icon test.");
            return true;
        }

        plugin.getStatusBarManager().sendResourcePackIconTest(player);
        sender.sendMessage(PREFIX + "Sent a status icon test to your chat and action bar.");
        return true;
    }

    private boolean resetPlayer(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /arcane admin resetplayer <player>");
            return true;
        }

        Player target = findOnlinePlayer(sender, args[2]);
        if (target == null) {
            return true;
        }

        plugin.getLevelManager().setLevel(target, 0);
        plugin.getPlayerArcaneManager().resetPlayerArcanes(target);
        plugin.getCooldownManager().clearCooldowns(target.getUniqueId());
        plugin.getFrostListener().unfreeze(target);
        plugin.getVoidListener().endFlight(target);
        unequipCustomEffectItems(target);
        clearCustomEffects(target);
        refreshPlayerState(target);

        sender.sendMessage(PREFIX + "Reset " + target.getName() + "'s levels, Arcanes, cooldowns, and custom effects.");
        target.sendMessage(PREFIX + "Your Arcane data was reset by an admin.");
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_PURPLE + "Arcane Admin Commands:");
        sender.sendMessage(ChatColor.GRAY + "/arcane admin setlevel <player> <level>");
        sender.sendMessage(ChatColor.GRAY + "/arcane admin givearcane <player> <arcane>");
        sender.sendMessage(ChatColor.GRAY + "/arcane admin giveitem <player> <item>");
        sender.sendMessage(ChatColor.GRAY + "/arcane admin reloadconfig");
        sender.sendMessage(ChatColor.GRAY + "/arcane admin statuscompact <true|false>");
        sender.sendMessage(ChatColor.GRAY + "/arcane admin statusshort <true|false>");
        sender.sendMessage(ChatColor.GRAY + "/arcane admin statuspack <true|false>");
        sender.sendMessage(ChatColor.GRAY + "/arcane admin statustest");
        sender.sendMessage(ChatColor.GRAY + "/arcane admin resetplayer <player>");
    }

    private Player findOnlinePlayer(CommandSender sender, String name) {
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) {
            target = Bukkit.getPlayer(name);
        }

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Invalid player. That player must be online.");
            return null;
        }

        return target;
    }

    private ItemStack createCustomItem(String item) {
        return switch (item) {
            case "hearthelmet" -> HeartHelmet.create();
            case "resistancechestplate" -> ResistanceChestplate.create();
            case "hasteleggings" -> HasteLeggings.create();
            case "speedboots" -> SpeedBoots.create();
            case "strengthaxe" -> StrengthAxe.create();
            default -> null;
        };
    }

    private void giveOrDrop(Player target, ItemStack item) {
        HashMap<Integer, ItemStack> leftovers = target.getInventory().addItem(item);
        for (ItemStack leftover : leftovers.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), leftover);
        }
        target.updateInventory();
    }

    private void refreshPlayerState(Player target) {
        plugin.getBonusHealthListener().refreshPlayer(target);

        if (plugin.getLevelManager().getLevel(target) <= 0) {
            target.removePotionEffect(PotionEffectType.SPEED);
            target.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
            target.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
        }
    }

    private void clearCustomEffects(Player target) {
        target.removePotionEffect(PotionEffectType.SPEED);
        target.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        target.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
        target.removePotionEffect(PotionEffectType.RESISTANCE);
        target.removePotionEffect(PotionEffectType.HASTE);
        target.removePotionEffect(PotionEffectType.STRENGTH);
        target.removePotionEffect(PotionEffectType.SLOWNESS);
        target.removePotionEffect(PotionEffectType.JUMP_BOOST);
        target.setFireTicks(0);
        target.setFallDistance(0);
    }

    private void unequipCustomEffectItems(Player target) {
        moveEquippedCustomItem(target, EquipmentSlotType.HELMET);
        moveEquippedCustomItem(target, EquipmentSlotType.CHESTPLATE);
        moveEquippedCustomItem(target, EquipmentSlotType.LEGGINGS);
        moveEquippedCustomItem(target, EquipmentSlotType.BOOTS);
        moveEquippedCustomItem(target, EquipmentSlotType.MAIN_HAND);
        moveEquippedCustomItem(target, EquipmentSlotType.OFF_HAND);
    }

    private void moveEquippedCustomItem(Player target, EquipmentSlotType slot) {
        ItemStack item = switch (slot) {
            case HELMET -> target.getInventory().getHelmet();
            case CHESTPLATE -> target.getInventory().getChestplate();
            case LEGGINGS -> target.getInventory().getLeggings();
            case BOOTS -> target.getInventory().getBoots();
            case MAIN_HAND -> target.getInventory().getItemInMainHand();
            case OFF_HAND -> target.getInventory().getItemInOffHand();
        };

        if (!isCustomEffectItem(item)) {
            return;
        }

        switch (slot) {
            case HELMET -> target.getInventory().setHelmet(null);
            case CHESTPLATE -> target.getInventory().setChestplate(null);
            case LEGGINGS -> target.getInventory().setLeggings(null);
            case BOOTS -> target.getInventory().setBoots(null);
            case MAIN_HAND -> target.getInventory().setItemInMainHand(null);
            case OFF_HAND -> target.getInventory().setItemInOffHand(null);
        }

        storeUnequippedItem(target, item);
    }

    private boolean isCustomEffectItem(ItemStack item) {
        return HeartHelmet.isHeartHelmet(item)
                || ResistanceChestplate.isResistanceChestplate(item)
                || HasteLeggings.isHasteLeggings(item)
                || SpeedBoots.isSpeedBoots(item)
                || StrengthAxe.isStrengthAxe(item);
    }

    private void storeUnequippedItem(Player target, ItemStack item) {
        int heldSlot = target.getInventory().getHeldItemSlot();

        for (int slot = 0; slot < 36; slot++) {
            if (slot == heldSlot) {
                continue;
            }

            if (target.getInventory().getItem(slot) == null) {
                target.getInventory().setItem(slot, item);
                target.updateInventory();
                return;
            }
        }

        target.getWorld().dropItemNaturally(target.getLocation(), item);
        target.updateInventory();
    }

    private boolean requiresPlayer(String subcommand) {
        return subcommand.equals("setlevel")
                || subcommand.equals("givearcane")
                || subcommand.equals("giveitem")
                || subcommand.equals("resetplayer");
    }

    private List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    private List<String> levelSuggestions() {
        int maxLevel = ConfigValues.getInt(plugin, "levels.max-level", 4, 0, 100);
        List<String> levels = new ArrayList<>();
        for (int level = 0; level <= maxLevel; level++) {
            levels.add(String.valueOf(level));
        }
        return levels;
    }

    private List<String> match(String input, List<String> options) {
        String lowerInput = input.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(lowerInput))
                .toList();
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
    }

    private Boolean parseBoolean(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        if (normalized.equals("true") || normalized.equals("on") || normalized.equals("yes")) {
            return true;
        }
        if (normalized.equals("false") || normalized.equals("off") || normalized.equals("no")) {
            return false;
        }
        return null;
    }

    private String capitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    private enum EquipmentSlotType {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS,
        MAIN_HAND,
        OFF_HAND
    }
}
