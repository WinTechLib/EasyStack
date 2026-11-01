package dev.easyfamily.EasyStack.Commands;

import dev.easyfamily.EasyStack.subsytem.EasySubsystem;

/** Executa a ação a cada loop por X ms. */
public class RunForCommand extends TimeoutCommand {
    public RunForCommand(Runnable action, long millis, EasySubsystem... subsystems) {
        super(new RunCommand(action, subsystems), millis);
    }
}
