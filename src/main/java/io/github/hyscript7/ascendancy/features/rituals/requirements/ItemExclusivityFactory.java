package io.github.hyscript7.ascendancy.features.rituals.requirements;

import io.github.hyscript7.ascendancy.features.rituals.RitualContext;
import io.github.hyscript7.ascendancy.features.rituals.RitualStage;
import org.bukkit.Material;

import java.util.Collections;
import java.util.Set;

/**
 * Fails if items other than the ones specified in allowedItemTypes are present,
 * otherwise passes check to the other requirement.
 */
public class ItemExclusivityFactory {
    private final Set<Material> allowedItemTypes;

    public ItemExclusivityFactory(Set<Material> allowedItemTypes) {
        this.allowedItemTypes = Collections.unmodifiableSet(allowedItemTypes);
    }

    /**
     * Makes a stage fail if other items than the ones in the allow list are present.
     * @param other The stage to run if the items are OK.
     * @return The new ritual stage
     */
    public ItemExclusivity on(RitualStage other) {
        return new ItemExclusivity(other);
    }

    public class ItemExclusivity implements RitualStage {
        private final RitualStage other;

        public ItemExclusivity(RitualStage other) {
            this.other = other;
        }

        @Override
        public boolean isComplete(RitualContext context) {
            if (context.getSacrificedItems().stream().anyMatch(item -> !allowedItemTypes.contains(item.getType()))) {
                return false;
            }
            return other.isComplete(context);
        }

        @Override
        public String getHint(RitualContext context) {
            return other.getHint(context);
        }
    }
}
