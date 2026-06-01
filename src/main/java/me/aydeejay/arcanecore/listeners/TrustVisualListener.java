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
            for (Player target : plugin.getServer().getOnlinePlayers()) {
                boolean hasOnlineTrust = false;

                for (Player other : plugin.getServer().getOnlinePlayers()) {
                    if (!target.equals(other) && plugin.getTrustManager().isTrustedByUUID(
                            target.getUniqueId(),
                            other.getUniqueId()
                    )) {
                        hasOnlineTrust = true;
                        break;
                    }
                }

                target.setPlayerListName((hasOnlineTrust ? ChatColor.GREEN : ChatColor.WHITE)
                        + target.getName());
            }
        }, 0L, 40L);
    }

    public void shutdown() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.setPlayerListName(null);
        }
    }
}
