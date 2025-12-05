package io.github.youngcoder45.ascendancy.rituals;

import io.github.hyscript7.ascendancy.features.rituals.*;
import io.github.hyscript7.ascendancy.features.rituals.requirements.ItemExclusivityFactory;
import io.github.hyscript7.ascendancy.features.rituals.requirements.ItemSacrifice;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

public class RitualOfTheWolf extends AbstractRitual {
    public RitualOfTheWolf() {
        super("ritual_wolf", "Ritual of the Wolf", RitualGrade.BASIC, buildStages());
    }

    private static final ItemExclusivityFactory exclusivity = new ItemExclusivityFactory(Set.of(Material.BONE, Material.ROTTEN_FLESH));

    private static List<RitualStage> buildStages() {
        return List.of(
                // Step 1
                exclusivity.on(
                        ItemSacrifice.builder().itemType(Material.BONE).minAmount(5).build()
                ),
                // Step 2
                exclusivity.on(
                        ItemSacrifice.builder().itemType(Material.ROTTEN_FLESH).minAmount(1).build()
                )
        );
    }

    @Override
    public ActiveRitualContext perform(RitualContext context) {
        // Destroy the fire otherwise the wolves instantly get set on fire
        context.getLocation().getWorld().getBlockAt(context.getLocation()).setType(Material.AIR);
        // Find out how many wolves we can spawn
        int bonesSacrificed = context.getSacrificedItems().stream().filter(itemStack -> itemStack.getType().equals(Material.BONE)).map(ItemStack::getAmount).findFirst().orElse(0);
        int wolfsToSpawn = bonesSacrificed / 5;
        int rottenFleshSacrificed = context.getSacrificedItems().stream().filter(itemStack -> itemStack.getType().equals(Material.ROTTEN_FLESH)).map(ItemStack::getAmount).findFirst().orElse(0);
        wolfsToSpawn = Math.min(rottenFleshSacrificed, wolfsToSpawn);
        // Spawn the wolves
        for (int i = 0; i < wolfsToSpawn; i++) {
            Wolf wolf = (Wolf) context.getLocation().getWorld().spawnEntity(context.getLocation().add(0, 1, 0), EntityType.WOLF);
            wolf.setOwner(context.getInvoker());
            wolf.setTamed(true);
            wolf.setCollarColor(org.bukkit.DyeColor.BLUE);
            // Epic particles <3
            context.getLocation().getWorld().spawnParticle(Particle.HEART, 2.5, 2.5, 2.5, 300);
        }
        return defaultInstantRitualContext(this, context);
    }
}
