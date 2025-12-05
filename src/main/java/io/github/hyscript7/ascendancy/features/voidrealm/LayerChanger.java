package io.github.hyscript7.ascendancy.features.voidrealm;

import io.github.hyscript7.ascendancy.AscendancyConfig;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LayerChanger {
    private final Map<UUID, BukkitTask> breakerTasks = new ConcurrentHashMap<>();

    private final int defaultWorldHeightVoidTerminatorOffset;

    public LayerChanger() {
        defaultWorldHeightVoidTerminatorOffset = AscendancyConfig.getInstance().getVoidRealm().defaultWorldHeightVoidTerminatorOffset();
    }

    public void changeLayer(Entity entity, @Nullable VoidRealmLayer newLayer) {
        changeLayer(entity, newLayer, false);
    }

    public void changeLayer(Entity entity, @Nullable VoidRealmLayer newLayer, boolean spawnAtBottom) {
        if (newLayer == null) {
            return;
        }
        switch (newLayer) {
            case ABYSS -> {
                Location location = entity.getLocation();
                location = translateCoordinates(location, location.getWorld(), VoidRealmLayer.ABYSS.getWorld());
                if (spawnAtBottom) {
                    location.setY(VoidRealmLayer.ABYSS.getWorld().getMinHeight());
                } else {
                    location.setY(VoidRealmLayer.ABYSS.getWorld().getMaxHeight());
                }
                entity.teleport(location);
            }
            case OBLIVION -> {
                Location location = entity.getLocation();
                location = translateCoordinates(location, location.getWorld(), VoidRealmLayer.OBLIVION.getWorld());
                if (spawnAtBottom) {
                    location.setY(VoidRealmLayer.OBLIVION.getWorld().getMinHeight());
                } else {
                    location.setY(VoidRealmLayer.OBLIVION.getWorld().getMaxHeight());
                }
                entity.teleport(location);
            }
            case REFLECTION -> {
                // Any -> Reflection is a special case. It always leads to 0, 320, 0 in the Reflection
                if (entity instanceof Player player) {
                    if (PlayerDataManager.getInstance().getPlayerData(player).isDead()) {
                        Location location = player.getLocation();
                        if (location.getY() < location.getWorld().getMinHeight()) {
                            location.setY(location.getWorld().getMaxHeight());
                            player.teleport(location);
                        }
                        return;
                    }
                }
                World world = VoidRealmLayer.REFLECTION.getWorld();
                Location location = new Location(world, 0, spawnAtBottom ? world.getMinHeight() : world.getMaxHeight(), 0, 0, 0);
                entity.teleport(location);
            }
        }
    }

    public void changeToOverworld(Entity entity) {
        Player player = (entity instanceof Player p) ? p : null;
        if (player != null && PlayerDataManager.getInstance().getPlayerData(player).isDead()) {
            return;
        }

        World world = Bukkit.getWorld("world"); // deal with it

        Location location = entity.getLocation();
        location = translateCoordinates(location, location.getWorld(), world);
        location.setY(world.getMinHeight() - defaultWorldHeightVoidTerminatorOffset);
        entity.teleport(location);
    }

    private Location translateCoordinates(Location source, World oldWorld, World newWorld) {

        if (oldWorld.equals(newWorld)) {
            return source.clone();
        }

        double oldScale = getWorldScale(oldWorld);
        double newScale = getWorldScale(newWorld);

        // Conversion factor (downscaling or upscaling)
        double factor = newScale / oldScale;

        Location out = source.clone();
        out.setWorld(newWorld);

        out.setX(source.getX() * factor);
        out.setZ(source.getZ() * factor);

        // Y unchanged unless you tell me otherwise
        out.setY(source.getY());

        return out;
    }

    private double getWorldScale(World world) {
        // Check void layers first
        VoidRealmLayer layer = VoidRealmLayer.fromWorld(world);
        if (layer != null) {
            return switch (layer) {
                case ABYSS -> 1.0 / 16.0;       // 1 abyss block = 16 overworld
                case OBLIVION -> 1.0 / 32.0;    // 1 oblivion block = 32 overworld
                case REFLECTION -> 1.0 / 128.0; // 1 reflection = 128 overworld
            };
        }

        // Overworld / Nether / End
        String name = world.getName().toLowerCase();

        if (name.contains("world")) return 1.0;
        if (name.contains("nether")) return 1.0 / 8.0;   // 1 nether = 8 overworld
        if (name.contains("the_end")) return 1.0 / 16.0; // 1 end = 16 overworld

        // unknown → assume overworld
        return 1.0;
    }
}
