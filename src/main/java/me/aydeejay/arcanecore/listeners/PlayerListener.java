package me.aydeejay.arcanecore.listeners;

import me.aydeejay.arcanecore.ArcaneCore;
import me.aydeejay.arcanecore.arcanes.LuckArcane;
import me.aydeejay.arcanecore.items.LevelItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.DecoratedPot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final ArcaneCore plugin;

    public PlayerListener(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    private boolean isNormalArcane(ItemStack item) {
        return plugin.getPlayerArcaneManager().isNormalArcane(item);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getLevelManager().ensureLevel(player);

        player.sendMessage(ChatColor.AQUA + "Your Arcane Level: "
                + plugin.getLevelManager().getLevel(player));

        plugin.getPlayerArcaneManager().giveOwnedArcane(player);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> plugin.getPlayerArcaneManager().giveOwnedArcane(player), 1L);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        int level = plugin.getLevelManager().getLevel(player);

        // Level 0 players keep their (lack of) level; the arcane is re-granted on respawn.
        if (level <= 0) {
            return;
        }

        plugin.getLevelManager().setLevel(player, level - 1);

        player.getWorld().dropItemNaturally(player.getLocation(), LevelItem.createItem());

        player.sendMessage(ChatColor.RED + "You lost 1 level.");
        player.sendMessage(ChatColor.YELLOW + "A level item dropped where you died.");
    }

    @EventHandler
    public void onPlayerDeathDrops(PlayerDeathEvent event) {
        event.getDrops().removeIf(this::isNormalArcane);
    }

    @EventHandler
    public void onArcaneDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        if (isNormalArcane(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot drop your Arcane.");
        }
    }

    @EventHandler
    public void onArcaneCraft(org.bukkit.event.inventory.PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item == null) {
                continue;
            }
            if (isNormalArcane(item)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    @EventHandler
    public void onArcaneInventoryMove(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item == null || !isNormalArcane(item)) {
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
        if (item == null || item.getType().isAir() || !isNormalArcane(item)) return;

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

        if (LuckArcane.isLuckArcane(event.getCurrentItem())
                || LuckArcane.isLuckArcane(event.getCursor())) {
            event.setCancelled(true);
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY && event.getWhoClicked() instanceof Player player) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if (LuckArcane.isLuckArcane(hotbarItem)) {
                event.setCancelled(true);
            }
        }

        if (event.getClick() == ClickType.SWAP_OFFHAND && event.getWhoClicked() instanceof Player player) {
            if (LuckArcane.isLuckArcane(player.getInventory().getItemInOffHand())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onArcaneClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top instanceof PlayerInventory) return;

        if (event.isShiftClick() && isNormalArcane(event.getCurrentItem())) {
            event.setCancelled(true);
            return;
        }

        if (event.getRawSlot() < top.getSize() && isNormalArcane(event.getCursor())) {
            event.setCancelled(true);
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY && event.getWhoClicked() instanceof Player player) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if (event.getRawSlot() < top.getSize() && isNormalArcane(hotbarItem)) {
                event.setCancelled(true);
            }
        }

        if (event.getClick() == ClickType.SWAP_OFFHAND && event.getWhoClicked() instanceof Player player) {
            ItemStack offhandItem = player.getInventory().getItemInOffHand();
            if (event.getRawSlot() < top.getSize() && isNormalArcane(offhandItem)) {
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

        String pickedUp = plugin.getPlayerArcaneManager().getNormalArcaneType(item);

        if (!owned.equalsIgnoreCase(pickedUp)
                || plugin.getPlayerArcaneManager().hasNormalArcane(player)) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @EventHandler
    public void onArcaneBundleClick(InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (isNormalArcane(cursor) || isNormalArcane(current)) {
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

        if (isNormalArcane(event.getItem())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot put your Arcane into pots.");
        }
    }

    @EventHandler
    public void onArcaneIntoBookshelf(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.CHISELED_BOOKSHELF) return;

        if (isNormalArcane(event.getItem())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot put your Arcane into shelves.");
        }
    }

    @EventHandler
    public void onArcaneBlockPlace(BlockPlaceEvent event) {
        if (isNormalArcane(event.getItemInHand())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place your Arcane.");
        }
    }
}
