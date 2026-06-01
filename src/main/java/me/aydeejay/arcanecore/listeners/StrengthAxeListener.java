package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.items.StrengthAxe;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class StrengthAxeListener implements Listener {

    private final ArcaneCore plugin;

    public StrengthAxeListener(ArcaneCore plugin) {
        this.plugin = plugin;
        startTask();
    }

    private void startTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {

                boolean hasAxe = StrengthAxe.isStrengthAxe(player.getInventory().getItemInMainHand())
                        || StrengthAxe.isStrengthAxe(player.getInventory().getItemInOffHand());

                if (hasAxe) {
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.STRENGTH,
                            60,
                            0,
                            true,
                            false
                    ));
                }
            }
        }, 0L, 20L);
    }
}
