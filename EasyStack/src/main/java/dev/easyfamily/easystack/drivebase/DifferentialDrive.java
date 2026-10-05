package dev.easyfamily.easystack.drivebase;

import dev.easyfamily.easystack.drivebase.drive.DrivePowers;
import dev.easyfamily.easystack.drivebase.drive.RobotDrive;
import dev.easyfamily.easystack.controllable.EasyControllable;

public class DifferentialDrive extends RobotDrive {

    private static final double DEFAULT_RIGHT_SIDE_MULTIPLIER = -1.0;

    private final EasyControllable left;
    private final EasyControllable right;

    private double rightSideMultiplier =
            DEFAULT_RIGHT_SIDE_MULTIPLIER;

    public DifferentialDrive(
            EasyControllable left,
            EasyControllable right
    ) {
        this(
                true,
                left,
                right
        );
    }

    public DifferentialDrive(
            boolean autoInvert,
            EasyControllable left,
            EasyControllable right
    ) {
        this.left = left;
        this.right = right;

        setRightSideInverted(autoInvert);
    }

    // ------------------------------------------------------------
    // Configuration
    // ------------------------------------------------------------

    public boolean isRightSideInverted() {
        return rightSideMultiplier == -1.0;
    }

    public DifferentialDrive setRightSideInverted(
            boolean inverted
    ) {
        rightSideMultiplier =
                inverted ? -1.0 : 1.0;

        return this;
    }

    @Override
    public DifferentialDrive setMaxOutput(
            double maxOutput
    ) {
        super.setMaxOutput(maxOutput);
        return this;
    }

    // ------------------------------------------------------------
    // Arcade Drive
    // ------------------------------------------------------------

    public void arcadeDrive(
            double forward,
            double turn
    ) {
        forward = clip(forward);
        turn = clip(turn);

        double leftPower =
                forward + turn;

        double rightPower =
                forward - turn;

        double[] wheelSpeeds = {
                leftPower,
                rightPower
        };

        normalize(wheelSpeeds);

        setMotorPowers(
                wheelSpeeds[0],
                wheelSpeeds[1]
        );
    }

    public void arcadeDrive(
            double forward,
            double turn,
            boolean squareInputs
    ) {
        if (squareInputs) {
            forward = squareInput(forward);
            turn = squareInput(turn);
        }

        arcadeDrive(
                forward,
                turn
        );
    }

    // ------------------------------------------------------------
    // Tank Drive
    // ------------------------------------------------------------

    public void tankDrive(
            double leftPower,
            double rightPower
    ) {
        leftPower = clip(leftPower);
        rightPower = clip(rightPower);

        double[] wheelSpeeds = {
                leftPower,
                rightPower
        };

        normalize(wheelSpeeds);

        setMotorPowers(
                wheelSpeeds[0],
                wheelSpeeds[1]
        );
    }

    public void tankDrive(
            double leftPower,
            double rightPower,
            boolean squareInputs
    ) {
        if (squareInputs) {
            leftPower = squareInput(leftPower);
            rightPower = squareInput(rightPower);
        }

        tankDrive(
                leftPower,
                rightPower
        );
    }

    // ------------------------------------------------------------
    // Direct Motor Control
    // ------------------------------------------------------------

    public void setMotorPowers(
            double leftPower,
            double rightPower
    ) {
        left.setPower(
                leftPower * maxOutput
        );

        right.setPower(
                rightPower
                        * rightSideMultiplier
                        * maxOutput
        );
    }

    // ------------------------------------------------------------
    // Drivetrain
    // ------------------------------------------------------------

    @Override
    public void drive(DrivePowers powers) {
        arcadeDrive(
                powers.getForward(),
                powers.getTurn()
        );
    }

    @Override
    public void stop() {
        left.setPower(0);
        right.setPower(0);
    }

    // ------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------

    public EasyControllable getLeft() {
        return left;
    }

    public EasyControllable getRight() {
        return right;
    }
}