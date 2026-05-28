package me.aydeejay.arcanecore;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class AbilityTriggerManager {

    private final HashMap<UUID, String> triggers = new HashMap<>();

    public String getTrigger(Player player) {
        return triggers.getOrDefault(player.getUniqueId(), "sneakrightclick");
    }

    public void setTrigger(Player player, String trigger) {
        triggers.put(player.getUniqueId(), trigger.toLowerCase());
    }

    public boolean isSneakRightClick(Player player) {
        return getTrigger(player).equalsIgnoreCase("sneakrightclick");
    }

    public boolean isSwap(Player player) {
        return getTrigger(player).equalsIgnoreCase("swap");
    }

    public boolean isSlot(Player player, int slot) {
        return getTrigger(player).equalsIgnoreCase("slot" + slot);
    }
}