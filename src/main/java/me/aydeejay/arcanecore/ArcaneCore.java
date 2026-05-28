package me.aydeejay.arcanecore;

import me.aydeejay.arcanecore.listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;
import me.aydeejay.arcanecore.listeners.BreezeListener;
import me.aydeejay.arcanecore.listeners.LevelItemListener;
import me.aydeejay.arcanecore.items.LevelItem;
import me.aydeejay.arcanecore.listeners.LuckListener;
import me.aydeejay.arcanecore.listeners.TrustListener;
import me.aydeejay.arcanecore.listeners.TrustVisualListener;
import me.aydeejay.arcanecore.listeners.FrostListener;
import me.aydeejay.arcanecore.ArcaneManager;
import me.aydeejay.arcanecore.listeners.EmberListener;
import me.aydeejay.arcanecore.listeners.AbilitySwapListener;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import me.aydeejay.arcanecore.arcanes.VoidArcane;
import org.bukkit.inventory.ShapelessRecipe;
import me.aydeejay.arcanecore.listeners.VoidListener;
import me.aydeejay.arcanecore.listeners.AbilitySlotListener;
import me.aydeejay.arcanecore.commands.VoidArcaneCommand;
import me.aydeejay.arcanecore.listeners.TraderListener;
import me.aydeejay.arcanecore.commands.TraderCommand;
import me.aydeejay.arcanecore.items.TraderItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.Bukkit;
import me.aydeejay.arcanecore.listeners.ArcaneGuiListener;
import me.aydeejay.arcanecore.listeners.ArcaneKillEffectListener;
import me.aydeejay.arcanecore.listeners.LevelZeroListener;
import me.aydeejay.arcanecore.listeners.MaceCooldownListener;

public class ArcaneCore extends JavaPlugin {

    private TrustManager trustManager;
    private LevelManager levelManager;
    private CooldownManager cooldownManager;
    private ArcaneManager arcaneManager;
    private AbilityTriggerManager abilityTriggerManager;
    private PlayerArcaneManager playerArcaneManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        trustManager = new TrustManager(this);
        trustManager.loadTrusts();
        levelManager = new LevelManager(this);
        levelManager.loadLevels();
        cooldownManager = new CooldownManager(this);
        arcaneManager = new ArcaneManager();
        abilityTriggerManager = new AbilityTriggerManager();
        playerArcaneManager = new PlayerArcaneManager(this);

        TrustVisualListener trustVisualListener = new TrustVisualListener(this);
        trustVisualListener.startVisualTask();

        getServer().getPluginManager().registerEvents(new MaceCooldownListener(), this);

        getServer().getPluginManager().registerEvents(new LevelZeroListener(this), this);

        getServer().getPluginManager().registerEvents(
                new TrustListener(this),
                this
        );

        getServer().getPluginManager().registerEvents(
                new PlayerListener(this),
                this
        );

        getServer().getPluginManager().registerEvents(
                new BreezeListener(this),
                this
        );

        getServer().getPluginManager().registerEvents(
                new LevelItemListener(this),
                this
        );

        FrostListener frostListener = new FrostListener(this);

        getServer().getPluginManager().registerEvents(
                frostListener,
                this
        );

        frostListener.startPassiveTask();

        LuckListener luckListener = new LuckListener(this);

        getServer().getPluginManager().registerEvents(
                luckListener,
                this
        );

        luckListener.startPassiveTask();

        EmberListener emberListener = new EmberListener(this);

        getServer().getPluginManager().registerEvents(
                emberListener,
                this
        );

        emberListener.startPassiveTask();

        getServer().getPluginManager().registerEvents(
                new AbilitySwapListener(this),
                this
        );

        getServer().getMessenger().registerIncomingPluginChannel(
                this,
                "arcanecore:ability",
                new AbilityPluginMessageListener(this)
        );

        VoidListener voidListener = new VoidListener(this);

        getServer().getPluginManager().registerEvents(
                voidListener,
                this
        );

        voidListener.startPassiveTask();

        getServer().getPluginManager().registerEvents(
                new AbilitySlotListener(this),
                this
        );

        NamespacedKey key = new NamespacedKey(this, "arcane_level_item");

        getServer().getPluginManager().registerEvents(
                new TraderListener(this),
                this
        );

        ShapedRecipe recipe = new ShapedRecipe(key, LevelItem.createItem());
        recipe.shape(
                "DSD",
                "NKN",
                "DSD"
        );

        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('S', Material.NETHER_STAR);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('K', Material.OMINOUS_TRIAL_KEY);

        getServer().addRecipe(recipe);

        ShapelessRecipe voidRecipe = new ShapelessRecipe(
                new NamespacedKey(this, "void_arcane"),
                VoidArcane.createItem()
        );

        voidRecipe.addIngredient(Material.DRAGON_EGG);

        getServer().addRecipe(voidRecipe);

        getServer().removeRecipe(new NamespacedKey(this, "trader"));

        ShapedRecipe traderRecipe = new ShapedRecipe(
                new NamespacedKey(this, "trader"),
                TraderItem.createItem()
        );

