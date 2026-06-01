package me.aydeejay.arcanecore.arcanes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class VoidArcane {

    public static final String ITEM_MODEL = "arcanesmp:void_arcane";

    public static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.DRAGON_EGG);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Void Arcane");
        meta.setLore(List.of(
                ChatColor.GRAY + "Passive: Void Heart",
                ChatColor.GRAY + "Ability: Void Flight",
                ChatColor.GRAY + "Ability: Void Breath",
                ChatColor.DARK_PURPLE + "Forbidden Arcane",
                ChatColor.LIGHT_PURPLE + "Not soulbound"
        ));

        meta.setItemModel(NamespacedKey.fromString(ITEM_MODEL));

        meta.getPersistentDataContainer().set(
                NamespacedKey.fromString("arcanecore:void_arcane"),
                PersistentDataType.BOOLEAN,
                true
        );

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isVoidArcane(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta.getPersistentDataContainer().has(
                NamespacedKey.fromString("arcanecore:void_arcane"),
                PersistentDataType.BOOLEAN
        );
    }
}