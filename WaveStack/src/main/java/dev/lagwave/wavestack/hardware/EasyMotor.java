package dev.lagwave.wavestack.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.lagwave.wavestack.controllable.EasyControllable;

public class EasyMotor implements EasyControllable {

    private final DcMotorEx motor;

    private double direction = 1.0;
    private double positionOffset = 0.0;

    private double lastPower = Double.NaN;
    private final double cacheTolerance;

    public EasyMotor(HardwareMap hardwareMap, String name) {
        this(hardwareMap, name, 0.01);
    }

    public EasyMotor(
            HardwareMap hardwareMap,
            String name,
            double cacheTolerance
    ) {
        this.motor = hardwareMap.get(DcMotorEx.class, name);
        this.cacheTolerance = cacheTolerance;
    }

    public double getRawPosition() {
        return motor.getCurrentPosition();
    }

    @Override
    public double getPosition() {
        return (getRawPosition() - positionOffset) * direction;
    }

    public double getCurrentPosition() {
        return getPosition();
    }

    public EasyMotor atPosition(double position) {
        positionOffset = getRawPosition() - (position * direction);
        return this;
    }

    public EasyMotor zero() {
        return atPosition(0.0);
    }

    public EasyMotor zeroed() {
        return zero();
    }

    @Override
    public double getVelocity() {
        return motor.getVelocity() * direction;
    }

    @Override
    public void setPower(double power) {

        double appliedPower = power * direction;

        if (
                Double.isNaN(lastPower)
                        || Math.abs(appliedPower - lastPower) > cacheTolerance
        ) {

            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setPower(appliedPower);

            lastPower = appliedPower;
        }
    }

    @Override
    public double getPower() {
        return lastPower;
    }

    public EasyMotor reverse() {
        direction *= -1.0;
        return this;
    }

    public EasyMotor reversed() {
        return reverse();
    }

    public EasyMotor floatMode() {
        motor.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.FLOAT
        );

        return this;
    }

    public EasyMotor brakeMode() {
        motor.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE
        );

        return this;
    }

    public DcMotorEx getMotor() {
        return motor;
    }
}