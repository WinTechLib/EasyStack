package dev.easyfamily.EasyStack.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import dev.easyfamily.EasyStack.subsytem.EasySubsystem;

/** A polled condition that can schedule/cancel commands on its rising/falling edges. */
public class EasyTrigger {

    private final BooleanSupplier condition;
    private final List<Runnable> onRising = new ArrayList<>();
    private final List<Runnable> onFalling = new ArrayList<>();

    private boolean last;
    private boolean initialized = false;

    public EasyTrigger(BooleanSupplier condition) {
        this.condition = Objects.requireNonNull(condition, "condition");
        EasyCommandScheduler.getInstance().addTrigger(this);
    }

    void poll() {
        boolean now = condition.getAsBoolean();

        if (initialized) {
            if (now && !last) {
                for (Runnable action : new ArrayList<>(onRising)) {
                    action.run();
                }
            }

            if (!now && last) {
                for (Runnable action : new ArrayList<>(onFalling)) {
                    action.run();
                }
            }
        }

        last = now;
        initialized = true;
    }

    public boolean get() {
        return condition.getAsBoolean();
    }

    // ------------------------------------------------------------
    // Basic command bindings
    // ------------------------------------------------------------

    public EasyTrigger onTrue(EasyCommand command) {
        Objects.requireNonNull(command, "command");
        onRising.add(command::schedule);
        return this;
    }

    public EasyTrigger onFalse(EasyCommand command) {
        Objects.requireNonNull(command, "command");
        onFalling.add(command::schedule);
        return this;
    }

    public EasyTrigger whileTrue(EasyCommand command) {
        Objects.requireNonNull(command, "command");
        onRising.add(command::schedule);
        onFalling.add(command::cancel);
        return this;
    }

    public EasyTrigger whileFalse(EasyCommand command) {
        Objects.requireNonNull(command, "command");
        onFalling.add(command::schedule);
        onRising.add(command::cancel);
        return this;
    }

    public EasyTrigger toggleOnTrue(EasyCommand command) {
        Objects.requireNonNull(command, "command");
        onRising.add(() -> {
            if (command.isScheduled()) {
                command.cancel();
            } else {
                command.schedule();
            }
        });
        return this;
    }

    // ------------------------------------------------------------
    // Fluent bindings
    // ------------------------------------------------------------

    /** Schedule a command on the press edge. */
    public EasyTrigger onPress(EasyCommand command) {
        return onTrue(command);
    }

    /** Schedule a one-shot action on press, reserving the supplied subsystems. */
    public EasyTrigger onPress(
            Runnable action,
            EasySubsystem... requirements
    ) {
        return onTrue(new InstantCommand(action, requirements));
    }

    /** Schedule a command on the release edge. */
    public EasyTrigger onRelease(EasyCommand command) {
        return onFalse(command);
    }

    /** Schedule a one-shot action on release, reserving the supplied subsystems. */
    public EasyTrigger onRelease(
            Runnable action,
            EasySubsystem... requirements
    ) {
        return onFalse(new InstantCommand(action, requirements));
    }

    /** Schedule once on press and cancel when released. */
    public EasyTrigger whileHeld(EasyCommand command) {
        return whileTrue(command);
    }

    /** Run an action continuously while held. */
    public EasyTrigger whileHeld(
            Runnable action,
            EasySubsystem... requirements
    ) {
        return whileTrue(new RunCommand(action, requirements));
    }

    /** Schedule while the condition is false. */
    public EasyTrigger whileNotHeld(EasyCommand command) {
        return whileFalse(command);
    }

    /** Toggle a command each time the button is pressed. */
    public EasyTrigger toggleOnPress(EasyCommand command) {
        return toggleOnTrue(command);
    }

    public EasyTrigger onTrueRun(Runnable action) {
        onRising.add(Objects.requireNonNull(action, "action"));
        return this;
    }

    public EasyTrigger onFalseRun(Runnable action) {
        onFalling.add(Objects.requireNonNull(action, "action"));
        return this;
    }

    // ------------------------------------------------------------
    // Trigger composition
    // ------------------------------------------------------------

    public EasyTrigger and(BooleanSupplier other) {
        Objects.requireNonNull(other, "other");
        return new EasyTrigger(() -> condition.getAsBoolean() && other.getAsBoolean());
    }

    public EasyTrigger or(BooleanSupplier other) {
        Objects.requireNonNull(other, "other");
        return new EasyTrigger(() -> condition.getAsBoolean() || other.getAsBoolean());
    }

    public EasyTrigger negate() {
        return new EasyTrigger(() -> !condition.getAsBoolean());
    }

    /** Only becomes true after the source condition remains true for millis. */
    public EasyTrigger debounce(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Debounce time cannot be negative.");
        }

        final long[] since = {-1L};
        return new EasyTrigger(() -> {
            if (!condition.getAsBoolean()) {
                since[0] = -1L;
                return false;
            }

            long now = System.nanoTime();
            if (since[0] < 0L) {
                since[0] = now;
            }

            return (now - since[0]) / 1_000_000L >= millis;
        });
    }
}
