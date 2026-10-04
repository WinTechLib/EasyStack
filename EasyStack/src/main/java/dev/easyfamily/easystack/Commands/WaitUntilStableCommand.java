package dev.easyfamily.easystack.Commands;

import java.util.function.BooleanSupplier;

/** Termina quando a condição ficar verdadeira de forma contínua por stableMillis (debounce). */
public class WaitUntilStableCommand extends EasyCommandBase {
    private final BooleanSupplier condition;
    private final long stableMillis;
    private long trueSinceNanos = -1;

    public WaitUntilStableCommand(BooleanSupplier condition, long stableMillis) {
        this.condition = condition;
        this.stableMillis = stableMillis;
    }

    @Override
    public void initialize() {
        trueSinceNanos = -1;
    }

    @Override
    public boolean isFinished() {
        if (!condition.getAsBoolean()) {
            trueSinceNanos = -1;
            return false;
        }
        long now = System.nanoTime();
        if (trueSinceNanos < 0) trueSinceNanos = now;
        return (now - trueSinceNanos) / 1_000_000L >= stableMillis;
    }
}
