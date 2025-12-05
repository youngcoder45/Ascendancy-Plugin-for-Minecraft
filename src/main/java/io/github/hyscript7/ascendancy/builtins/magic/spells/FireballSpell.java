package io.github.hyscript7.ascendancy.builtins.magic.spells;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.AbstractRegexIncantationSpell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;

public class FireballSpell extends AbstractRegexIncantationSpell {
    public FireballSpell() {
        super("fireball", "Ignis", ".*\\b([Ff]ire\\s[Bb]all|[Ff]ire\\s[Ss]phere|[Ii]gnis)\\b.*", SpellTier.COMMON, 20, 10 * 1000);
    }

    @Override
    public boolean cast(SpellContext context) {
        org.bukkit.entity.Fireball fireball = context.getCaster().launchProjectile(org.bukkit.entity.Fireball.class);
        fireball.setYield(2.0f);
        return true;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return true;
    }

    private static final String[] incantations = new String[] {"Fire Ball", "Fire Sphere", "Ignis"};

    @Override
    public String getIncantation() {
        return incantations[AscendancyPlugin.getInstance().getRandom().nextInt(incantations.length)];
    }
}
