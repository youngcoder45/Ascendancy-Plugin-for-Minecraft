package io.github.hyscript7.ascendancy.builtins.innate.commands.other;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.features.innate.names.AbstractInnateCommand;
import io.github.hyscript7.ascendancy.features.innate.names.InnateContext;
import io.github.hyscript7.ascendancy.features.innate.names.InnateUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BestowLive extends AbstractInnateCommand {
    public BestowLive() {
        super("bestow life", "Life Bestowal", false, false, false);
    }

    @Override
    public boolean execute(InnateContext context) {
        int livesToBestow = Integer.parseInt(context.args()[0]);
        PlayerData invokerData = PlayerDataManager.getInstance().getPlayerData(context.invoker());
        if (invokerData.getLives() <= livesToBestow) {
            if (invokerData.isDead()) {
                AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.ERROR, "You cannot bestow any lives while you are dead!");
            } else {
                AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.ERROR, "You cannot bestow " + livesToBestow + " lives, as you do not have that many or would die by doing so!");
            }
            return false;
        }
        PlayerData targetData = PlayerDataManager.getInstance().getPlayerData(context.target());
        if (targetData.isDead()) {
            AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.ERROR, "You cannot bestow lives to " + context.invoker().getName() + " while they are dead!");
            return false;
        }
        invokerData.removeLives(livesToBestow);
        targetData.addLives(livesToBestow);
        AscendancyMessagingAPI.getInstance().send(context.target(), AscendancyMessagingAPI.MessageType.INFO, "You have been bestowed " + livesToBestow + "live" + (livesToBestow > 1 ? "s" : "") +" by " + context.invoker().getName() + "!");
        AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.SUCCESS, "You have bestowed " + context.target().getName() + livesToBestow + "live" + (livesToBestow > 1 ? "s" : "") + "!");
        return true;
    }

    Pattern pattern = Pattern.compile(".+ I bestow you (?<amount>[\\da]+)? [Ll]i[fv]e(s)?");

    @Override
    public boolean matchesMessage(String message) {
        return pattern.matcher(InnateUtils.unifyInnateCommand(message)).matches();
    }

    @Override
    public String[] resolveArguments(String message) {
        Matcher matcher = pattern.matcher(InnateUtils.unifyInnateCommand(InnateUtils.removeNonAlpha(message)));
        // Should always match
        matcher.find();
        String amountString = matcher.group("amount");
        if (amountString.isEmpty() || amountString.replace("a", "").isEmpty()) {
            amountString = "1";
        }
        return new String[]{amountString};
    }

    @Override
    public String getUsage() {
        return "<name> I bestow you [n] (live|life)[s]";
    }
}
