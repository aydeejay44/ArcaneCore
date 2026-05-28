package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class TrustVisualListener implements Listener {

    private final ArcaneCore plugin;

    public TrustVisualListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    public void startVisualTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player viewer : plugin.getServer().getOnlinePlayers()) {
                for (Player target : plugin.getServer().getOnlinePlayers()) {

                    if (plugin.getTrustManager().isTrustedByUUID(
                            viewer.getUniqueId(),
                            target.getUniqueId()
                    )) {
                        target.setPlayerListName(ChatColor.GREEN + target.getName());
                    } else {
                        target.setPlayerListName(ChatColor.WHITE + target.getName());
                    }
                }
            }
        }, 0L, 40L);
    }
}