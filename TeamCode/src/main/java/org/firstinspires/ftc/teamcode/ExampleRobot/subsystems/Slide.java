package org.firstinspires.ftc.teamcode.ExampleRobot.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.easyfamily.EasyStack.hardware.Motors.EasyMotor;
import dev.easyfamily.EasyStack.hardware.Motors.EasyMotorGroup;
import dev.easyfamily.EasyStack.subsytem.EasySubsystemBase;

public class Slide extends EasySubsystemBase {

    private EasyMotor elevator1;
    private EasyMotor elevator2;
    private EasyMotorGroup elevator;

    // These targets are passed to moveToDegrees(), so they are degrees,
    // not raw encoder ticks. Calibrate them for the actual mechanism.
    private static final int HIGH_BASKET_POSITION = 2000;
    private static final int LOW_BASKET_POSITION = 1000;
    private static final int LOW_LIMIT_POSITION = 0;
    private static final int HIGH_LIMIT_POSITION = 2080;

    // TODO: Replace with gains tuned on the actual slide before operating it.
    private static final double SLIDE_KP = 0.0;
    private static final double SLIDE_KI = 0.0;
    private static final double SLIDE_KD = 0.0;
    private static final double SLIDE_KF = 0.0;

    @Override
    public void init(HardwareMap hardwareMap) {
        elevator1 = new EasyMotor(hardwareMap, "elevator");
        elevator2 = new EasyMotor(hardwareMap, "elevator1");

        elevator = new EasyMotorGroup(elevator1, elevator2)
                .reverse()
                .brakeMode()
                .setEncoderStrategy(EasyMotorGroup.EncoderStrategy.AVERAGE)
                .setDegreePositionLimits(
                        LOW_LIMIT_POSITION,
                        HIGH_LIMIT_POSITION
                )
                .setPositionPIDF(
                        SLIDE_KP,
                        SLIDE_KI,
                        SLIDE_KD,
                        SLIDE_KF
                )
                .crossCoupled()
                .stopAndResetEncoder();
    }

    public void basketZero() {
        moveToPosition(LOW_LIMIT_POSITION);
    }

    public void basketLow() {
        moveToPosition(LOW_BASKET_POSITION);
    }

    public void basketHigh() {
        moveToPosition(HIGH_BASKET_POSITION);
    }

    public void moveToPosition(int positionDegrees) {
        ensureInitialized();

        int target = Math.max(
                LOW_LIMIT_POSITION,
                Math.min(HIGH_LIMIT_POSITION, positionDegrees)
        );

        elevator.moveToDegrees(target);
    }

    public void stop() {
        if (elevator != null) {
            elevator.stop();
        }
    }

    public EasyMotorGroup getElevator() {
        ensureInitialized();
        return elevator;
    }

    @Override
    public void loop() {
        // EasyMotorGroup handles the position controller.
    }

    private void ensureInitialized() {
        if (elevator == null) {
            throw new IllegalStateException("Slide has not been initialized.");
        }
    }
}
