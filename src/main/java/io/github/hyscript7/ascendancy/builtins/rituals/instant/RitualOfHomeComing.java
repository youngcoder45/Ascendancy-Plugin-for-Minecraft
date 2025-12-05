package io.github.hyscript7.ascendancy.builtins.rituals.instant;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.features.rituals.*;
import io.github.hyscript7.ascendancy.features.rituals.requirements.ItemExclusivityFactory;
import io.github.hyscript7.ascendancy.features.rituals.requirements.ItemSacrifice;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RitualOfHomeComing extends AbstractRitual {

    // players in this range of the ritual will get teleported
    private static final double TARGET_RADIUS = 2.5d;

    public RitualOfHomeComing() {
        super("ritual teleport home", "Home Coming", RitualGrade.INTERMEDIATE, buildStages());
    }

    private static List<RitualStage> buildStages() {
        return List.of(
            new ItemExclusivityFactory(Set.of(Material.COMPASS)).on(ItemSacrifice.builder().itemType(Material.COMPASS).maxAmount(1).minAmount(1).build())
        );
    }

    @Override
    public ActiveRitualContext perform(RitualContext context) {
        final Location location = Optional.ofNullable(context.getInvoker().getRespawnLocation()).orElse(Bukkit.getWorld("world").getSpawnLocation());
        final int particleCount = (int) Math.round(TARGET_RADIUS*100);
        context.getLocation().getNearbyPlayers(TARGET_RADIUS).forEach(player -> {
            player.teleport(location);
            player.spawnParticle(Particle.PORTAL, location, particleCount, TARGET_RADIUS, TARGET_RADIUS, TARGET_RADIUS);
            player.playSound(player, Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
            AscendancyMessagingAPI.getInstance().send(player, AscendancyMessagingAPI.MessageType.SUCCESS, "You teleported to " + (context.getInvoker().equals(player) ? "your" : context.getInvoker().getName() + "'s") + " home.");
        });
        context.getLocation().getWorld().spawnParticle(Particle.REVERSE_PORTAL, context.getLocation(), particleCount, TARGET_RADIUS, TARGET_RADIUS, TARGET_RADIUS);
        context.getLocation().getWorld().playSound(context.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.0f);
        return defaultInstantRitualContext(this, context);
    }
}
