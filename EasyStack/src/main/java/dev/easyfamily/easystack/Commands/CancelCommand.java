package dev.easyfamily.easystack.Commands;

public class CancelCommand extends EasyCommandBase {
    private final EasyCommand[] targets;

    public CancelCommand(EasyCommand... targets) {
        this.targets = targets;
    }

    @Override
    public void initialize() {
        for (EasyCommand c : targets) c.cancel();
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
