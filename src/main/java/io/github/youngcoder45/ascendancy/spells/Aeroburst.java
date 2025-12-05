package io.github.youngcoder45.ascendancy.spells;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.AbstractRegexIncantationSpell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;
import org.bukkit.entity.WindCharge;

public class Aeroburst extends AbstractRegexIncantationSpell {
    public Aeroburst() {
        super("aeroburst", "Aeroburst", ".*\\b([Aa]eroburst|[Gg]ale\\s[Ss]hot|[Ss]piritus)\\b.*", SpellTier.COMMON, 25, 8 * 1000);
    }

    @Override
    public boolean cast(SpellContext context) {
        WindCharge windCharge = context.getCaster().launchProjectile(WindCharge.class);
        windCharge.setYield(1.2f); // Slightly stronger than default
        return true;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return true;
    }

    private static final String[] incantations = new String[] {"Aeroburst", "Gale Shot", "Spiritus"};

    @Override
    public String getIncantation() {
        return incantations[AscendancyPlugin.getInstance().getRandom().nextInt(incantations.length)];
    }
}
