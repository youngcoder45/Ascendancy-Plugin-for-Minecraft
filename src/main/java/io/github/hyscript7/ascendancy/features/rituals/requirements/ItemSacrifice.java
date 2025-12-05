package io.github.hyscript7.ascendancy.features.rituals.requirements;

import io.github.hyscript7.ascendancy.features.rituals.RitualContext;
import io.github.hyscript7.ascendancy.features.rituals.RitualStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Checks if an item has been sacrificed in the ritual.
 */
@AllArgsConstructor
@Builder
public class ItemSacrifice implements RitualStage {
    private final Material itemType;
    @Builder.Default
    private final int minAmount = 1;
    @Builder.Default
    private final int maxAmount = Integer.MAX_VALUE;
    @Builder.Default
    private final @Nullable Predicate<ItemMeta> metaPredicate = null;
    @Builder.Default
    private final @Nullable RitualStage fallback = null;

    @Override
    public boolean isComplete(RitualContext context) {
        Optional<ItemStack> stack = context.getSacrificedItems().stream().filter(meta -> meta.getType() == itemType).findFirst();
        if (stack.isEmpty() && fallback == null) return false;
        else if (stack.isEmpty() && fallback != null) {
            return fallback.isComplete(context);
        }
        if (metaPredicate != null && !metaPredicate.test(stack.get().getItemMeta())) return false;
        if (stack.get().getAmount() >= minAmount && stack.get().getAmount() <= maxAmount) {
            return true;
        } else if (fallback != null) {
            return fallback.isComplete(context);
        }
        return false;
    }

    @Override
    public String getHint(RitualContext context) {
        // Lord forgive me for I have sinned
        StringBuilder hintBuilder = new StringBuilder("Sacrifice ");
        if (minAmount == 1 && maxAmount == Integer.MAX_VALUE) {
            hintBuilder.append("a ");
        } else {
            if (maxAmount == minAmount) {
                hintBuilder.append(" exactly " + minAmount + " ");
            } else {
                hintBuilder.append("at least " + minAmount + " ");
                if (maxAmount != Integer.MAX_VALUE) {
                    hintBuilder.append("and at most " + maxAmount + " ");
                }
            }
            hintBuilder.append("of ");
        }
        hintBuilder.append(itemType.name().replace("_", " ").toLowerCase());
        if (fallback != null) {
            hintBuilder.append(" or ").append(fallback.getHint(context));
        }
        return hintBuilder.toString();
    }
}
