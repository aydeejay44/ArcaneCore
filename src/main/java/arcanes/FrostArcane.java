package me.aydeejay.arcanecore.arcanes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class FrostArcane {

    public static final String ITEM_MODEL = "arcanesmp:frost_arcane";

    public static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.PRISMARINE_SHARD);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.AQUA + "Frost Arcane");
        meta.setLore(List.of(
                ChatColor.GRAY + "Passive: Speed",
                ChatColor.GRAY + "Ability: Frozen Circle",
                ChatColor.GRAY + "Freezes enemies for 4 seconds",
                ChatColor.AQUA + "Bound to your soul"
        ));

        meta.setItemModel(NamespacedKey.fromString(ITEM_MODEL));

        meta.getPersistentDataContainer().set(
                NamespacedKey.fromString("arcanecore:frost_arcane"),
                PersistentDataType.BOOLEAN,
                true
        );

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isFrostArcane(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta.getPersistentDataContainer().has(
                NamespacedKey.fromString("arcanecore:frost_arcane"),
                PersistentDataType.BOOLEAN
        );
    }
}