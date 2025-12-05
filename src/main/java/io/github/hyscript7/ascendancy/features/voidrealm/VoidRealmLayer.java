package io.github.hyscript7.ascendancy.features.voidrealm;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public enum VoidRealmLayer {
    ABYSS(new NamespacedKey("ascendancy", "abyss")),
    OBLIVION(new NamespacedKey("ascendancy", "oblivion")),
    REFLECTION(new NamespacedKey("ascendancy", "self"));

    @Getter
    private final NamespacedKey key;
    private World world;

    VoidRealmLayer(NamespacedKey key) {
        this.key = key;
    }

    public World getWorld() {
        if (world == null) {
            world = Bukkit.getWorld(getKey());
            if (world == null) {
                throw new RuntimeException("World " + getKey() + " not found! Did the datapack load properly?");
            }
        }
        return world;
    }

    /**
     * Converts the specified void realm world into a layer enum reference.
     * @param world The world to turn into an enum reference
     * @return The enum or null if not a void realm world.
     */
    public static @Nullable VoidRealmLayer fromWorld(World world) {
        for (VoidRealmLayer layer : values()) {
            if (layer.getWorld().equals(world)) {
                return layer;
            }
        }
        return null;
    }

}
