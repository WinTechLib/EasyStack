package dev.easyfamily.EasyStack.hardware.Motors;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import dev.easyfamily.EasyStack.control.EasyPIDCoefficients;
import dev.easyfamily.EasyStack.control.EasyPIDFCoefficients;
import dev.easyfamily.EasyStack.controllable.EasyController;
import dev.easyfamily.EasyStack.controllable.EasyControllable;
import dev.easyfamily.EasyStack.feedforward.EasyFFController;

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

    private double targetRPM = 0.0;
    private double targetPosition = 0.0;

    public EasyMotorGroup(EasyMotor... motors) {
        if (motors == null || motors.length == 0) {
            throw new IllegalArgumentException("EasyMotorGroup requires at least one EasyMotor.");
        }

        this.motors = new ArrayList<>(Arrays.asList(motors));
    }

    public EasyMotorGroup forEachMotor(Consumer<EasyMotor> action) {
        for (EasyMotor m : motors) {
            action.accept(m);
        }
        return this;
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
        if (index < 0 || index >= motors.size()) {
            throw new IllegalArgumentException(
                    "Leader index out of range: " + index
            );
        }

        this.leaderIndex = index;
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
        targetRPM = rpm;

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
        targetPosition = ticks;

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

    public EasyMotorGroup setVelocityPIDF(EasyPIDFCoefficients coefficients) {
        all(m -> m.setVelocityPIDF(coefficients));
        return this;
    }

    public EasyMotorGroup setVelocityPIDF(double kP, double kI, double kD, double kF) {
        return setVelocityPIDF(new EasyPIDFCoefficients(kP, kI, kD, kF));
    }

    public EasyMotorGroup setVelocityFF(EasyFFController feedforward) {
        all(m -> m.setVelocityFF(feedforward));
        return this;
    }

    public EasyMotorGroup setVelocityFF(double kS, double kV) {
        return setVelocityFF(new EasyFFController(kS, kV));
    }

    public EasyMotorGroup setVelocityFF(double kS, double kV, double kA) {
        return setVelocityFF(new EasyFFController(kS, kV, kA));
    }

    public EasyMotorGroup setVelocityController(Supplier<EasyController> factory) {
        all(m -> m.setVelocityController(factory == null ? null : factory.get()));
        return this;
    }

    public EasyMotorGroup setPositionPID(EasyPIDCoefficients coefficients) {
        all(m -> m.setPositionPID(coefficients));
        return this;
    }

    public EasyMotorGroup setPositionPID(double kP, double kI, double kD) {
        return setPositionPID(new EasyPIDCoefficients(kP, kI, kD));
    }

    public EasyMotorGroup setPositionPIDF(EasyPIDFCoefficients coefficients) {
        all(m -> m.setPositionPIDF(coefficients));
        return this;
    }

    public EasyMotorGroup setPositionPIDF(double kP, double kI, double kD, double kF) {
        return setPositionPIDF(new EasyPIDFCoefficients(kP, kI, kD, kF));
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

    public double getTargetRPM() {
        return targetRPM;
    }

    public double getRPMError() {
        return targetRPM - getRPM();
    }public double getTargetPosition() {
        return targetPosition;
    }

    public double getPositionError() {
        return targetPosition - getPosition();
    }

    public double getTargetRotations() {
        double cpr = leader().getCPR();
        return cpr <= 0 ? 0.0 : targetPosition / cpr;
    }

    public double getTargetDegrees() {
        return getTargetRotations() * 360.0;
    }

    public double getRotationsError() {
        return getTargetRotations() - getRotations();
    }

    public double getDegreesError() {
        return getTargetDegrees() - getDegrees();
    }

    public double getAcceleration() {
        return leader().getAcceleration();
    }

    public EasyMotor.ControlMode getControlMode() {
        return leader().getControlMode();
    }

    public EncoderStrategy getEncoderStrategy() {
        return encoderStrategy;
    }

    public int getLeaderIndex() {
        return leaderIndex;
    }
    public double[] getRPMs() {
        return readAll(EasyMotor::getRPM);
    }

    public double[] getPositions() {
        return readAll(EasyMotor::getPosition);
    }

    public double[] getVelocities() {
        return readAll(EasyMotor::getVelocity);
    }

    public double[] getPowers() {
        return readAll(EasyMotor::getPower);
    }

    public double[] getCurrents() {
        return readAll(EasyMotor::getCurrentAmps);
    }
    public double[] getRPMErrors() {
        double[] out = getRPMs();
        for (int i = 0; i < out.length; i++) out[i] = targetRPM - out[i];
        return out;
    }
    public double[] getPositionErrors() {
        double[] out = getPositions();
        for (int i = 0; i < out.length; i++) out[i] = targetPosition - out[i];
        return out;
    }
    public double getRPMSpread() {
        return spread(getRPMs());
    }
    public double getPositionSpread() {
        return spread(getPositions());
    }

    public double getMaxCurrentAmps() {
        double max = 0.0;
        for (double c : getCurrents()) max = Math.max(max, c);
        return max;
    }

    public boolean isOverCurrent() {
        for (EasyMotor motor : motors) {
            if (motor.isOverCurrent()) return true;
        }
        return false;
    }

    public boolean hasPositionLimits() {
        return leader().hasPositionLimits();
    }

    public double getMinPositionLimit() {
        return leader().getMinPositionLimit();
    }

    public double getMaxPositionLimit() {
        return leader().getMaxPositionLimit();
    }

    public double getPowerLimit() {
        return leader().getPowerLimit();
    }

    public double getControllerOutput() {
        return leader().getControllerOutput();
    }

    public double getFeedforwardOutput() {
        return leader().getFeedforwardOutput();
    }

    public double getCPR() {
        return leader().getCPR();
    }

    public double getMaxRPM() {
        return leader().getMaxRPM();
    }

    public String getDebugString() {
        return String.format(
                Locale.US,
                "Group[%s,%s,n=%d] rpm=%.1f/%.1f (err %.1f, spread %.1f) pos=%.0f/%.0f (err %.0f, spread %.0f) amps=%.2f (max %.2f)",
                getControlMode().name(), encoderStrategy.name(), motors.size(),
                getRPM(), targetRPM, getRPMError(), getRPMSpread(),
                getPosition(), targetPosition, getPositionError(), getPositionSpread(),
                getCurrentAmps(), getMaxCurrentAmps()
        );
    }
    public EasyMotorGroup addTelemetry(Telemetry telemetry, String prefix) {
        String p = prefix == null || prefix.isEmpty() ? "" : prefix + " ";
        telemetry.addData(p + "mode", getControlMode());
        telemetry.addData(p + "rpm / target / error", String.format(Locale.US, "%.1f / %.1f / %.1f", getRPM(), targetRPM, getRPMError()));
        telemetry.addData(p + "pos / target / error", String.format(Locale.US, "%.0f / %.0f / %.0f", getPosition(), targetPosition, getPositionError()));
        telemetry.addData(p + "rpm spread", String.format(Locale.US, "%.1f", getRPMSpread()));
        telemetry.addData(p + "pos spread", String.format(Locale.US, "%.0f", getPositionSpread()));
        telemetry.addData(p + "amps total / max", String.format(Locale.US, "%.2f / %.2f", getCurrentAmps(), getMaxCurrentAmps()));
        for (int i = 0; i < motors.size(); i++) {
            EasyMotor m = motors.get(i);
            telemetry.addData(p + "motor " + i + (i == leaderIndex ? " (leader)" : ""),
                    String.format(Locale.US, "rpm=%.1f pos=%.0f pwr=%.2f amps=%.2f",
                            m.getRPM(), m.getPosition(), m.getPower(), m.getCurrentAmps()));
        }
        return this;
    }

    public EasyMotorGroup addTelemetry(Telemetry telemetry) {
        return addTelemetry(telemetry, "");
    }

    private double[] readAll(ToDoubleFunction<EasyMotor> reading) {
        double[] out = new double[motors.size()];
        for (int i = 0; i < out.length; i++) out[i] = reading.applyAsDouble(motors.get(i));
        return out;
    }

    private static double spread(double[] values) {
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (double v : values) {
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        return values.length == 0 ? 0.0 : max - min;
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