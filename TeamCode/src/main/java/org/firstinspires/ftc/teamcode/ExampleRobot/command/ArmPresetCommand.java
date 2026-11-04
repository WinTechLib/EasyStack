
package org.firstinspires.ftc.teamcode.ExampleRobot.command;

import java.util.Objects;

import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Arm;

import dev.easyfamily.EasyStack.Commands.EasyCommandBase;

public abstract class ArmPresetCommand extends EasyCommandBase {

    protected final Arm arm;

    private final Runnable action;

    public ArmPresetCommand(Arm subsystem, Runnable action) {

        this.arm = Objects.requireNonNull(subsystem);
        this.action = Objects.requireNonNull(action);

        addRequirements(arm);
    }

    @Override
    public void initialize() {
        action.run();
    }

    @Override
    public void execute() {
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public void end(boolean interrupted) {
    }
}
