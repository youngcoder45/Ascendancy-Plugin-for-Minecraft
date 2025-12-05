package io.github.hyscript7.ascendancy.registries;

import eu.projnull.spelis.ascendancy.rituals.RitualOfTheAnchor;
import eu.projnull.spelis.ascendancy.spells.Recall;
import io.github.hyscript7.ascendancy.AlreadyInitializedException;
import io.github.hyscript7.ascendancy.builtins.innate.commands.other.*;
import io.github.hyscript7.ascendancy.builtins.innate.commands.self.GetOwnLives;
import io.github.hyscript7.ascendancy.builtins.magic.arcanas.VoidWalk;
import io.github.hyscript7.ascendancy.builtins.magic.spells.FireballSpell;
import io.github.hyscript7.ascendancy.builtins.magic.spells.Vonszol;
import io.github.hyscript7.ascendancy.builtins.threefolds.existences.ThePrimordialAbyss;
import io.github.hyscript7.ascendancy.builtins.threefolds.rituals.AbyssInnateProtect;
import io.github.hyscript7.ascendancy.builtins.threefolds.rituals.AbyssLayerAscend;
import io.github.hyscript7.ascendancy.builtins.threefolds.rituals.AbyssLayerDescend;
import io.github.hyscript7.ascendancy.features.threefold.ThreefoldAudience;
import io.github.hyscript7.ascendancy.features.threefold.ThreefoldIncantation;
import io.github.youngcoder45.ascendancy.spells.Fulmen;
import io.github.youngcoder45.ascendancy.spells.Sanatio;
import io.github.youngcoder45.ascendancy.spells.Ventus;
import io.github.youngcoder45.ascendancy.spells.Aeroburst;
import io.github.youngcoder45.ascendancy.spells.Expelliarmus;
import io.github.hyscript7.ascendancy.builtins.rituals.instant.RitualOfDeathReturn;
import io.github.hyscript7.ascendancy.builtins.rituals.instant.RitualOfHomeComing;
import io.github.hyscript7.ascendancy.builtins.rituals.instant.RitualOfSoulSending;
import io.github.hyscript7.ascendancy.builtins.rituals.lasting.RitualOfVoidRevealing;
import io.github.hyscript7.ascendancy.features.innate.names.InnateCommand;
import io.github.hyscript7.ascendancy.builtins.innate.commands.self.GetOwnName;
import io.github.hyscript7.ascendancy.features.magic.Spell;
import io.github.hyscript7.ascendancy.features.rituals.Ritual;
import io.github.hyscript7.ascendancy.builtins.rituals.instant.RitualOfLevitation;
import io.github.youngcoder45.ascendancy.rituals.RitualOfSunshine;
import io.github.youngcoder45.ascendancy.rituals.RitualOfTheWolf;
import lombok.Getter;

/**
 * A singleton for managing and initializing all registries in the plugin.
 */
@Getter
public class RegistryManager {
    private static RegistryManager instance;

    private final Registry<InnateCommand> innateCommandRegistry;
    private final Registry<Ritual> ritualRegistry;
    private final Registry<Spell> spellRegistry;
    private final Registry<ThreefoldAudience> threefoldAudienceRegistry;
    private final Registry<ThreefoldIncantation> threefoldIncantationRegistry;

    private boolean initialized;

    private RegistryManager() {
        this.initialized = false;
        this.innateCommandRegistry = new Registry<>("InnateCommands");
        this.ritualRegistry = new Registry<>("Rituals");
        this.spellRegistry= new Registry<>("Spells");
        this.threefoldAudienceRegistry = new Registry<>("ThreefoldAudiences");
        this.threefoldIncantationRegistry = new Registry<>("ThreefoldIncantations");
    }

    public static RegistryManager getInstance() {
        if (instance == null) {
            instance = new RegistryManager();
        }
        return instance;
    }

    public void initialize() {
        if (initialized) {
            throw new AlreadyInitializedException("The RegistryManager has already been initialized!");
        }

        // Add all instances
        registerInnateCommands();
        registerRituals();
        registerSpells();
        registerThreefoldAudiences();
        registerThreefoldIncantations();

        // Lock all registries

        initialized = true;
    }

    private void registerInnateCommands() {
        innateCommandRegistry.register(new GetOwnName());
        innateCommandRegistry.register(new GetOwnLives());
        innateCommandRegistry.register(new BestowItem());
        innateCommandRegistry.register(new BestowHealth());
        innateCommandRegistry.register(new BestowLive());
        innateCommandRegistry.register(new Die());
        innateCommandRegistry.register(new DropHandItem());
        innateCommandRegistry.register(new Surrender());
        innateCommandRegistry.register(new CloseYourEyes());
        // TODO: Add commands
    }

    private void registerRituals() {
        ritualRegistry.register(new RitualOfLevitation());
        ritualRegistry.register(new RitualOfHomeComing());
        ritualRegistry.register(new RitualOfDeathReturn());
        ritualRegistry.register(new RitualOfSoulSending());
        ritualRegistry.register(new RitualOfVoidRevealing());
        ritualRegistry.register(new RitualOfSunshine());
        ritualRegistry.register(new RitualOfTheWolf());
        ritualRegistry.register(new RitualOfTheAnchor());
        // TODO: Add rituals
    }

    private void registerSpells() {
        spellRegistry.register(new FireballSpell());
        spellRegistry.register(new VoidWalk());
        spellRegistry.register(new Fulmen());
        spellRegistry.register(new Ventus());
        spellRegistry.register(new Sanatio());
        spellRegistry.register(new Vonszol());
        spellRegistry.register(new Recall());
        spellRegistry.register(new Aeroburst());
        spellRegistry.register(new Expelliarmus());
        // TODO: Add spells
    }

    private void registerThreefoldAudiences() {
        threefoldAudienceRegistry.register(new ThePrimordialAbyss());
        // TODO: Register threefold audiences
    }

    private void registerThreefoldIncantations() {
        // TODO: Register threefold incantations
        threefoldIncantationRegistry.register(new AbyssLayerDescend());
        threefoldIncantationRegistry.register(new AbyssLayerAscend());
        threefoldIncantationRegistry.register(new AbyssInnateProtect());
    }
}
