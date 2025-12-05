package io.github.hyscript7.ascendancy.features.innate.protections.impl;

import io.github.hyscript7.ascendancy.features.innate.protections.InnateProtection;

import java.util.UUID;

public class TimedProtection implements InnateProtection {
    private final UUID target;
    private final long duration;
    private final long startTime;

    public TimedProtection(long duration, UUID target) {
        this.duration = duration;
        this.target = target;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public UUID getPlayerUuid() {
        return target;
    }

    @Override
    public boolean isActive() {
        return System.currentTimeMillis() - startTime <= duration;
    }
}
