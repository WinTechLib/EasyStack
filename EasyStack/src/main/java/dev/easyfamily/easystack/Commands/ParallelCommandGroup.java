package dev.easyfamily.easystack.Commands;

import java.util.LinkedHashSet;
import java.util.Set;

/** Roda todos juntos; termina quando TODOS terminarem. */
public class ParallelCommandGroup extends CommandGroupBase {
    private final Set<EasyCommand> running = new LinkedHashSet<>();

    public ParallelCommandGroup(EasyCommand... cmds) {
        register(true, cmds);
    }

    @Override
    public void initialize() {
        running.clear();
        for (EasyCommand c : commands) {
            c.initialize();
            running.add(c);
        }
    }

    @Override
    public void execute() {
        for (EasyCommand c : commands) {
            if (!running.contains(c)) continue;
            c.execute();
            if (c.isFinished()) {
                c.end(false);
                running.remove(c);
            }
        }
    }

    @Override
    public void end(boolean interrupted) {
        for (EasyCommand c : running) c.end(true);
        running.clear();
    }

    @Override
    public boolean isFinished() {
        return running.isEmpty();
    }
}
