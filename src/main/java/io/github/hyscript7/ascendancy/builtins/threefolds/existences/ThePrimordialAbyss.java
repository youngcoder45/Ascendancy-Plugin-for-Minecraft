package io.github.hyscript7.ascendancy.builtins.threefolds.existences;

import io.github.hyscript7.ascendancy.features.threefold.ThreefoldAudience;
import io.github.hyscript7.ascendancy.features.threefold.ThreefoldAudiencePowerLevel;

public class ThePrimordialAbyss extends ThreefoldAudience {
    public static final String id = "primordial abyss";
    public ThePrimordialAbyss() {
        super(id, "The Primordial Abyss", new String[]{
                "The Abyss that transcends the realms",
                "You are the darkness deep beneath this world",
                "The bane and source of all life and creation"
        }, ThreefoldAudiencePowerLevel.PRIMORDIAL);
    }
}
