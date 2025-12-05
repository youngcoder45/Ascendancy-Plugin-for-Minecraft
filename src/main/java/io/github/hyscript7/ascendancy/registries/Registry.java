package io.github.hyscript7.ascendancy.registries;

import java.util.*;
import java.util.function.Supplier;

/**
 * Generic registry for managing plugin content (spells, rituals, relics, etc.)
 * @param <T> The type being registered
 */
public class Registry<T extends Identifiable> {
    private final Map<String, T> entries = new HashMap<>();
    private final Map<String, Supplier<T>> factories = new HashMap<>();
    private final String registryName;
    private boolean locked = false;

    public Registry(String registryName) {
        this.registryName = registryName;
    }

    /**
     * Register an instance
     * @param entry The instance
     */
    public void register(T entry) {
        if (locked) {
            throw new IllegalStateException("Registry " + registryName + " is locked!");
        }

        String id = entry.getId();
        if (entries.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate registration: " + id);
        }

        entries.put(id, entry);
    }

    /**
     * Register an instance factory (for lazy initialization)
     * @param id The unique identifier
     * @param factory A supplier for the instance
     */
    public void registerFactory(String id, Supplier<T> factory) {
        if (locked) {
            throw new IllegalStateException("Registry " + registryName + " is locked!");
        }

        if (factories.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate factory registration: " + id);
        }

        factories.put(id, factory);
    }

    /**
     * Query for an instance by its unique identifier
     * @return The instance
     */
    public Optional<T> get(String id) {
        T entry = entries.get(id);
        if (entry != null) {
            return Optional.of(entry);
        }

        // Try to create from factory
        Supplier<T> factory = factories.get(id);
        if (factory != null) {
            T created = factory.get();
            entries.put(id, created);
            return Optional.of(created);
        }

        return Optional.empty();
    }

    /**
     * Get a set of all registered IDs
     * @return A set of unique identifiers
     */
    public Set<String> getIds() {
        Set<String> ids = new HashSet<>(entries.keySet());
        ids.addAll(factories.keySet());
        return Collections.unmodifiableSet(ids);
    }

    /**
     * Get a set of all registered instances, registering lazy instances if needed.
     */
    public Collection<T> getAll() {
        // Instantiate all factories
        for (String id : factories.keySet()) {
            if (!entries.containsKey(id)) {
                get(id); // Will create and cache
            }
        }
        return Collections.unmodifiableCollection(entries.values());
    }

    /**
     * Locks the registry to prevent further modifications
     */
    public void lock() {
        this.locked = true;
    }

    /**
     * Checks if an ID is already registered
     */
    public boolean contains(String id) {
        return entries.containsKey(id) || factories.containsKey(id);
    }

    /**
     * Gets the name of the registry
     * @return The registry name
     */
    public String getName() {
        return registryName;
    }
}
