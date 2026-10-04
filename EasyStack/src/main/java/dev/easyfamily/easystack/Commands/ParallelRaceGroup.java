package dev.easyfamily.easystack.Commands;

import java.util.HashSet;
import java.util.Set;

/** Roda todos juntos; termina quando QUALQUER UM terminar (os demais são interrompidos). */
public class ParallelRaceGroup extends CommandGroupBase {
    private final Set<EasyCommand> done = new HashSet<>();
    private boolean finished = false;

    public ParallelRaceGroup(EasyCommand... cmds) {
        register(true, cmds);
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
            c.execute();
            if (c.isFinished()) {
                done.add(c);
                finished = true;
            }
        }
    }

    @Override
    public void end(boolean interrupted) {
        for (EasyCommand c : commands) c.end(!done.contains(c));
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
