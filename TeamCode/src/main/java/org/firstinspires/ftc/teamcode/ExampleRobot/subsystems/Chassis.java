
package org.firstinspires.ftc.teamcode.ExampleRobot.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.easyfamily.EasyStack.drivebase.MecanumDrive;
import dev.easyfamily.EasyStack.hardware.Motors.EasyMotor;
import dev.easyfamily.EasyStack.subsytem.EasySubsystemBase;

public class Chassis extends EasySubsystemBase {

    private EasyMotor motorFe;
    private EasyMotor motorFd;
    private EasyMotor motorTe;
    private EasyMotor motorTd;

    private MecanumDrive mecanumDrive;
    private double strafe = 0.0;
    private double forward = 0.0;
    private double turn = 0.0;
    private double heading = 0.0;

    private boolean fieldCentric = false;
    private boolean squareInputs = false;
    private boolean stopped = true;

    @Override
    public void init(HardwareMap hardwareMap) {

        motorFe = new EasyMotor(hardwareMap, "motorFe");
        motorFd = new EasyMotor(hardwareMap, "motorFd");
        motorTe = new EasyMotor(hardwareMap, "motorTe");
        motorTd = new EasyMotor(hardwareMap, "motorTd");

        mecanumDrive = new MecanumDrive(
                true,
                motorFe,
                motorFd,
                motorTe,
                motorTd
        );
    }
    public void driveRobotCentric(
            double strafe,
            double forward,
            double turn
    ) {
        driveRobotCentric(
                strafe,
                forward,
                turn,
                false
        );
    }

    public void driveRobotCentric(
            double strafe,
            double forward,
            double turn,
            boolean squareInputs
    ) {
        this.strafe = strafe;
        this.forward = forward;
        this.turn = turn;

        this.squareInputs = squareInputs;
        this.fieldCentric = false;
        this.stopped = false;
    }

    public void driveFieldCentric(
            double strafe,
            double forward,
            double turn,
            double heading
    ) {
        driveFieldCentric(
                strafe,
                forward,
                turn,
                heading,
                false
        );
    }

    public void driveFieldCentric(
            double strafe,
            double forward,
            double turn,
            double heading,
            boolean squareInputs
    ) {
        this.strafe = strafe;
        this.forward = forward;
        this.turn = turn;
        this.heading = heading;

        this.squareInputs = squareInputs;
        this.fieldCentric = true;
        this.stopped = false;
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

    public Chassis setFieldCentricCalculator(
            MecanumDrive.FieldCentricCalculator calculator
    ) {
        ensureInitialized();

        mecanumDrive.setFieldCentricCalculator(calculator);
        return this;
    }

    public Chassis useDefaultFieldCentricCalculator() {
        ensureInitialized();

        mecanumDrive.useDefaultFieldCentricCalculator();
        return this;
    }
    public void setMotorPowers(
            double frontLeft,
            double frontRight,
            double backLeft,
            double backRight
    ) {
        ensureInitialized();

        stopped = true;

        mecanumDrive.driveWithMotorPowers(
                frontLeft,
                frontRight,
                backLeft,
                backRight
        );
    }

    public void stop() {
        stopped = true;

        strafe = 0.0;
        forward = 0.0;
        turn = 0.0;

        if (mecanumDrive != null) {
            mecanumDrive.stop();
        }
    }
    @Override
    public void loop() {

        if (mecanumDrive == null) {
            return;
        }

        if (stopped) {
            mecanumDrive.stop();
            return;
        }

        if (fieldCentric) {

            mecanumDrive.driveFieldCentric(
                    strafe,
                    forward,
                    turn,
                    heading,
                    squareInputs
            );

        } else {

            mecanumDrive.driveRobotCentric(
                    strafe,
                    forward,
                    turn,
                    squareInputs
            );
        }
    }
    private void ensureInitialized() {
        if (mecanumDrive == null) {
            throw new IllegalStateException(
                    "Chassis has not been initialized."
            );
        }
    }

    public MecanumDrive getMecanumDrive() {
        ensureInitialized();
        return mecanumDrive;
    }
}
