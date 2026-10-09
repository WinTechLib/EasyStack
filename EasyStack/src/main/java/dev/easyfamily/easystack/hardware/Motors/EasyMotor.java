package dev.easyfamily.easystack.hardware.Motors;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import dev.easyfamily.easystack.control.EasyPIDFController;
import java.util.Iterator;
import java.util.Locale;

import dev.easyfamily.easystack.control.PIDCoefficients;
import dev.easyfamily.easystack.control.PIDFCoefficients;
import dev.easyfamily.easystack.controllable.EasyController;
import dev.easyfamily.easystack.controllable.EasyControllable;
import dev.easyfamily.easystack.feedforward.FFController;

public class EasyMotor implements EasyControllable {

    private enum Mode {
        POWER,
        VELOCITY,
        POSITION
    }

    private final DcMotorEx motor;

    private EasyMotorType type;
    private double cprOverride = Double.NaN;
    private double maxRPMOverride = Double.NaN;

    private double direction = 1.0;
    private double positionOffset = 0.0;

    private double lastPower = Double.NaN;
    private double powerCacheTolerance;

    private double lastVelocity = 0;
    private double lastAcceleration = 0;
    private long lastVelocityTime = System.nanoTime();

    private Mode mode = Mode.POWER;
    private double powerLimit = 1.0;
    private double staticFeedforward = 0.0;

    private final EasyPIDFController velocityPID = new EasyPIDFController(0, 0, 0, 0);
    private final EasyPIDFController positionPID = new EasyPIDFController(0, 0, 0, 0);

    private EasyController customVelocityController;
    private EasyController customPositionController;
    private FFController velocityFF;
    private PIDFCoefficients positionPIDFCoeffs;

    private PIDFCoefficients velocityCoeffs;
    private PIDCoefficients positionCoeffs;
    private PIDFCoefficients velocityDefaults;
    private PIDCoefficients positionDefaults;

    private double rpmTolerance = Double.NaN;
    private double positionTolerance = Double.NaN;

    private double targetRPM = 0.0;
    private double targetPosition = 0.0;

    private final VoltageSensor voltageSensor;
    private boolean voltageCompensation = false;
    private double nominalVoltage = 12.0;
    private double voltage = Double.NaN;
    private long lastVoltageRead = 0L;

    private double deadband = 0.0;
    private double rampRate = 0.0;
    private double rampOutput = 0.0;
    private long lastRampTime = 0L;

    private boolean hasPositionLimits = false;
    private double minPositionLimit = 0.0;
    private double maxPositionLimit = 0.0;

    private long homeStart = 0L;
    private long lastHomeCall = 0L;

    public EasyMotor(HardwareMap hardwareMap, String name) {
        this(hardwareMap, name, EasyMotorType.CONFIG, 0.01);
    }

    public EasyMotor(HardwareMap hardwareMap, String name, EasyMotorType type) {
        this(hardwareMap, name, type, 0.01);
    }

    public EasyMotor(HardwareMap hardwareMap, String name, double powerCacheTolerance) {
        this(hardwareMap, name, EasyMotorType.CONFIG, powerCacheTolerance);
    }

