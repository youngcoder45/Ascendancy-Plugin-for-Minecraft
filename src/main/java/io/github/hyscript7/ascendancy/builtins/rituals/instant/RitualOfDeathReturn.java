package io.github.hyscript7.ascendancy.builtins.rituals.instant;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.features.rituals.*;
import io.github.hyscript7.ascendancy.features.rituals.requirements.ItemExclusivityFactory;
import io.github.hyscript7.ascendancy.features.rituals.requirements.ItemSacrifice;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

public class RitualOfDeathReturn extends AbstractRitual {

    // players in this range of the ritual will get teleported
    private static final double TARGET_RADIUS = 2.5d;

    public RitualOfDeathReturn() {
        super("ritual teleport grave", "Return to Death", RitualGrade.INTERMEDIATE, buildStages());
    }

    private static List<RitualStage> buildStages() {
        return List.of(
                new ItemExclusivityFactory(Set.of(Material.RECOVERY_COMPASS)).on(ItemSacrifice.builder().itemType(Material.RECOVERY_COMPASS).maxAmount(1).minAmount(1).build())
        );
    }

    @Override
    public boolean canPerform(RitualContext context) {
        if (context.getInvoker().getLastDeathLocation() == null) {
            AscendancyMessagingAPI.getInstance().send(context.getInvoker(), AscendancyMessagingAPI.MessageType.ERROR, "You do not have a death location!\nBreak the ritual to get your recovery compass back.");
            return false;
        }
        return super.canPerform(context);
    }

    @Override
    public void onCancel(RitualContext context) {
        context.getSacrificedItems().stream().filter(item -> item.getType().equals(Material.RECOVERY_COMPASS)).toList().forEach(item -> {
            context.getInvoker().give(item);
            context.getSacrificedItems().remove(item);
        });
    }

    @Override
    public ActiveRitualContext perform(RitualContext context) {
        final Location deathLocation = context.getInvoker().getLastDeathLocation();
        if (deathLocation == null) {
            throw new IllegalStateException("RitualOfDeathReturn ran even though it cannot be performed (death location is null)");
        }
        context.getSacrificedItems().stream().filter(item -> item.getType().equals(Material.RECOVERY_COMPASS)).toList().forEach(item -> {
            context.getInvoker().give(item);
            context.getSacrificedItems().remove(item);
        });
        final int particleCount = (int) Math.round(TARGET_RADIUS*100);
        context.getLocation().getNearbyPlayers(TARGET_RADIUS).forEach(player -> {
            player.teleport(deathLocation);
            player.spawnParticle(Particle.PORTAL, deathLocation, particleCount, TARGET_RADIUS, TARGET_RADIUS, TARGET_RADIUS);
            player.playSound(player, Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
            AscendancyMessagingAPI.getInstance().send(player, AscendancyMessagingAPI.MessageType.SUCCESS, "You teleported to " + (context.getInvoker().equals(player) ? "your" : context.getInvoker().getName() + "'s") + " death location.");
        });
        deathLocation.getWorld().spawnParticle(Particle.REVERSE_PORTAL, deathLocation, particleCount, TARGET_RADIUS, TARGET_RADIUS, TARGET_RADIUS);
        context.getLocation().getWorld().playSound(context.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.0f);
        return defaultInstantRitualContext(this, context);
    }
}
