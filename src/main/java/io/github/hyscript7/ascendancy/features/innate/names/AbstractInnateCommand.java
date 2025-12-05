package io.github.hyscript7.ascendancy.features.innate.names;

public abstract class AbstractInnateCommand implements InnateCommand {
    private final String id;
    private final String displayName;

    private final boolean harmful;
    private final boolean fun;
    private final boolean targetsSelf;

    protected AbstractInnateCommand(String id, String displayName, boolean harmful, boolean fun, boolean targetsSelf) {
        this.id = id;
        this.displayName = displayName;
        this.harmful = harmful;
        this.fun = fun;
        this.targetsSelf = targetsSelf;
    }

    @Override
    public boolean canExecute(InnateContext context) {
        return true;
    }

    @Override
    public boolean isHarmful() {
        return harmful;
    }

    @Override
    public boolean isFun() {
        return fun;
    }

    @Override
    public boolean targetsSelf() {
        return targetsSelf;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
