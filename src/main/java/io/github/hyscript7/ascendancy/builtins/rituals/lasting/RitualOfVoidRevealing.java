package io.github.hyscript7.ascendancy.builtins.rituals.lasting;

import io.github.hyscript7.ascendancy.AscendancyConfig;
import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.rituals.*;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.vehicle.VehicleCollisionEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class RitualOfVoidRevealing extends AbstractRitual {
    private final int allowedHeight;
    private final int spawnProtectionDistance;
    private final int maximumRadius;

    public RitualOfVoidRevealing() {
        super("ritual orbital laser", "Void Revealing", RitualGrade.MASTER, buildStages());
        allowedHeight = AscendancyConfig.getInstance().getRituals().voidRevealing().spawnProtectionBypassHeightBelow();
        spawnProtectionDistance = AscendancyConfig.getInstance().getRituals().voidRevealing().spawnProtectionDistance();
        maximumRadius = AscendancyConfig.getInstance().getRituals().voidRevealing().maxRange();
    }

    private static List<RitualStage> buildStages() {
        // In order
        return List.of(
            new RitualStage() {
                @Override
                public boolean isComplete(RitualContext context) {
                    return context.getCatalyst().getType().equals(Material.WITHER_ROSE) || context.getSacrificedItems().stream().anyMatch(item -> item.getType().equals(Material.WITHER_ROSE));
                }

                @Override
                public String getHint(RitualContext context) {
                    return "Sacrifice a wither rose or restart the ritual using one as a catalyst.";
                }
            },
            new RitualStage() {
                @Override
                public boolean isComplete(RitualContext context) {
                    return context.getSacrificedEntities().getOrDefault(EntityType.PLAYER, 0) > 0;
                }

                @Override
                public String getHint(RitualContext context) {
                    return "Sacrifice a Player";
                }
            },
            new RitualStage() {
                @Override
                public boolean isComplete(RitualContext context) {
                    return context.getSacrificedItems().stream().anyMatch(item -> item.getType().equals(Material.END_CRYSTAL));
                }

                @Override
                public String getHint(RitualContext context) {
                    return "Sacrifice an End Crystal.";
                }
            }
        );
    }

    private World overworld = null;

    private Location getWorldSpawn() {
        if (overworld == null) {
            overworld = Bukkit.getWorld("world");
        }
        assert overworld != null;
        return overworld.getSpawnLocation();
    }

    private boolean isInProtectedDistance(Location location) {
        Location spawnLocation = getWorldSpawn();
        return new Vector(location.getX(), 0, location.getZ()).distance(new Vector(spawnLocation.getX(), 0, getWorldSpawn().getZ())) < spawnProtectionDistance;
    }

    @Override
    public boolean canPerform(RitualContext context) {
        // Disallow in the void realm
        if (VoidRealmLayer.fromWorld(context.getLocation().getWorld()) != null) return false;
        // Disallow too close to spawn
        if (isInProtectedDistance(context.getLocation()) && context.getLocation().getY() > allowedHeight) return false;
        return super.canPerform(context);
    }

    @Override
    public ActiveRitualContext perform(RitualContext context, Consumer<Location> onSelfCancel) {
        int crystalCount = context.getSacrificedItems().stream().filter(itemStack -> itemStack.getType().equals(Material.END_CRYSTAL)).map(ItemStack::getAmount).findFirst().orElse(1);
        int radius = Math.min(Math.max(2, crystalCount / 2), maximumRadius);
        AscendancyPlugin.getInstance().getLogger().info("Orbital Laser Ritual at " + context.getLocation().getBlockX() + " " + context.getLocation().getBlockY() + " " + context.getLocation().getBlockZ() + " initializing with radius " + radius + ".");
        return new ActiveRitualContext(
            context.getInvoker(),
            context.getLocation(),
            this,
            Optional.of(
                Bukkit.getScheduler().runTaskTimer(
                        AscendancyPlugin.getInstance(),
                        new Active(context.getLocation(), radius) {
                            @Override
                            void completed() {
                                onSelfCancel.accept(context.getLocation());
                            }
                        }, // Start below the ritual blocks, otherwise we'll break the ritual instantly as it begins.
                        20L, 4L
                )
            )
        );
    }

    @Override
    public void onCancel(RitualContext context) {
        context.getLocation().getNearbyPlayers(32).forEach(player -> {
            AscendancyMessagingAPI.getInstance().send(player, AscendancyMessagingAPI.MessageType.ERROR, "The ritual has been interrupted!");
        });
        context.getLocation().getWorld().playSound(context.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.0f, 0.5f);
        context.getLocation().createExplosion(5.0f);
    }

    // Hello, if you are reading this because there is a bug, good luck. 🫠
    private static abstract class Active implements Runnable {
        private static final double safeRadius = 2.0d;
        private final Location origin;
        private final int radius;
        private int currentRadius = 0; // animation tracker

        protected Active(Location origin, int radius) {
            this.origin = origin;
            this.radius = radius;
        }

        @Override
        public void run() {
            World world = origin.getWorld();
            if (world == null) return;

            int baseX = origin.getBlockX();
            int baseZ = origin.getBlockZ();
            int startY = origin.getBlockY();

            // Animation complete
            if (currentRadius > radius) {
                origin.getWorld().createExplosion(origin, 2.5f);
                AscendancyPlugin.getInstance().getLogger().info("Orbital Laser Ritual at " + origin.getBlockX() + " " + origin.getBlockY() + " " + origin.getBlockZ() + " completing!");
                completed();
                return;
            }

            int r = currentRadius;
            double r2 = r * r;
            double prevR2 = (r - 1) * (r - 1);

            for (int x = baseX - r; x <= baseX + r; x++) {
                for (int z = baseZ - r; z <= baseZ + r; z++) {

                    int dx = x - baseX;
                    int dz = z - baseZ;
                    double dist2 = dx * dx + dz * dz;

                    if (r == 0) {
                        if (dist2 != 0) continue;
                    } else {
                        if (dist2 > r2) continue;
                        if (dist2 < prevR2) continue;
                    }

                    // 💥 PARTICLE FX ON SURFACE
                    // Find the topmost solid block in this column
                    int topY = startY;
                    while (topY >= -64) {
                        Block check = world.getBlockAt(x, topY, z);
                        if (!check.getType().isAir()) break;
                        topY--;
                    }

                    // If column was entirely air (??), fallback to startY
                    if (topY < -64) topY = startY;

                    // Particle spawn position on top of the real surface
                    Location surface = new Location(world, x + 0.5, topY + 1.1, z + 0.5);

                    // Portal poof
                    world.spawnParticle(Particle.PORTAL, surface, 6, 0.2, 0.2, 0.2, 0.05);

                    // Ash fallout
                    world.spawnParticle(Particle.ASH, surface, 3, 0.1, 0.1, 0.1, 0.01);

                    // Soul fire flickers
                    if (r % 2 == 0) {
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, surface, 2, 0.1, 0.1, 0.1, 0.01);
                    }

                    // Shockwave puff
                    if (r % 3 == 0) {
                        world.spawnParticle(Particle.CLOUD, surface.clone().add(0, 0.3, 0), 2, 0.1, 0.1, 0.1, 0.0);
                    }

                    // Block crumble
                    Block block = world.getBlockAt(x, topY, z);
                    world.spawnParticle(Particle.BLOCK_CRUMBLE, surface, 8, 0.2, 0.2, 0.2, block.getBlockData());


                    // 🕳️ Now delete the planet below this point
                    for (int y = startY; y >= -64; y--) {
                        Block block1 = world.getBlockAt(x, y, z);
                        if (block1.getLocation().distance(origin) <= safeRadius) continue;
                        block1.setType(Material.AIR, false);
                    }
                }
            }

            // Can you tell ChatGPT wrote this?
            // Yes. - Spelis

            currentRadius++;
        }

        abstract void completed();
    }
}
