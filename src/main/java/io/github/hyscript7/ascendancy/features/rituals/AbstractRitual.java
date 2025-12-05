package io.github.hyscript7.ascendancy.features.rituals;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractRitual implements Ritual {
    @Getter
    private final String id;
    @Getter
    private final String displayName;

    @Getter
    private final RitualGrade grade;

    private final List<RitualStage> orderedStages;

    protected AbstractRitual(String id, String displayName, RitualGrade grade, List<RitualStage> orderedStages) {
        this.id = id;
        this.displayName = displayName;
        this.grade = grade;
        this.orderedStages = Collections.unmodifiableList(orderedStages);
    }

    @Override
    public List<RitualStage> getStages() {
        return orderedStages;
    }

    @Override
    public RitualStage getCurrentStage(RitualContext context) {
        for (RitualStage stage : orderedStages) {
            if (!stage.isComplete(context)) {
                return stage;
            }
        }
        return orderedStages.getFirst();
    }

    @Override
    public boolean canPerform(RitualContext context) {
        for (RitualStage stage : orderedStages) {
            if (!stage.isComplete(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ActiveRitualContext perform(RitualContext context, Consumer<Location> onSelfCancel) {
        return perform(context);
    }

    public ActiveRitualContext perform(RitualContext context) {
        return defaultInstantRitualContext(this, context);
    }

    @Override
    public boolean catalystAppropriate(ItemStack itemStack) {
        RitualGrade otherGrade = RitualGrade.fromCatalyst(itemStack.getType());
        if (otherGrade == null) return false;
        return otherGrade.compareTo(grade) >= 0;
    }

    public static ActiveRitualContext defaultInstantRitualContext(Ritual ritual, RitualContext context) {
        return new ActiveRitualContext(context.getInvoker(), context.getLocation(), ritual, Optional.empty());
    }
}
