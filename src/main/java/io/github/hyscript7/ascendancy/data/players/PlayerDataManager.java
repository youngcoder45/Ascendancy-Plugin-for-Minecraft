package io.github.hyscript7.ascendancy.data.players;

import io.github.hyscript7.ascendancy.AlreadyInitializedException;
import io.github.hyscript7.ascendancy.NotInitializedException;
import io.github.hyscript7.ascendancy.data.players.storage.PlayerDataStorage;
import io.github.hyscript7.ascendancy.data.players.names.TrueNameManager;
import io.github.hyscript7.ascendancy.features.magic.SpellCooldownManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages player data loading, saving, and caching
 */
public class PlayerDataManager {
    private static PlayerDataManager instance;

    private final Plugin plugin;
    private final PlayerDataStorage storage;
    private final Map<UUID, PlayerData> cache;

    // Auto-save task
    private BukkitRunnable autoSaveTask;
    private final int autoSaveInterval; // in ticks

    private PlayerDataManager(Plugin plugin, PlayerDataStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.cache = new ConcurrentHashMap<>();
        this.autoSaveInterval = 6000; // 5 minutes default
    }

    public static void initialize(Plugin plugin, PlayerDataStorage storage) {
        if (instance != null) {
            throw new AlreadyInitializedException("PlayerDataManager has already been initialized!");
        }
        instance = new PlayerDataManager(plugin, storage);
        instance.startAutoSave();
    }

    public static PlayerDataManager getInstance() {
        if (instance == null) {
            throw new NotInitializedException("PlayerDataManager hasn't been initialized yet!");
        }
        return instance;
    }

    /**
     * Asynchronously attempts to load player data from cache, or if it's missing, from disk.
     * If no player data exists, it saves & returns a new player data instance for the given UUID.
     * <p>
     * May fail without throwing. In such a case, default player data is returned and failure is logged.
     *
     * @param uuid The UUID of the player
     * @return Never-null player data of the given player
     */
    public CompletableFuture<PlayerData> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check cache first
                if (cache.containsKey(uuid)) {
                    return cache.get(uuid);
                }

                // Load from storage
                PlayerData data = storage.load(uuid);

                // If no data exists, create new
                if (data == null) {
                    data = createNewPlayerData(uuid);
                } else {
                    // Register existing true name
                    if (data.getTrueName() != null) {
                        TrueNameManager.getInstance()
                                .registerTrueName(uuid, data.getTrueName());
                    } else {
                        // Handle legacy data without true name
                        String trueName = TrueNameManager.getInstance()
                                .generateAndRegisterTrueName(uuid);
                        data.setTrueName(trueName);
                    }
                }

