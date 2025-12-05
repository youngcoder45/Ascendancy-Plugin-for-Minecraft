package io.github.hyscript7.ascendancy.features.voidrealm.listeners;

import io.github.hyscript7.ascendancy.AscendancyConfig;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.features.voidrealm.LayerChanger;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles changing layers & entering the void realm
 */
public class VoidRealmLayerChanger implements Listener {
    private final Map<UUID, BukkitTask> breakerTasks = new ConcurrentHashMap<>();
    private final LayerChanger layerChanger = new LayerChanger();

    private final double escapeThresholdPercentage;
    private final int reflectionWorldSizeRadius;

    public VoidRealmLayerChanger() {
        escapeThresholdPercentage = 1.0d + AscendancyConfig.getInstance().getVoidRealm().escapeHeightOvershootPercentage() / 100.0d;
        reflectionWorldSizeRadius = AscendancyConfig.getInstance().getVoidRealm().reflectionWorldSizeRadius();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVoidDamageTeleportEntityToTheVoidRealm(EntityDamageEvent event) {
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            VoidRealmLayer layer = VoidRealmLayer.fromWorld(event.getEntity().getWorld());
            if (layer != null) {
                switch (layer) {
                    case ABYSS -> layerChanger.changeLayer(event.getEntity(), VoidRealmLayer.OBLIVION);
                    // All roads lead to Reflection of Self
                    case OBLIVION, REFLECTION -> layerChanger.changeLayer(event.getEntity(), VoidRealmLayer.REFLECTION);
                    default -> {}
                }
                event.setCancelled(true);
            } else {
                switch (event.getEntity()) {
                    case Player playerEntity -> {
                        // Teleport players who jump into the void out of combat into the Void Realm
                        // TODO: If in combat, don't teleport.
                        layerChanger.changeLayer(playerEntity, VoidRealmLayer.ABYSS);
                        event.setCancelled(true);
                    }
                    case LivingEntity entity -> {
                        // Save mobs with more than 50% HP
                        double maxHealth = Optional.ofNullable(entity.getAttribute(Attribute.MAX_HEALTH)).map(AttributeInstance::getValue).orElse(20.0d);
                        if (entity.getHealth() > (maxHealth / 2)) {
                            layerChanger.changeLayer(entity, VoidRealmLayer.ABYSS);
                            event.setCancelled(true);
                        }
                    }
                    case Item itemEntity -> {
                        // Always save items
                        layerChanger.changeLayer(itemEntity, VoidRealmLayer.ABYSS);
                        event.setCancelled(true);
                    }
                    default -> {}
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onKineticDamageWhileEscapingTheVoid(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!breakerTasks.containsKey(player.getUniqueId())) return;
        if (breakerTasks.get(player.getUniqueId()).isCancelled()) return;

        EntityDamageEvent.DamageCause cause = event.getCause();

        // Block all kinetic / movement-based damage while bedrock breaker is active
        switch (cause) {
            case FALL,
                 FLY_INTO_WALL,
                 CRAMMING,
                 SUFFOCATION -> event.setCancelled(true);
            default -> {}
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMoveWhileInTheVoid(PlayerMoveEvent event) {
        VoidRealmLayer layer = VoidRealmLayer.fromWorld(event.getPlayer().getWorld());
        if (layer != null) {
            if (layer == VoidRealmLayer.REFLECTION) {
                handleReflectionWraparound(event.getPlayer());
            }

            if (event.getPlayer().getLocation().getY() < layer.getWorld().getLogicalHeight() * escapeThresholdPercentage) {
                return;
            }
            switch (layer) {
                case ABYSS -> {
                    layerChanger.changeToOverworld(event.getPlayer());
                    startBedrockBreaker(event.getPlayer());
                }
                case OBLIVION -> layerChanger.changeLayer(event.getPlayer(), VoidRealmLayer.ABYSS, true);
                case REFLECTION -> layerChanger.changeLayer(event.getPlayer(), VoidRealmLayer.REFLECTION, true);
                default -> {}
            }
        }
    }

    private void startBedrockBreaker(Player player) {
        // Cancel any existing task for this player
        BukkitTask old = breakerTasks.remove(player.getUniqueId());
        if (old != null) {
            old.cancel();
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(
                AscendancyPlugin.getInstance(),
                new VoidRealmLayerChanger.BedrockBreakerTask(player),
                10L,
                5L
        );

        breakerTasks.put(player.getUniqueId(), task);
    }

    private final class BedrockBreakerTask implements Runnable {

        private static final Set<Material> breakableBlocks =
                Set.of(Material.BEDROCK, Material.DEEPSLATE, Material.TUFF, Material.GRAVEL);

        private final Player player;
        private final double cancelY;
        private final double radius;

        private BedrockBreakerTask(Player player) {
            this.player = player;

            World world = Bukkit.getWorld("world");
            assert world != null; // If world "world" is null, the server is either retarded or an API changed
            this.cancelY = world.getMinHeight()
                    + AscendancyConfig.getInstance().getVoidRealm().defaultWorldHeightVoidTerminatorOffset();
            this.radius = AscendancyConfig.getInstance().getVoidRealm().bedrockBreakerRadius();
        }

        @Override
        public void run() {
            BukkitTask task = breakerTasks.get(player.getUniqueId());
            if (task == null) return; // already cancelled elsewhere

            // If the player vanished, logged out, or teleported to another world, stop the task
            if (!player.isOnline() ||
                    VoidRealmLayer.fromWorld(player.getWorld()) != null ||
                    !player.getWorld().getName().equals("world")) {

                task.cancel();
                breakerTasks.remove(player.getUniqueId());
                return;
            }

            // Player rose above termination height -> stop (listener handles the rest)
            if (player.getLocation().getY() > cancelY) {
                task.cancel();
                breakerTasks.remove(player.getUniqueId());
                return;
            }

            removeBedrockSphere(player, radius);
        }

        private void removeBedrockSphere(Player player, double radius) {
            Location center = player.getLocation();
            World world = center.getWorld();

            int r = (int) Math.ceil(radius);

            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {

                        if (x * x + y * y + z * z > radius * radius) continue;

                        Location loc = center.clone().add(x, y, z);
                        if (loc.getY() < world.getMinHeight() || loc.getY() > world.getMaxHeight()) continue;

                        Block block = world.getBlockAt(loc);
                        if (breakableBlocks.contains(block.getType())) {
                            block.setType(Material.AIR, false);
                        }
                    }
                }
            }
        }
    }

    private void handleReflectionWraparound(Player player) {
        Location loc = player.getLocation();
        double x = loc.getX();
        double z = loc.getZ();

        // Calculate distance from [0, y, 0]
        double distanceSquared = x * x + z * z;
        double radiusSquared = reflectionWorldSizeRadius * reflectionWorldSizeRadius;

        // Check if player is outside the circular boundary
        if (distanceSquared > radiusSquared) {
            // Calculate the angle from origin
            double angle = Math.atan2(z, x);

            // Teleport to opposite side of the circle
            // Subtract a small epsilon to ensure they're inside the boundary
            double newX = -Math.cos(angle) * (reflectionWorldSizeRadius - 2);
            double newZ = -Math.sin(angle) * (reflectionWorldSizeRadius - 2);

            Location newLoc = loc.clone();
            newLoc.setX(newX);
            newLoc.setZ(newZ);

            player.teleport(newLoc);
        }
    }
}
