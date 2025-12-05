package io.github.youngcoder45.ascendancy.spells;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.AbstractRegexIncantationSpell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

public class Expelliarmus extends AbstractRegexIncantationSpell {
    public Expelliarmus() {
        super("expelliarmus", "Expelliarmus", ".*\\b([Ee]xpelliarmus|[Dd]isarm)\\b.*", SpellTier.RARE, 50, 15 * 1000);
    }

    @Override
    public boolean cast(SpellContext context) {
        Player caster = context.getCaster();
        Location eyeLocation = caster.getEyeLocation();
        RayTraceResult result = caster.getWorld().rayTraceEntities(eyeLocation, eyeLocation.getDirection(), 20, 0.5, entity -> entity instanceof LivingEntity && !entity.equals(caster));

        if (result != null && result.getHitEntity() instanceof LivingEntity target) {
            EntityEquipment equipment = target.getEquipment();
            if (equipment != null) {
                ItemStack mainHand = equipment.getItemInMainHand();
                if (!mainHand.getType().isAir()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), mainHand);
                    equipment.setItemInMainHand(null);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return true;
    }

    private static final String[] incantations = new String[] {"Expelliarmus", "Disarm"};

    @Override
    public String getIncantation() {
        return incantations[AscendancyPlugin.getInstance().getRandom().nextInt(incantations.length)];
    }
}
