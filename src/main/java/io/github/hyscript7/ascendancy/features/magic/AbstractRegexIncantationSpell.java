package io.github.hyscript7.ascendancy.features.magic;

import java.util.regex.Pattern;

public abstract class AbstractRegexIncantationSpell extends AbstractSpell {
    private final Pattern incantationPattern;

    public AbstractRegexIncantationSpell(String id, String displayName, String regex, SpellTier tier, double manaCost, long cooldownMillis) {
        super(id, displayName, tier, manaCost, cooldownMillis);
        this.incantationPattern = Pattern.compile(regex);
    }

    @Override
    public boolean incantationMatches(SpellContext context) {
        return incantationMatches(context.getRawIncantation());
    }

    @Override
    public boolean incantationMatches(String s) {
        return incantationPattern.matcher(s).matches();
    }
}
