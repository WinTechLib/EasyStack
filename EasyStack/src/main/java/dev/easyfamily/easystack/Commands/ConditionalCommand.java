package dev.easyfamily.easystack.Commands;

import java.util.function.BooleanSupplier;


public class ConditionalCommand extends EasyCommandBase {
    private final EasyCommand onTrue;
    private final EasyCommand onFalse;
    private final BooleanSupplier condition;
    private EasyCommand selected;

    public ConditionalCommand(EasyCommand onTrue, EasyCommand onFalse, BooleanSupplier condition) {
        this.onTrue = onTrue;
        this.onFalse = onFalse;
        this.condition = condition;
        requirements.addAll(onTrue.getRequirements());
        requirements.addAll(onFalse.getRequirements());
    }

    @Override
    public void initialize() {
        selected = condition.getAsBoolean() ? onTrue : onFalse;
        selected.initialize();
    }

    @Override public void execute() { if (selected != null) selected.execute(); }
    @Override public void end(boolean interrupted) { if (selected != null) selected.end(interrupted); }
    @Override public boolean isFinished() { return selected == null || selected.isFinished(); }
}
