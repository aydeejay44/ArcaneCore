package me.aydeejay.arcanecore;

import me.aydeejay.arcanecore.arcanes.VoidArcane;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import me.aydeejay.arcanecore.arcanes.BreezeArcane;
import me.aydeejay.arcanecore.arcanes.FrostArcane;
import me.aydeejay.arcanecore.arcanes.EmberArcane;
import me.aydeejay.arcanecore.arcanes.LuckArcane;
import me.aydeejay.arcanecore.arcanes.VoidArcane;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import java.util.HashMap;
import java.util.List;

public class ArcaneLevelCommand implements CommandExecutor {

    private final ArcaneCore plugin;

    public ArcaneLevelCommand(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Keep admin set command working
        if (args.length == 4 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("arcanecore.admin")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "That player is not online.");
                return true;
            }

            String arcane = args[3].toLowerCase();

            if (arcane.equals("void")) {
                giveItem(target, VoidArcane.createItem());
                sender.sendMessage(ChatColor.GREEN + "Gave Void Arcane to " + target.getName() + ".");
                return true;
            }

            ItemStack newArcane = plugin.getPlayerArcaneManager().createNormalArcane(arcane);
            if (newArcane == null) {
                sender.sendMessage(ChatColor.RED + "Use: breeze, frost, ember, luck, or void.");
                return true;
            }

            plugin.getPlayerArcaneManager().setOwnedNormalArcane(target, arcane);
            plugin.getPlayerArcaneManager().removeAllNormalArcanes(target);
            giveItem(target, newArcane);

            sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s Arcane to " + arcane + ".");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        openArcaneGui(player);
        return true;
    }

    private void openArcaneGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Arcane Status");

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        ItemStack cyanPane = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta cyanMeta = cyanPane.getItemMeta();
        cyanMeta.setDisplayName(" ");
        cyanPane.setItemMeta(cyanMeta);

        gui.setItem(9, cyanPane);
        gui.setItem(18, cyanPane);

        ItemStack bluePane = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta blueMeta = bluePane.getItemMeta();
        blueMeta.setDisplayName(" ");
        bluePane.setItemMeta(blueMeta);

        gui.setItem(11, bluePane);
        gui.setItem(20, bluePane);

        ItemStack orangePane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta orangeMeta = orangePane.getItemMeta();
        orangeMeta.setDisplayName(" ");
        orangePane.setItemMeta(orangeMeta);

        gui.setItem(15, orangePane);
        gui.setItem(24, orangePane);

        ItemStack limePane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta limeMeta = limePane.getItemMeta();
        limeMeta.setDisplayName(" ");
        limePane.setItemMeta(limeMeta);

        gui.setItem(17, limePane);
        gui.setItem(26, limePane);

        ItemStack purplePane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta purpleMeta = purplePane.getItemMeta();
        purpleMeta.setDisplayName(" ");
        purplePane.setItemMeta(purpleMeta);

        gui.setItem(3, purplePane);
        gui.setItem(5, purplePane);



        int level = plugin.getLevelManager().getLevel(player);
        String arcane = plugin.getPlayerArcaneManager().getOwnedNormalArcane(player);
        boolean hasVoid = plugin.getPlayerArcaneManager().hasVoidArcane(player);

        gui.setItem(11, makeItem(Material.ENCHANTED_BOOK, ChatColor.LIGHT_PURPLE + "Arcane",
                List.of(ChatColor.GRAY + "Current: " + ChatColor.WHITE + arcane)));

        gui.setItem(13, makeItem(Material.EXPERIENCE_BOTTLE, ChatColor.AQUA + "Level",
                List.of(ChatColor.GRAY + "Current Level: " + ChatColor.WHITE + level)));

        gui.setItem(15, makeItem(hasVoid ? Material.ENDER_EYE : Material.ENDER_PEARL,
                ChatColor.DARK_PURPLE + "Void Arcane",
                List.of(ChatColor.GRAY + "Owned: " + ChatColor.WHITE + (hasVoid ? "Yes" : "No"))));

        gui.setItem(22, makeItem(Material.CLOCK, ChatColor.GOLD + "Cooldowns",
                List.of(
                        ChatColor.GRAY + "Breeze: " + cooldownText(player, "breeze"),
                        ChatColor.GRAY + "Frost: " + cooldownText(player, "frost"),
                        ChatColor.GRAY + "Ember: " + cooldownText(player, "ember"),
                        ChatColor.GRAY + "Luck: " + cooldownText(player, "luck"),
                        ChatColor.GRAY + "Void Flight: " + cooldownText(player, "void_flight"),
                        ChatColor.GRAY + "Void Breath: " + cooldownText(player, "void_breath")
                )));

        ItemStack breezeInfo = BreezeArcane.createItem();
        ItemMeta breezeMeta = breezeInfo.getItemMeta();
        breezeMeta.setDisplayName(ChatColor.AQUA + "Breeze");
        breezeMeta.setLore(List.of(
                ChatColor.GRAY + "Passive: no fall damage",
                ChatColor.GRAY + "Ability: wind dash",
                ChatColor.GRAY + "Style: fast movement"
        ));

        if (arcane.equalsIgnoreCase("breeze")) {
            breezeMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
            breezeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        breezeInfo.setItemMeta(breezeMeta);
        gui.setItem(10, breezeInfo);

        ItemStack frostInfo = FrostArcane.createItem();
        ItemMeta frostMeta = frostInfo.getItemMeta();
        frostMeta.setDisplayName(ChatColor.AQUA + "Frost");
        frostMeta.setLore(List.of(
                ChatColor.GRAY + "Passive: speed I",
                ChatColor.GRAY + "Ability: freezes enemies",
                ChatColor.GRAY + "Style: control"
        ));

        if (arcane.equalsIgnoreCase("frost")) {
            frostMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
            frostMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        frostInfo.setItemMeta(frostMeta);
        gui.setItem(12, frostInfo);

        ItemStack emberInfo = EmberArcane.createItem();
        ItemMeta emberMeta = emberInfo.getItemMeta();
        emberMeta.setDisplayName(ChatColor.RED + "Ember");
        emberMeta.setLore(List.of(
                ChatColor.GRAY + "Passive: fire resistance",
                ChatColor.GRAY + "Ability: fire beam",
                ChatColor.GRAY + "Style: high damage"
        ));

        if (arcane.equalsIgnoreCase("ember")) {
            emberMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
            emberMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        emberInfo.setItemMeta(emberMeta);
        gui.setItem(14, emberInfo);

        ItemStack luckInfo = LuckArcane.createItem();
        ItemMeta luckMeta = luckInfo.getItemMeta();
        luckMeta.setDisplayName(ChatColor.GREEN + "Luck");
        luckMeta.setLore(List.of(
                ChatColor.GRAY + "Passive: hero of the village X",
                ChatColor.GRAY + "Ability: chance effects",
                ChatColor.GRAY + "Style: luck"
        ));

        if (arcane.equalsIgnoreCase("luck")) {
            luckMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
            luckMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        luckInfo.setItemMeta(luckMeta);
        gui.setItem(16, luckInfo);

        ItemStack voidInfo = VoidArcane.createItem();
        ItemMeta voidMeta = voidInfo.getItemMeta();
        voidMeta.setDisplayName(ChatColor.DARK_PURPLE + "Void");
        voidMeta.setLore(List.of(
                ChatColor.GRAY + "Separate from normal Arcanes",
                ChatColor.GRAY + "Passive: 4 extra hearts",
                ChatColor.GRAY + "Ability: Void Flight",
                ChatColor.GRAY + "Ability: Void Breath",
                ChatColor.GRAY + "Style: Absolute Power"
        ));

        if (hasVoid) {
            voidMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
            voidMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        voidInfo.setItemMeta(voidMeta);
        gui.setItem(4, voidInfo);

        player.openInventory(gui);
        startGuiCooldownUpdater(player);
    }

    private ItemStack makeItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private void giveItem(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);

        for (ItemStack leftoverItem : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
        }
    }

    private String cooldownText(Player player, String key) {
        long millis = plugin.getCooldownManager().getRemainingMillis(player.getUniqueId(), key);

        if (millis <= 0) {
            return ChatColor.GREEN + "Ready";
        }

        double seconds = millis / 1000.0;
        return ChatColor.RED + String.format("%.1fs", seconds);
    }

    private void startGuiCooldownUpdater(Player player) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {

            if (!player.isOnline()
                    || !player.getOpenInventory().getTitle().equals(ChatColor.DARK_PURPLE + "Arcane Status")) {
                task.cancel();
                return;
            }

            Inventory gui = player.getOpenInventory().getTopInventory();

            gui.setItem(22, makeItem(Material.CLOCK, ChatColor.GOLD + "Cooldowns",
                    List.of(
                            ChatColor.GRAY + "Breeze: " + cooldownText(player, "breeze"),
                            ChatColor.GRAY + "Frost: " + cooldownText(player, "frost"),
                            ChatColor.GRAY + "Ember: " + cooldownText(player, "ember"),
                            ChatColor.GRAY + "Void Flight: " + cooldownText(player, "void_flight"),
                            ChatColor.GRAY + "Void Breath: " + cooldownText(player, "void_breath")
                    )));

        }, 0L, 20L);
    }
}