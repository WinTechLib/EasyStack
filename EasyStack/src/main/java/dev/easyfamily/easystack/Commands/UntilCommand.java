package dev.easyfamily.easystack.Commands;

import java.util.function.BooleanSupplier;

public class UntilCommand extends WrapperCommand {
    private final BooleanSupplier condition;

    public UntilCommand(EasyCommand inner, BooleanSupplier condition) {
        super(inner);
        this.condition = condition;
    }

    @Override
    public boolean isFinished() {
        return inner.isFinished() || condition.getAsBoolean();
    }

    @Override
    public void end(boolean interrupted) {
        inner.end(interrupted || !inner.isFinished());
    }
}
