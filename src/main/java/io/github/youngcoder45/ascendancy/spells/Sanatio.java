package io.github.youngcoder45.ascendancy.spells;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.AbstractRegexIncantationSpell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;
import org.bukkit.attribute.Attribute;

public class Sanatio extends AbstractRegexIncantationSpell {
    public Sanatio() {
        super("sanatio", "Sanatio", ".*\\b([Ss]anatio|[Dd]ivine\\s[Ll]ight)\\b.*", SpellTier.RARE, 50, 30 * 1000);
    }

    @Override
    public boolean cast(SpellContext context) {
        double maxHealth = context.getCaster().getAttribute(Attribute.MAX_HEALTH).getValue();
        double currentHealth = context.getCaster().getHealth();
        
        double newHealth = Math.min(maxHealth, currentHealth + 8); // Heal 4 hearts
        context.getCaster().setHealth(newHealth);
        return true;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return true;
    }

    private static final String[] incantations = new String[] {"Sanatio", "Divine Light"};

    @Override
    public String getIncantation() {
        return incantations[AscendancyPlugin.getInstance().getRandom().nextInt(incantations.length)];
    }
}
