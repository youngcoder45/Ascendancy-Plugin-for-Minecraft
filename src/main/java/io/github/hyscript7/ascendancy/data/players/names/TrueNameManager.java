package io.github.hyscript7.ascendancy.data.players.names;

import io.github.hyscript7.ascendancy.AlreadyInitializedException;
import io.github.hyscript7.ascendancy.NotInitializedException;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages true name generation and lookups.
 * Thread-safe for concurrent access.
 */
public class TrueNameManager {
    private static TrueNameManager instance;

    private final Plugin plugin;
    private final TrueNameGenerator generator;

    // The single source of truth for name->uuid mapping
    private final Map<String, UUID> trueNameToUuid;
    // Reverse lookup cache
    private final Map<UUID, String> uuidToTrueName;

    private TrueNameManager(Plugin plugin) {
        this.plugin = plugin;
        this.generator = new TrueNameGenerator();
        this.trueNameToUuid = new ConcurrentHashMap<>();
        this.uuidToTrueName = new ConcurrentHashMap<>();
    }

    public static void initialize(Plugin plugin) {
        if (instance != null) {
            throw new AlreadyInitializedException("TrueNameManager already initialized!");
        }
        instance = new TrueNameManager(plugin);
    }

    public static TrueNameManager getInstance() {
        if (instance == null) {
            throw new NotInitializedException("TrueNameManager not initialized yet!");
        }
        return instance;
    }

    /**
     * Generates and registers a new unique true name.
     * Thread-safe.
     *
     * @param uuid The player's UUID to associate with this name
     * @return A unique true name
     */
    public String generateAndRegisterTrueName(UUID uuid) {
        // Generate until we get a unique one
        String name = generator.generateUniqueName(
                trueNameToUuid.keySet(),
                generator::generateName
        );

        // Register it atomically
        registerTrueName(uuid, name);

        plugin.getLogger().info("Generated true name '" + name + "' for " + uuid);
        return name;
    }

    /**
     * Registers an existing true name (e.g., when loading from storage).
     *
     * @param uuid Player UUID
     * @param trueName The true name to register
     * @throws IllegalStateException if the name is already taken by another player
     */
    public void registerTrueName(UUID uuid, String trueName) {
        // Check if name is already taken by someone else
        UUID existingOwner = trueNameToUuid.get(trueName);
        if (existingOwner != null && !existingOwner.equals(uuid)) {
            throw new IllegalStateException(
                    "True name '" + trueName + "' already taken by " + existingOwner
            );
        }

        // Remove old name if player had one
        String oldName = uuidToTrueName.get(uuid);
        if (oldName != null && !oldName.equals(trueName)) {
            trueNameToUuid.remove(oldName);
        }

        // Register new mapping
        trueNameToUuid.put(trueName, uuid);
        uuidToTrueName.put(uuid, trueName);
    }

    /**
     * Unregisters a player's true name (e.g., on logout or data deletion).
     *
     * @param uuid Player UUID
     */
    public void unregisterTrueName(UUID uuid) {
        String name = uuidToTrueName.remove(uuid);
        if (name != null) {
            trueNameToUuid.remove(name);
        }
    }

    /**
     * Checks if a true name is currently registered.
     *
     * @param trueName The name to check
     * @return true if taken, false otherwise
     */
    public boolean isTrueNameTaken(String trueName) {
        return trueNameToUuid.containsKey(trueName);
    }

    /**
     * Finds the owner of a true name.
     * O(1) lookup.
     *
     * @param trueName The true name to look up
     * @return The owner's UUID, or null if not found
     */
    @Nullable
    public UUID findTrueNameOwner(String trueName) {
        return trueNameToUuid.get(trueName);
    }

    /**
     * Gets a player's true name.
     * O(1) lookup.
     *
     * @param uuid Player UUID
     * @return Their true name, or null if not registered
     */
    @Nullable
    public String getTrueName(UUID uuid) {
        return uuidToTrueName.get(uuid);
    }

    /**
     * Returns all currently registered true names.
     *
     * @return Unmodifiable set of all true names
     */
    public Set<String> getAllRegisteredTrueNames() {
        return Collections.unmodifiableSet(trueNameToUuid.keySet());
    }

    /**
     * Loads all true names from player data into the cache.
     * Should be called during plugin startup after storage is ready.
     *
     * @param allPlayerData All player data to index
     */
    public void indexAllTrueNames(Collection<PlayerData> allPlayerData) {
        plugin.getLogger().info("Indexing true names from " + allPlayerData.size() + " players...");

        int indexed = 0;
        for (PlayerData data : allPlayerData) {
            if (data.getTrueName() != null) {
                try {
                    registerTrueName(data.getUuid(), data.getTrueName());
                    indexed++;
                } catch (IllegalStateException e) {
                    plugin.getLogger().log(Level.WARNING,
                            "Duplicate true name detected: " + data.getTrueName(), e);
                }
            }
        }

        plugin.getLogger().info("Indexed " + indexed + " true names");
    }

    /**
     * Clears all cached true names.
     * Use with caution - typically only for testing or full reload.
     */
    public void clearCache() {
        trueNameToUuid.clear();
        uuidToTrueName.clear();
    }
}