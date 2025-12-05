package io.github.hyscript7.ascendancy;

import io.github.hyscript7.ascendancy.data.players.PlayerDataListener;
import io.github.hyscript7.ascendancy.data.players.storage.JsonPlayerDataStorage;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.data.players.storage.PlayerDataStorage;
import io.github.hyscript7.ascendancy.data.players.names.TrueNameManager;
import io.github.hyscript7.ascendancy.features.bossprog.listeners.BossKillListener;
import io.github.hyscript7.ascendancy.features.bossprog.listeners.EquipmentRestrictionEnforcer;
import io.github.hyscript7.ascendancy.features.innate.names.listeners.InnateCommandListener;
import io.github.hyscript7.ascendancy.features.magic.listeners.BookCastListener;
import io.github.hyscript7.ascendancy.features.magic.listeners.LootGenerator;
import io.github.hyscript7.ascendancy.features.magic.listeners.SpellIncantationListener;
import io.github.hyscript7.ascendancy.features.rituals.listeners.RitualCraftingListener;
import io.github.hyscript7.ascendancy.features.secretchat.SecretChatListener;
import io.github.hyscript7.ascendancy.features.threefold.listeners.ThreefoldListener;
import io.github.hyscript7.ascendancy.features.voidrealm.listeners.*;
import io.github.hyscript7.ascendancy.registries.RegistryManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class AscendancyPlugin extends JavaPlugin {
    private static AscendancyPlugin instance;
    @Getter
    private final Random random = new Random();

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Plugin initializing");

        saveDefaultConfig();

        PlayerDataStorage playerStorage = new JsonPlayerDataStorage(this);

        PlayerDataManager.initialize(this, playerStorage);
        TrueNameManager.initialize(this);
        getLogger().info("Player data system initialized");

        TrueNameManager.getInstance().indexAllTrueNames(
                PlayerDataManager.getInstance().getAllData()
        );
        getLogger().info("True Names indexed successfully");

        RegistryManager.getInstance().initialize();
        getLogger().info("Registries initialized successfully");

        registerListeners();
        getLogger().info("Listeners registered successfully");

        getLogger().info("Plugin enabled successfully!");

        AscendancyMessagingAPI.getInstance().broadcastBoxed(AscendancyMessagingAPI.MessageType.INFO, "Ascendancy", null, "Hello World");
    }

    @Override
    public void onDisable() {
        try {
            PlayerDataManager.getInstance().shutdown();
        } catch (NotInitializedException e) {
            getLogger().warning("PlayerDataManager hasn't been initialized yet!");
        }
        getLogger().info("Plugin disabled successfully!");
    }

    private void registerListeners() {
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerDataListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new InnateCommandListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new VoidRealmRespawnListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new DeadPlayerRestrictionListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new VoidRealmLayerChanger(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new VoidRealmStateListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new VoidRealmEffects(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new RitualCraftingListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new BookCastListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new SpellIncantationListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new LootGenerator(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new BossKillListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new EquipmentRestrictionEnforcer(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new SecretChatListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new ThreefoldListener(), this);
    }

    public static AscendancyPlugin getInstance() {
        if (instance == null) {
            throw new NotInitializedException("The plugin hasn't been initialized yet!");
        }
        return instance;
    }
}