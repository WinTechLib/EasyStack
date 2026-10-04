package dev.easyfamily.easystack.Commands;

public class InstantCommand extends EasyCommandBase {
    private final Runnable action;

    public InstantCommand(Runnable action, dev.easyfamily.easystack.subsytem.EasySubsystem... subsystems) {
        this.action = action;
        addRequirements(subsystems);
    }

    public InstantCommand() {
        this(() -> {});
    }

    @Override
    public void initialize() {
        action.run();
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
