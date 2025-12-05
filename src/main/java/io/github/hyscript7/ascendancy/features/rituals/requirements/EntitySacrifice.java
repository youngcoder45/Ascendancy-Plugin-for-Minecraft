package io.github.hyscript7.ascendancy.features.rituals.requirements;

import io.github.hyscript7.ascendancy.features.rituals.RitualContext;
import io.github.hyscript7.ascendancy.features.rituals.RitualStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * Checks if an entity has been sacrificed in the ritual.
 */
@AllArgsConstructor
@Builder
public class EntitySacrifice implements RitualStage {
    private final EntityType entityType;
    @Builder.Default
    private final int minAmount = 1;
    @Builder.Default
    private final int maxAmount = Integer.MAX_VALUE;
    @Builder.Default
    private final @Nullable RitualStage fallback = null;

    @Override
    public boolean isComplete(RitualContext context) {
        int amount = context.getSacrificedEntities().getOrDefault(entityType, 0);
        if (amount >= minAmount && amount <= maxAmount) {
            return true;
        } else if (fallback != null) {
            return fallback.isComplete(context);
        }
        return false;
    }

    @Override
    public String getHint(RitualContext context) {
        StringBuilder hintBuilder = new StringBuilder("Sacrifice ");
        if (minAmount == 1 && maxAmount == Integer.MAX_VALUE) {
            hintBuilder.append("a ");
        } else {
            hintBuilder.append("at least " + minAmount + " ");
        }
        if (maxAmount != Integer.MAX_VALUE) {
            hintBuilder.append("and at most " + maxAmount + " ");
        }
        if (minAmount != 1 || maxAmount != Integer.MAX_VALUE) {
            hintBuilder.append("of ");
        }
        hintBuilder.append(entityType.name().replace("_", " ").toLowerCase());
        if (fallback != null) {
            hintBuilder.append(" or ").append(fallback.getHint(context));
        }
        return hintBuilder.toString();
    }
}
