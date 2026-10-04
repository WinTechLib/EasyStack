package dev.easyfamily.easystack.subsytem;

import dev.easyfamily.easystack.Commands.EasyCommand;
import dev.easyfamily.easystack.Commands.InstantCommand;

public abstract class EasyStateSubsystem<S extends Enum<S>> extends EasySubsystemBase {
    private S state;
    private S previous;

    protected EasyStateSubsystem(S initial) {
        this.state = initial;
        this.previous = initial;
    }

    public S getState() { return state; }

    public S getPreviousState() { return previous; }

    public boolean isIn(S s) { return state == s; }

    public void setState(S next) {
        if (next == state) return;
        previous = state;
        state = next;
        onStateChanged(previous, state);
    }
    protected void onStateChanged(S from, S to) {}

    public EasyCommand setStateCommand(S next) {
        return new InstantCommand(() -> setState(next), this);
    }
}
