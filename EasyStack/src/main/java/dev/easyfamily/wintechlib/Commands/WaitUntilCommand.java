package dev.easyfamily.wintechlib.Commands;

import java.util.function.BooleanSupplier;

public class WaitUntilCommand extends EasyCommandBase {
    private final BooleanSupplier condition;

    public WaitUntilCommand(BooleanSupplier condition) {
        this.condition = condition;
    }

    @Override
    public boolean isFinished() {
        return condition.getAsBoolean();
    }
}
