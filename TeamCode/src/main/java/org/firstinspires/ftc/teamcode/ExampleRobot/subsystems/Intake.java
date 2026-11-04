package org.firstinspires.ftc.teamcode.ExampleRobot.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.easyfamily.EasyStack.hardware.Motors.EasyMotor;
import dev.easyfamily.EasyStack.hardware.Servo.EasyServo;
import dev.easyfamily.EasyStack.subsytem.EasySubsystemBase;

public class Intake extends EasySubsystemBase {

    private EasyServo extensionLeft;
    private EasyServo extensionRight;
    private EasyServo angleServo;
    private EasyMotor intakeMotor;

    // Calibrate these positions to avoid mechanically forcing the servos.
    private static final double RETRACTED_POSITION = 0.0;
    private static final double EXTENDED_POSITION = 1.0;

    private static final double MIN_ANGLE = 0.0;
    private static final double MAX_ANGLE = 180.0;

    private static final double INTAKE_POWER = 1.0;
    private static final double OUTTAKE_POWER = -1.0;

    // Set true only if the right extension servo is mounted mirrored.
    private static final boolean RIGHT_SERVO_INVERTED = false;

    @Override
    public void init(HardwareMap hardwareMap) {
        extensionLeft = new EasyServo(hardwareMap, "intakeExtendLeft")
                .addState("retracted", RETRACTED_POSITION)
                .addState("extended", EXTENDED_POSITION);

        extensionRight = new EasyServo(hardwareMap, "intakeExtendRight")
                .setInverted(RIGHT_SERVO_INVERTED)
                .addState("retracted", RETRACTED_POSITION)
                .addState("extended", EXTENDED_POSITION);

        angleServo = new EasyServo(hardwareMap, "intakeAngle", 180.0);
        intakeMotor = new EasyMotor(hardwareMap, "intakeMotor");
        intakeMotor.setPower(0.0);
    }

    // ------------------------------------------------------------
    // Extension: analog position from 0.0 to 1.0
    // ------------------------------------------------------------

    public void extend() {
        setExtensionPosition(EXTENDED_POSITION);
    }

    public void retract() {
        setExtensionPosition(RETRACTED_POSITION);
    }

    public void setExtensionPosition(double position) {
        ensureInitialized();

        if (!Double.isFinite(position)) {
            throw new IllegalArgumentException("Extension position must be finite.");
        }

        double target = clamp(position, 0.0, 1.0);
        extensionLeft.setPosition(target);
        extensionRight.setPosition(target);
    }

    // ------------------------------------------------------------
    // Angle
    // ------------------------------------------------------------

    public void setAngle(double degrees) {
        ensureInitialized();

        if (!Double.isFinite(degrees)) {
            throw new IllegalArgumentException("Angle must be finite.");
        }

        angleServo.setAngle(clamp(degrees, MIN_ANGLE, MAX_ANGLE));
    }

    public double getAngle() {
        ensureInitialized();
        return angleServo.getAngle();
    }

    // ------------------------------------------------------------
    // Roller motor
    // ------------------------------------------------------------

    public void intakeOn() {
        setIntakePower(INTAKE_POWER);
    }

    public void outtake() {
        setIntakePower(OUTTAKE_POWER);
    }

    public void intakeOff() {
        ensureInitialized();
        intakeMotor.setPower(0.0);
    }

    public void setIntakePower(double power) {
        ensureInitialized();

        if (!Double.isFinite(power)) {
            throw new IllegalArgumentException("Intake power must be finite.");
        }

        intakeMotor.setPower(clamp(power, -1.0, 1.0));
    }

    /** Stops only the roller motor; positional servos keep their last targets. */
    public void stop() {
        if (intakeMotor != null) {
            intakeMotor.setPower(0.0);
        }
    }

    public EasyServo getExtensionLeft() {
        ensureInitialized();
        return extensionLeft;
    }

    public EasyServo getExtensionRight() {
        ensureInitialized();
        return extensionRight;
    }

    public EasyServo getAngleServo() {
        ensureInitialized();
        return angleServo;
    }

    public EasyMotor getIntakeMotor() {
        ensureInitialized();
        return intakeMotor;
    }

    @Override
    public void loop() {
        // The command scheduler drives the servos and roller motor.
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void ensureInitialized() {
        if (extensionLeft == null || extensionRight == null
                || angleServo == null || intakeMotor == null) {
            throw new IllegalStateException("Intake has not been initialized.");
        }
    }
}
