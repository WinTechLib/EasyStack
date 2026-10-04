package dev.easyfamily.easystack.Commands;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import dev.easyfamily.easystack.subsytem.EasySubsystem;

public class FunctionalCommand extends EasyCommandBase {
    private final Runnable onInit;
    private final Runnable onExecute;
    private final Consumer<Boolean> onEnd;
    private final BooleanSupplier finished;

    public FunctionalCommand(Runnable onInit, Runnable onExecute, Consumer<Boolean> onEnd,
                             BooleanSupplier isFinished, EasySubsystem... subsystems) {
        this.onInit = onInit;
        this.onExecute = onExecute;
        this.onEnd = onEnd;
        this.finished = isFinished;
        addRequirements(subsystems);
    }

    @Override public void initialize() { onInit.run(); }
    @Override public void execute() { onExecute.run(); }
    @Override public void end(boolean interrupted) { onEnd.accept(interrupted); }
    @Override public boolean isFinished() { return finished.getAsBoolean(); }
}
