package io.github.hyscript7.ascendancy;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

@Getter
public class AscendancyConfig {
    private static AscendancyConfig instance;
    public static AscendancyConfig getInstance() {
        if (instance == null) {
            instance = new AscendancyConfig(AscendancyPlugin.getInstance());
        }
        return instance;
    }

    public record InnateNames(Honeypot honeypot) {
        public record Honeypot(String name, boolean enabled) {}
    }

    public record VoidBan(int defaultMaxLives, int startingLives, int resurrectionLives, int reviveLives, boolean pveEnabled) {}

    public record VoidRealm(int escapeHeightOvershootPercentage, int defaultWorldHeightVoidTerminatorOffset, double bedrockBreakerRadius, int reflectionWorldSizeRadius, Strain strain, DeadPlayerRestrictions deadPlayerRestrictions) {
        public record Strain(boolean enabled, boolean targetDead, boolean targetLiving, boolean targetNonPlayers) {}
        public record DeadPlayerRestrictions(boolean noBuild, boolean noBreak, boolean noInteract) {}
    }

    public record SpellBooks(List<Float> customModelData) {}

    public record BossProgression(int diamondKills, int netheriteKills, int killCreditRadius, int netherKills) {}

    public record Rituals(VoidRevealing voidRevealing) {
        public record VoidRevealing(int spawnProtectionDistance, int maxRange, int spawnProtectionBypassHeightBelow) {}
    }

    private final InnateNames innateNames;
    private final VoidBan voidBan;
    private final VoidRealm voidRealm;
    private final SpellBooks spellBooks;
    private final BossProgression bossProgression;
    private final Rituals rituals;

    private AscendancyConfig(AscendancyPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        this.innateNames = new InnateNames(
                new InnateNames.Honeypot(
                        config.getString("innate_names.honeypot.name", "Obscuron"),
                        config.getBoolean("innate_names.honeypot.enabled", false)
                )
        );
        this.voidBan = new VoidBan(
                config.getInt("void_ban.default_max_lives", 10),
                config.getInt("void_ban.starting_lives", 10),
                config.getInt("void_ban.resurrection_lives", 10),
                config.getInt("void_ban.revive_lives", 10),
                config.getBoolean("void_ban.pve_enabled", false)
        );
        this.voidRealm = new VoidRealm(
                config.getInt("void_realm.escape_height_overshoot_percentage", 10),
                config.getInt("void_realm.default_world_height_void_terminator_offset", 10),
                config.getDouble("void_realm.bedrock_breaker_radius", 3.0),
                config.getInt("void_realm.reflection_world_size_radius", 500),
                new VoidRealm.Strain(
                        config.getBoolean("void_realm.strain.enabled", true),
                        config.getBoolean("void_realm.strain.target_dead", true),
                        config.getBoolean("void_realm.strain.target_living", true),
                        config.getBoolean("void_realm.strain.target_non_players", false)
                ),
                new VoidRealm.DeadPlayerRestrictions(
                        config.getBoolean("void_realm.dead_player_restrictions.no_build", true),
                        config.getBoolean("void_realm.dead_player_restrictions.no_break", true),
                        config.getBoolean("void_realm.dead_player_restrictions.no_interact", false)
                )
        );
        this.spellBooks = new SpellBooks(
                config.getFloatList("spell_books.custom_model_data")
        );
        this.bossProgression = new BossProgression(
                config.getInt("boss_progression.diamond_kills"),
                config.getInt("boss_progression.netherite_kills"),
                config.getInt("boss_progression.kill_credit_radius"),
                config.getInt("boss_progression.nether_kills")
        );
        this.rituals = new Rituals(
                new Rituals.VoidRevealing(
                        config.getInt("rituals.void_revealing.spawn_protection_distance", 300),
                        config.getInt("rituals.void_revealing.max_range", 32),
                        config.getInt("rituals.void_revealing.spawn_protection_y_bypass_below", 0)
                )
        );
    }
}
