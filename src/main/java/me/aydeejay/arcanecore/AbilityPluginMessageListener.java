package me.aydeejay.arcanecore;

import me.aydeejay.arcanecore.arcanes.BreezeArcane;
import me.aydeejay.arcanecore.arcanes.EmberArcane;
import me.aydeejay.arcanecore.arcanes.FrostArcane;
import me.aydeejay.arcanecore.arcanes.LuckArcane;
import me.aydeejay.arcanecore.arcanes.VoidArcane;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;

public class AbilityPluginMessageListener implements PluginMessageListener {

    private final ArcaneCore plugin;

    public AbilityPluginMessageListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

        if (!channel.equals("arcanecore:ability")) {
            return;
        }

        String messageType = "ability";

        if (message.length > 0) {
            try {
                int length = message[0];
                if (length > 0 && message.length >= length + 1) {
                    messageType = new String(message, 1, length, StandardCharsets.UTF_8);
                }
            } catch (Exception ignored) {
                messageType = "ability";
            }
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (messageType.equalsIgnoreCase("void_flight")) {
            if (!VoidArcane.isVoidArcane(item)) {
                player.sendMessage(ChatColor.RED + "Hold the Void Arcane to use Void Flight.");
                return;
            }
            if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
                player.sendMessage(ChatColor.RED + "You unlock this ability at Level 3.");
                return;
            }
            plugin.getVoidListener().activateVoidFlight(player);
            return;
        }

        if (messageType.equalsIgnoreCase("void_breath")) {
            if (!VoidArcane.isVoidArcane(item)) {
                player.sendMessage(ChatColor.RED + "Hold the Void Arcane to use Void Breath.");
                return;
            }
            if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
                player.sendMessage(ChatColor.RED + "You unlock this ability at Level 3.");
                return;
            }
            plugin.getVoidListener().activateVoidBreath(player);
            return;
        }

        if (!plugin.getArcaneManager().isAnyArcane(item)) {
            player.sendMessage(ChatColor.RED + "Hold an Arcane to use its ability.");
            return;
        }

        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            player.sendMessage(ChatColor.RED + "You unlock this ability at Level 3.");
            return;
        }

        if (BreezeArcane.isBreezeArcane(item)) {
            plugin.getBreezeListener().activateBreeze(player);
        } else if (LuckArcane.isLuckArcane(item)) {
            plugin.getLuckListener().activateLuck(player);
        } else if (FrostArcane.isFrostArcane(item)) {
            plugin.getFrostListener().activateFrost(player);
        } else if (EmberArcane.isEmberArcane(item)) {
            plugin.getEmberListener().activateEmber(player);
        } else if (VoidArcane.isVoidArcane(item)) {
            plugin.getVoidListener().activateVoidFlight(player);
        }
    }
}
