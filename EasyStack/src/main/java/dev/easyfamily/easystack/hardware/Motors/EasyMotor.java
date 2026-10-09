package dev.easyfamily.easystack.hardware.Motors;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import dev.easyfamily.easystack.controllable.EasyControllable;

public class EasyMotor implements EasyControllable {

    private final DcMotorEx motor;

    private EasyMotorType type;
    private double cprOverride = Double.NaN;
    private double maxRPMOverride = Double.NaN;

    private double direction = 1.0;
    private double positionOffset = 0.0;

    private double lastPower = Double.NaN;
    private double cacheTolerance;

    private double lastVelocity = 0;
    private double lastAcceleration = 0;
    private long lastVelocityTime = System.nanoTime();

    public EasyMotor(HardwareMap hardwareMap, String name) {
        this(hardwareMap, name, EasyMotorType.CONFIG, 0.01);
    }

    public EasyMotor(HardwareMap hardwareMap, String name, EasyMotorType type) {
        this(hardwareMap, name, type, 0.01);
    }

    public EasyMotor(HardwareMap hardwareMap, String name, double cacheTolerance) {
        this(hardwareMap, name, EasyMotorType.CONFIG, cacheTolerance);
    }

    public EasyMotor(
            HardwareMap hardwareMap,
            String name,
            EasyMotorType type,
            double cacheTolerance
    ) {
        this.motor = hardwareMap.get(DcMotorEx.class, name);
        this.type = type == null ? EasyMotorType.CONFIG : type;
        this.cacheTolerance = Math.max(0.0, cacheTolerance);

        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    public EasyMotor setType(EasyMotorType type) {
        this.type = type == null ? EasyMotorType.CONFIG : type;
        cprOverride = Double.NaN;
        maxRPMOverride = Double.NaN;
        return this;
    }

    public EasyMotorType getType() {
        return type;
    }

    public EasyMotor setCPR(double cpr) {
        cprOverride = cpr;
        return this;
    }

    public EasyMotor setMaxRPM(double maxRPM) {
        maxRPMOverride = maxRPM;
        return this;
    }

    public double getCPR() {
        if (!Double.isNaN(cprOverride)) {
            return cprOverride;
        }

        if (!type.isConfig()) {
            return type.getCPR();
        }

        return motor.getMotorType().getTicksPerRev();
    }

    public double getMaxRPM() {
        if (!Double.isNaN(maxRPMOverride)) {
            return maxRPMOverride;
        }

        if (!type.isConfig()) {
            return type.getMaxRPM();
        }

        return motor.getMotorType().getMaxRPM();
    }

    public double getRPM() {
        double cpr = getCPR();
        return cpr <= 0 ? 0.0 : getVelocity() / cpr * 60.0;
    }

    public double getRotations() {
        double cpr = getCPR();
        return cpr <= 0 ? 0.0 : getPosition() / cpr;
    }

    public double getDegrees() {
        return getRotations() * 360.0;
    }

    public double getSpeedPercent() {
        double max = getMaxRPM();
        return max <= 0 ? 0.0 : Math.abs(getRPM()) / max;
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

    public EasyMotor stopAndResetEncoder() {
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        positionOffset = 0.0;
        lastPower = Double.NaN;

        return this;
    }

    public double getRawVelocity() {
        return motor.getVelocity();
    }

    @Override
    public double getVelocity() {
        return motor.getVelocity() * direction;
    }

    public double getAcceleration() {
        long now = System.nanoTime();
        double dt = (now - lastVelocityTime) / 1e9;

        if (dt < 0.005) {
            return lastAcceleration;
        }

        double velocity = getVelocity();

        lastAcceleration = (velocity - lastVelocity) / dt;
        lastVelocity = velocity;
        lastVelocityTime = now;

        return lastAcceleration;
    }

    @Override
    public void setPower(double power) {
        power = Math.max(-1.0, Math.min(1.0, power));

        double appliedPower = power * direction;

        boolean shouldWrite =
                Double.isNaN(lastPower)
                        || Math.abs(appliedPower - lastPower) > cacheTolerance
                        || (appliedPower == 0.0 && lastPower != 0.0)
                        || (Math.abs(appliedPower) == 1.0 && appliedPower != lastPower);

        if (shouldWrite) {
            motor.setPower(appliedPower);
            lastPower = appliedPower;
        }
    }

    @Override
    public double getPower() {
        return Double.isNaN(lastPower) ? 0.0 : lastPower * direction;
    }

    public EasyMotor stop() {
        setPower(0);
        return this;
    }

    public EasyMotor setCachingTolerance(double tolerance) {
        cacheTolerance = Math.max(0.0, tolerance);
        return this;
    }

    public double getCachingTolerance() {
        return cacheTolerance;
    }

    public EasyMotor reverse() {
        direction *= -1.0;
        lastPower = Double.NaN;
        return this;
    }

    public EasyMotor reversed() {
        return reverse();
    }

    public EasyMotor setInverted(boolean inverted) {
        direction = inverted ? -1.0 : 1.0;
        lastPower = Double.NaN;
        return this;
    }

    public boolean getInverted() {
        return direction == -1.0;
    }

    public EasyMotor floatMode() {
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        return this;
    }

    public EasyMotor brakeMode() {
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        return this;
    }

    public double getCurrent(CurrentUnit unit) {
        return motor.getCurrent(unit);
    }

    public double getCurrentAlert(CurrentUnit unit) {
        return motor.getCurrentAlert(unit);
    }

    public EasyMotor setCurrentAlert(double current, CurrentUnit unit) {
        motor.setCurrentAlert(current, unit);
        return this;
    }

    public boolean isOverCurrent() {
        return motor.isOverCurrent();
    }

    public DcMotorEx getMotor() {
        return motor;
    }
}