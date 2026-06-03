package me.aydeejay.arcanecore;

import me.aydeejay.arcanecore.arcanes.BreezeArcane;
import me.aydeejay.arcanecore.arcanes.EmberArcane;
import me.aydeejay.arcanecore.arcanes.FrostArcane;
import me.aydeejay.arcanecore.arcanes.LuckArcane;
import me.aydeejay.arcanecore.arcanes.VoidArcane;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PlayerArcaneManager {

    private final ArcaneCore plugin;
    private final Random random = new Random();
    private final File file;
    private final Map<UUID, String> owned = new HashMap<>();

    public PlayerArcaneManager(ArcaneCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arcanes.yml");
        load();
    }

    public boolean isNormalArcane(ItemStack item) {
        return BreezeArcane.isBreezeArcane(item)
                || FrostArcane.isFrostArcane(item)
                || EmberArcane.isEmberArcane(item)
                || LuckArcane.isLuckArcane(item);
    }

    public String getNormalArcaneType(ItemStack item) {
        if (BreezeArcane.isBreezeArcane(item)) return "breeze";
        if (FrostArcane.isFrostArcane(item)) return "frost";
        if (EmberArcane.isEmberArcane(item)) return "ember";
        if (LuckArcane.isLuckArcane(item)) return "luck";
        return "none";
    }

    public ItemStack createNormalArcane(String type) {
        if (type == null) {
            return null;
        }

        return switch (type.toLowerCase()) {
            case "breeze" -> BreezeArcane.createItem();
            case "frost" -> FrostArcane.createItem();
            case "ember" -> EmberArcane.createItem();
            case "luck" -> LuckArcane.createItem();
            default -> null;
        };
    }

    public ItemStack createRandomNormalArcane() {
        int roll = random.nextInt(4);

        return switch (roll) {
            case 0 -> BreezeArcane.createItem();
            case 1 -> FrostArcane.createItem();
            case 2 -> EmberArcane.createItem();
            default -> LuckArcane.createItem();
        };
    }

    public boolean hasNormalArcane(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isNormalArcane(item)) {
                return true;
            }
        }
        return false;
    }

    public String getOwnedNormalArcane(Player player) {
        return owned.getOrDefault(player.getUniqueId(), "none");
    }

    public void setOwnedNormalArcane(Player player, String type) {
        owned.put(player.getUniqueId(), type.toLowerCase());
        save();
    }

    public void clearOwnedNormalArcane(Player player) {
        owned.remove(player.getUniqueId());
        save();
    }

    public void removeAllNormalArcanes(Player player) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (isNormalArcane(item)) {
                player.getInventory().setItem(slot, null);
            }
        }

        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (isNormalArcane(offhand)) {
            player.getInventory().setItemInOffHand(null);
        }

        player.updateInventory();
    }

    public void giveOwnedArcane(Player player) {
        String type = getOwnedNormalArcane(player);
        ItemStack arcane = createNormalArcane(type);

        if (arcane == null) {
            arcane = createRandomNormalArcane();
            type = getNormalArcaneType(arcane);
            setOwnedNormalArcane(player, type);
        }

        removeAllNormalArcanes(player);
        giveSingle(player, arcane);
    }

    public void replaceOwnedArcane(Player player, ItemStack newArcane) {
        String type = getNormalArcaneType(newArcane);

        if (type.equalsIgnoreCase("none")) {
            return;
        }

        removeAllNormalArcanes(player);
        setOwnedNormalArcane(player, type);
        giveSingle(player, createNormalArcane(type));
    }

    public void resetPlayerArcanes(Player player) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (plugin.getArcaneManager().isAnyArcane(item)) {
                player.getInventory().setItem(slot, null);
            }
        }

        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (plugin.getArcaneManager().isAnyArcane(offhand)) {
            player.getInventory().setItemInOffHand(null);
        }

        clearOwnedNormalArcane(player);
        player.updateInventory();
    }

    // Soulbound items must never hit the ground. If the inventory is full the
    // grant is skipped; the next grant (join / respawn) restores it.
    private void giveSingle(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "Inventory full - free a slot to receive your Arcane.");
            return;
        }
        player.getInventory().addItem(item);
    }

    public boolean hasVoidArcane(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (VoidArcane.isVoidArcane(item)) {
                return true;
            }
        }
        return false;
    }

    public void load() {
        owned.clear();

        if (file.exists()) {
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            for (String key : data.getKeys(false)) {
                try {
                    owned.put(UUID.fromString(key), data.getString(key, "none"));
                } catch (IllegalArgumentException ignored) {
                    // Skip non-UUID keys.
                }
            }
        }

        migrateLegacyArcanes();
    }

    private void migrateLegacyArcanes() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("player-arcanes");
        if (section == null) {
            return;
        }

        boolean migrated = false;
        for (String key : section.getKeys(false)) {
            try {
                owned.put(UUID.fromString(key), section.getString(key, "none"));
                migrated = true;
            } catch (IllegalArgumentException ignored) {
                // Skip non-UUID keys.
            }
        }

        if (!migrated || !saveToFile()) {
            return;
        }

        plugin.getConfig().set("player-arcanes", null);
        plugin.saveConfig();
    }

    public void save() {
        saveToFile();
    }

    private boolean saveToFile() {
        FileConfiguration data = new YamlConfiguration();
        for (Map.Entry<UUID, String> entry : owned.entrySet()) {
            data.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            data.save(file);
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save arcanes.yml: " + e.getMessage());
            return false;
        }
    }
}
