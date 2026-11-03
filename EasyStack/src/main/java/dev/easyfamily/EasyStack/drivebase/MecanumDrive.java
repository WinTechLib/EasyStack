
package dev.easyfamily.EasyStack.drivebase;

import java.util.Objects;

import dev.easyfamily.EasyStack.drivebase.drive.DrivePowers;
import dev.easyfamily.EasyStack.drivebase.drive.RobotDrive;
import dev.easyfamily.EasyStack.hardware.Motors.EasyMotor;

public class MecanumDrive extends RobotDrive {

    private final EasyMotor frontLeft;
    private final EasyMotor frontRight;
    private final EasyMotor backLeft;
    private final EasyMotor backRight;

    private double rightSideMultiplier = -1.0;


    @FunctionalInterface
    public interface FieldCentricCalculator {

        double[] calculate(
                double strafe,
                double forward,
                double turn,
                double heading
        );
    }

    private FieldCentricCalculator fieldCentricCalculator =
            this::calculateDefaultFieldCentric;

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
        this.frontLeft = Objects.requireNonNull(frontLeft, "frontLeft");
        this.frontRight = Objects.requireNonNull(frontRight, "frontRight");
        this.backLeft = Objects.requireNonNull(backLeft, "backLeft");
        this.backRight = Objects.requireNonNull(backRight, "backRight");

        setRightSideInverted(autoInvert);
    }

    public boolean isRightSideInverted() {
        return rightSideMultiplier == -1.0;
    }

    public MecanumDrive setRightSideInverted(boolean inverted) {
        rightSideMultiplier = inverted ? -1.0 : 1.0;
        return this;
    }

    public MecanumDrive setFieldCentricCalculator(
            FieldCentricCalculator calculator
    ) {
        this.fieldCentricCalculator =
                Objects.requireNonNull(calculator, "calculator");

        return this;
    }
    public MecanumDrive useDefaultFieldCentricCalculator() {
        this.fieldCentricCalculator =
                this::calculateDefaultFieldCentric;

        return this;
    }

    @Override
    public MecanumDrive setMaxOutput(double maxOutput) {
        super.setMaxOutput(maxOutput);
        return this;
    }

    public void driveRobotCentric(
            double strafe,
            double forward,
            double turn
    ) {
        strafe = clipUnit(strafe);
        forward = clipUnit(forward);
        turn = clipUnit(turn);

        double[] wheelPowers = calculateMecanumPowers(
                strafe,
                forward,
                turn
        );

        applyWheelPowers(wheelPowers);
    }

    public void driveRobotCentric(
            double strafe,
            double forward,
            double turn,
            boolean squareInputs
    ) {
        if (squareInputs) {
            strafe = squareInputValue(strafe);
            forward = squareInputValue(forward);
            turn = squareInputValue(turn);
        }

        driveRobotCentric(strafe, forward, turn);
    }

    public void driveFieldCentric(
            double strafe,
            double forward,
            double turn,
            double heading
    ) {
        strafe = clipUnit(strafe);
        forward = clipUnit(forward);
        turn = clipUnit(turn);

        if (!isFinite(heading)) {
            throw new IllegalArgumentException(
                    "Heading must be a finite value."
            );
        }

        double[] wheelPowers = fieldCentricCalculator.calculate(
                strafe,
                forward,
                turn,
                heading
        );

        applyWheelPowers(wheelPowers);
    }

    public void driveFieldCentric(
            double strafe,
            double forward,
            double turn,
            double heading,
            boolean squareInputs
    ) {
        if (squareInputs) {
            strafe = squareInputValue(strafe);
            forward = squareInputValue(forward);
            turn = squareInputValue(turn);
        }

        driveFieldCentric(
                strafe,
                forward,
                turn,
                heading
        );
    }
    private double[] calculateDefaultFieldCentric(
            double strafe,
            double forward,
            double turn,
            double heading
    ) {
        double cos = Math.cos(heading);
        double sin = Math.sin(heading);

        double robotStrafe =
                strafe * cos + forward * sin;

        double robotForward =
                -strafe * sin + forward * cos;

        return calculateMecanumPowers(
                robotStrafe,
                robotForward,
                turn
        );
    }
    private double[] calculateMecanumPowers(
            double strafe,
            double forward,
            double turn
    ) {
        return new double[] {

                // Front Left
                forward + strafe + turn,

                // Front Right
                forward - strafe - turn,

                // Back Left
                forward - strafe + turn,

                // Back Right
                forward + strafe - turn
        };
    }
    private void applyWheelPowers(double[] wheelPowers) {

        double[] normalized = normalizeWheelPowers(wheelPowers);

        driveWithMotorPowers(
                normalized[0],
                normalized[1],
                normalized[2],
                normalized[3]
        );
    }

    private double[] normalizeWheelPowers(double[] powers) {

        if (powers == null || powers.length != 4) {
            throw new IllegalArgumentException(
                    "Field Centric calculator must return exactly 4 wheel powers."
            );
        }

        double[] normalized = powers.clone();
        double maxMagnitude = 0.0;

        for (double power : normalized) {

            if (!isFinite(power)) {
                throw new IllegalArgumentException(
                        "Wheel powers must be finite values."
                );
            }

            maxMagnitude = Math.max(
                    maxMagnitude,
                    Math.abs(power)
            );
        }

        if (maxMagnitude > 1.0) {

            for (int i = 0; i < normalized.length; i++) {
                normalized[i] /= maxMagnitude;
            }
        }

        return normalized;
    }


    public void driveWithMotorPowers(
            double frontLeft,
            double frontRight,
            double backLeft,
            double backRight
    ) {
        this.frontLeft.setPower(
                clipUnit(frontLeft * maxOutput)
        );

        this.frontRight.setPower(
                clipUnit(
                        frontRight
                                * rightSideMultiplier
                                * maxOutput
                )
        );

        this.backLeft.setPower(
                clipUnit(backLeft * maxOutput)
        );

        this.backRight.setPower(
                clipUnit(
                        backRight
                                * rightSideMultiplier
                                * maxOutput
                )
        );
    }


    private static double clipUnit(double value) {
        return Math.max(-1.0, Math.min(1.0, value));
    }

    private static double squareInputValue(double value) {
        value = clipUnit(value);
        return Math.copySign(value * value, value);
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value)
                && !Double.isInfinite(value);
    }


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
