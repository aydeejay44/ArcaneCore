package me.aydeejay.arcanecore.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.persistence.PersistentDataType;

public class HeartHelmet {

    private static final NamespacedKey KEY = NamespacedKey.fromString("arcanecore:heart_helmet");
    private static final NamespacedKey MODEL_KEY = NamespacedKey.fromString("arcanesmp:the_crown");

    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.NETHERITE_HELMET);
        ArmorMeta meta = (ArmorMeta) item.getItemMeta();

        meta.setDisplayName(ChatColor.YELLOW + "The Crown");
        meta.setItemModel(MODEL_KEY);
        meta.setUnbreakable(true);

        EquippableComponent equippable = meta.getEquippable();
        equippable.setSlot(EquipmentSlot.HEAD);
        equippable.setModel(MODEL_KEY);
        meta.setEquippable(equippable);

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
                "The Crown"
        );
    }
}
