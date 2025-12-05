package io.github.hyscript7.ascendancy.features.rituals;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents a ritual which was activated.
 * @param player The invoker (original casting circle owner)
 * @param location The location of the casting flame
 * @param ritual The ritual reference
 * @param task A bukkit task if this is a lasting ritual, otherwise an empty optional.
 */
public record ActiveRitualContext(Player player, Location location, Ritual ritual, Optional<BukkitTask> task) {
    public void cancel() {
        task.ifPresent(BukkitTask::cancel);
    }

    public boolean isLasting() {
        return task.isPresent();
    }
}
