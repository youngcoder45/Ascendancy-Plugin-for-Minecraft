package io.github.hyscript7.ascendancy.builtins.innate.commands.other;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.features.innate.names.AbstractInnateCommand;
import io.github.hyscript7.ascendancy.features.innate.names.InnateContext;
import io.github.hyscript7.ascendancy.features.innate.names.InnateUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BestowItem extends AbstractInnateCommand {

    public BestowItem() {
        super("bestow item", "Item Bestowal", false, false, false);
    }

    @Override
    public boolean execute(InnateContext context) {
        int count = Integer.parseInt(context.args()[0]);
        String thing = context.args()[1];

        Material material = Material.matchMaterial(thing);

        ItemStack theThing = removeOnceFromInvokerAndReturnItemStack(context.invoker(), material, count);
        if (theThing != null) {
            count = theThing.getAmount();
            addToTarget(context.target(), theThing);
        }

        if (material != null && theThing != null) {
            AscendancyMessagingAPI.getInstance().send(context.target(), AscendancyMessagingAPI.MessageType.INFO, "You have been bestowed " + count +" by " + context.invoker().getName() + "!");
            AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.SUCCESS, "You have bestowed " + context.target().getName() + " " +  count + "x " + thing.toLowerCase() + "!");
            return true;
        } else {
            AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.ERROR, "You do not have any " + thing.toLowerCase() + " that you could bestow!");
            return false;
        }
    }

    private @Nullable ItemStack removeOnceFromInvokerAndReturnItemStack(Player player, Material material, int userAmount) {
        ItemStack firstMatch = null;
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack != null && itemStack.getType() == material) {
                firstMatch = itemStack.clone();
                int amount = Math.min(firstMatch.getAmount(), userAmount);
                firstMatch.setAmount(amount);
                itemStack.setAmount(itemStack.getAmount() - amount);
                break;
            }
        }
        return firstMatch;
    }

    private void addToTarget(Player player, ItemStack stack) {
        player.getInventory().addItem(stack).forEach(
                // For all items which didn't fit in inventory
                (fuckIfIKnow,remainingItemStack) -> player.getLocation().getWorld().dropItem(player.getLocation(), remainingItemStack)
        );
    }

    @Override
    public boolean matchesMessage(String message) {
        return InnateUtils.unifyInnateCommand(message).matches(".+ I bestow you (the|\\d+)? .+ (item)?(s)?");
    }

    private static final Pattern argsPattern = Pattern.compile("(?<count>the|\\d+)? (?<thing>.+)(s)? (item)?(s)?");

    @Override
    public String[] resolveArguments(String message) {
        String pureArgs = InnateUtils.unifyInnateCommand(InnateUtils.removeNonAlpha(message, true)).split("I bestow you")[1].trim();
        Matcher m = argsPattern.matcher(pureArgs.trim());
        m.matches(); // Since this won't be called unless the matchesMessage() passes, we should be good.
        String countGroup = m.group("count");
        if (countGroup == null || countGroup.isEmpty() || countGroup.equalsIgnoreCase("the")) {
            countGroup = "1";
        }
        String thing = m.group("thing");
        return new String[]{countGroup, thing};
    }

    @Override
    public String getUsage() {
        return "<name> I bestow you (the | [1|2|...|64]) <item name> [item(s)]";
    }
}
