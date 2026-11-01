package org.firstinspires.ftc.teamcode.opModes;

import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Slide;

import dev.easyfamily.EasyStack.components.EasyCommandComponent;
import dev.easyfamily.EasyStack.components.EasySubsystemComponent;
import dev.easyfamily.EasyStack.opmode.EasyOpMode;

public class opteste extends EasyOpMode {

    public opteste() {
        addComponents(
                new EasySubsystemComponent(
                        Slide.INSTANCE
                ),
                new EasyCommandComponent()
        );
    }

    @Override
    public void initialize() {

    }

    @Override
    public void onUpdate() {

    }
}
