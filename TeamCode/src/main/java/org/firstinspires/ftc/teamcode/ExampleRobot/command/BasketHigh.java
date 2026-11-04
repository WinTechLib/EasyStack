
package org.firstinspires.ftc.teamcode.ExampleRobot.command;

import java.util.Objects;

import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Slide;

import dev.easyfamily.EasyStack.Commands.EasyCommandBase;

public class BasketHigh extends EasyCommandBase {

    private final Slide slide;

    private boolean finished;

    public BasketHigh(Slide subsystem) {

        this.slide = Objects.requireNonNull(subsystem);

        addRequirements(this.slide);
    }

    @Override
    public void initialize() {
        finished = false;

        slide.basketHigh();

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

        if (interrupted) {
            slide.stop();
        }
    }
}
