package io.github.hyscript7.ascendancy.features.rituals;

import lombok.Getter;
import org.bukkit.Material;

import java.util.Set;

public enum RitualGrade {
    BASIC(Material.AMETHYST_SHARD, Material.WHITE_BANNER, Material.ZOMBIE_HEAD), // White banner because of pillager banners and faction creation
    INTERMEDIATE(Material.BLAZE_POWDER, Material.BREEZE_ROD, Material.PRISMARINE_CRYSTALS, Material.PRISMARINE_SHARD, Material.ENDER_PEARL, Material.SKELETON_SKULL, Material.CREEPER_HEAD),
    ADVANCED(Material.ENDER_EYE, Material.END_CRYSTAL, Material.ANCIENT_DEBRIS, Material.PIGLIN_HEAD, Material.WITHER_SKELETON_SKULL),
    MASTER(Material.ECHO_SHARD, Material.NETHERITE_INGOT, Material.WITHER_ROSE, Material.DRAGON_BREATH, Material.PLAYER_HEAD),
    MYTHIC(Material.NETHER_STAR, Material.DRAGON_HEAD);

    @Getter
    private final Set<Material> catalysts;

    RitualGrade(Material ...catalystType) {
        this.catalysts = Set.of(catalystType);
    }

    /**
     * Returns the ritual grade corresponding to the given catalyst material.
     * @param catalyst The material
     * @return A ritual grade if the material is a catalyst, otherwise null.
     */
    public static RitualGrade fromCatalyst(Material catalyst) {
        for (RitualGrade grade : RitualGrade.values()) {
            if (grade.catalysts.contains(catalyst)) {
                return grade;
            }
        }
        return null;
    }

    public boolean greaterThan(RitualGrade other) {
        return this.compareTo(other) > 0;
    }

    public boolean lesserThan(RitualGrade other) {
        return this.compareTo(other) < 0;
    }
}
