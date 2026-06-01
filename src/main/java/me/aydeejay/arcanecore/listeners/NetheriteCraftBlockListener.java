package me.aydeejay.arcanecore.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;

public class NetheriteCraftBlockListener implements Listener {

    @EventHandler
    public void onNetheriteSmithing(PrepareSmithingEvent event) {
        ItemStack result = event.getResult();

        if (result == null) return;

        Material type = result.getType();

        if (type == Material.NETHERITE_HELMET ||
                type == Material.NETHERITE_CHESTPLATE ||
                type == Material.NETHERITE_LEGGINGS ||
                type == Material.NETHERITE_BOOTS ||
                type == Material.NETHERITE_SWORD ||
                type == Material.NETHERITE_AXE ||
                type == Material.NETHERITE_PICKAXE ||
                type == Material.NETHERITE_SHOVEL ||
                type == Material.NETHERITE_HOE) {

            event.setResult(null);
        }
    }
}