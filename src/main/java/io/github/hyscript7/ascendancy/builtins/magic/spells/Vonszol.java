package io.github.hyscript7.ascendancy.builtins.magic.spells;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.AbstractRegexIncantationSpell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicInteger;

public class Vonszol extends AbstractRegexIncantationSpell {
    private static final double range = 5.5;
    private static final double pullStrength = 2.5; // The (quite possibly and I hope so) maximum of the strength function

    public Vonszol() {
        super("vonszol", "Vonszol", ".*\\b([Vv]on[sz]{2}ol|[Aa]lmighty\\s[Pp]ull)\\b.*", SpellTier.UNCOMMON, 20, 15 * 1000);
    }

    @Override
    public boolean cast(SpellContext context) {
        Location centerLocation = context.getLocation();
        AtomicInteger counter = new AtomicInteger(0);
        context.getLocation().getNearbyLivingEntities(range).forEach(target -> {
            if (target.equals(context.getCaster())) return; // Don't move caster, as they are already at the center.
            calculateStrengthAndLaunchEntity(target, centerLocation);
            counter.incrementAndGet();
        });
        if (counter.get() == 0) {
            // If no entities were hit, don't trigger the cool down
            return false;
        }
        playParticles(centerLocation, range, 20, Color.PURPLE);
        playParticles(centerLocation, range / 3, 10, Color.fromRGB(0x80, 0x80 / 2, 0x80));
        return true;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return true;
    }

    private static final String[] incantations = new String[] {"Vonszol", "Almighty Pull"};

    @Override
    public String getIncantation() {
        return incantations[AscendancyPlugin.getInstance().getRandom().nextInt(incantations.length)];
    }

    private void calculateStrengthAndLaunchEntity(LivingEntity targetEntity, Location centerLocation) {
        Vector center = centerLocation.toVector();
        Vector target = targetEntity.getLocation().toVector();
        Vector dir = center.clone().subtract(target).normalize().multiply(pullStrength);

        try {
            dir.checkFinite();
        } catch (IllegalArgumentException e) {
            // Lmao what
            return;
        }

        double distance = dir.length();
        if (distance < 0.001) distance = 0.001; // Anti divide by zero (though we get positive infinities sometimes anyway)

        // As distance approaches 0, the pull strength minimizes at 0.1
        double scale = Math.max(0.5, distance / 10.0);
        Vector velocity = dir.normalize().multiply(pullStrength * scale);
        targetEntity.setVelocity(velocity);
    }

    /**
     * @param center The center point of the sphere
     * @param radius The radius of the sphere
     * @param detail The higher the detail, the more perfect the sphere and the laggier the server
     * @param color The color of the sphere
     */
    private void playParticles(Location center, double radius, int detail, Color color) {
        for (int i = 0; i <= detail; i++) {
            double theta = Math.PI * i / detail; // vertical angle

            for (int j = 0; j <= detail; j++) {
                double phi = 2 * Math.PI * j / detail; // horizontal angle

                double x = radius * Math.sin(theta) * Math.cos(phi);
                double y = radius * Math.cos(theta);
                double z = radius * Math.sin(theta) * Math.sin(phi);

                Location particleLoc = center.clone().add(x, y, z);

                center.getWorld().spawnParticle(
                        Particle.DUST,
                        particleLoc,
                        1,
                        new Particle.DustOptions(color, 1)
                );
            }
        }
    }
}
