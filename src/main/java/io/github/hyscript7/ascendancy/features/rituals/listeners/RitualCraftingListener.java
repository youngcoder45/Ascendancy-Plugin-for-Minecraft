package io.github.hyscript7.ascendancy.features.rituals.listeners;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.features.rituals.ActiveRitualContext;
import io.github.hyscript7.ascendancy.features.rituals.Ritual;
import io.github.hyscript7.ascendancy.features.rituals.RitualContext;
import io.github.hyscript7.ascendancy.features.rituals.RitualGrade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles ritual creation, sacrifice collection, and ritual circle lifecycle.
 */
public class RitualCraftingListener implements Listener {
    private static final Set<Material> CATALYST_MATERIALS = Arrays.stream(RitualGrade.values())
            .map(RitualGrade::getCatalysts)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

    private static final double MAX_SACRIFICE_DISTANCE = 2.5;

    // Maps ritual contexts to their owning player's UUID
    private final Map<RitualContext, UUID> pendingRituals;

    // Maps active ritual contexts to their owning player's UUID
    private final Map<ActiveRitualContext, UUID> activeRituals;

    // Spatial index: location -> ritual context for fast lookups
    private final Map<Location, RitualContext> ritualsByLocation;

    public RitualCraftingListener() {
        this.pendingRituals = new HashMap<>();
        this.activeRituals = new HashMap<>();
        this.ritualsByLocation = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCatalystRightClicked(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
        if (!(event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND))) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.SOUL_FIRE) return;

        Player player = event.getPlayer();

        // Check if ritual already exists at this location
        if (ritualsByLocation.containsKey(block.getLocation())) {
            RitualContext ritualContext = ritualsByLocation.get(block.getLocation());
            if (ritualContext.isRitualIdentified()) {
                if (ritualContext.isRitualReady()) {
                    if (player.isSneaking()) {
                        activateRitual(ritualContext, event.getPlayer());
                    } else {
                        AscendancyMessagingAPI.getInstance().send(event.getPlayer(), AscendancyMessagingAPI.MessageType.INFO, "Shift + Right Click to activate the ritual.");
                    }
                }
            } else {
                AscendancyMessagingAPI.getInstance().send(event.getPlayer(), AscendancyMessagingAPI.MessageType.ERROR, "There is already a ritual here!");
            }
            event.setCancelled(true);
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !CATALYST_MATERIALS.contains(item.getType())) return;

        // Consume catalyst item
        consumeItemFromHand(player, item);

        // Create ritual context
        RitualGrade grade = RitualGrade.fromCatalyst(item.getType());
        RitualContext context = RitualContext.builder()
                .invoker(player)
                .location(block.getLocation().clone())
                .catalyst(item)
                .grade(grade)
                .sacrificedItems(new ArrayList<>())
                .sacrificedEntities(new HashMap<>())
                .completedStages(new ArrayList<>())
                .build();

        // Register ritual
        pendingRituals.put(context, player.getUniqueId());
        ritualsByLocation.put(block.getLocation().clone(), context);

        event.setCancelled(true);

