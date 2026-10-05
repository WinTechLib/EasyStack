package dev.easyfamily.easystack.drivebase;

import dev.easyfamily.easystack.drivebase.drive.DrivePowers;
import dev.easyfamily.easystack.drivebase.drive.RobotDrive;
import dev.easyfamily.easystack.geometry.Rotation2d;
import dev.easyfamily.easystack.geometry.Vector2d;
import dev.easyfamily.easystack.hardware.Motors.EasyMotor;

public class MecanumDrive extends RobotDrive {

    private final EasyMotor frontLeft;
    private final EasyMotor frontRight;
    private final EasyMotor backLeft;
    private final EasyMotor backRight;

    private double rightSideMultiplier = -1.0;

    public MecanumDrive(
            EasyMotor frontLeft,
            EasyMotor frontRight,
            EasyMotor backLeft,
            EasyMotor backRight
    ) {
        this(
                true,
                frontLeft,
                frontRight,
                backLeft,
                backRight
        );
    }

    public MecanumDrive(
            boolean autoInvert,
            EasyMotor frontLeft,
            EasyMotor frontRight,
            EasyMotor backLeft,
            EasyMotor backRight
    ) {
        this.frontLeft = frontLeft;
        this.frontRight = frontRight;
        this.backLeft = backLeft;
        this.backRight = backRight;

        setRightSideInverted(autoInvert);
    }

    // ------------------------------------------------------------
    // Configuration
    // ------------------------------------------------------------

    public boolean isRightSideInverted() {
        return rightSideMultiplier == -1.0;
    }

    public MecanumDrive setRightSideInverted(
            boolean inverted
    ) {
        rightSideMultiplier =
                inverted ? -1.0 : 1.0;

        return this;
    }

    @Override
    public MecanumDrive setMaxOutput(
            double maxOutput
    ) {
        super.setMaxOutput(maxOutput);
        return this;
    }

    // ------------------------------------------------------------
    // Robot Centric
    // ------------------------------------------------------------

    public void driveRobotCentric(
            double strafe,
            double forward,
            double turn
    ) {
        driveFieldCentric(
                strafe,
                forward,
                turn,
                0.0
        );
    }

    public void driveRobotCentric(
            double strafe,
            double forward,
            double turn,
            boolean squareInputs
    ) {
        if (squareInputs) {
            strafe = squareInput(strafe);
            forward = squareInput(forward);
            turn = squareInput(turn);
        }

        driveRobotCentric(
                strafe,
                forward,
                turn
        );
    }

    // ------------------------------------------------------------
    // Field Centric
    // ------------------------------------------------------------

    public void driveFieldCentric(
            double strafe,
            double forward,
            double turn,
            double heading
    ) {
        strafe = clip(strafe);
        forward = clip(forward);
        turn = clip(turn);

        Vector2d input = new Vector2d(
                strafe,
                forward
        );

        input = input.rotateBy(Rotation2d.fromRadians(-heading));

        double theta = input.angle();

        double[] wheelSpeeds = new double[4];

        wheelSpeeds[0] =
                Math.sin(theta + Math.PI / 4);

        wheelSpeeds[1] =
                Math.sin(theta - Math.PI / 4);

        wheelSpeeds[2] =
                Math.sin(theta - Math.PI / 4);

        wheelSpeeds[3] =
                Math.sin(theta + Math.PI / 4);

        normalize(
                wheelSpeeds,
                input.norm()
        );

        wheelSpeeds[0] += turn;
        wheelSpeeds[1] -= turn;
        wheelSpeeds[2] += turn;
        wheelSpeeds[3] -= turn;

        normalize(wheelSpeeds);

        driveWithMotorPowers(
                wheelSpeeds[0],
                wheelSpeeds[1],
                wheelSpeeds[2],
                wheelSpeeds[3]
        );
    }

    public void driveFieldCentric(
            double strafe,
            double forward,
            double turn,
            double heading,
            boolean squareInputs
    ) {
        if (squareInputs) {
            strafe = squareInput(strafe);
            forward = squareInput(forward);
            turn = squareInput(turn);
        }

        driveFieldCentric(
                strafe,
                forward,
                turn,
                heading
        );
    }

    // ------------------------------------------------------------
    // Direct Motor Control
    // ------------------------------------------------------------

    public void driveWithMotorPowers(
            double frontLeft,
            double frontRight,
            double backLeft,
            double backRight
    ) {
        this.frontLeft.setPower(
                frontLeft
                        * maxOutput
        );

        this.frontRight.setPower(
                frontRight
                        * rightSideMultiplier
                        * maxOutput
        );

        this.backLeft.setPower(
                backLeft
                        * maxOutput
        );

        this.backRight.setPower(
                backRight
                        * rightSideMultiplier
                        * maxOutput
        );
    }

    // ------------------------------------------------------------
    // Drivetrain
    // ------------------------------------------------------------

    @Override
    public void drive(DrivePowers powers) {
        driveRobotCentric(
                powers.getStrafe(),
                powers.getForward(),
                powers.getTurn()
        );
    }

    @Override
    public void stop() {
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }
}