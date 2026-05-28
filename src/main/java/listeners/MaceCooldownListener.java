package me.aydeejay.arcanecore.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.UUID;

public class MaceCooldownListener implements Listener {

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

    private final long cooldownMillis = 10_000;

    @EventHandler
    public void onMaceUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.MACE) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (cooldowns.containsKey(uuid)) {
            long end = cooldowns.get(uuid);

            if (now < end) {
                event.setCancelled(true);

                long secondsLeft = (end - now) / 1000 + 1;

                player.sendActionBar(ChatColor.RED + "Mace Cooldown: " + secondsLeft + "s");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.5f);
                return;
            }
        }

        cooldowns.put(uuid, now + cooldownMillis);
    }
}