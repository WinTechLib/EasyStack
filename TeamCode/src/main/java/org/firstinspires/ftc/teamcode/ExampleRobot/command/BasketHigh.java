package org.firstinspires.ftc.teamcode.ExampleRobot.command;

import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Slide;
import dev.easyfamily.EasyStack.Commands.EasyCommandBase;

public class BasketHigh extends EasyCommandBase {

    private final Slide slide;

    public BasketHigh(Slide subsystem) {
        this.slide = subsystem;
        addRequirements(this.slide);
    }

    @Override
    public void initialize() {
        slide.basketHigh();
    }

    @Override
    public void execute() {
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void end(boolean interrupted) {

        if (interrupted) {
            slide.stop();
        }
    }
}