    public EasyMotor(
            HardwareMap hardwareMap,
            String name,
            EasyMotorType type,
            double powerCacheTolerance
    ) {
        this.motor = hardwareMap.get(DcMotorEx.class, name);
        this.type = type == null ? EasyMotorType.CONFIG : type;
        this.powerCacheTolerance = Math.max(0.0, powerCacheTolerance);

        Iterator<VoltageSensor> sensors = hardwareMap.voltageSensor.iterator();
        this.voltageSensor = sensors.hasNext() ? sensors.next() : null;

        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    public EasyMotor(
            HardwareMap hardwareMap,
            String name,
            EasyMotorType type,
            PIDCoefficients positionPID
    ) {
        this(hardwareMap, name, type, 0.01);
        setPositionPID(positionPID);
    }

    public EasyMotor(
            HardwareMap hardwareMap,
            String name,
            EasyMotorType type,
            PIDFCoefficients positionPIDF
    ) {
        this(hardwareMap, name, type, 0.01);
        setPositionPIDF(positionPIDF);
    }

    public EasyMotor(
            HardwareMap hardwareMap,
            String name,
            EasyMotorType type,
            FFController velocityFF
    ) {
        this(hardwareMap, name, type, 0.01);
        setVelocityFF(velocityFF);
    }

    public EasyMotor(
            HardwareMap hardwareMap,
            String name,
            EasyMotorType type,
            FFController velocityFF,
            PIDFCoefficients positionPIDF
    ) {
        this(hardwareMap, name, type, 0.01);
        setVelocityFF(velocityFF);
        setPositionPIDF(positionPIDF);
    }

    public EasyMotor(
            HardwareMap hardwareMap,
            String name,
            EasyMotorType type,
            EasyController velocityController,
            FFController velocityFF,
            EasyController positionController
    ) {
        this(hardwareMap, name, type, 0.01);
        setVelocityController(velocityController);
        setVelocityFF(velocityFF);
        setPositionController(positionController);
    }

    public EasyMotor setType(EasyMotorType type) {
        this.type = type == null ? EasyMotorType.CONFIG : type;
        cprOverride = Double.NaN;
        maxRPMOverride = Double.NaN;
        clearDefaults();
        return this;
    }

    public EasyMotorType getType() {
        return type;
    }

    public EasyMotor setCPR(double cpr) {
        cprOverride = cpr;
        clearDefaults();
        return this;
    }

    public EasyMotor setMaxRPM(double maxRPM) {
        maxRPMOverride = maxRPM;
        clearDefaults();
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

    public double getRotationsPerSecond() {
        return getRPM() / 60.0;
    }

    public double getDegreesPerSecond() {
        return getRPM() * 6.0;
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

    public EasyMotor setRPM(double rpm) {
        if (mode != Mode.VELOCITY) {
            resetControllers();
            mode = Mode.VELOCITY;
        }

        targetRPM = rpm;

        if (rpm == 0.0) {
            resetControllers();
            writePower(0.0);
            return this;
        }

        EasyController controller = customVelocityController;

        if (controller == null) {
            velocityPID.setPIDF(activeVelocityCoeffs());
            controller = velocityPID;
        }

        double output = controller.calculate(getRPM(), rpm);

        if (velocityFF != null) {
            output += velocityFF.calculate(rpm);
        }

        writePower(clampPower(output * calculateVoltageMultiplier()));

        return this;
    }

    public EasyMotor setSpeedPercent(double percent) {
        return setRPM(percent * getMaxRPM());
    }

    public EasyMotor setRotationsPerSecond(double rotationsPerSecond) {
        return setRPM(rotationsPerSecond * 60.0);
    }

    public EasyMotor setDegreesPerSecond(double degreesPerSecond) {
        return setRPM(degreesPerSecond / 6.0);
    }

    public EasyMotor setVelocityTicks(double ticksPerSecond) {
        double cpr = getCPR();
        return setRPM(cpr <= 0 ? 0.0 : ticksPerSecond / cpr * 60.0);
    }

    public double getTargetRPM() {
        return targetRPM;
    }

    public double getRPMError() {
        return targetRPM - getRPM();
    }

    public EasyMotor setVelocityPIDF(PIDFCoefficients coefficients) {
        velocityCoeffs = coefficients;
        customVelocityController = null;
        return this;
    }

    public EasyMotor setVelocityPIDF(double kP, double kI, double kD, double kF) {
        return setVelocityPIDF(new PIDFCoefficients(kP, kI, kD, kF));
    }

    public EasyMotor setVelocityController(EasyController controller) {
        customVelocityController = controller;
        return this;
    }

    public EasyMotor setVelocityFF(FFController feedforward) {
        velocityFF = feedforward;
        velocityDefaults = null;
        return this;
    }

    public EasyMotor setVelocityFF(double kS, double kV) {
        return setVelocityFF(new FFController(kS, kV));
    }

    public EasyMotor setVelocityFF(double kS, double kV, double kA) {
        return setVelocityFF(new FFController(kS, kV, kA));
    }

    public EasyMotor setRPMTolerance(double rpm) {
        rpmTolerance = Math.abs(rpm);
        return this;
    }

    public EasyMotor moveToPosition(double ticks) {
        if (mode != Mode.POSITION) {
            resetControllers();
            mode = Mode.POSITION;
        }

        if (hasPositionLimits) {
            ticks = Math.max(minPositionLimit, Math.min(maxPositionLimit, ticks));
        }

        targetPosition = ticks;

        EasyController controller;

        if (customPositionController != null) {
            controller = customPositionController;
        } else {
            if (positionPIDFCoeffs != null) {
                positionPID.setPIDF(positionPIDFCoeffs);
            } else {
                PIDCoefficients c = activePositionCoeffs();
                positionPID.setPIDF(c.kP, c.kI, c.kD, 0.0);
            }

            controller = positionPID;
        }

        writePower(clampPower((controller.calculate(getPosition(), ticks) + staticFeedforward) * calculateVoltageMultiplier()));

        return this;
    }

    public EasyMotor moveToRotations(double rotations) {
        return moveToPosition(rotations * getCPR());
    }

    public EasyMotor moveToDegrees(double degrees) {
        return moveToPosition(degrees / 360.0 * getCPR());
    }

    public double getTargetPosition() {
        return targetPosition;
    }

    public double getPositionError() {
        return targetPosition - getPosition();
    }

    public EasyMotor setPositionPID(PIDCoefficients coefficients) {
        positionCoeffs = coefficients;
        positionPIDFCoeffs = null;
        customPositionController = null;
        return this;
    }

    public EasyMotor setPositionPID(double kP, double kI, double kD) {
        return setPositionPID(new PIDCoefficients(kP, kI, kD));
    }

    public EasyMotor setPositionPIDF(PIDFCoefficients coefficients) {
        positionPIDFCoeffs = coefficients;
        customPositionController = null;
        return this;
    }

    public EasyMotor setPositionPIDF(double kP, double kI, double kD, double kF) {
        return setPositionPIDF(new PIDFCoefficients(kP, kI, kD, kF));
    }

    public EasyMotor setPositionController(EasyController controller) {
        customPositionController = controller;
        return this;
    }

    public EasyMotor setStaticFeedforward(double power) {
        staticFeedforward = power;
        return this;
    }

    public EasyMotor setPositionTolerance(double ticks) {
        positionTolerance = Math.abs(ticks);
        return this;
    }

    public EasyMotor setDegreesTolerance(double degrees) {
        positionTolerance = Math.abs(degrees) / 360.0 * getCPR();
        return this;
    }

    public EasyMotor setPowerLimit(double limit) {
        powerLimit = Math.max(0.0, Math.min(1.0, Math.abs(limit)));
        return this;
    }

    public boolean isAtTarget() {
        if (mode == Mode.VELOCITY) {
            return Math.abs(getRPMError()) <= activeRPMTolerance();
        }

        if (mode == Mode.POSITION) {
            return Math.abs(getPositionError()) <= activePositionTolerance();
        }

        return false;
    }

    public boolean isAtRPM() {
        return mode == Mode.VELOCITY && isAtTarget();
    }

    public boolean isAtPosition() {
        return mode == Mode.POSITION && isAtTarget();
    }

    public EasyMotor resetControllers() {
        velocityPID.reset();
        positionPID.reset();

        if (customVelocityController != null) {
            customVelocityController.reset();
        }

        if (customPositionController != null) {
            customPositionController.reset();
        }

        return this;
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

    public EasyMotor setCurrentPositionAsZero() {
        positionOffset = getRawPosition();
        resetControllers();
        return this;
    }

    public EasyMotor setPositionOffset(double position) {
        positionOffset = getRawPosition() - (position * direction);
        resetControllers();
        return this;
    }

    public EasyMotor stopAndResetEncoder() {
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        positionOffset = 0.0;
        lastPower = Double.NaN;
        resetControllers();

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
        mode = Mode.POWER;

        if (Math.abs(power) < deadband) {
            power = 0.0;
        }

        writePower(power);
    }

    void setPowerDirect(double power) {
        mode = Mode.POWER;
        writePower(power, false);
    }

    @Override
    public double getPower() {
        return Double.isNaN(lastPower) ? 0.0 : lastPower * direction;
    }

    public EasyMotor stop() {
        mode = Mode.POWER;
        writePower(0.0, false);
        return this;
    }

    public EasyMotor setDeadband(double deadband) {
        this.deadband = Math.abs(deadband);
        return this;
    }

    public EasyMotor setPowerRamp(double powerPerSecond) {
        this.rampRate = Math.max(0.0, powerPerSecond);
        return this;
    }

    public EasyMotor setPositionLimits(double minTicks, double maxTicks) {
        this.minPositionLimit = Math.min(minTicks, maxTicks);
        this.maxPositionLimit = Math.max(minTicks, maxTicks);
        this.hasPositionLimits = true;
        return this;
    }

    public EasyMotor setDegreePositionLimits(double minDegrees, double maxDegrees) {
        double cpr = getCPR();
        return setPositionLimits(minDegrees / 360.0 * cpr, maxDegrees / 360.0 * cpr);
    }

    public EasyMotor clearPositionLimits() {
        this.hasPositionLimits = false;
        return this;
    }

    public boolean isAtUpperLimit() {
        return hasPositionLimits && getPosition() >= maxPositionLimit;
    }

    public boolean isAtLowerLimit() {
        return hasPositionLimits && getPosition() <= minPositionLimit;
    }

    public boolean homeToPhysicalLimit(double power, double stallCurrentAmps) {
        long now = System.nanoTime();

        if (now - lastHomeCall > 250_000_000L) {
            homeStart = now;
        }

        lastHomeCall = now;

        boolean previousLimits = hasPositionLimits;
        hasPositionLimits = false;
        setPower(power);
        hasPositionLimits = previousLimits;

        boolean settled = now - homeStart > 300_000_000L;

        if (settled && getCurrentAmps() > stallCurrentAmps) {
            stop();
            setCurrentPositionAsZero();
            lastHomeCall = 0L;
            return true;
        }

        return false;
    }

    public EasyMotor setVoltageCompensation(boolean enabled) {
        this.voltageCompensation = enabled;
        return this;
    }

    public EasyMotor setVoltageCompensation(double nominalVolts) {
        this.nominalVoltage = Math.abs(nominalVolts);
        this.voltageCompensation = nominalVolts != 0.0;
        return this;
    }

    public double getVoltage() {
        if (voltageSensor == null) {
            return Double.NaN;
        }

        long now = System.nanoTime();

        if (lastVoltageRead == 0L || now - lastVoltageRead > 1_000_000_000L) {
            voltage = voltageSensor.getVoltage();
            lastVoltageRead = now;
        }

        return voltage;
    }

    public double getCurrentAmps() {
        return motor.getCurrent(CurrentUnit.AMPS);
    }

    public boolean isMoving() {
        return isMoving(5.0);
    }

    public boolean isMoving(double rpmThreshold) {
        return Math.abs(getRPM()) > rpmThreshold;
    }

    @Override
    public String toString() {
        return String.format(
                Locale.US,
                "EasyMotor{rpm=%.1f, position=%.0f, power=%.2f}",
                getRPM(),
                getPosition(),
                getPower()
        );
    }

    public EasyMotor setPowerCacheTolerance(double tolerance) {
        powerCacheTolerance = Math.max(0.0, tolerance);
        return this;
    }

    public double getPowerCacheTolerance() {
        return powerCacheTolerance;
    }

    public EasyMotor reverse() {
        direction *= -1.0;
        lastPower = Double.NaN;
        resetControllers();
        return this;
    }

    public EasyMotor reversed() {
        return reverse();
    }

    public EasyMotor setInverted(boolean inverted) {
        direction = inverted ? -1.0 : 1.0;
        lastPower = Double.NaN;
        resetControllers();
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

    private void writePower(double power) {
        writePower(power, true);
    }

    private void writePower(double power, boolean applyShaping) {
        power = Math.max(-1.0, Math.min(1.0, power));

        if (applyShaping) {
            power = applyPositionLimits(power);
            power = applyPowerRamp(power);
        } else {
            rampOutput = power;
        }

        double appliedPower = power * direction;

        boolean shouldWrite =
                Double.isNaN(lastPower)
                        || Math.abs(appliedPower - lastPower) > powerCacheTolerance
                        || (appliedPower == 0.0 && lastPower != 0.0)
                        || (Math.abs(appliedPower) == 1.0 && appliedPower != lastPower);

        if (shouldWrite) {
            motor.setPower(appliedPower);
            lastPower = appliedPower;
        }
    }

    private double applyPositionLimits(double power) {
        if (!hasPositionLimits) {
            return power;
        }

        double position = getPosition();

        if (position >= maxPositionLimit) {
            power = Math.min(power, staticFeedforward);
        }

        if (position <= minPositionLimit) {
            power = Math.max(power, 0.0);
        }

        return power;
    }

    private double applyPowerRamp(double power) {
        if (rampRate <= 0.0) {
            rampOutput = power;
            return power;
        }

        long now = System.nanoTime();
        double dt = lastRampTime == 0L ? 0.0 : Math.min((now - lastRampTime) / 1e9, 0.1);
        lastRampTime = now;

        boolean increasing = Math.abs(power) > Math.abs(rampOutput) && power * rampOutput >= 0.0;

        if (increasing) {
            double magnitude = Math.min(Math.abs(power), Math.abs(rampOutput) + rampRate * dt);
            rampOutput = Math.signum(power) * magnitude;
        } else {
            rampOutput = power;
        }

        return rampOutput;
    }

    private double calculateVoltageMultiplier() {
        if (!voltageCompensation) {
            return 1.0;
        }

        double currentVoltage = getVoltage();

        if (Double.isNaN(currentVoltage) || currentVoltage < 6.0) {
            return 1.0;
        }

        return Math.max(0.7, Math.min(1.4, nominalVoltage / currentVoltage));
    }

    private double clampPower(double power) {
        return Math.max(-powerLimit, Math.min(powerLimit, power));
    }

    private void clearDefaults() {
        velocityDefaults = null;
        positionDefaults = null;
    }

    private PIDFCoefficients activeVelocityCoeffs() {
        if (velocityCoeffs != null) {
            return velocityCoeffs;
        }

        if (velocityDefaults == null) {
            double max = getMaxRPM();

            velocityDefaults = new PIDFCoefficients(
                    max > 0 ? 0.5 / max : 0.0,
                    0.0,
                    0.0,
                    max > 0 && velocityFF == null ? 1.0 / max : 0.0
            );
        }

        return velocityDefaults;
    }

    private PIDCoefficients activePositionCoeffs() {
        if (positionCoeffs != null) {
            return positionCoeffs;
        }

        if (positionDefaults == null) {
            double cpr = getCPR();

            positionDefaults = new PIDCoefficients(
                    cpr > 0 ? 2.0 / cpr : 0.002,
                    0.0,
                    0.0
            );
        }

        return positionDefaults;
    }

    private double activeRPMTolerance() {
        if (!Double.isNaN(rpmTolerance)) {
            return rpmTolerance;
        }

        double max = getMaxRPM();
        return max > 0 ? max * 0.03 : 20.0;
    }

    private double activePositionTolerance() {
        if (!Double.isNaN(positionTolerance)) {
            return positionTolerance;
        }

        double cpr = getCPR();
        return cpr > 0 ? cpr / 72.0 : 10.0;
    }
}