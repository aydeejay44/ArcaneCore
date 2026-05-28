package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import me.aydeejay.arcanecore.items.LevelItem;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.PlayerInventory;
import me.aydeejay.arcanecore.items.LevelItem;
import me.aydeejay.arcanecore.arcanes.BreezeArcane;
import me.aydeejay.arcanecore.arcanes.FrostArcane;
import me.aydeejay.arcanecore.arcanes.EmberArcane;
import me.aydeejay.arcanecore.arcanes.LuckArcane;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.block.DecoratedPot;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;

public class PlayerListener implements Listener {

    private final ArcaneCore plugin;

    private boolean isNormalArcane(ItemStack item) {
        return BreezeArcane.isBreezeArcane(item)
                || FrostArcane.isFrostArcane(item)
                || EmberArcane.isEmberArcane(item)
                || LuckArcane.isLuckArcane(item);
    }

    private void fixArcaneDupes(Player player) {
        String owned = plugin.getPlayerArcaneManager().getOwnedNormalArcane(player);

        if (owned == null || owned.equalsIgnoreCase("none")) return;

        plugin.getPlayerArcaneManager().removeAllNormalArcanes(player);

        ItemStack arcane = plugin.getPlayerArcaneManager().createNormalArcane(owned);

        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(arcane);

        for (ItemStack item : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }

    public PlayerListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.sendMessage(ChatColor.AQUA + "Your Arcane Level: " +
                plugin.getLevelManager().getLevel(player));

        plugin.getPlayerArcaneManager().giveOwnedArcane(player);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        int level = plugin.getLevelManager().getLevel(player);

        // Level 0 players should not lose/drop levels
        if (level <= 0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getPlayerArcaneManager().giveOwnedArcane(player);
            }, 1L);
            return;
        }

        plugin.getLevelManager().setLevel(player, level - 1);

        player.getWorld().dropItemNaturally(
                player.getLocation(),
                LevelItem.createItem()
        );

        player.sendMessage(ChatColor.RED + "You lost 1 level.");
        player.sendMessage(ChatColor.YELLOW + "A level item dropped where you died.");

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getPlayerArcaneManager().giveOwnedArcane(player);
        }, 1L);
    }

    @EventHandler
    public void onPlayerDeathDrops(PlayerDeathEvent event) {
        event.getDrops().removeIf(item -> isNormalArcane(item));
    }

    @EventHandler
    public void onArcaneDrop(PlayerDropItemEvent event) {

        ItemStack item = event.getItemDrop().getItemStack();

        if (plugin.getPlayerArcaneManager().isNormalArcane(item)) {

            event.setCancelled(true);

            event.getPlayer().sendMessage(
                    ChatColor.RED + "You cannot drop your Arcane."
            );
        }
    }

    @EventHandler
    public void onArcaneCraft(org.bukkit.event.inventory.PrepareItemCraftEvent event) {

        for (ItemStack item : event.getInventory().getMatrix()) {

            if (item == null) {
                continue;
            }

            if (plugin.getPlayerArcaneManager().isNormalArcane(item)) {

                event.getInventory().setResult(null);
                return;
            }
        }
    }

    @EventHandler
    public void onArcaneInventoryMove(org.bukkit.event.inventory.InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack item = event.getCurrentItem();

        if (item == null) {
            return;
        }

        if (!plugin.getPlayerArcaneManager().isNormalArcane(item)) {
            return;
        }

        if (event.getClickedInventory() == player.getInventory()) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You cannot store your Arcane.");
    }

    @EventHandler
    public void onArcaneMoveToChest(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());

        if (!name.equalsIgnoreCase("Breeze")
                && !name.equalsIgnoreCase("Frost")
                && !name.equalsIgnoreCase("Ember")
                && !name.equalsIgnoreCase("Luck")) {
            return;
        }

        InventoryType type = clickedInventory.getType();

        if (type == InventoryType.CHEST
                || type == InventoryType.BARREL
                || type == InventoryType.SHULKER_BOX) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArcaneTrade(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.MERCHANT) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String name = ChatColor.stripColor(meta.getDisplayName());

        if (name.equalsIgnoreCase("Luck")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArcaneClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();

        if (top instanceof PlayerInventory) return;

        // Shift-click arcane from player inventory into container
        if (event.isShiftClick() && isNormalArcane(event.getCurrentItem())) {
            event.setCancelled(true);
            return;
        }

        // Placing cursor item into container slot
        if (event.getRawSlot() < top.getSize() && isNormalArcane(event.getCursor())) {
            event.setCancelled(true);
            return;
        }

        // Number key / hotbar swap into container
        if (event.getClick() == ClickType.NUMBER_KEY && event.getWhoClicked() instanceof Player player) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());

            if (event.getRawSlot() < top.getSize() && isNormalArcane(hotbarItem)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onArcaneDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();

        if (top instanceof PlayerInventory) return;
        if (!isNormalArcane(event.getOldCursor())) return;

        for (int slot : event.getRawSlots()) {
            if (slot < top.getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onArcaneHopperMove(InventoryMoveItemEvent event) {
        if (isNormalArcane(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoinAntiDupe(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                fixArcaneDupes(event.getPlayer());
            }
        }.runTaskLater(plugin, 20L);
    }

    @EventHandler
    public void onRespawnAntiDupe(PlayerRespawnEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                fixArcaneDupes(event.getPlayer());
            }
        }.runTaskLater(plugin, 5L);
    }

    @EventHandler
    public void onInventoryCloseAntiDupe(InventoryCloseEvent event) {
        // Disabled so players can move their Arcane around
        // inside their own inventory without it being reset.
    }

    @EventHandler
    public void onArcanePickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack item = event.getItem().getItemStack();

        if (!isNormalArcane(item)) return;

        String owned = plugin.getPlayerArcaneManager().getOwnedNormalArcane(player);

        if (owned == null || owned.equalsIgnoreCase("none")) {
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }

        if (player.getInventory().containsAtLeast(item, 1)) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @EventHandler
    public void onHeldSlotChangeAntiDupe(PlayerItemHeldEvent event) {
        // Disabled because this was preventing players from moving Arcanes
        // around normally inside their own inventory.
    }

    @EventHandler
    public void onArcaneBundleClick(InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (plugin.getPlayerArcaneManager().isNormalArcane(cursor)
                || plugin.getPlayerArcaneManager().isNormalArcane(current)) {

            if (cursor != null && cursor.getType() == Material.BUNDLE) {
                event.setCancelled(true);
                return;
            }

            if (current != null && current.getType() == Material.BUNDLE) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onArcaneIntoDecoratedPot(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        if (!(event.getClickedBlock().getState() instanceof DecoratedPot)) return;

        ItemStack item = event.getItem();

        if (plugin.getPlayerArcaneManager().isNormalArcane(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot put your Arcane into pots.");
        }
    }

    @EventHandler
    public void onArcaneIntoBookshelf(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        if (event.getClickedBlock().getType() != Material.CHISELED_BOOKSHELF) return;

        ItemStack item = event.getItem();

        if (plugin.getPlayerArcaneManager().isNormalArcane(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot put your Arcane into shelves.");
        }
    }

    @EventHandler
    public void onArcaneBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (plugin.getPlayerArcaneManager().isNormalArcane(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot place your Arcane.");
        }
    }
}