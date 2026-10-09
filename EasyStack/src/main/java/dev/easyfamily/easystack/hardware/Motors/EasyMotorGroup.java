package dev.easyfamily.easystack.hardware.Motors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import dev.easyfamily.easystack.control.PIDCoefficients;
import dev.easyfamily.easystack.control.PIDFCoefficients;
import dev.easyfamily.easystack.controllable.EasyController;
import dev.easyfamily.easystack.controllable.EasyControllable;
import dev.easyfamily.easystack.feedforward.FFController;

public class EasyMotorGroup implements EasyControllable {

    public enum EncoderStrategy {
        AVERAGE,
        LEADER_ONLY,
        MAX,
        MIN
    }

    public static final double DEFAULT_CROSS_COUPLING_GAIN = 0.5;

    private final List<EasyMotor> motors;
    private EncoderStrategy encoderStrategy = EncoderStrategy.LEADER_ONLY;
    private int leaderIndex = 0;

    private boolean crossCoupled = false;
    private double crossCouplingGain = DEFAULT_CROSS_COUPLING_GAIN;

    public EasyMotorGroup(EasyMotor... motors) {
        if (motors == null || motors.length == 0) {
            throw new IllegalArgumentException("EasyMotorGroup requires at least one EasyMotor.");
        }

        this.motors = new ArrayList<>(Arrays.asList(motors));
    }

    public EasyMotorGroup crossCoupled() {
        return crossCoupled(crossCouplingGain);
    }

    public EasyMotorGroup crossCoupled(double gain) {
        this.crossCoupled = true;
        this.crossCouplingGain = Math.max(0.0, gain);

        if (encoderStrategy == EncoderStrategy.LEADER_ONLY) {
            encoderStrategy = EncoderStrategy.AVERAGE;
        }

        return this;
    }

    public EasyMotorGroup setCrossCouplingGain(double gain) {
        this.crossCouplingGain = Math.max(0.0, gain);
        return this;
    }

    public double getCrossCouplingGain() {
        return crossCouplingGain;
    }

    public boolean isCrossCoupled() {
        return crossCoupled && motors.size() > 1;
    }

    public EasyMotorGroup setEncoderStrategy(EncoderStrategy strategy) {
        this.encoderStrategy = strategy == null ? EncoderStrategy.LEADER_ONLY : strategy;
        return this;
    }

    public EasyMotorGroup setLeaderMotor(int index) {
        if (index >= 0 && index < motors.size()) {
            this.leaderIndex = index;
        }
        return this;
    }

    public EasyMotor getLeader() {
        return leader();
    }

    public List<EasyMotor> getMotors() {
        return new ArrayList<>(motors);
    }

    public EasyMotor get(int index) {
        return motors.get(index);
    }

    public List<EasyMotor> getFollowers() {
        List<EasyMotor> followers = new ArrayList<>(motors);
        followers.remove(leaderIndex);
        return followers;
    }

    public int size() {
        return motors.size();
    }

    @Override
    public void setPower(double power) {
        if (!isCrossCoupled()) {
            leader().setPower(power);
            mirrorLeader();
        } else {
            all(m -> m.setPower(power));
        }
    }

    @Override
    public double getPower() {
        return leader().getPower();
    }

    @Override
    public double getPosition() {
        return read(EasyMotor::getPosition);
    }

    @Override
    public double getVelocity() {
        return read(EasyMotor::getVelocity);
    }

    public double getRPM() {
        return read(EasyMotor::getRPM);
    }

    public double getRotations() {
        double cpr = leader().getCPR();
        return cpr <= 0 ? 0.0 : getPosition() / cpr;
    }

    public double getDegrees() {
        return getRotations() * 360.0;
    }

    public EasyMotorGroup setRPM(double rpm) {
        if (!isCrossCoupled()) {
            leader().setRPM(rpm);
            mirrorLeader();
        } else if (rpm == 0.0) {
            all(m -> m.setRPM(0.0));
        } else {
            coupled(rpm, EasyMotor::getRPM, EasyMotor::setRPM);
        }

        return this;
    }

