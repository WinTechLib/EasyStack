package dev.easyfamily.easystack.Commands;

import java.util.function.BooleanSupplier;

/** Repete o comando até a condição ficar verdadeira. */
public class RepeatUntilCommand extends EasyCommandBase {
    private final EasyCommand inner;
    private final BooleanSupplier condition;

    public RepeatUntilCommand(EasyCommand inner, BooleanSupplier condition) {
        this.inner = inner;
        this.condition = condition;
        requirements.addAll(inner.getRequirements());
    }

    @Override
    public void initialize() {
        inner.initialize();
    }

    @Override
    public void execute() {
        inner.execute();
        if (inner.isFinished()) {
            inner.end(false);
            inner.initialize();
        }
    }

    @Override
    public void end(boolean interrupted) {
        inner.end(true);
    }

    @Override
    public boolean isFinished() {
        return condition.getAsBoolean();
    }
}
