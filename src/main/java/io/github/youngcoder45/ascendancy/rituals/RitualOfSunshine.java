package io.github.youngcoder45.ascendancy.rituals;

import io.github.hyscript7.ascendancy.features.rituals.*;
import io.github.hyscript7.ascendancy.features.rituals.requirements.ItemExclusivityFactory;
import io.github.hyscript7.ascendancy.features.rituals.requirements.ItemSacrifice;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.List;
import java.util.Set;

public class RitualOfSunshine extends AbstractRitual {
    public RitualOfSunshine() {
        super("ritual_sunshine", "Ritual of Sunshine", RitualGrade.INTERMEDIATE, buildStages());
    }

    private static List<RitualStage> buildStages() {
        return List.of(
                new ItemExclusivityFactory(Set.of(Material.SUNFLOWER, Material.GOLD_INGOT)).on(
                        ItemSacrifice.builder().itemType(Material.SUNFLOWER).minAmount(1).maxAmount(1).fallback(
                                ItemSacrifice.builder().itemType(Material.GOLD_INGOT).minAmount(1).maxAmount(1).build()
                        ).build()
        ));
    }

    @Override
    public ActiveRitualContext perform(RitualContext context) {
        World world = context.getLocation().getWorld();
        if (world != null) {
            world.setTime(1000); // Set to morning
            world.setStorm(false);
            world.setThundering(false);
        }
        return defaultInstantRitualContext(this, context);
    }
}
