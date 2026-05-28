package me.aydeejay.arcanecore.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;

import java.util.List;

public class LevelItem {

    public static final String NAME = ChatColor.AQUA + "Arcane Level";
    public static final int CUSTOM_MODEL_DATA = 2001;

    public static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(NAME);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.setItemModel(new NamespacedKey("arcanesmp", "level_item"));
        meta.setLore(List.of(
                ChatColor.GRAY + "Consume to gain",
                ChatColor.AQUA + "+1 Arcane Level"
        ));

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isLevelItem(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta.hasCustomModelData()
                && meta.getCustomModelData() == CUSTOM_MODEL_DATA;
    }
}