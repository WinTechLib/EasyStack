package dev.easyfamily.easystack.Commands;

import java.util.function.Consumer;

public class FinallyCommand extends WrapperCommand {
    private final Consumer<Boolean> action;

    public FinallyCommand(EasyCommand inner, Consumer<Boolean> action) {
        super(inner);
        this.action = action;
    }

    @Override
    public void end(boolean interrupted) {
        inner.end(interrupted);
        action.accept(interrupted);
    }
}
