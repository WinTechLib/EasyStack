
package org.firstinspires.ftc.teamcode.ExampleRobot.command;

import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Arm;

public class ArmStow extends ArmPresetCommand {

    public ArmStow(Arm subsystem) {
        super(subsystem, () -> subsystem.stow());
    }
}
