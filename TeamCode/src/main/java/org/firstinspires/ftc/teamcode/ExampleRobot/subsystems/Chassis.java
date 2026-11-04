package org.firstinspires.ftc.teamcode.ExampleRobot.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.easyfamily.EasyStack.drivebase.MecanumDrive;
import dev.easyfamily.EasyStack.hardware.Motors.EasyMotor;
import dev.easyfamily.EasyStack.subsytem.EasySubsystemBase;

public class Chassis extends EasySubsystemBase {

    private EasyMotor frontLeft;
    private EasyMotor frontRight;
    private EasyMotor backLeft;
    private EasyMotor backRight;

    private MecanumDrive mecanumDrive;

    @Override
    public void init(HardwareMap hardwareMap) {
        frontLeft = new EasyMotor(hardwareMap, "motorFe");
        frontRight = new EasyMotor(hardwareMap, "motorFd");
        backLeft = new EasyMotor(hardwareMap, "motorTe");
        backRight = new EasyMotor(hardwareMap, "motorTd");

        mecanumDrive = new MecanumDrive(
                true,
                frontLeft,
                frontRight,
                backLeft,
                backRight
        );
    }

    public void driveRobotCentric(
            double strafe,
            double forward,
            double turn,
            boolean squareInputs
    ) {
        ensureInitialized();
        mecanumDrive.driveRobotCentric(
                strafe,
                forward,
                turn,
                squareInputs
        );
    }

    public void driveFieldCentric(
            double strafe,
            double forward,
            double turn,
            double heading,
            boolean squareInputs
    ) {
        ensureInitialized();
        mecanumDrive.driveFieldCentric(
                strafe,
                forward,
                turn,
                heading,
                squareInputs
        );
    }

    public Chassis setMaxOutput(double maxOutput) {
        ensureInitialized();
        mecanumDrive.setMaxOutput(maxOutput);
        return this;
    }

    public Chassis setRightSideInverted(boolean inverted) {
        ensureInitialized();
        mecanumDrive.setRightSideInverted(inverted);
        return this;
    }

    public void stop() {
        if (mecanumDrive != null) {
            mecanumDrive.stop();
        }
    }

    public MecanumDrive getMecanumDrive() {
        ensureInitialized();
        return mecanumDrive;
    }

    @Override
    public void loop() {
        // The default RunCommand continuously updates the drivetrain.
    }

    private void ensureInitialized() {
        if (mecanumDrive == null) {
            throw new IllegalStateException("Chassis has not been initialized.");
        }
    }
}
