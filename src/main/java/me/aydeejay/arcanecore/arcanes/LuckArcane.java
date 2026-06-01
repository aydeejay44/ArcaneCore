package me.aydeejay.arcanecore.arcanes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class LuckArcane {

    public static final String NAME = ChatColor.GREEN + "Luck Arcane";
    public static final int CUSTOM_MODEL_DATA = 1002;

    public static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(NAME);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.setItemModel(NamespacedKey.fromString("arcanesmp:luck_arcane"));

        meta.setLore(List.of(
                ChatColor.GRAY + "Passive: Hero of the Village X",
                ChatColor.GRAY + "Ability: Lucky Hits",
                ChatColor.GRAY + "Unpredictable Arcane power",
                ChatColor.AQUA + "Bound to your soul"
        ));

        meta.getPersistentDataContainer().set(
                NamespacedKey.fromString("arcanecore:luck_arcane"),
                PersistentDataType.BOOLEAN,
                true
        );

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isLuckArcane(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();

        return meta.getPersistentDataContainer().has(
                NamespacedKey.fromString("arcanecore:luck_arcane"),
                PersistentDataType.BOOLEAN
        );
    }
}