package dev.easyfamily.EasyStack.Commands;

import dev.easyfamily.EasyStack.subsytem.EasySubsystem;

/** Executa a ação a cada loop até ser cancelado. */
public class RunCommand extends EasyCommandBase {
    private final Runnable action;

    public RunCommand(Runnable action, EasySubsystem... subsystems) {
        this.action = action;
        addRequirements(subsystems);
    }

    @Override
    public void execute() {
        action.run();
    }
}
