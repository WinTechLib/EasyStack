
package org.firstinspires.ftc.teamcode.ExampleRobot.command;

import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Claw;

public class ClawOpen extends ClawStateCommand {

    public ClawOpen(Claw subsystem) {
        super(subsystem, subsystem::open);
    }
}
