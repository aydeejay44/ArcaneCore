package me.aydeejay.arcanecore;

import me.aydeejay.arcanecore.arcanes.BreezeArcane;
import me.aydeejay.arcanecore.arcanes.EmberArcane;
import me.aydeejay.arcanecore.arcanes.FrostArcane;
import me.aydeejay.arcanecore.arcanes.LuckArcane;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.aydeejay.arcanecore.arcanes.VoidArcane;

import java.util.Random;

public class PlayerArcaneManager {

    private final ArcaneCore plugin;
    private final Random random = new Random();

    public PlayerArcaneManager(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    public boolean isNormalArcane(ItemStack item) {
        return BreezeArcane.isBreezeArcane(item)
                || FrostArcane.isFrostArcane(item)
                || EmberArcane.isEmberArcane(item)
                || LuckArcane.isLuckArcane(item);
    }

    public String getNormalArcaneType(ItemStack item) {
        if (BreezeArcane.isBreezeArcane(item)) return "breeze";
        if (FrostArcane.isFrostArcane(item)) return "frost";
        if (EmberArcane.isEmberArcane(item)) return "ember";
        if (LuckArcane.isLuckArcane(item)) return "luck";
        return "none";
    }

    public ItemStack createNormalArcane(String type) {
        return switch (type.toLowerCase()) {
            case "breeze" -> BreezeArcane.createItem();
            case "frost" -> FrostArcane.createItem();
            case "ember" -> EmberArcane.createItem();
            case "luck" -> LuckArcane.createItem();
            default -> BreezeArcane.createItem();
        };
    }

    public ItemStack createRandomNormalArcane() {
        int roll = random.nextInt(4);

        return switch (roll) {
            case 0 -> BreezeArcane.createItem();
            case 1 -> FrostArcane.createItem();
            case 2 -> EmberArcane.createItem();
            default -> LuckArcane.createItem();
        };
    }

    public boolean hasNormalArcane(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isNormalArcane(item)) {
                return true;
            }
        }

        return false;
    }

    public String getOwnedNormalArcane(Player player) {
        return plugin.getConfig().getString(
                "player-arcanes." + player.getUniqueId(),
                "none"
        );
    }

    public void setOwnedNormalArcane(Player player, String type) {
        plugin.getConfig().set(
                "player-arcanes." + player.getUniqueId(),
                type.toLowerCase()
        );
        plugin.saveConfig();
    }

    public void removeAllNormalArcanes(Player player) {

        for (ItemStack item : player.getInventory().getContents()) {
            if (isNormalArcane(item)) {
                item.setAmount(0);
            }
        }

        ItemStack offhand = player.getInventory().getItemInOffHand();

        if (isNormalArcane(offhand)) {
            player.getInventory().setItemInOffHand(null);
        }
    }

    public void giveOwnedArcane(Player player) {
        String owned = getOwnedNormalArcane(player);

        if (owned.equalsIgnoreCase("none")) {
            ItemStack randomArcane = createRandomNormalArcane();
            owned = getNormalArcaneType(randomArcane);
            setOwnedNormalArcane(player, owned);
        }

        removeAllNormalArcanes(player);
        player.getInventory().addItem(createNormalArcane(owned));
    }

    public void replaceOwnedArcane(Player player, ItemStack newArcane) {
        String type = getNormalArcaneType(newArcane);

        if (type.equalsIgnoreCase("none")) {
            return;
        }

        removeAllNormalArcanes(player);
        setOwnedNormalArcane(player, type);
        player.getInventory().addItem(createNormalArcane(type));
    }

    public boolean hasVoidArcane(Player player) {

        for (ItemStack item : player.getInventory().getContents()) {

            if (VoidArcane.isVoidArcane(item)) {
                return true;
            }
        }

        return false;
    }
}