    public EasyMotorGroup setSpeedPercent(double percent) {
        return setRPM(percent * leader().getMaxRPM());
    }

    public EasyMotorGroup moveToPosition(double ticks) {
        if (!isCrossCoupled()) {
            leader().moveToPosition(ticks);
            mirrorLeader();
        } else {
            coupled(ticks, EasyMotor::getPosition, EasyMotor::moveToPosition);
        }

        return this;
    }

    public EasyMotorGroup moveToRotations(double rotations) {
        return moveToPosition(rotations * leader().getCPR());
    }

    public EasyMotorGroup moveToDegrees(double degrees) {
        return moveToPosition(degrees / 360.0 * leader().getCPR());
    }

    public boolean isAtTarget() {
        if (!isCrossCoupled()) {
            return leader().isAtTarget();
        }

        for (EasyMotor motor : motors) {
            if (!motor.isAtTarget()) return false;
        }

        return true;
    }

    public EasyMotorGroup setVelocityPIDF(PIDFCoefficients coefficients) {
        all(m -> m.setVelocityPIDF(coefficients));
        return this;
    }

    public EasyMotorGroup setVelocityPIDF(double kP, double kI, double kD, double kF) {
        return setVelocityPIDF(new PIDFCoefficients(kP, kI, kD, kF));
    }

    public EasyMotorGroup setVelocityFF(FFController feedforward) {
        all(m -> m.setVelocityFF(feedforward));
        return this;
    }

    public EasyMotorGroup setVelocityFF(double kS, double kV) {
        return setVelocityFF(new FFController(kS, kV));
    }

    public EasyMotorGroup setVelocityFF(double kS, double kV, double kA) {
        return setVelocityFF(new FFController(kS, kV, kA));
    }

    public EasyMotorGroup setVelocityController(Supplier<EasyController> factory) {
        all(m -> m.setVelocityController(factory == null ? null : factory.get()));
        return this;
    }

    public EasyMotorGroup setPositionPID(PIDCoefficients coefficients) {
        all(m -> m.setPositionPID(coefficients));
        return this;
    }

    public EasyMotorGroup setPositionPID(double kP, double kI, double kD) {
        return setPositionPID(new PIDCoefficients(kP, kI, kD));
    }

    public EasyMotorGroup setPositionPIDF(PIDFCoefficients coefficients) {
        all(m -> m.setPositionPIDF(coefficients));
        return this;
    }

    public EasyMotorGroup setPositionPIDF(double kP, double kI, double kD, double kF) {
        return setPositionPIDF(new PIDFCoefficients(kP, kI, kD, kF));
    }

    public EasyMotorGroup setPositionController(Supplier<EasyController> factory) {
        all(m -> m.setPositionController(factory == null ? null : factory.get()));
        return this;
    }

    public EasyMotorGroup setPositionLimits(double minTicks, double maxTicks) {
        all(m -> m.setPositionLimits(minTicks, maxTicks));
        return this;
    }

    public EasyMotorGroup setDegreePositionLimits(double minDegrees, double maxDegrees) {
        all(m -> m.setDegreePositionLimits(minDegrees, maxDegrees));
        return this;
    }

    public EasyMotorGroup clearPositionLimits() {
        all(EasyMotor::clearPositionLimits);
        return this;
    }

    public boolean isAtUpperLimit() {
        return leader().isAtUpperLimit();
    }

    public boolean isAtLowerLimit() {
        return leader().isAtLowerLimit();
    }

    public EasyMotorGroup setDeadband(double deadband) {
        all(m -> m.setDeadband(deadband));
        return this;
    }

    public EasyMotorGroup setPowerRamp(double powerPerSecond) {
        all(m -> m.setPowerRamp(powerPerSecond));
        return this;
    }

    public EasyMotorGroup setVoltageCompensation(boolean enabled) {
        all(m -> m.setVoltageCompensation(enabled));
        return this;
    }

    public EasyMotorGroup setVoltageCompensation(double nominalVolts) {
        all(m -> m.setVoltageCompensation(nominalVolts));
        return this;
    }

