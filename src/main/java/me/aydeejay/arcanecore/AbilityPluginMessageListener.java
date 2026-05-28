package me.aydeejay.arcanecore;

import me.aydeejay.arcanecore.arcanes.BreezeArcane;
import me.aydeejay.arcanecore.arcanes.EmberArcane;
import me.aydeejay.arcanecore.arcanes.FrostArcane;
import me.aydeejay.arcanecore.arcanes.LuckArcane;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.PluginMessageListener;
import me.aydeejay.arcanecore.listeners.BreezeListener;
import me.aydeejay.arcanecore.listeners.FrostListener;
import me.aydeejay.arcanecore.listeners.EmberListener;
import me.aydeejay.arcanecore.listeners.LuckListener;
import me.aydeejay.arcanecore.arcanes.VoidArcane;
import me.aydeejay.arcanecore.listeners.VoidListener;
import java.nio.charset.StandardCharsets;

public class AbilityPluginMessageListener implements PluginMessageListener {

    private final ArcaneCore plugin;
    private final BreezeListener breezeListener;
    private final FrostListener frostListener;
    private final EmberListener emberListener;
    private final LuckListener luckListener;
    private final VoidListener voidListener;

    public AbilityPluginMessageListener(ArcaneCore plugin) {
        this.plugin = plugin;
        this.breezeListener = new BreezeListener(plugin);
        this.frostListener = new FrostListener(plugin);
        this.emberListener = new EmberListener(plugin);
        this.luckListener = new LuckListener(plugin);
        this.voidListener = new VoidListener(plugin);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {

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

        if (!channel.equals("arcanecore:ability")) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (messageType.equalsIgnoreCase("void_flight")) {
            if (!VoidArcane.isVoidArcane(item)) {
                player.sendActionBar("§cHold the Void Arcane to use Void Flight.");
                return;
            }

            voidListener.activateVoidFlight(player);
            return;
        }

        if (messageType.equalsIgnoreCase("void_breath")) {
            if (!VoidArcane.isVoidArcane(item)) {
                player.sendActionBar("§cHold the Void Arcane to use Void Breath.");
                return;
            }

            voidListener.activateVoidBreath(player);
            return;
        }

        if (!plugin.getArcaneManager().isAnyArcane(item)
                && !VoidArcane.isVoidArcane(item)) {
            player.sendActionBar("§cHold an Arcane to use its ability.");
            return;
        }

        if (!plugin.getLevelManager().canUseArcaneAbility(player)) {
            player.sendActionBar("§cYou unlock this ability at Level 3.");
            return;
        }

        if (BreezeArcane.isBreezeArcane(item)) {
            breezeListener.activateBreeze(player);
            return;
        }

        if (LuckArcane.isLuckArcane(item)) {
            luckListener.activateLuck(player);
            return;
        }

        if (FrostArcane.isFrostArcane(item)) {
            frostListener.activateFrost(player);
            return;
        }

        if (EmberArcane.isEmberArcane(item)) {
            emberListener.activateEmber(player);
            return;
        }

        if (VoidArcane.isVoidArcane(item)) {
            voidListener.activateVoidFlight(player);
            return;
        }

        if (messageType.equalsIgnoreCase("warden_blade")) {
            player.performCommand("__warden_blade_internal");
            return;
        }
    }
}