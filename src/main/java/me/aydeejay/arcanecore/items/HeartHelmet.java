package me.aydeejay.arcanecore.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;

public class HeartHelmet {

    private static final NamespacedKey KEY = NamespacedKey.fromString("arcanecore:heart_helmet");

    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.NETHERITE_HELMET);
        ArmorMeta meta = (ArmorMeta) item.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "Heart Helmet");
        meta.setUnbreakable(true);
        meta.setTrim(new ArmorTrim(TrimMaterial.QUARTZ, TrimPattern.FLOW));

        meta.addEnchant(Enchantment.PROTECTION, 3, true);
        meta.addEnchant(Enchantment.RESPIRATION, 3, true);
        meta.addEnchant(Enchantment.AQUA_AFFINITY, 1, true);
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);

        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.BOOLEAN, true);

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isHeartHelmet(ItemStack item) {
        return CustomItemMatcher.matches(
                item,
                Material.NETHERITE_HELMET,
                KEY,
                "Heart Helmet"
        );
    }
}
