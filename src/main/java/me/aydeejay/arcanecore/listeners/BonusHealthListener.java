package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.items.HeartHelmet;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class BonusHealthListener implements Listener {

    private final ArcaneCore plugin;
    private final NamespacedKey modifierKey;

    public BonusHealthListener(ArcaneCore plugin) {
        this.plugin = plugin;
        this.modifierKey = new NamespacedKey(plugin, "bonus_health");
        startTask();
    }

    private void startTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                applyBonusHealth(player);
            }
        }, 0L, 20L);
    }

    // Bonus max-health is expressed as a single, owned AttributeModifier instead
    // of stomping the base value. This composes with vanilla and other plugins
    // and is removed cleanly when the conditions no longer hold.
    private void applyBonusHealth(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) {
            return;
        }

        double bonus = 0.0;

        if (plugin.getLevelManager().getLevel(player) > 0) {
            if (HeartHelmet.isHeartHelmet(player.getInventory().getHelmet())) {
                bonus += 10.0; // 5 hearts
            }
            if (plugin.getArcaneManager().hasVoid(player)) {
                bonus += plugin.getConfig().getInt("void.passive.extra-hearts", 5) * 2.0;
            }
        }

        removeBonusHealth(attr);

        if (bonus > 0) {
            attr.addModifier(new AttributeModifier(
                    modifierKey,
                    bonus,
                    AttributeModifier.Operation.ADD_NUMBER
            ));
        }

        capHealth(player, attr);
    }

    public void shutdown() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
            if (attr == null) {
                continue;
            }

            removeBonusHealth(attr);
            capHealth(player, attr);
        }
    }

    private void removeBonusHealth(AttributeInstance attr) {
        attr.getModifiers().stream()
                .filter(m -> modifierKey.equals(m.getKey()))
                .forEach(attr::removeModifier);
    }

    private void capHealth(Player player, AttributeInstance attr) {
        if (player.getHealth() > attr.getValue()) {
            player.setHealth(attr.getValue());
        }
    }
}
