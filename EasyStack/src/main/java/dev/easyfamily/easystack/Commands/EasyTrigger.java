package dev.easyfamily.easystack.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/** Condição (botão, sensor...) que agenda/cancela comandos nas bordas de subida/descida. */
public class EasyTrigger {
    private final BooleanSupplier condition;
    private final List<Runnable> onRising = new ArrayList<>();
    private final List<Runnable> onFalling = new ArrayList<>();
    private boolean last;
    private boolean initialized = false;

    public EasyTrigger(BooleanSupplier condition) {
        this.condition = condition;
        EasyCommandScheduler.getInstance().addTrigger(this);
    }

    void poll() {
        boolean now = condition.getAsBoolean();
        if (initialized) {
            if (now && !last) for (Runnable r : new ArrayList<>(onRising)) r.run();
            if (!now && last) for (Runnable r : new ArrayList<>(onFalling)) r.run();
        }
        last = now;
        initialized = true;
    }

    public boolean get() { return condition.getAsBoolean(); }

    /** Agenda o comando quando a condição vira true. */
    public EasyTrigger onTrue(EasyCommand command) {
        onRising.add(command::schedule);
        return this;
    }

    /** Agenda o comando quando a condição vira false. */
    public EasyTrigger onFalse(EasyCommand command) {
        onFalling.add(command::schedule);
        return this;
    }

    /** Roda enquanto a condição for true (cancela ao soltar). */
    public EasyTrigger whileTrue(EasyCommand command) {
        onRising.add(command::schedule);
        onFalling.add(command::cancel);
        return this;
    }

    /** Roda enquanto a condição for false. */
    public EasyTrigger whileFalse(EasyCommand command) {
        onFalling.add(command::schedule);
        onRising.add(command::cancel);
        return this;
    }

    /** Cada vez que vira true: agenda se parado, cancela se rodando. */
    public EasyTrigger toggleOnTrue(EasyCommand command) {
        onRising.add(() -> {
            if (command.isScheduled()) command.cancel(); else command.schedule();
        });
        return this;
    }

    public EasyTrigger onTrueRun(Runnable action) {
        onRising.add(action);
        return this;
    }

    public EasyTrigger onFalseRun(Runnable action) {
        onFalling.add(action);
        return this;
    }

    public EasyTrigger and(BooleanSupplier other) {
        return new EasyTrigger(() -> condition.getAsBoolean() && other.getAsBoolean());
    }

    public EasyTrigger or(BooleanSupplier other) {
        return new EasyTrigger(() -> condition.getAsBoolean() || other.getAsBoolean());
    }

    public EasyTrigger negate() {
        return new EasyTrigger(() -> !condition.getAsBoolean());
    }

    /** Só vira true depois de ficar true continuamente por X ms. */
    public EasyTrigger debounce(long millis) {
        final long[] since = {-1};
        return new EasyTrigger(() -> {
            if (!condition.getAsBoolean()) {
                since[0] = -1;
                return false;
            }
            long now = System.nanoTime();
            if (since[0] < 0) since[0] = now;
            return (now - since[0]) / 1_000_000L >= millis;
        });
    }
}
