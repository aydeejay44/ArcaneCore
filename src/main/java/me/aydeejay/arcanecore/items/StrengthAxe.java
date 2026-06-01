package me.aydeejay.arcanecore.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class StrengthAxe {

    private static final NamespacedKey KEY = NamespacedKey.fromString("arcanecore:strength_axe");

    public static ItemStack create() {
        ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta meta = axe.getItemMeta();

        if (meta == null) return axe;

        meta.setDisplayName(ChatColor.RED + "Strength Axe");
        meta.setUnbreakable(true);

        meta.addEnchant(Enchantment.SHARPNESS, 5, true);
        meta.addEnchant(Enchantment.SMITE, 5, true);
        meta.addEnchant(Enchantment.BANE_OF_ARTHROPODS, 5, true);
        meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addEnchant(Enchantment.SILK_TOUCH, 1, true);

        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.BOOLEAN, true);

        axe.setItemMeta(meta);
        return axe;
    }

    public static boolean isStrengthAxe(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(KEY, PersistentDataType.BOOLEAN);
    }
}
