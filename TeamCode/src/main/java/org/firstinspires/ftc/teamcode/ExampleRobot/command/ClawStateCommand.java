
package org.firstinspires.ftc.teamcode.ExampleRobot.command;

import java.util.Objects;

import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Claw;

import dev.easyfamily.EasyStack.Commands.EasyCommandBase;

public abstract class ClawStateCommand extends EasyCommandBase {

    private final Runnable action;
    private boolean finished;

    public ClawStateCommand(Claw subsystem, Runnable action) {

        Objects.requireNonNull(subsystem, "Claw subsystem");

        this.action = Objects.requireNonNull(action, "Action");

        addRequirements(subsystem);
    }

    @Override
    public void initialize() {
        finished = false;
        action.run();
        finished = true;
    }

    @Override
    public void execute() {
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void end(boolean interrupted) {
    }
}
