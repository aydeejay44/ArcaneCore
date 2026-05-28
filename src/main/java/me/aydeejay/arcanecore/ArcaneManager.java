package me.aydeejay.arcanecore;

import me.aydeejay.arcanecore.arcanes.BreezeArcane;
import me.aydeejay.arcanecore.arcanes.FrostArcane;
import me.aydeejay.arcanecore.arcanes.LuckArcane;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.aydeejay.arcanecore.arcanes.EmberArcane;
import me.aydeejay.arcanecore.arcanes.VoidArcane;

public class ArcaneManager {

    public boolean isAnyArcane(ItemStack item) {
        return BreezeArcane.isBreezeArcane(item)
                || LuckArcane.isLuckArcane(item)
                || FrostArcane.isFrostArcane(item)
        || EmberArcane.isEmberArcane(item);
    }

    public boolean hasBreeze(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (BreezeArcane.isBreezeArcane(item)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasLuck(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (LuckArcane.isLuckArcane(item)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasFrost(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (FrostArcane.isFrostArcane(item)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasEmber(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (EmberArcane.isEmberArcane(item)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasVoid(Player player) {

        for (ItemStack item : player.getInventory().getContents()) {

            if (VoidArcane.isVoidArcane(item)) {
                return true;
            }
        }

        return false;
    }
}