package dev.easyfamily.easystack.Commands;

public class SequentialCommandGroup extends CommandGroupBase {
    private int index = -1;

    public SequentialCommandGroup(EasyCommand... cmds) {
        register(false, cmds);
    }

    @Override
    public void initialize() {
        index = 0;
        if (!commands.isEmpty()) commands.get(0).initialize();
    }

    @Override
    public void execute() {
        if (index < 0 || index >= commands.size()) return;
        EasyCommand current = commands.get(index);
        current.execute();
        if (current.isFinished()) {
            current.end(false);
            index++;
            if (index < commands.size()) commands.get(index).initialize();
        }
    }

    @Override
    public void end(boolean interrupted) {
        if (interrupted && index >= 0 && index < commands.size()) {
            commands.get(index).end(true);
        }
        index = -1;
    }

    @Override
    public boolean isFinished() {
        return index >= commands.size();
    }
}
