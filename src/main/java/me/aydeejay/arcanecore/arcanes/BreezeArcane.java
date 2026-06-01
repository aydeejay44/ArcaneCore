package me.aydeejay.arcanecore.arcanes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class BreezeArcane {

    public static final String NAME = ChatColor.AQUA + "Breeze Arcane";
    public static final int CUSTOM_MODEL_DATA = 1001;

    public static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(NAME);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.setItemModel(NamespacedKey.fromString("arcanesmp:breeze_arcane"));
        meta.setLore(List.of(
                ChatColor.GRAY + "Passive: No Fall Damage",
                ChatColor.GRAY + "Ability: Dash Forward",
                ChatColor.GRAY + "Fast mobility Arcane",
                ChatColor.AQUA + "Bound to your soul"
        ));
        meta.getPersistentDataContainer().set(
                NamespacedKey.fromString("arcanecore:breeze_arcane"),
                PersistentDataType.BOOLEAN,
                true
        );

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isBreezeArcane(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();

        return meta.getPersistentDataContainer().has(
                NamespacedKey.fromString("arcanecore:breeze_arcane"),
                PersistentDataType.BOOLEAN
        );
    }
}