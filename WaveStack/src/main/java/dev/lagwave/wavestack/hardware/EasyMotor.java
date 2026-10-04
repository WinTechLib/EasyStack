package dev.lagwave.wavestack.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import dev.lagwave.wavestack.controllable.EasyControllable;

public class EasyMotor implements EasyControllable {

    private final DcMotorEx motor;

    private double direction = 1.0;
    private double positionOffset = 0.0;

    private double lastPower = Double.NaN;
    private final double cacheTolerance;

    private double lastVelocity = 0;
    private long lastVelocityTime = System.nanoTime();

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

    public double getRawVelocity() {
        return motor.getVelocity();
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

    public EasyMotor stop() {
        setPower(0);
        return this;
    }

    public double getAcceleration() {

        double velocity = getVelocity();
        long now = System.nanoTime();

        double dt = (now - lastVelocityTime) / 1e9;

        if (dt <= 0) {
            return 0;
        }

        double acceleration = (velocity - lastVelocity) / dt;

        lastVelocity = velocity;
        lastVelocityTime = now;

        return acceleration;
    }

    public double getCurrentAlert(CurrentUnit unit) {
        return motor.getCurrentAlert(unit);
    }

    public EasyMotor setCurrentAlert(
            double current,
            CurrentUnit unit
    ) {
        motor.setCurrentAlert(current, unit);
        return this;
    }

    public boolean isOverCurrent() {
        return motor.isOverCurrent();
    }

    public double getCPR() {
        return motor.getMotorType().getTicksPerRev();
    }

    public double getMaxRPM() {
        return motor.getMotorType().getMaxRPM();
    }

    public double getRPM() {
        return getVelocity()
                / getCPR()
                * 60.0;
    }

    public EasyMotor setInverted(boolean inverted) {
        direction = inverted ? -1.0 : 1.0;
        return this;
    }

    public boolean getInverted() {
        return direction == -1.0;
    }

    public EasyMotor stopAndResetEncoder() {
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        positionOffset = 0.0;
        lastPower = Double.NaN;

        return this;
    }
}