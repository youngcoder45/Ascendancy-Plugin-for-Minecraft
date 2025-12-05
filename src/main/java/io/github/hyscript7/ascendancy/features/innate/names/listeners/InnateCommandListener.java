package io.github.hyscript7.ascendancy.features.innate.names.listeners;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.data.players.names.TrueNameManager;
import io.github.hyscript7.ascendancy.features.innate.names.InnateCommand;
import io.github.hyscript7.ascendancy.features.innate.names.InnateContext;
import io.github.hyscript7.ascendancy.features.innate.names.InnateUtils;
import io.github.hyscript7.ascendancy.features.innate.protections.InnateProtectionManager;
import io.github.hyscript7.ascendancy.registries.RegistryManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;
import java.util.logging.Level;

public class InnateCommandListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onChatted(AsyncChatEvent event) {
        Player invoker = event.getPlayer();
        String message = ((TextComponent) event.message()).content();

        // TODO: Figure out immunity bypasses
        InnateContext.InnateContextBuilder builder = InnateContext.builder()
                .invoker(invoker)
                .originalMessage(message)
                .invokerHasImmunityBypass(false);

        boolean selfTarget = InnateUtils.removeNonAlpha(message).startsWith("I");
        Player target;

        if (selfTarget) {
            target = invoker;
        } else {
            String targetName = InnateUtils.parseInnateNameAtSentenceBeginning(message);
            if (targetName == null) {
                return;
            }
            UUID targetUuid = TrueNameManager.getInstance().findTrueNameOwner(targetName);
            if (targetUuid == null) {
                return;
            }
            target = Bukkit.getPlayer(targetUuid);
        }

        if (target == null) return;

        builder.target(target);

        // Only protect if the invoker isn't targeting self.
        if (!selfTarget) {
            builder.targetHasImmunity(InnateProtectionManager.getInstance().isProtected(target.getUniqueId()));
        } else {
            builder.targetHasImmunity(false);
        }

        InnateCommand command = parseInnateCommand(message, selfTarget);

        if (command == null) {
            return;
        }

        InnateContext context = builder.args(command.resolveArguments(message)).build();

        Bukkit.getScheduler().runTask(AscendancyPlugin.getInstance(), () -> handleExecution(context, command));
    }

    private void handleExecution(InnateContext context, InnateCommand command) {
        boolean hasImmunity = context.targetHasImmunity();
        boolean canExecute = command.canExecute(context);

        logInnateCommand(context, command, canExecute);

        if (canExecute && !hasImmunity) {
            playCommandCastEffects(context, command);
            command.execute(context);
        } else {
            playCommandFailEffects(context, command);
        }
    }

    private void playCommandCastEffects(InnateContext ctx, InnateCommand cmd) {
        Player invoker = ctx.invoker();
        Player target = ctx.target();

        if (!cmd.targetsSelf()) {
            playEffect(invoker, Sound.ENTITY_EVOKER_CAST_SPELL, Particle.ENCHANT, 30);
        }

        if (cmd.isHarmful()) {
            playEffect(target, Sound.ENTITY_ELDER_GUARDIAN_CURSE, Particle.SOUL, 50);
        } else {
            playEffect(target, Sound.BLOCK_ENCHANTMENT_TABLE_USE, Particle.END_ROD, 20);
        }
    }

    private void playCommandFailEffects(InnateContext ctx, InnateCommand cmd) {
        playEffect(ctx.invoker(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, Particle.ANGRY_VILLAGER, 30);
        if (ctx.targetHasImmunity() && !ctx.invokerHasImmunityBypass()) {
            AscendancyMessagingAPI.getInstance().send(ctx.invoker(), AscendancyMessagingAPI.MessageType.ERROR, "A mysterious force prevents the name from resonating with its owners soul.");
        }
    }

    private void playEffect(Player p, Sound sound, Particle particle, int count) {
        p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
        p.spawnParticle(particle, p.getLocation().add(0, 1, 0), count, 0.4, 0.5, 0.4);
    }

    private void logInnateCommand(InnateContext ctx, InnateCommand cmd, boolean successful) {
        AscendancyPlugin.getInstance().getLogger().log(
                Level.INFO,
                "%s has %s %s's true name to invoke %s".formatted(
                        ctx.invoker().getName(),
                        successful ? "used" : "attempted to use",
                        ctx.target().getName(),
                        cmd.getId()
                )
        );
    }

    private InnateCommand parseInnateCommand(String message, boolean selfTarget) {
        return RegistryManager.getInstance().getInnateCommandRegistry().getAll().stream()
                .filter(cmd -> cmd.targetsSelf() == selfTarget)
                .filter(cmd -> cmd.matchesMessage(message))
                .findFirst()
                .orElse(null);
    }
}
