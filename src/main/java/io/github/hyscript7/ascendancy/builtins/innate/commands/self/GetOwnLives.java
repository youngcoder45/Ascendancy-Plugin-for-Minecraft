package io.github.hyscript7.ascendancy.builtins.innate.commands.self;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.features.innate.names.AbstractInnateCommand;
import io.github.hyscript7.ascendancy.features.innate.names.InnateContext;
import io.github.hyscript7.ascendancy.features.innate.names.InnateUtils;

public class GetOwnLives extends AbstractInnateCommand {

    public GetOwnLives() {
        super("get_own_lives", "Show Own Lives", false, false, true);
    }

    @Override
    public boolean execute(InnateContext context) {
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(context.invoker());
        String fullHearts = "<dark_red>❤".repeat(playerData.getLives());
        String emptyHearts = "<dark_gray>❤".repeat(playerData.getMaxLives() - playerData.getLives());
        AscendancyMessagingAPI.getInstance().send(context.invoker(), AscendancyMessagingAPI.MessageType.INFO, "Your lives: " + fullHearts + emptyHearts);
        return true;
    }

    @Override
    public boolean matchesMessage(String message) {
        return InnateUtils.unifyInnateCommand(InnateUtils.removeNonAlpha(message)).equalsIgnoreCase("I wish to know my lives") || message.equalsIgnoreCase("I desire my lives") || message.equalsIgnoreCase("I recall my lives");
    }

    @Override
    public String[] resolveArguments(String message) {
        return new String[0];
    }

    @Override
    public String getUsage() {
        return "I [wish to know | desire | recall] my lives";
    }
}