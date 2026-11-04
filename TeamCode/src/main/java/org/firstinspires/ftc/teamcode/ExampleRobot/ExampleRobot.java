package org.firstinspires.ftc.teamcode.ExampleRobot;

import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.easyfamily.EasyStack.Commands.EasyCommand;

import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Arm;
import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Chassis;
import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Claw;
import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Intake;
import org.firstinspires.ftc.teamcode.ExampleRobot.subsystems.Slide;

/** Central container for the ExampleRobot hardware and commands. */
public class ExampleRobot {

    // ------------------------------------------------------------
    // Subsystems
    // ------------------------------------------------------------

    public final Chassis chassis = new Chassis();
    public final Slide elevator = new Slide();
    public final Arm arm = new Arm();
    public final Claw claw = new Claw();
    public final Intake intake = new Intake();

    // Create command instances once; bindings reuse these instances.
    public final Commands commands = new Commands();

    public class Commands {

        // Slide / elevator
        public final EasyCommand highBasket =
                elevator.runOnce(elevator::basketHigh);

        public final EasyCommand lowBasket =
                elevator.runOnce(elevator::basketLow);

        public final EasyCommand zeroBasket =
                elevator.runOnce(elevator::basketZero);

        // Arm
        public final EasyCommand armStow =
                arm.runOnce(arm::stow);

        public final EasyCommand armIntake =
                arm.runOnce(arm::intake);

        public final EasyCommand armScore =
                arm.runOnce(arm::score);

        // Claw
        public final EasyCommand clawOpen =
                claw.runOnce(claw::open);

        public final EasyCommand clawClose =
                claw.runOnce(claw::close);

        // Intake angle presets
        public final EasyCommand intakeGround =
                intake.runOnce(() -> intake.setAngle(90.0));

        public final EasyCommand intakeTransfer =
                intake.runOnce(() -> intake.setAngle(30.0));

        // Intake roller
        public final EasyCommand intakeOn =
                intake.runOnce(intake::intakeOn);

        public final EasyCommand intakeOuttake =
                intake.runOnce(intake::outtake);

        public final EasyCommand intakeOff =
                intake.runOnce(intake::intakeOff);
    }

    // ------------------------------------------------------------
    // Hardware initialization
    // ------------------------------------------------------------

    public void init(HardwareMap hardwareMap) {
        chassis.init(hardwareMap);
        elevator.init(hardwareMap);
        arm.init(hardwareMap);
        claw.init(hardwareMap);
        intake.init(hardwareMap);
    }

    // ------------------------------------------------------------
    // Subsystem periodic methods
    // ------------------------------------------------------------

    public void loop() {
        chassis.loop();
        elevator.loop();
        arm.loop();
        claw.loop();
        intake.loop();
    }

    /** Stop powered mechanisms when the OpMode ends. */
    public void stop() {
        chassis.stop();
        elevator.stop();
        arm.stop();
        intake.stop();
        // The positional claw servo intentionally keeps its last position.
    }
}
