package me.aydeejay.arcanecore.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class LevelItem {

    public static final String NAME = ChatColor.AQUA + "Arcane Level";
    public static final int CUSTOM_MODEL_DATA = 2001;

    private static final NamespacedKey KEY = NamespacedKey.fromString("arcanecore:level_item");

    public static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(NAME);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.setItemModel(NamespacedKey.fromString("arcanesmp:level_item"));
        meta.setLore(List.of(
                ChatColor.GRAY + "Consume to gain",
                ChatColor.AQUA + "+1 Arcane Level"
        ));
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isLevelItem(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(KEY, PersistentDataType.BOOLEAN);
    }
}
