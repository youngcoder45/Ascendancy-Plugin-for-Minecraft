package io.github.hyscript7.ascendancy.features.bossprog;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public enum BossType {
    ELDER_GUARDIAN(BossFamily.OVERWORLD, BossSource.VANILLA),
    WARDEN(BossFamily.OVERWORLD, BossSource.VANILLA),
    WITHER(BossFamily.THE_NETHER, BossSource.VANILLA),
    HOVERING_INFERNO(BossFamily.THE_NETHER, BossSource.INCENDIUM),
    ENDER_DRAGON(BossFamily.THE_END, BossSource.VANILLA),
    EMPRESS_OF_LIGHT(BossFamily.THE_END, BossSource.STELLARITY);

    @Getter
    private final BossFamily family;
    @Getter
    private final BossSource source;

    BossType(BossFamily family, BossSource source) {
        this.family = family;
        this.source = source;
    }

    public static @Nullable BossType fromEntity(Entity entity) {
        if (entity.getType().equals(EntityType.BLAZE)) {
            Blaze blaze = (Blaze) entity;
            // If another mod adds a blaze with 700 HP, fuck me I guess.
            AttributeInstance aReasonToCry = blaze.getAttribute(Attribute.MAX_HEALTH);
            if (aReasonToCry != null && aReasonToCry.getBaseValue() == 700.0d) {
                return HOVERING_INFERNO;
            }
        }
        if (entity.getType().equals(EntityType.VINDICATOR)) {
            Vindicator vindicator = (Vindicator) entity;
            AttributeInstance anotherReasonToCry = vindicator.getAttribute(Attribute.MAX_HEALTH);
            if (anotherReasonToCry != null && anotherReasonToCry.getBaseValue() == 500.0d) {
                return EMPRESS_OF_LIGHT;
            }
        }
        return switch (entity.getType()) {
            case ELDER_GUARDIAN -> ELDER_GUARDIAN;
            case WARDEN -> WARDEN;
            case WITHER -> WITHER;
            case ENDER_DRAGON -> ENDER_DRAGON;
            default -> null;
        };
    }
}
