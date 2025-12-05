package io.github.hyscript7.ascendancy.features.innate.protections;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class InnateProtectionManager {
    private static InnateProtectionManager instance;

    public static InnateProtectionManager getInstance() {
        if (instance == null) {
            instance = new InnateProtectionManager();
        }
        return instance;
    }

    private final Map<UUID, Set<InnateProtection>> activeProtections;
    private final Map<UUID, BukkitTask> queuedDeletions;

    public InnateProtectionManager() {
        activeProtections = new HashMap<>();
        queuedDeletions = new ConcurrentHashMap<>();
    }

    public boolean isProtected(UUID playerUuid) {
        if (!activeProtections.containsKey(playerUuid)) return false;
        if (activeProtections.get(playerUuid).isEmpty()) return false;
        if (activeProtections.get(playerUuid).stream().anyMatch(InnateProtection::isActive)) {
            return true;
        } else {
            return purgeInactiveProtectionsAndReturnFalse(playerUuid);
        }
    }

    private boolean purgeInactiveProtectionsAndReturnFalse(UUID playerUuid) {
        activeProtections.get(playerUuid).removeIf(Predicate.not(InnateProtection::isActive));
        return false;
    }

    public void addProtection(InnateProtection protection) {
        activeProtections.computeIfAbsent(protection.getPlayerUuid(), uuid -> new HashSet<>()).add(protection);
    }

    public void removeProtection(InnateProtection protection) {
        if (!activeProtections.containsKey(protection.getPlayerUuid())) return;
        activeProtections.get(protection.getPlayerUuid()).remove(protection);
    }

    public void removeProtections(UUID playerUuid) {
        if (!activeProtections.containsKey(playerUuid)) return;
        activeProtections.get(playerUuid).clear();
    }

    /**
     * Call this method when a player leaves the server.
     * <p>
     * They will be given 5 minutes to join back before their protections are cleared.
     * @param playerUuid The UUID of the player who left.
     */
    public void purgePlayer(UUID playerUuid) {
        if (!activeProtections.containsKey(playerUuid)) return;
        queuedDeletions.put(
                playerUuid,
                Bukkit.getScheduler().runTaskLater(AscendancyPlugin.getInstance(), () -> {
                    if (Bukkit.getServer().getOfflinePlayer(playerUuid).isOnline()) {
                        // If by some miracle the player rejoined and this task wasn't cancelled...
                        return;
                    }
                    activeProtections.remove(playerUuid);
                }, 5 * 60 * 20) // 5 minutes
        );
    }

    /**
     * Call this method when a player joins the server.
     * @param playerUuid The UUID of the player who joined.
     */
    public void purgeWaitingTasks(UUID playerUuid) {
        if (!queuedDeletions.containsKey(playerUuid)) return;
        BukkitTask task = queuedDeletions.remove(playerUuid);
        task.cancel();
    }
}
