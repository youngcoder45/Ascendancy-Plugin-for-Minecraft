package io.github.hyscript7.ascendancy.features.magic;

public abstract class AbstractSpell implements Spell {
    private final String id;
    private final String displayName;

    private final SpellTier tier;
    private final double manaCost;
    private final long cooldownMillis;

    public AbstractSpell(String id, String displayName, SpellTier tier, double manaCost, long cooldownMillis) {
        this.id = id;
        this.displayName = displayName;
        this.tier = tier;
        this.manaCost = manaCost;
        this.cooldownMillis = cooldownMillis;
    }

    @Override
    public SpellTier getTier() {
        return tier;
    }

    @Override
    public double getManaCost() {
        return manaCost;
    }

    @Override
    public long getCooldownMillis() {
        return cooldownMillis;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
