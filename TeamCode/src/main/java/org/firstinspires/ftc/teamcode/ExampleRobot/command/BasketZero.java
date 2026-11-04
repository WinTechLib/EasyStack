
package org.firstinspires.ftc.teamcode.ExampleRobot.command;

import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Slide;

import dev.easyfamily.EasyStack.Commands.EasyCommandBase;

public class BasketZero extends EasyCommandBase {

    private final Slide slide;

    public BasketZero(Slide subsystem) {
        this.slide = subsystem;
        addRequirements(slide);
    }

    @Override
    public void initialize() {
        slide.basketZero();
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
        if (interrupted) {
            slide.stop();
        }
    }
}
