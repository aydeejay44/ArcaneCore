package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.arcanes.*;
import me.aydeejay.arcanecore.gui.TraderMenuHolder;
import me.aydeejay.arcanecore.items.TraderItem;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TraderListener implements Listener {

    private final ArcaneCore plugin;
    private final String title = ChatColor.GOLD + "Trader Reroll";
    private final java.util.Set<java.util.UUID> rerolling = new java.util.HashSet<>();

    public TraderListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTraderUse(PlayerInteractEvent event) {
        if (event.getHand() == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getHand() == EquipmentSlot.HAND
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();

        if (!TraderItem.isTrader(item)) return;

        event.setCancelled(true);
        openTraderGui(player);
    }

    private void openTraderGui(Player player) {
        TraderMenuHolder holder = new TraderMenuHolder();
        Inventory gui = Bukkit.createInventory(holder, 27, title);
        holder.setInventory(gui);

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        String current = plugin.getPlayerArcaneManager().getOwnedNormalArcane(player);

        gui.setItem(4, makeItem(Material.ENCHANTED_BOOK, ChatColor.AQUA + "Current Arcane",
                List.of(ChatColor.GRAY + "Current: " + ChatColor.WHITE + current)));

        ItemStack breeze = BreezeArcane.createItem();
        ItemMeta breezeMeta = breeze.getItemMeta();
        breezeMeta.setLore(List.of(
                ChatColor.GRAY + "Chance: " +
                        getChance("breeze") + "%"
        ));
        breeze.setItemMeta(breezeMeta);
        gui.setItem(10, breeze);

        ItemStack frost = FrostArcane.createItem();
        ItemMeta frostMeta = frost.getItemMeta();
        frostMeta.setLore(List.of(
                ChatColor.GRAY + "Chance: " +
                        getChance("frost") + "%"
        ));
        frost.setItemMeta(frostMeta);
        gui.setItem(11, frost);

        ItemStack ember = EmberArcane.createItem();
        ItemMeta emberMeta = ember.getItemMeta();
        emberMeta.setLore(List.of(
                ChatColor.GRAY + "Chance: " +
                        getChance("ember") + "%"
        ));
        ember.setItemMeta(emberMeta);
        gui.setItem(12, ember);

        gui.setItem(15, makeItem(Material.LIME_WOOL, ChatColor.GREEN + "Confirm Reroll",
                List.of(ChatColor.GRAY + "Consumes 1 Trader",
                        ChatColor.GRAY + "Replaces your normal Arcane")));

        gui.setItem(16, makeItem(Material.RED_WOOL, ChatColor.RED + "Cancel",
                List.of(ChatColor.GRAY + "Close this menu")));

        player.openInventory(gui);
    }

    @EventHandler
    public void onTraderGuiClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TraderMenuHolder)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getRawSlot() == 16) {
            player.closeInventory();
            return;
        }

        if (event.getRawSlot() != 15) return;

        ItemStack trader = findTrader(player);

        if (trader == null) {
            player.sendMessage(ChatColor.RED + "You need a Trader to reroll.");
            player.closeInventory();
            return;
        }

        if (!hasConfiguredTraderChance(player)) {
            return;
        }

        startTraderAnimation(player);
    }

    private ItemStack findTrader(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (TraderItem.isTrader(item)) {
                return item;
            }
        }
        return null;
    }

    private void rollTrader(Player player) {
        int breezeChance = getChance("breeze");
        int frostChance = getChance("frost");
        int emberChance = getChance("ember");

        int total = breezeChance + frostChance + emberChance;
        if (total <= 0) {
            player.sendMessage(ChatColor.RED + "Trader chances are not configured correctly.");
            return;
        }
        int roll = ThreadLocalRandom.current().nextInt(total);

        ItemStack reward;
        String rewardName;

        if (roll < breezeChance) {
            reward = BreezeArcane.createItem();
            rewardName = "Breeze";
        } else if (roll < breezeChance + frostChance) {
            reward = FrostArcane.createItem();
            rewardName = "Frost";
        } else if (roll < breezeChance + frostChance + emberChance) {
            reward = EmberArcane.createItem();
            rewardName = "Ember";
        } else {
            throw new IllegalStateException("Trader roll exceeded configured total");
        }

        plugin.getLogger().info("[Trader] Rolled " + roll + " -> " + rewardName);
        plugin.getPlayerArcaneManager().replaceOwnedArcane(player, reward);

        player.sendMessage(ChatColor.LIGHT_PURPLE + "The Trader gave you: " + rewardName);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 100, 0.8, 1, 0.8, 0.4);
    }

    private int getChance(String arcane) {
        return Math.max(0, plugin.getConfig().getInt("trader-chances." + arcane, 0));
    }

    private boolean hasConfiguredTraderChance(Player player) {
        if (getChance("breeze") + getChance("frost") + getChance("ember") > 0) {
            return true;
        }

        player.sendMessage(ChatColor.RED + "Trader chances are not configured correctly.");
        return false;
    }

    private ItemStack makeItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private void startTraderAnimation(Player player) {
        Inventory gui = player.getOpenInventory().getTopInventory();

        if (rerolling.contains(player.getUniqueId())) return;
        rerolling.add(player.getUniqueId());

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline()
                        || !(player.getOpenInventory().getTopInventory().getHolder() instanceof TraderMenuHolder)) {
                    rerolling.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                ItemStack preview;

                int roll = ThreadLocalRandom.current().nextInt(3);

                switch (roll) {
                    case 0 -> preview = BreezeArcane.createItem();
                    case 1 -> preview = FrostArcane.createItem();
                    default -> preview = EmberArcane.createItem();
                }

                gui.setItem(22, preview);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.5f);

                ticks++;

                if (ticks >= 30) {
                    cancel();

                    ItemStack trader = findTrader(player);

                    if (trader == null) {
                        rerolling.remove(player.getUniqueId());
                        player.sendMessage(ChatColor.RED + "You need a Trader to reroll.");
                        player.closeInventory();
                        return;
                    }

                    if (!hasConfiguredTraderChance(player)) {
                        rerolling.remove(player.getUniqueId());
                        player.closeInventory();
                        return;
                    }

                    trader.setAmount(trader.getAmount() - 1);

                    rollTrader(player);
                    rerolling.remove(player.getUniqueId());
                    player.closeInventory();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    @EventHandler
    public void onTraderGuiClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof TraderMenuHolder)) return;

        if (rerolling.contains(player.getUniqueId())) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && rerolling.contains(player.getUniqueId())) {
                    openTraderGui(player);
                }
            }, 1L);
        }
    }
}
