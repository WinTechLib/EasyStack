package org.firstinspires.ftc.teamcode.ExampleRobot;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import dev.easyfamily.EasyStack.Commands.AxisCommand;
import dev.easyfamily.EasyStack.Commands.EasyCommandScheduler;
import dev.easyfamily.EasyStack.gamepad.EasyGamepad;
import dev.easyfamily.EasyStack.Commands.EasyTrigger;

@TeleOp(name = "Example TeleOp", group = "ExampleRobot")
public class ExampleTeleOp extends OpMode {

    private ExampleRobot robot;
    private EasyGamepad driver;
    private EasyGamepad operator;
    private EasyCommandScheduler scheduler;

    @Override
    public void init() {
        // Reset before creating Gamepad bindings, because each binding
        // registers an EasyTrigger in the singleton scheduler.
        scheduler = EasyCommandScheduler.getInstance();
        scheduler.reset();

        robot = new ExampleRobot();
        robot.init(hardwareMap);

        driver = new EasyGamepad(gamepad1)
                .setInvertY(true)
                .setStickDeadzone(0.05f);

        operator = new EasyGamepad(gamepad2)
                .setStickDeadzone(0.05f);

        configureBindings();
        configureDefaultCommands();

        robot.chassis.setMaxOutput(0.8);
    }

    // ------------------------------------------------------------
    // Button bindings: no polling if-statements in the OpMode loop
    // ------------------------------------------------------------

    private void configureBindings() {

        // Slide / elevator
        operator.dpadUpButton()
                .onPress(robot.commands.highBasket);

        operator.dpadLeftButton()
                .onPress(robot.commands.lowBasket);

        operator.dpadDownButton()
                .onPress(robot.commands.zeroBasket);

        // Arm
        operator.aButton()
                .onPress(robot.commands.armStow);

        operator.xButton()
                .onPress(robot.commands.armIntake);

        operator.yButton()
                .onPress(robot.commands.armScore);

        // Claw
        operator.leftBumperButton()
                .onPress(robot.commands.clawOpen);

        operator.rightBumperButton()
                .onPress(robot.commands.clawClose);

        // Intake angle
        operator.dpadRightButton()
                .onPress(robot.commands.intakeGround);

        operator.leftStickButtonTrigger()
                .onPress(robot.commands.intakeTransfer);

        // Intake roller
        operator.bButton()
                .onPress(robot.commands.intakeOn);

        operator.backButton()
                .onPress(robot.commands.intakeOuttake);

        operator.startButton()
                .onPress(robot.commands.intakeOff);
    }

    // ------------------------------------------------------------
    // Default commands
    // ------------------------------------------------------------

    private void configureDefaultCommands() {

        // Chassis is driven continuously whenever no other command owns it.
        robot.chassis.setDefaultCommand(
                robot.chassis.runEnd(
                        () -> robot.chassis.driveRobotCentric(
                                driver.leftStickX(),
                                driver.leftStickY(),
                                driver.rightStickX(),
                                true
                        ),
                        robot.chassis::stop
                )
        );

        // Right trigger maps from 0.0 to 1.0 to the intake extension position.
        // Preserve the servo's last position when this default command is interrupted.
        robot.intake.setDefaultCommand(
                new AxisCommand(
                        operator::rightTrigger,
                        0.05,
                        robot.intake::setExtensionPosition,
                        false,
                        robot.intake
                )
        );
    }

    @Override
    public void loop() {
        // Update the cached button states before the scheduler polls triggers.
        driver.update();
        operator.update();

        scheduler.run();
        robot.loop();

        telemetry.addData("Drive command", scheduler.getCurrentCommand(robot.chassis));
        telemetry.addData("Slide command", scheduler.getCurrentCommand(robot.elevator));
        telemetry.addData("Arm command", scheduler.getCurrentCommand(robot.arm));
        telemetry.addData("Claw command", scheduler.getCurrentCommand(robot.claw));
        telemetry.addData("Intake command", scheduler.getCurrentCommand(robot.intake));
        telemetry.addData("Intake extension input", operator.rightTrigger());
        telemetry.addData("Intake angle", robot.intake.getAngle());
        telemetry.update();
    }

    @Override
    public void stop() {
        scheduler.cancelAll();
        robot.stop();
    }
}
