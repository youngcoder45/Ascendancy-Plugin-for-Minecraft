package eu.projnull.spelis.ascendancy.spells;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.AbstractRegexIncantationSpell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

public class Recall extends AbstractRegexIncantationSpell {
    public Recall() {
        super("recall", "Recall", ".*\\b[Rr]ecall\\b.*", SpellTier.RARE, 60, 20*1000);
    }

    @Override
    public boolean cast(SpellContext context) {
        ItemStack mainHandItem = context.getCaster().getInventory().getItemInMainHand();
        NamespacedKey compassKey = new NamespacedKey(AscendancyPlugin.getInstance(), "teleportation_compass");

        if (mainHandItem.getType() != Material.COMPASS) return false;
        if (!(mainHandItem.getItemMeta() instanceof CompassMeta compassMeta)) return false;
        if (!compassMeta.hasLodestone() || !compassMeta.getPersistentDataContainer().has(compassKey)) return false;

        Location lodestoneLocation = compassMeta.getLodestone();

        assert lodestoneLocation != null;
        if (lodestoneLocation.getBlock().getType() != Material.LODESTONE) return false;

        lodestoneLocation.add(0.5,1,0.5);
        context.getCaster().teleport(lodestoneLocation);
        return true;
    }

    @Override
    public boolean canCast(SpellContext context) {
        // Since this collides with an innate name command, we won't execute in cases where it seems to be one.
        if (context.getRawIncantation().toLowerCase().startsWith("i recall my")) {
            return false;
        }
        // I know IDEA is screaming to return the condition itself, but screw that.
        return true;
    }

    private static final String[] incantations = new String[] {"Recall"};

    @Override
    public String getIncantation() {
        return incantations[AscendancyPlugin.getInstance().getRandom().nextInt(incantations.length)];
    }
}
