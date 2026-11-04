
package org.firstinspires.ftc.teamcode.ExampleRobot.command;

import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Arm;

public class ArmScore extends ArmPresetCommand {

    public ArmScore(Arm subsystem) {
        super(subsystem, () -> subsystem.score());
    }
}
