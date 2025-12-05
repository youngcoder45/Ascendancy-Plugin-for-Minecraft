package io.github.hyscript7.ascendancy.data.players;

import io.github.hyscript7.ascendancy.AscendancyConfig;
import io.github.hyscript7.ascendancy.features.bossprog.BossType;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class PlayerData {
    private final UUID uuid;
    private boolean dirty;

    private int maxLives;
    private int lives;

    // This is a flag which serves as a way to let us know whether we performed onVoidBan actions already.
    private boolean dead;

    @Setter
    private boolean inVoidRealm;
    @Setter
    private VoidRealmLayer currentVoidRealmLayer;

    private int pvpDeaths;
    private int pveDeaths;
    private int pvpKills;

    private String trueName;

    private Set<String> knownTrueNames;

    private Set<String> knownSpells;

    private Set<BossType> killedBosses;

    private long firstSeenTimestamp;
    private long lastSeenTimestamp;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.dirty = true;
        this.maxLives = AscendancyConfig.getInstance().getVoidBan().defaultMaxLives();
        this.lives = AscendancyConfig.getInstance().getVoidBan().startingLives();
        this.dead = false;
        this.inVoidRealm = false;
        this.currentVoidRealmLayer = null;
        this.pvpDeaths = 0;
        this.pveDeaths = 0;
        this.pvpKills = 0;
        this.trueName = null;
        this.knownTrueNames = new HashSet<>();
        this.knownSpells = new HashSet<>();
        this.killedBosses = new HashSet<>();
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void markClean() {
        this.dirty = false;
    }

    public void setMaxLives(int maxLives) {
        markDirty();
        this.maxLives = maxLives;
    }

    public void setLives(int lives) {
        markDirty();
        this.lives = Math.max(0, Math.min(maxLives, lives));
    }

    public void addLives(int lives) {
        markDirty();
        this.lives = Math.min(maxLives, this.lives + lives);
    }

    public void removeLives(int lives) {
        markDirty();
        this.lives = Math.max(0, this.lives - lives);
    }

    public void setDead(boolean dead) {
        markDirty();
        this.dead = dead;
    }

    public void setPvpDeaths(int pvpDeaths) {
        markDirty();
        this.pvpDeaths = pvpDeaths;
    }

    public void incrementPvpDeaths() {
        markDirty();
        this.pvpDeaths += 1;
    }

    public void setPveDeaths(int pveDeaths) {
        markDirty();
        this.pveDeaths = pveDeaths;
    }

    public void incrementPveDeaths() {
        markDirty();
        this.pveDeaths += 1;
    }

    public void setPvpKills(int pvpKills) {
        markDirty();
        this.pvpKills = pvpKills;
    }

    public void incrementPvpKills() {
        markDirty();
        this.pvpKills += 1;
    }

    public void setTrueName(String trueName) {
        markDirty();
        this.trueName = trueName;
    }

    public void setKnownTrueNames(Set<String> knownTrueNames) {
        markDirty();
        this.knownTrueNames = knownTrueNames;
    }

    public void learnName(String name) {
        markDirty();
        this.knownTrueNames.add(name);
    }

    public boolean knowsTrueName(String name) {
        return knownTrueNames.contains(name) || name.equalsIgnoreCase(trueName);
    }

    public Set<String> getKnownTrueNames() {
        return Collections.unmodifiableSet(knownTrueNames);
    }

    public void setKnownSpells(Set<String> knownSpells) {
        markDirty();
        this.knownSpells = knownSpells;
    }

    public void learnSpells(String ...spells) {
        markDirty();
        knownSpells.addAll(Arrays.asList(spells));
    }

    public boolean knowsSpell(String spell) {
        return knownSpells.contains(spell);
    }

    /**
     * Returns an immutable list of known spells.
     * @return A list of all learned spells IDs (as in registries)
     */
    public Set<String> getKnownSpells() {
        return Collections.unmodifiableSet(knownSpells);
    }

    public void setKilledBosses(Set<BossType> killedBosses) {
        markDirty();
        this.killedBosses = killedBosses;
    }

    public void addBossKill(BossType bossType) {
        markDirty();
        this.killedBosses.add(bossType);
    }

    public void removeBossKill(BossType bossType) {
        markDirty();
        this.killedBosses.remove(bossType);
    }

    public int getKilledBossesCount() {
        return killedBosses.size();
    }

    public boolean hasKilledBoss(BossType bossType) {
        return killedBosses.contains(bossType);
    }

    public Set<BossType> getKilledBosses() {
        return Collections.unmodifiableSet(killedBosses);
    }

    public void setFirstSeenTimestamp(long timestamp) {
        markDirty();
        this.firstSeenTimestamp = timestamp;
    }

    public void setFirstSeenTimestamp() {
        setFirstSeenTimestamp(System.currentTimeMillis());
    }

    public void setLastSeenTimestamp(long timestamp) {
        markDirty();
        this.lastSeenTimestamp = timestamp;
    }

    public void updateLastSeenTimestamp() {
        setLastSeenTimestamp(System.currentTimeMillis());
    }
}
