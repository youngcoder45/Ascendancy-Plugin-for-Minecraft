package io.github.youngcoder45.ascendancy.spells;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.AbstractRegexIncantationSpell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;
import org.bukkit.util.Vector;

public class Ventus extends AbstractRegexIncantationSpell {
    public Ventus() {
        super("ventus", "Ventus", ".*\\b([Vv]entus|[Dd]ash|[Ww]ind)\\b.*", SpellTier.COMMON, 15, 5 * 1000);
    }

    @Override
    public boolean cast(SpellContext context) {
        Vector direction = context.getCaster().getLocation().getDirection().multiply(2);
        direction.setY(0.5); // Add a little upward boost
        context.getCaster().setVelocity(direction);
        return true;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return true;
    }

    private static final String[] incantations = new String[] {"Ventus", "Dash", "Wind"};

    @Override
    public String getIncantation() {
        return incantations[AscendancyPlugin.getInstance().getRandom().nextInt(incantations.length)];
    }
}
