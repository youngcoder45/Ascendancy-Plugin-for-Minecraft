package io.github.hyscript7.ascendancy.builtins.rituals.instant;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.data.players.names.TrueNameManager;
import io.github.hyscript7.ascendancy.features.innate.protections.InnateProtectionManager;
import io.github.hyscript7.ascendancy.features.rituals.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RitualOfSoulSending extends AbstractRitual {
    // players in this range of the ritual will get teleported
    private static final double TARGET_RADIUS = 2.5d;

    public RitualOfSoulSending() {
        super("ritual teleport innate", "Soul Sending", RitualGrade.ADVANCED, buildStages());
    }

    private static List<RitualStage> buildStages() {
        return List.of(
                new RitualStage() {
                    @Override
                    public boolean isComplete(RitualContext context) {
                        return context.getSacrificedItems().stream().anyMatch(item -> item.getType().equals(Material.NAME_TAG));
                    }

                    @Override
                    public String getHint(RitualContext context) {
                        return "Sacrifice a name tag with someone's innate name engraved on it";
                    }
                },
                new RitualStage() {
                    @Override
                    public boolean isComplete(RitualContext context) {
                        return context.getCatalyst().getType().equals(Material.END_CRYSTAL) || context.getSacrificedItems().stream().anyMatch(item -> item.getType().equals(Material.GHAST_TEAR));
                    }

                    @Override
                    public String getHint(RitualContext context) {
                        return "Sacrifice a ghast tear (or restart the ritual with an End Crystal.";
                    }
                }
        );
    }

    @Override
    public boolean catalystAppropriate(ItemStack itemStack) {
        if (!super.catalystAppropriate(itemStack)) {
            return false;
        }
        Material material = itemStack.getType();
        RitualGrade catalyst = RitualGrade.fromCatalyst(material);
        return material.equals(Material.ENDER_EYE) || material.equals(Material.END_CRYSTAL) || catalyst.greaterThan(RitualGrade.ADVANCED);
    }

    @Override
    public void onCancel(RitualContext context) {
        context.getSacrificedItems().stream().filter(item -> item.getType().equals(Material.NAME_TAG)).toList().forEach(item -> {
            context.getInvoker().give(item);
            context.getSacrificedItems().remove(item);
        });
    }

    @Override
    public boolean canPerform(RitualContext context) {
        Optional<ItemStack> nametag = context.getSacrificedItems().stream().filter(item -> item.getType().equals(Material.NAME_TAG)).findFirst();
        if (nametag.isEmpty()) {
            return false;
        }
        if (!nametag.get().getItemMeta().hasCustomName()) {
            AscendancyMessagingAPI.getInstance().send(context.getInvoker(), AscendancyMessagingAPI.MessageType.ERROR, "The nametag must be renamed to someone's innate name!\nCancel the ritual to retrieve your nametag.");
            return false;
        }
        return super.canPerform(context);
    }

    @Override
    public ActiveRitualContext perform(RitualContext context) {
        ItemStack nametag = context.getSacrificedItems().stream().filter(item -> item.getType().equals(Material.NAME_TAG)).findFirst().get();
        String innateName = nametag.getItemMeta().getDisplayName();

        if (!TrueNameManager.getInstance().isTrueNameTaken(innateName)) {
            AscendancyMessagingAPI.getInstance().send(context.getInvoker(), AscendancyMessagingAPI.MessageType.ERROR, "You invoked the ritual, but it didn't react to the burnt name.");
            return defaultInstantRitualContext(this, context);
        }

        UUID targetUuid = TrueNameManager.getInstance().findTrueNameOwner(innateName);

        Player target = Bukkit.getPlayer(targetUuid);

        if (target == null) {
            AscendancyMessagingAPI.getInstance().send(context.getInvoker(), AscendancyMessagingAPI.MessageType.ERROR, "You invoked the ritual, but it couldn't find the targeted soul.\n<dark_gray>(Target player is offline)");
            return defaultInstantRitualContext(this, context);
        }

        if (InnateProtectionManager.getInstance().isProtected(targetUuid)) {
            AscendancyMessagingAPI.getInstance().send(context.getInvoker(), AscendancyMessagingAPI.MessageType.ERROR, "You invoked the ritual, but it couldn't find the targeted soul.");
            return defaultInstantRitualContext(this, context);
        }

        final int particleCount = (int) Math.round(TARGET_RADIUS*100);
        final Location location = target.getLocation();
        context.getLocation().getNearbyPlayers(TARGET_RADIUS).forEach(player -> {
            player.teleport(location);
            player.spawnParticle(Particle.PORTAL, location, particleCount, TARGET_RADIUS, TARGET_RADIUS, TARGET_RADIUS);
            player.playSound(player, Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
            AscendancyMessagingAPI.getInstance().send(player, AscendancyMessagingAPI.MessageType.SUCCESS, "You teleported to " + target.getName() + ".");
        });
        context.getLocation().getWorld().spawnParticle(Particle.REVERSE_PORTAL, context.getLocation(), particleCount, TARGET_RADIUS, TARGET_RADIUS, TARGET_RADIUS);
        context.getLocation().getWorld().playSound(context.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.0f);
        return defaultInstantRitualContext(this, context);
    }
}
