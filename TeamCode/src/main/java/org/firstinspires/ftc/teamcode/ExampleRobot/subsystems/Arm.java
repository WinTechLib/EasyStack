package org.firstinspires.ftc.teamcode.ExampleRobot.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.easyfamily.EasyStack.control.wpiLibControllers.EasyArmFeedforward;
import dev.easyfamily.EasyStack.hardware.Motors.EasyMotor;
import dev.easyfamily.EasyStack.subsytem.EasySubsystemBase;

public class Arm extends EasySubsystemBase {

    private EasyMotor armMotor;
    private EasyArmFeedforward armFeedforward;

    // TODO: Tune these values on the real arm. Zero gains are placeholders.
    private static final double ARM_KP = 0.0;
    private static final double ARM_KI = 0.0;
    private static final double ARM_KD = 0.0;

    private static final double ARM_KS = 0.0;
    private static final double ARM_KCOS = 0.0;
    private static final double ARM_KV = 0.0;
    private static final double ARM_KA = 0.0;

    // These are mechanism-specific angle targets in degrees.
    private static final double MIN_POSITION = 0.0;
    private static final double MAX_POSITION = 120.0;

    private static final double STOW_POSITION = 0.0;
    private static final double INTAKE_POSITION = 45.0;
    private static final double SCORE_POSITION = 110.0;

    @Override
    public void init(HardwareMap hardwareMap) {
        armFeedforward = new EasyArmFeedforward(
                ARM_KS,
                ARM_KCOS,
                ARM_KV,
                ARM_KA
        );

        armMotor = new EasyMotor(hardwareMap, "armMotor")
                .setPositionFeedforward(armFeedforward)
                .setPositionPID(ARM_KP, ARM_KI, ARM_KD);
    }

    public void setAngle(double degrees) {
        ensureInitialized();
        armMotor.moveToDegrees(
                clamp(degrees, MIN_POSITION, MAX_POSITION)
        );
    }

    public void stow() {
        setAngle(STOW_POSITION);
    }

    public void intake() {
        setAngle(INTAKE_POSITION);
    }

    public void score() {
        setAngle(SCORE_POSITION);
    }

    public void stop() {
        if (armMotor != null) {
            armMotor.stop();
        }
    }

    public EasyMotor getArmMotor() {
        ensureInitialized();
        return armMotor;
    }

    @Override
    public void loop() {
        // EasyMotor handles position control and feedforward.
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void ensureInitialized() {
        if (armMotor == null) {
            throw new IllegalStateException("Arm has not been initialized.");
        }
    }
}
