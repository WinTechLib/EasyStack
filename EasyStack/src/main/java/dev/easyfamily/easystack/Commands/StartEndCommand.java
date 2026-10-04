package dev.easyfamily.easystack.Commands;

import dev.easyfamily.easystack.subsytem.EasySubsystem;

/** Roda onStart ao iniciar e onEnd ao terminar/ser interrompido. Roda até ser cancelado. */
public class StartEndCommand extends EasyCommandBase {
    private final Runnable onStart;
    private final Runnable onEnd;

    public StartEndCommand(Runnable onStart, Runnable onEnd, EasySubsystem... subsystems) {
        this.onStart = onStart;
        this.onEnd = onEnd;
        addRequirements(subsystems);
    }

    @Override
    public void initialize() { onStart.run(); }

    @Override
    public void end(boolean interrupted) { onEnd.run(); }
}
