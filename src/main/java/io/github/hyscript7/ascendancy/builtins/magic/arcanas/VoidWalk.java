package io.github.hyscript7.ascendancy.builtins.magic.arcanas;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.AbstractRegexIncantationSpell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;

public class VoidWalk extends AbstractRegexIncantationSpell {

    public VoidWalk() {
        super("void walk", "Void Walk", ".*([Vv]oid\s([Ww]alk|[Ss]tep)).*", SpellTier.ARCANA, 40, 30 * 1000);
    }

    @Override
    public boolean cast(SpellContext context) {
        // If I ever write production code like this, smack me with a metal pipe.
        context.getCaster().teleport(
                context.getCaster().getLocation()
                        .add(context.getCaster().getLocation().getDirection().multiply(10))
        );
        return true;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return true;
    }

    private static final String[] incantations = new String[] {"Void Walk", "Void Step"};

    @Override
    public String getIncantation() {
        return incantations[AscendancyPlugin.getInstance().getRandom().nextInt(incantations.length)];
    }
}
