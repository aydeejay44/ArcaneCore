package me.aydeejay.arcanecore.arcanes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class EmberArcane {

    public static final String ITEM_MODEL = "arcanesmp:ember_arcane";

    public static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "Ember Arcane");
        meta.setLore(List.of(
                ChatColor.GRAY + "Passive: Fire Resistance",
                ChatColor.GRAY + "Ability: Ember Blast",
                ChatColor.GRAY + "Shoots a fiery beam",
                ChatColor.AQUA + "Bound to your soul"
        ));

        meta.setItemModel(NamespacedKey.fromString(ITEM_MODEL));

        meta.getPersistentDataContainer().set(
                NamespacedKey.fromString("arcanecore:ember_arcane"),
                PersistentDataType.BOOLEAN,
                true
        );

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isEmberArcane(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta.getPersistentDataContainer().has(
                NamespacedKey.fromString("arcanecore:ember_arcane"),
                PersistentDataType.BOOLEAN
        );
    }
}