package eu.projnull.spelis.ascendancy.rituals;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.rituals.*;
import io.github.hyscript7.ascendancy.features.rituals.requirements.ItemExclusivityFactory;
import io.github.hyscript7.ascendancy.features.rituals.requirements.ItemSacrifice;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Set;

public class RitualOfTheAnchor extends AbstractRitual {
    public RitualOfTheAnchor() {
        super("ritual_anchor","Ritual of the Anchor", RitualGrade.ADVANCED,buildStages());
    }

    private static final ItemExclusivityFactory exclusivity = new ItemExclusivityFactory(Set.of(Material.ENDER_PEARL, Material.COMPASS, Material.LODESTONE));

    private static List<RitualStage> buildStages() {
        return List.of(
                exclusivity.on(
                        ItemSacrifice.builder().itemType(Material.LODESTONE).minAmount(1).build()
                ),
                exclusivity.on(
                        ItemSacrifice.builder().itemType(Material.COMPASS).minAmount(1).build()
                ),
                exclusivity.on(
                        ItemSacrifice.builder().itemType(Material.ENDER_PEARL).minAmount(5).build()
                )
        );
    }

    @Override
    public ActiveRitualContext perform(RitualContext context) {
        ActiveRitualContext defaultInstant = defaultInstantRitualContext(this, context);
        Player invoker = context.getInvoker();
        Location location = context.getLocation().clone();
        Block lodestoneBlock = location.getWorld().getBlockAt(location);
        lodestoneBlock.setType(Material.LODESTONE);

        ItemStack compassItem = new ItemStack(Material.COMPASS,1);
        if (!(compassItem.getItemMeta() instanceof CompassMeta compassMeta)) {
            invoker.give(compassItem);
            invoker.give(new ItemStack(Material.LODESTONE,1));
            invoker.give(new ItemStack(Material.ENDER_PEARL,5));
            // Please correct me if this is the wrong way to refund, because I didn't check lmao.
            lodestoneBlock.setType(Material.AIR); // make sure not to duplicate the lodestone.

            return defaultInstant;
        }

        compassMeta.setLodestone(location);
        compassMeta.setLodestoneTracked(true);
        compassMeta.getPersistentDataContainer().set(new NamespacedKey(AscendancyPlugin.getInstance(), "teleportation_compass"), PersistentDataType.BOOLEAN, true);
        compassItem.setItemMeta(compassMeta);

        invoker.give(compassItem);

        return defaultInstant;
    }
}