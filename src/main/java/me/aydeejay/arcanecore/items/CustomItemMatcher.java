package me.aydeejay.arcanecore.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

final class CustomItemMatcher {

    private CustomItemMatcher() {
    }

    static boolean matches(ItemStack item, Material material, NamespacedKey key, String displayName) {
        if (item == null || item.getType() != material || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN)) {
            return true;
        }

        return meta.hasDisplayName()
                && displayName.equals(ChatColor.stripColor(meta.getDisplayName()));
    }
}
