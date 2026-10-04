package dev.easyfamily.easystack.Commands;

import java.util.HashSet;
import java.util.Set;


public class ParallelDeadlineGroup extends CommandGroupBase {
    private final EasyCommand deadline;
    private final Set<EasyCommand> done = new HashSet<>();
    private boolean finished = false;

    public ParallelDeadlineGroup(EasyCommand deadline, EasyCommand... others) {
        this.deadline = deadline;
        register(true, deadline);
        register(true, others);
    }

    @Override
    public void initialize() {
        done.clear();
        finished = false;
        for (EasyCommand c : commands) c.initialize();
    }

    @Override
    public void execute() {
        for (EasyCommand c : commands) {
            if (c == deadline || done.contains(c)) continue;
            c.execute();
            if (c.isFinished()) {
                c.end(false);
                done.add(c);
            }
        }
        deadline.execute();
        if (deadline.isFinished()) finished = true;
    }

    @Override
    public void end(boolean interrupted) {
        deadline.end(interrupted);
        for (EasyCommand c : commands) {
            if (c != deadline && !done.contains(c)) c.end(true);
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