        traderRecipe.shape(
                "DGN",
                "EKE",
                "NGD"
        );

        traderRecipe.setIngredient('D', Material.DIAMOND_BLOCK);
        traderRecipe.setIngredient('G', Material.GOLD_BLOCK);
        traderRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        traderRecipe.setIngredient('E', Material.EMERALD_BLOCK);
        traderRecipe.setIngredient('K', Material.OMINOUS_TRIAL_KEY);

        getServer().addRecipe(traderRecipe);

        getServer().getPluginManager().registerEvents(new ArcaneGuiListener(this), this);

        getServer().getPluginManager().registerEvents(new ArcaneKillEffectListener(this), this);

        ArcaneLevelCommand arcaneCommand = new ArcaneLevelCommand(this);

        getCommand("arcane").setExecutor(arcaneCommand);

        getCommand("givebreeze").setExecutor(new GiveBreezeCommand(this));

        getCommand("giveluck").setExecutor(new GiveLuckCommand(this));

        getCommand("givefrost").setExecutor(new GiveFrostCommand(this));

        getCommand("giveember").setExecutor(new GiveEmberCommand(this));

        getCommand("givevoid").setExecutor(new VoidArcaneCommand(this));

        getCommand("trust").setExecutor(new TrustCommand(this));
        getCommand("untrust").setExecutor(new UntrustCommand(this));

        getCommand("trustlist").setExecutor(new TrustListCommand(this));

        getCommand("levelitem").setExecutor(new LevelItemCommand());

        getCommand("__arcane_internal_ability").setExecutor(new ArcaneAbilityCommand(this));

        getCommand("givetrader").setExecutor(new TraderCommand());

        getCommand("withdrawlevel").setExecutor(new AbilityCommand(this));

        getLogger().info("ArcaneCore enabled!");

        registerEasyAnvil();
        registerEasyShulker();
        registerEasyGoldenApple();
        registerEasyCobweb();
        registerEasyPackedIce();
        registerStringCobweb();
        registerStringToCobwebStack();
    }

    private void registerEasyGoldenApple() {
        ItemStack result = new ItemStack(Material.GOLDEN_APPLE);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "easy_golden_apple"), result);
        recipe.shape(
                " G ",
                "GAG",
                " G "
        );

        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('A', Material.APPLE);

        Bukkit.addRecipe(recipe);
    }

    private void registerEasyCobweb() {
        ItemStack result = new ItemStack(Material.COBWEB);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "easy_cobweb"), result);
        recipe.shape(
                "S S",
                " S ",
                "S S"
        );

        recipe.setIngredient('S', Material.STRING);

        Bukkit.addRecipe(recipe);
    }

    private void registerEasyPackedIce() {
        ItemStack result = new ItemStack(Material.PACKED_ICE);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "easy_packed_ice"), result);
        recipe.shape("I");

        recipe.setIngredient('I', Material.ICE);

        Bukkit.addRecipe(recipe);
    }

    private void registerStringCobweb() {
        ItemStack result = new ItemStack(Material.COBWEB);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "string_cobweb"), result);
        recipe.shape(
                "T T",
                " T ",
                "T T"
        );

        recipe.setIngredient('T', Material.TRIPWIRE_HOOK);

        Bukkit.addRecipe(recipe);
    }

    private void registerStringToCobwebStack() {
        ItemStack result = new ItemStack(Material.STRING, 64);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "string_to_cobweb_stack"), result);
        recipe.shape("S");

        recipe.setIngredient('S', Material.STRING);

        Bukkit.addRecipe(recipe);
    }

    @Override
    public void onDisable() {
        trustManager.saveTrusts();
        levelManager.saveLevels();
        getLogger().info("ArcaneCore disabled!");
    }

    public TrustManager getTrustManager() {
        return trustManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ArcaneManager getArcaneManager() {
        return arcaneManager;
    }

    public AbilityTriggerManager getAbilityTriggerManager() {
        return abilityTriggerManager;
    }

    public PlayerArcaneManager getPlayerArcaneManager() {
        return playerArcaneManager;
    }

    private void registerEasyAnvil() {
        ItemStack anvil = new ItemStack(Material.ANVIL);

        ShapedRecipe recipe = new ShapedRecipe(
                new NamespacedKey(this, "easy_anvil"),
                anvil
        );

        recipe.shape(
                "B",
                "I"
        );

        recipe.setIngredient('B', Material.IRON_BLOCK);
        recipe.setIngredient('I', Material.IRON_INGOT);

        Bukkit.addRecipe(recipe);
    }

    private void registerEasyShulker() {
        ItemStack shulker = new ItemStack(Material.SHULKER_BOX);

        ShapedRecipe recipe = new ShapedRecipe(
                new NamespacedKey(this, "easy_shulker"),
                shulker
        );

        recipe.shape(
                "ADA",
                "DBD",
                "ADA"
        );

        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('B', Material.BUNDLE);

        Bukkit.addRecipe(recipe);
    }
}