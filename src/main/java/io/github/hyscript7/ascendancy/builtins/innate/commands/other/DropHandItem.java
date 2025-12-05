package io.github.hyscript7.ascendancy.builtins.innate.commands.other;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.features.innate.names.AbstractInnateCommand;
import io.github.hyscript7.ascendancy.features.innate.names.InnateContext;
import io.github.hyscript7.ascendancy.features.innate.names.InnateUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class DropHandItem extends AbstractInnateCommand {
    private static final double MAX_DISTANCE = 32.0;
    private static final double MIN_DISTANCE = 6.0;

    public DropHandItem() {
        super("drop item in hand", "Drop Item in Main Hand", true, true, false);
    }

    @Override
    public boolean execute(InnateContext context) {
        double distance = context.invoker().getLocation().distance(context.target().getLocation());
        if (distance > MAX_DISTANCE || distance < MIN_DISTANCE) {
            AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.ERROR, context.target().getName() + " is either too far from you or too close to you!");
            AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.WARNING, "Someone just tried to make you drop your item, but they were too far away!");
            return false;
        }
        ItemStack itemInHand = context.target().getInventory().getItemInMainHand();
        if (itemInHand.getType().equals(Material.AIR)) {
            AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.ERROR, context.target().getName() + " isn't holding anything!");
            AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.WARNING, "Someone just tried to make you drop your item, but you aren't holding anything!");
            return false;
        }
        context.target().dropItem(context.target().getInventory().getHeldItemSlot());
        return true;
    }

    @Override
    public boolean matchesMessage(String message) {
        return InnateUtils.unifyInnateCommand(message).toLowerCase().matches(".+ (drop)\\s(the|your\\s)?item((\\swhich|\\sthat)?\\syou\\sare\\sholding)?(\\sin\\syour\\shand)?");
    }

    @Override
    public String[] resolveArguments(String message) {
        return new String[]{};
    }

    @Override
    public String getUsage() {
        return "<name> drop [the|your] item [[(which|that)] you are holding [in your hand]]";
    }
}
