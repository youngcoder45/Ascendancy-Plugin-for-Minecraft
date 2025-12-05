package io.github.hyscript7.ascendancy.features.magic.listeners;

import io.github.hyscript7.ascendancy.AscendancyConfig;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.Spell;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import io.github.hyscript7.ascendancy.registries.RegistryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class LootGenerator implements Listener {
    private static final Map<SpellTier, Double> SPELL_DROP_CHANCES = createSpellDropChances();

    private static final String WORLD_OVERWORLD = "world";
    private static final String WORLD_NETHER = "world_nether";
    private static final String WORLD_END = "world_the_end";

    private static final double END_EPIC_DISTANCE_THRESHOLD = 10000.0;
    private static final int DEEPSLATE_Y_LEVEL = 0;

    private static Map<SpellTier, Double> createSpellDropChances() {
        Map<SpellTier, Double> map = new EnumMap<>(SpellTier.class);
        map.put(SpellTier.COMMON, 0.25);
        map.put(SpellTier.UNCOMMON, 0.15);
        map.put(SpellTier.RARE, 0.10);
        map.put(SpellTier.EPIC, 0.05);
        return Collections.unmodifiableMap(map);
    }

    private final Map<SpellTier, List<Spell>> spellCache;
    private final List<Float> tornSpellBookCustomModelData;
    private final Random random;

    public LootGenerator() {
        this.tornSpellBookCustomModelData = AscendancyConfig.getInstance().getSpellBooks().customModelData();
        this.spellCache = new EnumMap<>(SpellTier.class);
        this.random = ThreadLocalRandom.current();

        // Initialize spell cache with lists for better random access
        RegistryManager.getInstance().getSpellRegistry().getAll().forEach(spell -> {
            spellCache.computeIfAbsent(spell.getTier(), k -> new ArrayList<>()).add(spell);
        });
    }

    @EventHandler
    public void onLootChestOpen(LootGenerateEvent event) {
        Location lootLocation = event.getLootContext().getLocation();

        World world = lootLocation.getWorld();
        if (world == null) return;

        SpellTier spellTier = determineSpellTier(world, lootLocation);
        if (spellTier == null) return;

        if (!shouldDropSpell(spellTier)) return;

        Spell spell = selectRandomSpell(spellTier);
        if (spell == null) return;

        ItemStack spellBook = createTornSpellBook(spell);
        event.getLoot().add(spellBook);

        logSpellDrop(spell, spellTier, lootLocation);
    }

    /**
     * Determines the spell tier based on world type and location
     * @param world The world of the loot chest
     * @param location The exact location of the loot chest
     * @return The spell tier
     */
    private SpellTier determineSpellTier(World world, Location location) {
        String worldName = world.getName();

        if (WORLD_END.equals(worldName)) {
            return determineEndSpellTier(location);
        } else if (WORLD_NETHER.equals(worldName)) {
            return determineNetherSpellTier();
        } else if (WORLD_OVERWORLD.equals(worldName)) {
            return determineOverworldSpellTier(location);
        } else if (VoidRealmLayer.fromWorld(world) != null) {
            return determineVoidSpellTier(location);
        } else if (worldName.contains("ginnungagap")) { // Yggdrasil compatability
            return determineYggdrasilSpellTier();
        }

        // Default for other worlds
        return SpellTier.COMMON;
    }

    private SpellTier determineYggdrasilSpellTier() {
        return random.nextBoolean() ?  SpellTier.RARE : SpellTier.EPIC;
    }

    private SpellTier determineVoidSpellTier(Location location) {
        return switch (VoidRealmLayer.fromWorld(location.getWorld())) {
            case VoidRealmLayer.ABYSS -> SpellTier.UNCOMMON;
            case VoidRealmLayer.OBLIVION -> random.nextBoolean() ? SpellTier.EPIC : SpellTier.RARE;
            case VoidRealmLayer.REFLECTION -> SpellTier.ARCANA;
        };
    }

    private SpellTier determineEndSpellTier(Location location) {
        double distanceFromOrigin = Math.sqrt(
                Math.pow(location.getX(), 2) + Math.pow(location.getZ(), 2)
        );
        return distanceFromOrigin > END_EPIC_DISTANCE_THRESHOLD
                ? SpellTier.EPIC
                : SpellTier.RARE;
    }

    private SpellTier determineNetherSpellTier() {
        return random.nextBoolean() ? SpellTier.UNCOMMON : SpellTier.COMMON;
    }

    private SpellTier determineOverworldSpellTier(Location location) {
        return location.getBlockY() < DEEPSLATE_Y_LEVEL
                ? SpellTier.UNCOMMON
                : SpellTier.COMMON;
    }

    /**
     * Uses a triangular distribution to determine if a spell should drop
     * Creates a bias toward lower values, making drops feel more random
     */
    private boolean shouldDropSpell(SpellTier tier) {
        Double dropChance = SPELL_DROP_CHANCES.get(tier);
        if (dropChance == null || dropChance <= 0) return false;

        // Triangular distribution: difference between two random values
        double dice1 = random.nextDouble();
        double dice2 = random.nextDouble();
        double distance = Math.abs(dice1 - dice2);

        return distance <= dropChance;
    }

    private Spell selectRandomSpell(SpellTier tier) {
        List<Spell> spells = spellCache.get(tier);
        if (spells == null || spells.isEmpty()) return null;

        return spells.get(random.nextInt(spells.size()));
    }

    private ItemStack createTornSpellBook(Spell spell) {
        ItemStack itemStack = ItemStack.of(Material.WRITTEN_BOOK, 1);
        BookMeta meta = (BookMeta) itemStack.getItemMeta();

        // Set book content
        meta.addPages(Component.text(spell.getIncantation()));
        meta.setAuthor("???");
        meta.setGeneration(BookMeta.Generation.TATTERED);

        // Set custom model data
        if (!tornSpellBookCustomModelData.isEmpty()) {
            meta.setCustomModelData(Math.round(tornSpellBookCustomModelData.get(0)));
        }

        // TODO: better formatting

        // Set title
        Component title = Component.text("Torn Spellbook")
                .decoration(TextDecoration.ITALIC, false);
        meta.itemName(title);

        // Add lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Read to learn the spell")
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private void logSpellDrop(Spell spell, SpellTier tier, Location location) {
        Logger logger = AscendancyPlugin.getInstance().getLogger();
        logger.info(String.format(
                "Generated %s spell '%s' at %s (%d, %d, %d)",
                tier.name(),
                spell.getIncantation(),
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        ));
    }
}