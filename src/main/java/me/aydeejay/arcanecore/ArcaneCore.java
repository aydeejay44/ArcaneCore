package me.aydeejay.arcanecore;

import me.aydeejay.arcanecore.arcanes.VoidArcane;
import me.aydeejay.arcanecore.commands.AbilityCommand;
import me.aydeejay.arcanecore.commands.ArcaneAbilityCommand;
import me.aydeejay.arcanecore.commands.ArcaneLevelCommand;
import me.aydeejay.arcanecore.commands.GiveBreezeCommand;
import me.aydeejay.arcanecore.commands.GiveEmberCommand;
import me.aydeejay.arcanecore.commands.GiveFrostCommand;
import me.aydeejay.arcanecore.commands.GiveHasteLeggingsCommand;
import me.aydeejay.arcanecore.commands.GiveHeartHelmetCommand;
import me.aydeejay.arcanecore.commands.GiveLuckCommand;
import me.aydeejay.arcanecore.commands.GiveResistanceChestplateCommand;
import me.aydeejay.arcanecore.commands.GiveSpeedBootsCommand;
import me.aydeejay.arcanecore.commands.GiveStrengthAxeCommand;
import me.aydeejay.arcanecore.commands.LevelItemCommand;
import me.aydeejay.arcanecore.commands.SetArcaneLevelCommand;
import me.aydeejay.arcanecore.commands.TraderCommand;
import me.aydeejay.arcanecore.commands.TrustCommand;
import me.aydeejay.arcanecore.commands.TrustListCommand;
import me.aydeejay.arcanecore.commands.UntrustCommand;
import me.aydeejay.arcanecore.commands.VoidArcaneCommand;
import me.aydeejay.arcanecore.items.LevelItem;
import me.aydeejay.arcanecore.items.TraderItem;
import me.aydeejay.arcanecore.listeners.AbilitySlotListener;
import me.aydeejay.arcanecore.listeners.AbilitySwapListener;
import me.aydeejay.arcanecore.listeners.ArcaneGuiListener;
import me.aydeejay.arcanecore.listeners.ArcaneKillEffectListener;
import me.aydeejay.arcanecore.listeners.BonusHealthListener;
import me.aydeejay.arcanecore.listeners.BreezeListener;
import me.aydeejay.arcanecore.listeners.CustomArmorListener;
import me.aydeejay.arcanecore.listeners.EmberListener;
import me.aydeejay.arcanecore.listeners.FrostListener;
import me.aydeejay.arcanecore.listeners.LevelItemListener;
import me.aydeejay.arcanecore.listeners.LevelZeroListener;
import me.aydeejay.arcanecore.listeners.LuckListener;
import me.aydeejay.arcanecore.listeners.PlayerListener;
import me.aydeejay.arcanecore.listeners.StrengthAxeListener;
import me.aydeejay.arcanecore.listeners.TraderListener;
import me.aydeejay.arcanecore.listeners.TrustListener;
import me.aydeejay.arcanecore.listeners.TrustVisualListener;
import me.aydeejay.arcanecore.listeners.VoidListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class ArcaneCore extends JavaPlugin {

    private TrustManager trustManager;
    private LevelManager levelManager;
    private CooldownManager cooldownManager;
    private ArcaneManager arcaneManager;
    private AbilityTriggerManager abilityTriggerManager;
    private PlayerArcaneManager playerArcaneManager;
    private ArcaneStatusBarManager statusBarManager;

    // Single registered listener instances. Ability triggers (slot / swap /
    // plugin-message paths) reuse these so shared state (dash tracking,
    // cooldowns) lives in one place instead of throwaway copies.
    private BreezeListener breezeListener;
    private FrostListener frostListener;
    private EmberListener emberListener;
    private LuckListener luckListener;
    private VoidListener voidListener;
    private BonusHealthListener bonusHealthListener;
    private TrustVisualListener trustVisualListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        reloadConfig();

        trustManager = new TrustManager(this);
        trustManager.load();
        levelManager = new LevelManager(this);
        levelManager.load();
        cooldownManager = new CooldownManager(this);
        arcaneManager = new ArcaneManager();
        abilityTriggerManager = new AbilityTriggerManager(this);
        playerArcaneManager = new PlayerArcaneManager(this);
        statusBarManager = new ArcaneStatusBarManager(this);

        // Ability listeners first so the plugin-message channel can reuse them.
        breezeListener = new BreezeListener(this);
        frostListener = new FrostListener(this);
        emberListener = new EmberListener(this);
        luckListener = new LuckListener(this);
        voidListener = new VoidListener(this);

        register(breezeListener);
        register(frostListener);
        register(emberListener);
        register(luckListener);
        register(voidListener);

        frostListener.startPassiveTask();
        luckListener.startPassiveTask();
        emberListener.startPassiveTask();
        voidListener.startPassiveTask();

        getServer().getMessenger().registerIncomingPluginChannel(
                this,
                "arcanecore:ability",
                new AbilityPluginMessageListener(this)
        );

        bonusHealthListener = new BonusHealthListener(this);
        register(bonusHealthListener);
        register(new StrengthAxeListener(this));
        register(new CustomArmorListener(this));
        register(new LevelZeroListener(this));
        register(new TrustListener(this));
        register(new PlayerListener(this));
        register(new LevelItemListener(this));
        register(new AbilitySwapListener(this));
        register(new AbilitySlotListener(this));
        register(new ArcaneGuiListener(this));
        register(new ArcaneKillEffectListener(this));
        register(new TraderListener(this));

        trustVisualListener = new TrustVisualListener(this);
        register(trustVisualListener);
        trustVisualListener.startVisualTask();

        registerRecipes();
        registerCommands();
        statusBarManager.start();

        getLogger().info("ArcaneCore enabled!");
    }

    @Override
    public void onDisable() {
        if (statusBarManager != null) {
            statusBarManager.shutdown();
        }
        if (voidListener != null) {
            voidListener.shutdown();
        }
        if (bonusHealthListener != null) {
            bonusHealthListener.shutdown();
        }
        if (trustVisualListener != null) {
            trustVisualListener.shutdown();
        }
        trustManager.save();
        levelManager.save();
        abilityTriggerManager.save();
        playerArcaneManager.save();
        getLogger().info("ArcaneCore disabled!");
    }

    private void register(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void registerCommands() {
        cmd("arcane", new ArcaneLevelCommand(this));
        cmd("givebreeze", new GiveBreezeCommand(this));
        cmd("giveluck", new GiveLuckCommand(this));
        cmd("givefrost", new GiveFrostCommand(this));
        cmd("giveember", new GiveEmberCommand(this));
        cmd("givevoid", new VoidArcaneCommand(this));
        cmd("trust", new TrustCommand(this));
        cmd("untrust", new UntrustCommand(this));
        cmd("trustlist", new TrustListCommand(this));
        cmd("levelitem", new LevelItemCommand());
        cmd("__arcane_internal_ability", new ArcaneAbilityCommand(this));
        cmd("givetrader", new TraderCommand());
        cmd("withdrawlevel", new AbilityCommand(this));
        cmd("ability", new AbilityCommand(this));
        cmd("setarcanelevel", new SetArcaneLevelCommand(this));
        cmd("givestrengthaxe", new GiveStrengthAxeCommand());
        cmd("givehearthelmet", new GiveHeartHelmetCommand());
        cmd("giveresistancechestplate", new GiveResistanceChestplateCommand());
        cmd("givehasteleggings", new GiveHasteLeggingsCommand());
        cmd("givespeedboots", new GiveSpeedBootsCommand());
    }

    private void cmd(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().warning("Command '" + name + "' is missing from plugin.yml; skipping registration.");
            return;
        }
        command.setExecutor(executor);
        if (executor instanceof TabCompleter tabCompleter) {
            command.setTabCompleter(tabCompleter);
        }
    }

    private void registerRecipes() {
        ShapedRecipe levelRecipe = new ShapedRecipe(key("arcane_level_item"), LevelItem.createItem());
        levelRecipe.shape("DSD", "NKN", "DSD");
        levelRecipe.setIngredient('D', Material.DIAMOND_BLOCK);
        levelRecipe.setIngredient('S', Material.NETHER_STAR);
        levelRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        levelRecipe.setIngredient('K', Material.OMINOUS_TRIAL_KEY);
        addRecipe(levelRecipe, "arcane_level_item");

        ShapelessRecipe voidRecipe = new ShapelessRecipe(key("void_arcane"), VoidArcane.createItem());
        voidRecipe.addIngredient(Material.DRAGON_EGG);
        addRecipe(voidRecipe, "void_arcane");

        ShapedRecipe traderRecipe = new ShapedRecipe(key("trader"), TraderItem.createItem());
        traderRecipe.shape("DGN", "EKE", "NGD");
        traderRecipe.setIngredient('D', Material.DIAMOND_BLOCK);
        traderRecipe.setIngredient('G', Material.GOLD_BLOCK);
        traderRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        traderRecipe.setIngredient('E', Material.EMERALD_BLOCK);
        traderRecipe.setIngredient('K', Material.OMINOUS_TRIAL_KEY);
        addRecipe(traderRecipe, "trader");

        ShapedRecipe anvil = new ShapedRecipe(key("easy_anvil"), new ItemStack(Material.ANVIL));
        anvil.shape("B", "I");
        anvil.setIngredient('B', Material.IRON_BLOCK);
        anvil.setIngredient('I', Material.IRON_INGOT);
        addRecipe(anvil, "easy_anvil");

        ShapedRecipe shulker = new ShapedRecipe(key("easy_shulker"), new ItemStack(Material.SHULKER_BOX));
        shulker.shape("ADA", "DBD", "ADA");
        shulker.setIngredient('A', Material.AMETHYST_SHARD);
        shulker.setIngredient('D', Material.DIAMOND);
        shulker.setIngredient('B', Material.BUNDLE);
        addRecipe(shulker, "easy_shulker");

        ShapedRecipe goldenApple = new ShapedRecipe(key("easy_golden_apple"), new ItemStack(Material.GOLDEN_APPLE));
        goldenApple.shape(" G ", "GAG", " G ");
        goldenApple.setIngredient('G', Material.GOLD_INGOT);
        goldenApple.setIngredient('A', Material.APPLE);
        addRecipe(goldenApple, "easy_golden_apple");

        ShapedRecipe packedIce = new ShapedRecipe(key("easy_packed_ice"), new ItemStack(Material.PACKED_ICE));
        packedIce.shape("I");
        packedIce.setIngredient('I', Material.ICE);
        addRecipe(packedIce, "easy_packed_ice");

        ShapedRecipe cobweb = new ShapedRecipe(key("tripwire_cobweb_recipe"), new ItemStack(Material.COBWEB, 1));
        cobweb.shape("T T", " T ", "T T");
        cobweb.setIngredient('T', new RecipeChoice.MaterialChoice(Material.TRIPWIRE_HOOK));
        addRecipe(cobweb, "tripwire_cobweb_recipe");
    }

    private NamespacedKey key(String name) {
        return new NamespacedKey(this, name);
    }

    // Remove any existing recipe with this key before adding, so /reload does
    // not spam "duplicate recipe key" warnings.
    private void addRecipe(Recipe recipe, String keyName) {
        Bukkit.removeRecipe(key(keyName));
        Bukkit.addRecipe(recipe);
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

    public BreezeListener getBreezeListener() {
        return breezeListener;
    }

    public FrostListener getFrostListener() {
        return frostListener;
    }

    public EmberListener getEmberListener() {
        return emberListener;
    }

    public LuckListener getLuckListener() {
        return luckListener;
    }

    public VoidListener getVoidListener() {
        return voidListener;
    }

    public BonusHealthListener getBonusHealthListener() {
        return bonusHealthListener;
    }

    public ArcaneStatusBarManager getStatusBarManager() {
        return statusBarManager;
    }
}
