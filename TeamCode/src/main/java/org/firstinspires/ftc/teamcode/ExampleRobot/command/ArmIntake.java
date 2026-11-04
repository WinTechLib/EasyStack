
package org.firstinspires.ftc.teamcode.ExampleRobot.command;

import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Arm;

public class ArmIntake extends ArmPresetCommand {

    public ArmIntake(Arm subsystem) {
        super(subsystem, () -> subsystem.intake());
    }
}
