package io.github.hyscript7.ascendancy.features.magic;

public enum SpellTier {
    COMMON(),
    UNCOMMON,
    RARE,
    EPIC,
    ARCANA(true);

    private final boolean requiresLearning;

    SpellTier() {
        this.requiresLearning = false;
    }

    SpellTier(boolean requiresLearning) {
        this.requiresLearning = requiresLearning;
    }

    public boolean requiresLearning() {
        return requiresLearning;
    }
}
