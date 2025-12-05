package io.github.hyscript7.ascendancy.features.magic;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpellCooldownManager {
    private static SpellCooldownManager instance;

    private final Map<UUID, Map<Spell, Long>> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> arcanaCooldownExpiry = new ConcurrentHashMap<>();

    private SpellCooldownManager() {}

    public static SpellCooldownManager getInstance() {
        if (instance == null) {
            instance = new SpellCooldownManager();
        }
        return instance;
    }

    /**
     * Puts the player on cooldown
     * @param player The player to put on cooldown
     * @param spell Which spell the cooldown belongs to
     */
    public void setPlayerOnCooldown(Player player, Spell spell) {
        if (spell.getTier().equals(SpellTier.ARCANA)) {
            arcanaCooldownExpiry.put(player.getUniqueId(), System.currentTimeMillis() + spell.getCooldownMillis());
        }
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(spell, System.currentTimeMillis());
    }

    /**
     * Checks if a spell is on cooldown for the specified player
     * @param player The player to test
     * @param spell The spell to check
     * @return true if the player is on cooldown, otherwise false
     */
    public boolean isPlayerOnCooldown(Player player, Spell spell) {
        return getRemainingCooldownMillis(player, spell) > 0;
    }

    /**
     * Checks how much time is left until the cooldown is up for the specified player.
     * @param player The player to test
     * @param spell The spell to check
     * @return A negative integer if the cooldown is up, otherwise positive (in milliseconds)
     */
    public long getRemainingCooldownMillis(Player player, Spell spell) {
        if (spell.getTier().equals(SpellTier.ARCANA)) {
            return arcanaCooldownExpiry.getOrDefault(player.getUniqueId(), 0L) - now();
        }
        long timeSinceLastCast = now() - cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).getOrDefault(spell, 0L);
        return spell.getCooldownMillis() - timeSinceLastCast;
    }

    /**
     * Removes a player from the cooldown cache once all their cooldowns are up.
     * <p>
     * This should be called when the player leaves the server.
     * @param uuid The UUID of the player to purge.
     */
    public void queuePlayerRemoval(UUID uuid) {
        Map<Spell, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null || playerCooldowns.isEmpty()) {
            cooldowns.remove(uuid);
            return;
        }

        long now = now();

        // Finds the timestamp (millis) at which the last cooldown ends.
        long latestEnd = Math.max(playerCooldowns.entrySet().stream()
                .mapToLong(e -> e.getValue() + e.getKey().getCooldownMillis())
                .max()
                .orElse(now), arcanaCooldownExpiry.getOrDefault(uuid, 0L));

        long delay = latestEnd - now;

        if (delay <= 0) {
            // Everything is already expired
            cooldowns.remove(uuid);
            return;
        }

        // Schedule removal after cooldowns expire
        Bukkit.getScheduler().runTaskLater(
            AscendancyPlugin.getInstance(),
            () -> {
                Player player = Bukkit.getPlayer(uuid);
                // Check if the player rejoined by chance
                if (player != null && player.isOnline()) {
                    return;
                }
                cooldowns.remove(uuid);
            },
            millisToTicks(delay)
        );
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    private long millisToTicks(long millis) {
        return millis / 50L;
    }

}
