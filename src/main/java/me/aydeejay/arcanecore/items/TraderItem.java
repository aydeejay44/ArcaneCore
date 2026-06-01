package me.aydeejay.arcanecore.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class TraderItem {

    public static final String ITEM_MODEL = "arcanesmp:trader";

    public static ItemStack createItem() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "Trader");
        meta.setLore(List.of(
                ChatColor.GRAY + "Right click to trade for a random Arcane reward.",
                ChatColor.DARK_PURPLE + "Mystery Arcane Item"
        ));

        meta.setItemModel(NamespacedKey.fromString(ITEM_MODEL));

        meta.getPersistentDataContainer().set(
                NamespacedKey.fromString("arcanecore:trader"),
                PersistentDataType.BOOLEAN,
                true
        );

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isTrader(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta.getPersistentDataContainer().has(
                NamespacedKey.fromString("arcanecore:trader"),
                PersistentDataType.BOOLEAN
        );
    }
}