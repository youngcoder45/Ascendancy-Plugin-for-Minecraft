package io.github.hyscript7.ascendancy.features.bossprog;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Set;

public enum ItemTier {
    WOODEN(Set.of(Material.WOODEN_SWORD, Material.WOODEN_AXE, Material.WOODEN_PICKAXE, Material.WOODEN_SHOVEL, Material.WOODEN_HOE, Material.LEATHER_HORSE_ARMOR), Set.of(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS)),
    STONE(Set.of(Material.STONE_SWORD, Material.STONE_AXE, Material.STONE_PICKAXE, Material.STONE_SHOVEL, Material.STONE_HOE), Set.of(Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS)),
    IRON(Set.of(Material.IRON_SWORD, Material.IRON_AXE, Material.IRON_PICKAXE, Material.IRON_SHOVEL, Material.IRON_HOE, Material.IRON_HORSE_ARMOR), Set.of(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS)),
    GOLDEN(Set.of(Material.GOLDEN_SWORD, Material.GOLDEN_AXE, Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_HOE, Material.GOLDEN_HORSE_ARMOR), Set.of(Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS)),
    DIAMOND(Set.of(Material.DIAMOND_SWORD, Material.DIAMOND_AXE, Material.DIAMOND_PICKAXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE, Material.DIAMOND_HORSE_ARMOR), Set.of(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS)),
    NETHERITE(Set.of(Material.NETHERITE_SWORD, Material.NETHERITE_AXE, Material.NETHERITE_PICKAXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE), Set.of(Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS));

    private final Set<Material> items;
    private final Set<Material> armors;

    ItemTier(Set<Material> items,  Set<Material> armors) {
        this.items = items;
        this.armors = armors;
    }

    private static final HashMap<Material, ItemTier> lookupCache = getLookupCache();

    private static HashMap<Material, ItemTier> getLookupCache() {
        HashMap<Material, ItemTier> lookupCache = new HashMap<>();
        for (ItemTier itemTier : ItemTier.values()) {
            for (Material material : itemTier.items) {
                lookupCache.put(material, itemTier);
            }
            for (Material material : itemTier.armors) {
                lookupCache.put(material, itemTier);
            }
        }
        return lookupCache;
    }

    public static @Nullable ItemTier fromMaterial(Material material) {
        return lookupCache.get(material);
    }

    public boolean isArmor(Material material) {
        return armors.contains(material);
    }

    public boolean isItem(Material material) {
        return items.contains(material);
    }
}