    public boolean homeToPhysicalLimit(double power, double stallCurrentAmps) {
        boolean done = leader().homeToPhysicalLimit(power, stallCurrentAmps);
        mirrorLeader();

        if (done) {
            all(EasyMotor::setCurrentPositionAsZero);
        }

        return done;
    }

    public boolean isMoving() {
        return leader().isMoving();
    }

    public double getCurrentAmps() {
        double total = 0.0;

        for (EasyMotor motor : motors) {
            total += motor.getCurrentAmps();
        }

        return total;
    }

    public double getVoltage() {
        return leader().getVoltage();
    }

    public EasyMotorGroup setStaticFeedforward(double power) {
        all(m -> m.setStaticFeedforward(power));
        return this;
    }

    public EasyMotorGroup setPowerLimit(double limit) {
        all(m -> m.setPowerLimit(limit));
        return this;
    }

    public EasyMotorGroup setRPMTolerance(double rpm) {
        all(m -> m.setRPMTolerance(rpm));
        return this;
    }

    public EasyMotorGroup setPositionTolerance(double ticks) {
        all(m -> m.setPositionTolerance(ticks));
        return this;
    }

    public EasyMotorGroup setDegreesTolerance(double degrees) {
        all(m -> m.setDegreesTolerance(degrees));
        return this;
    }

    public EasyMotorGroup setType(EasyMotorType type) {
        all(m -> m.setType(type));
        return this;
    }

    public EasyMotorGroup setCPR(double cpr) {
        all(m -> m.setCPR(cpr));
        return this;
    }

    public EasyMotorGroup setMaxRPM(double maxRPM) {
        all(m -> m.setMaxRPM(maxRPM));
        return this;
    }

    public EasyMotorGroup setPowerCacheTolerance(double tolerance) {
        all(m -> m.setPowerCacheTolerance(tolerance));
        return this;
    }

    public EasyMotorGroup resetControllers() {
        all(EasyMotor::resetControllers);
        return this;
    }

    public EasyMotorGroup stop() {
        all(EasyMotor::stop);
        return this;
    }

    public EasyMotorGroup stopAndResetEncoder() {
        all(EasyMotor::stopAndResetEncoder);
        return this;
    }

    public EasyMotorGroup setCurrentPositionAsZero() {
        all(EasyMotor::setCurrentPositionAsZero);
        return this;
    }

    public EasyMotorGroup reverse() {
        all(EasyMotor::reverse);
        return this;
    }

    public EasyMotorGroup brakeMode() {
        all(EasyMotor::brakeMode);
        return this;
    }

    public EasyMotorGroup floatMode() {
        all(EasyMotor::floatMode);
        return this;
    }

    private EasyMotor leader() {
        return motors.get(leaderIndex);
    }

    private void all(Consumer<EasyMotor> action) {
        for (EasyMotor motor : motors) {
            action.accept(motor);
        }
    }

    private double read(ToDoubleFunction<EasyMotor> reading) {
        switch (encoderStrategy) {
            case MAX:
                return motors.stream().mapToDouble(reading).max().orElse(0.0);
            case MIN:
                return motors.stream().mapToDouble(reading).min().orElse(0.0);
            case AVERAGE:
                return motors.stream().mapToDouble(reading).average().orElse(0.0);
            case LEADER_ONLY:
            default:
                return reading.applyAsDouble(leader());
        }
    }

    private void mirrorLeader() {
        double power = leader().getPower();

        for (int i = 0; i < motors.size(); i++) {
            if (i != leaderIndex) {
                motors.get(i).setPowerDirect(power);
            }
        }
    }

    private void coupled(
            double target,
            ToDoubleFunction<EasyMotor> reading,
            BiConsumer<EasyMotor, Double> command
    ) {
        int n = motors.size();
        double[] values = new double[n];
        double sum = 0.0;

        for (int i = 0; i < n; i++) {
            values[i] = reading.applyAsDouble(motors.get(i));
            sum += values[i];
        }

        double average = sum / n;

        for (int i = 0; i < n; i++) {
            command.accept(motors.get(i), target + crossCouplingGain * (average - values[i]));
        }
    }
}