        playRitualCreatedEffects(context, player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSoulFireDestroyed(BlockBreakEvent event) {
        if (event.isCancelled() || event.getBlock().getType() != Material.SOUL_FIRE) return;

        Location location = event.getBlock().getLocation();

        // Clean up pending rituals
        cleanupPendingRitualsAt(location);

        // Clean up active rituals
        cleanupActiveRitualsAt(location);

        // Remove from spatial index
        ritualsByLocation.remove(location);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemSacrificedByDropping(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;

        Item itemEntity = event.getItemDrop();
        RitualContext ritual = findNearestRitual(itemEntity.getLocation(), MAX_SACRIFICE_DISTANCE);

        if (ritual == null) return;

        ItemStack droppedItem = itemEntity.getItemStack();

        // Add or merge with existing sacrificed items
        addSacrificedItem(ritual, droppedItem);

        itemEntity.remove();

        playItemSacrificedEffects(ritual, event.getPlayer());

        if (ritual.isRitualIdentified() && ritual.isRitualReady()) {
            handleRitualPrepared(ritual, event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntitySacrificed(EntityDeathEvent event) {
        if (event.isCancelled()) return;

        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null && !(entity instanceof Player)) return;
        else if (killer == null) killer = (Player) entity;

        RitualContext ritual = findNearestRitual(entity.getLocation(), MAX_SACRIFICE_DISTANCE);

        if (ritual == null) return;

        // Increment sacrificed entity count
        ritual.getSacrificedEntities().merge(entity.getType(), 1, Integer::sum);

        // Consume all drops and experience
        event.getDrops().clear();
        event.setDroppedExp(0);

        playEntitySacrificedEffects(ritual, killer);

        if (ritual.isRitualIdentified() && ritual.isRitualReady()) {
            handleRitualPrepared(ritual, killer);
        }
    }

    /**
     * Finds the nearest ritual to a location within a maximum distance.
     * <p>
     * Runs in O(n), other checks should be done before calling this method.
     *
     * @param location    The location to search for
     * @param maxDistance The maximum allowed distance to match to a ritual
     */
    private RitualContext findNearestRitual(Location location, double maxDistance) {
        if (ritualsByLocation.isEmpty()) return null;

        RitualContext nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Map.Entry<Location, RitualContext> entry : ritualsByLocation.entrySet()) {
            Location ritualLocation = entry.getKey();

            // Must be in same world
            if (!ritualLocation.getWorld().equals(location.getWorld())) continue;

            double distance = ritualLocation.distance(location);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = entry.getValue();
            }
        }

        return nearestDistance <= maxDistance ? nearest : null;
    }

    /**
     * Adds an item to ritual's sacrificed items, merging with existing stacks of same type.
     *
     * @param ritual The ritual context
     * @param item   The item stack that was sacrificed
     */
    private void addSacrificedItem(RitualContext ritual, ItemStack item) {
        ritual.getSacrificedItems().stream()
                .filter(stack -> stack.getType().equals(item.getType()))
                .findFirst()
                .ifPresentOrElse(
                        existingStack -> existingStack.setAmount(existingStack.getAmount() + item.getAmount()),
                        () -> ritual.getSacrificedItems().add(item.clone())
                );
    }

    /**
     * A utility method which consumes one item from player's hand.
     * Purely here to handle the potential edge case of 0-amount items.
     *
     * @param player The player
     * @param item   The item stack to yoink
     */
    private void consumeItemFromHand(Player player, ItemStack item) {
        if (item.getAmount() - 1 > 0) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }
    }

    /**
     * Cleans up all pending rituals (contexts) at the given location.
     *
     * @param location The location of the ritual flame
     */
    private void cleanupPendingRitualsAt(Location location) {
        pendingRituals.keySet().stream()
                .filter(ritual -> ritual.getLocation().equals(location))
                .toList() // Collect to avoid ConcurrentModificationException
                .forEach(ritual -> {
                    pendingRituals.remove(ritual);
                    if (!ritual.isRitualReady()) {
                        // If the ritual was identified, let it know it got cancelled
                        // If the ritual ran already, isRitualReady **should** be true, meaning this won't run.
                        if (ritual.isRitualIdentified()) {
                            ritual.getIdentifiedRitual().onCancel(ritual);
                        }
                        // Also since isRitualReady is false, it means the ritual couldn't have run, and isn't active,
                        // So we announce the ritual being cancelled to the owner.
                        playRitualBrokenEffects(ritual, ritual.getInvoker());
                    }
                });
    }

    /**
     * Cleans up all active rituals at the given location.
     *
     * @param location The location of the ritual flame
     */
    private void cleanupActiveRitualsAt(Location location) {
        activeRituals.keySet().stream()
                .filter(ritual -> ritual.location().equals(location))
                .toList() // Collect to avoid ConcurrentModificationException
                .forEach(ritual -> {
                    if (ritual.isLasting()) {
                        ritual.cancel();
                        playActiveRitualBrokenEffects(ritual, ritual.player());
                    }
                    activeRituals.remove(ritual);
                });
    }

    /**
     * @param context The ritual context
     * @param actor The responsible player (or null for ritual owner)
     */
    private void handleRitualPrepared(RitualContext context, @Nullable Player actor) {
        if (!context.isRitualIdentified()) throw new IllegalStateException("Called on an unidentified ritual");
        if (!context.isRitualReady()) throw new IllegalStateException("Called on an unfinished ritual");
        if (actor == null) actor = context.getInvoker();
        String ritualName = context.getIdentifiedRitual().getDisplayName();
        String message = "The <bold>" + ritualName + "</bold> ritual is ready!" + "\nShift + Right Click to activate the ritual." + "\n<red>If you cancel the ritual now, you might not get your items back!";
        AscendancyMessagingAPI.getInstance().sendBoxed(actor, AscendancyMessagingAPI.MessageType.INFO, "Ritual", null, message);
    }

    private void activateRitual(RitualContext context, @NotNull Player actor) {
        Ritual ritual = context.getIdentifiedRitual();
        if (ritual == null) throw new IllegalStateException("Ritual not identified");
        cleanupPendingRitualsAt(context.getLocation());
        ActiveRitualContext activeContext = ritual.perform(context, this::cleanupActiveRitualsAt);
        if (activeContext.isLasting()) {
            activeRituals.put(activeContext, activeContext.player().getUniqueId());
        } else {
            // TODO: Seems a bit funky but I am to tired to do this properly.
            //       Check whether this can be removed right away while still supporting breaking active rituals...
            //       ... or if we should store active rituals in their own location index map,
            //       or maybe use an either  container?
            ritualsByLocation.remove(context.getLocation());
            Block block = context.getLocation().getWorld().getBlockAt(context.getLocation());
            if (block.getType().equals(Material.SOUL_FIRE)) {
                block.setType(Material.AIR);
                playRitualExtinguished(context);
            }
        }
    }

    private void playRitualExtinguished(RitualContext context) {
        Location location = context.getLocation();
        World world = location.getWorld();
        world.playSound(location, Sound.BLOCK_FIRE_EXTINGUISH,  SoundCategory.BLOCKS, 0.5f, 1); // 👻 Boo! Magic numbers.
    }

    private void playRitualCreatedEffects(RitualContext ritual, Player actor) {
        actor.sendMessage(
                Component.text("Ritual started with grade ", NamedTextColor.AQUA)
                        .append(Component.text(ritual.getGrade().name(), NamedTextColor.BLUE)
                                .style(Style.style(TextDecoration.BOLD)))
        );
    }

    private void playRitualBrokenEffects(RitualContext ritual, Player actor) {
        actor.sendMessage(
                Component.text("Ritual circle at ", NamedTextColor.DARK_GRAY)
                        .append(Component.text(ritual.getLocation().getBlockX() + " " + ritual.getLocation().getBlockY() + " " + ritual.getLocation().getBlockZ(), NamedTextColor.RED)
                                .style(Style.style(TextDecoration.BOLD)))
                        .append(Component.text(" has been broken!", NamedTextColor.DARK_GRAY))
        );
    }

    private void playActiveRitualBrokenEffects(ActiveRitualContext ritual, Player actor) {
        actor.sendMessage(
                Component.text("Running ritual at ", NamedTextColor.DARK_GRAY)
                        .append(Component.text(ritual.location().getBlockX() + " " + ritual.location().getBlockY() + " " + ritual.location().getBlockZ(), NamedTextColor.RED)
                                .style(Style.style(TextDecoration.BOLD)))
                        .append(Component.text(" has been broken!", NamedTextColor.DARK_GRAY))
        );
    }

    private void playItemSacrificedEffects(RitualContext ritual, Player actor) {
        Component message = Component.text("Sacrificed Items:\n", NamedTextColor.AQUA);

        for (ItemStack item : ritual.getSacrificedItems()) {
            message = message.append(
                    Component.text("  " + item.getType().name(), NamedTextColor.DARK_AQUA)
                            .append(Component.text(": ", NamedTextColor.GRAY))
                            .append(Component.text(item.getAmount(), NamedTextColor.AQUA))
                            .append(Component.text("\n"))
            );
        }

        actor.sendMessage(message);
    }

    private void playEntitySacrificedEffects(RitualContext ritual, Player actor) {
        Component message = Component.text("Sacrificed Mobs:\n", NamedTextColor.AQUA);

        for (Map.Entry<org.bukkit.entity.EntityType, Integer> entry : ritual.getSacrificedEntities().entrySet()) {
            message = message.append(
                    Component.text("  " + entry.getKey().name(), NamedTextColor.DARK_AQUA)
                            .append(Component.text(": ", NamedTextColor.GRAY))
                            .append(Component.text(entry.getValue(), NamedTextColor.AQUA))
                            .append(Component.text("\n"))
            );
        }

        actor.sendMessage(message);
    }
}
