package io.github.youngcoder45.ascendancy.spells;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.AbstractRegexIncantationSpell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class Fulmen extends AbstractRegexIncantationSpell {
    public Fulmen() {
        super("fulmen", "Fulmen", ".*\\b([Ff]ulmen|[Ll]ightning\\s[Ss]trike|[Tt]hunder)\\b.*", SpellTier.UNCOMMON, 30, 15 * 1000);
    }

    @Override
    public boolean cast(SpellContext context) {
        Block targetBlock = context.getCaster().getTargetBlockExact(50);
        if (targetBlock == null) return false;
        
        Location targetLocation = targetBlock.getLocation();
        context.getCaster().getWorld().strikeLightning(targetLocation);
        return true;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return true;
    }

    private static final String[] incantations = new String[] {"Fulmen", "Lightning Strike", "Thunder"};

    @Override
    public String getIncantation() {
        return incantations[AscendancyPlugin.getInstance().getRandom().nextInt(incantations.length)];
    }
}
