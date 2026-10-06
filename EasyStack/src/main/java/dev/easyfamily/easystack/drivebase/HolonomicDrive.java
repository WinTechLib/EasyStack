package dev.easyfamily.easystack.drivebase;

import dev.easyfamily.easystack.drivebase.drive.DrivePowers;
import dev.easyfamily.easystack.drivebase.drive.RobotDrive;
import dev.easyfamily.easystack.geometry.Rotation2d;
import dev.easyfamily.easystack.geometry.Vector2d;
import dev.easyfamily.easystack.controllable.EasyControllable;

public class HolonomicDrive extends RobotDrive {

    public static final double DEFAULT_RIGHT_MOTOR_ANGLE =
            Math.PI / 3.0;

    public static final double DEFAULT_LEFT_MOTOR_ANGLE =
            2.0 * Math.PI / 3.0;

    public static final double DEFAULT_SLIDE_MOTOR_ANGLE =
            3.0 * Math.PI / 2.0;

    private final EasyControllable[] motors;

    private double rightMotorAngle =
            DEFAULT_RIGHT_MOTOR_ANGLE;

    private double leftMotorAngle =
            DEFAULT_LEFT_MOTOR_ANGLE;

    private double slideMotorAngle =
            DEFAULT_SLIDE_MOTOR_ANGLE;

    // ------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------

    public HolonomicDrive(
            EasyControllable left,
            EasyControllable right,
            EasyControllable slide
    ) {
        motors = new EasyControllable[]{
                left,
                right,
                slide
        };
    }

    public HolonomicDrive(
            EasyControllable left,
            EasyControllable right,
            EasyControllable slide,
            double leftMotorAngle,
            double rightMotorAngle,
            double slideMotorAngle
    ) {
        motors = new EasyControllable[]{
                left,
                right,
                slide
        };

        this.leftMotorAngle = leftMotorAngle;
        this.rightMotorAngle = rightMotorAngle;
        this.slideMotorAngle = slideMotorAngle;
    }

    /**
     * Construtor para configuração de 4 motores.
     *
     * Ordem:
     * frontLeft, frontRight, backLeft, backRight
     */
    public HolonomicDrive(
            EasyControllable frontLeft,
            EasyControllable frontRight,
            EasyControllable backLeft,
            EasyControllable backRight
    ) {
        motors = new EasyControllable[]{
                frontLeft,
                frontRight,
                backLeft,
                backRight
        };
    }

    // ------------------------------------------------------------
    // Configuration
    // ------------------------------------------------------------

    public HolonomicDrive setMotorAngles(
            double leftMotorAngle,
            double rightMotorAngle,
            double slideMotorAngle
    ) {
        this.leftMotorAngle = leftMotorAngle;
        this.rightMotorAngle = rightMotorAngle;
        this.slideMotorAngle = slideMotorAngle;

        return this;
    }

    public double getLeftMotorAngle() {
        return leftMotorAngle;
    }

    public double getRightMotorAngle() {
        return rightMotorAngle;
    }

    public double getSlideMotorAngle() {
        return slideMotorAngle;
    }

    @Override
    public HolonomicDrive setMaxOutput(
            double maxOutput
    ) {
        super.setMaxOutput(maxOutput);
        return this;
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

        Vector2d vector = new Vector2d(
                strafe,
                forward
        );

        vector = vector.rotateBy(
                Rotation2d.fromRadians(-heading)
        );

        if (motors.length == 3) {

            Vector2d leftVector =
                    Vector2d.fromPolar(
                            1.0,
                            leftMotorAngle
                    );

            Vector2d rightVector =
                    Vector2d.fromPolar(
                            1.0,
                            rightMotorAngle
                    );

            Vector2d slideVector =
                    Vector2d.fromPolar(
                            1.0,
                            slideMotorAngle
                    );

            double[] speeds = new double[3];

            speeds[0] =
                    project(vector, leftVector)
                            + turn;

            speeds[1] =
                    project(vector, rightVector)
                            + turn;

            speeds[2] =
                    project(vector, slideVector)
                            + turn;

            normalize(speeds);

            setMotorPowers(
                    speeds[0],
                    speeds[1],
                    speeds[2]
            );

        } else {

            // ----------------------------------------------------
            // 4 motores
            // Funciona como um Mecanum
            // ----------------------------------------------------

            double theta = vector.angle();

            double[] speeds = new double[4];

            speeds[0] =
                    Math.sin(theta + Math.PI / 4.0);

            speeds[1] =
                    Math.sin(theta - Math.PI / 4.0);

            speeds[2] =
                    Math.sin(theta - Math.PI / 4.0);

            speeds[3] =
                    Math.sin(theta + Math.PI / 4.0);

            normalize(
                    speeds,
                    vector.norm()
            );

            speeds[0] += turn;
            speeds[1] -= turn;
            speeds[2] += turn;
            speeds[3] -= turn;

            normalize(speeds);

            setMotorPowers(
                    speeds[0],
                    speeds[1],
                    speeds[2],
                    speeds[3]
            );
        }
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

    // ------------------------------------------------------------
    // Direct Motor Control - 3 motors
    // ------------------------------------------------------------

    public void setMotorPowers(
            double left,
            double right,
            double slide
    ) {
        motors[0].setPower(
                left * maxOutput
        );

        motors[1].setPower(
                right * maxOutput
        );

        motors[2].setPower(
                slide * maxOutput
        );
    }

    // ------------------------------------------------------------
    // Direct Motor Control - 4 motors
    // ------------------------------------------------------------

    public void setMotorPowers(
            double frontLeft,
            double frontRight,
            double backLeft,
            double backRight
    ) {
        motors[0].setPower(
                frontLeft * maxOutput
        );

        motors[1].setPower(
                frontRight * -maxOutput
        );

        motors[2].setPower(
                backLeft * maxOutput
        );

        motors[3].setPower(
                backRight * -maxOutput
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
        for (EasyControllable motor : motors) {
            motor.setPower(0);
        }
    }

    // ------------------------------------------------------------
    // Utility
    // ------------------------------------------------------------

    private double project(
            Vector2d vector,
            Vector2d onto
    ) {
        double denominator = onto.dot(onto);

        if (denominator < 1e-12) {
            return 0.0;
        }

        return vector.dot(onto) / Math.sqrt(denominator);
    }
}