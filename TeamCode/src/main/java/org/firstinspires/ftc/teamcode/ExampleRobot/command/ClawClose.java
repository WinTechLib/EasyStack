
package org.firstinspires.ftc.teamcode.ExampleRobot.command;

import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Claw;

public class ClawClose extends ClawStateCommand {

    public ClawClose(Claw subsystem) {
        super(subsystem, subsystem::close);
    }
}