                // Cache it
                cache.put(uuid, data);
                return data;

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + uuid, e);
                // Return new data as fallback
                PlayerData fallback = createNewPlayerData(uuid);
                cache.put(uuid, fallback);
                return fallback;
            }
        });
    }

    /**
     * Synchronously attempts to load player data from cache, or if it's missing, from disk.
     * If no player data exists, it saves & returns a new player data instance for the given UUID.
     * <p>
     * May fail without throwing. In such a case, default player data is returned and failure is logged.
     * <p>
     * Implementation detail: Calls the asynchronous function but waits for it to complete before returning.
     *
     * @param uuid The UUID of the player
     * @return Never-null player data of the given player
     */
    public PlayerData loadPlayerDataSync(UUID uuid) {
        try {
            return loadPlayerData(uuid).get();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data synchronously for " + uuid, e);
            return createNewPlayerData(uuid);
        }
    }

    /**
     * Attempts to load the given player's data from the cache.
     *
     * @param uuid The UUID of the player
     * @return the player data or null if it's not cached
     */
    public PlayerData getPlayerData(UUID uuid) {
        return cache.get(uuid);
    }

    /**
     * Attempts to load the given player's data from the cache.
     *
     * @param player The player
     * @return the player data or null if it's not cached
     */
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    /**
     * Asynchronously attempts to save player data (if it has changed since last save.)
     * <p>
     * May fail without throwing. In such a case, the failure is logged.
     *
     * @param uuid The UUID of the player to save
     * @return A completable future which doesn't return anything
     */
    public CompletableFuture<Void> savePlayerData(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) {
            return CompletableFuture.completedFuture(null);
        }

        // IO is slow, only save if data has changed.
        if (!data.isDirty()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                storage.save(data);
                data.markClean();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + uuid, e);
            }
        });
    }

    /**
     * Synchronously attempts to save player data (if it has changed since last save.)
     * <p>
     * May fail without throwing. In such a case, the failure is logged.
     * <p>
     * Implementation detail: Calls the asynchronous function but waits for it to complete before returning.
     *
     * @param uuid The UUID of the player
     */
    public void savePlayerDataSync(UUID uuid) {
        try {
            savePlayerData(uuid).get();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data synchronously for " + uuid, e);
        }
    }

    /**
     * Asynchronously saves all player data (if it has changed since last save.)
     *
     * @return A completable future which doesn't return anything
     */
    public CompletableFuture<Void> saveAll() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (UUID uuid : cache.keySet()) {
            futures.add(savePlayerData(uuid));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Asynchronously Save player data and flushes it from the cache.
     *
     * @param uuid The UUID of the player
     * @return A completable future which doesn't return anything
     */
    public CompletableFuture<Void> unloadPlayerData(UUID uuid) {
        return savePlayerData(uuid).thenRun(() -> {
            TrueNameManager.getInstance().unregisterTrueName(uuid);
            SpellCooldownManager.getInstance().queuePlayerRemoval(uuid);
            cache.remove(uuid);
        });
    }

    /**
     * Instantiates a new instance of player data and returns it.
     *
     * @param uuid The UUID of the player.
     * @return The new Player Data with a True Name and First Join Time.
     */
    private PlayerData createNewPlayerData(UUID uuid) {
        PlayerData data = new PlayerData(uuid);

        // Generate true name using TrueNameManager
        String trueName = TrueNameManager.getInstance()
                .generateAndRegisterTrueName(uuid);
        data.setTrueName(trueName);

        data.setFirstSeenTimestamp();

        plugin.getLogger().info("Created new player data for " + uuid
                + " with true name: " + trueName);

        return data;
    }

    /**
     * Starts the auto save task
     */
    private void startAutoSave() {
        autoSaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                int savedCount = 0;
                for (UUID uuid : cache.keySet()) {
                    PlayerData data = cache.get(uuid);
                    if (data != null && data.isDirty()) {
                        savePlayerData(uuid);
                        savedCount++;
                    }
                }

                if (savedCount > 0) {
                    plugin.getLogger().info("Auto-saved " + savedCount + " player data files");
                }
            }
        };

        autoSaveTask.runTaskTimerAsynchronously(plugin, autoSaveInterval, autoSaveInterval);
    }

    /**
     * Shuts down the Player Data Manager.
     * <p>
     * Saves all player data <b>synchronously</b> before returning.
     */
    public void shutdown() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }

        plugin.getLogger().info("Saving all player data...");

        // Save all synchronously on shutdown
        for (UUID uuid : cache.keySet()) {
            savePlayerDataSync(uuid);
        }

        // Clear true name cache
        TrueNameManager.getInstance().clearCache();

        plugin.getLogger().info("Player data saved successfully");
    }

    /**
     * Returns an unmodifiable collection of all cached player data.
     * @return An unmodifiable collection containing all cached PlayerData instances
     */
    public Collection<PlayerData> getAllCachedData() {
        return Collections.unmodifiableCollection(cache.values());
    }

    /**
     * Returns an unmodifiable collection of all player data.
     * @return An unmodifiable collection containing all PlayerData from the storage.
     */
    public Collection<PlayerData> getAllData() {
        try {
            return Collections.unmodifiableCollection(storage.loadAll());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks whether a given player's data is cached or exists on the disk.
     *
     * @param uuid The UUID of the player
     * @return True if either cached or exists on disk, otherwise false
     */
    public boolean playerDataExists(UUID uuid) {
        return cache.containsKey(uuid) || storage.exists(uuid);
    }
}
