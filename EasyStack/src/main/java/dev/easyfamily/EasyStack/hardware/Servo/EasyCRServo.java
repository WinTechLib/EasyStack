package dev.easyfamily.EasyStack.hardware.Servo;

import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.easyfamily.EasyStack.control.EasyPIDController;
import dev.easyfamily.EasyStack.control.EasyPIDCoefficients;
import dev.easyfamily.EasyStack.controllable.EasyRpmControllable;
import dev.easyfamily.EasyStack.feedforward.EasyFFController;
import dev.easyfamily.EasyStack.feedforward.EasyFFCoefficients;
import dev.easyfamily.EasyStack.util.LowPassFilter;

public class EasyCRServo implements dev.easyfamily.EasyStack.controllable.EasyCRServo, EasyRpmControllable {
    private final CRServoImplEx servo;
    private final String name;
    private double cachingTolerance = 0.005;
    private double lastPower = Double.NaN;
    private boolean inverted = false;

    private EasyAbsoluteAnalogEncoder encoder;
    private java.util.function.DoubleSupplier genericEncoder;
    private double encoderFullRotationTicks = 8192.0;

    private EasyPIDController pid;
    private EasyFFController ff;
    private double target = Double.NaN;
    private double targetTolerance;
    private boolean inPerfil = false;
    private double maxRpm = 0.0;
    private double minimumPower = 0.0;
    private EasyPIDController velPid;
    private double targetRpm = Double.NaN;
    private double toleranceRpm = 10.0;
    private double encoderSignal = 1.0;

    private double measuredRpm = Double.NaN;
    private double lastPositionValue;
    private long lastTimer;
    private boolean validSample = false;
    private LowPassFilter rpmFilter = new LowPassFilter(0.05);

    public EasyCRServo(HardwareMap hwMap, String name) {
        this.servo = hwMap.get(CRServoImplEx.class, name);
        this.name = name;
    }

    @Override
    public EasyCRServo setInverted(boolean inverted) {
        this.inverted = inverted;
        servo.setDirection(inverted ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
        lastPower = Double.NaN;
        return this;
    }

    @Override
    public EasyCRServo reverse() {
        return setInverted(!inverted);
    }

    @Override
    public EasyCRServo setPwmRange(PwmControl.PwmRange range) {
        servo.setPwmRange(range);
        lastPower = Double.NaN;
        return this;
    }

    @Override
    public EasyCRServo setCachingTolerance(double tolerance) {
        this.cachingTolerance = Math.max(0.0, tolerance);
        return this;
    }

    public EasyCRServo setPositionalControl(EasyAbsoluteAnalogEncoder encoder, EasyPIDCoefficients coefficients, double targetTolerance) {
        this.encoder = encoder;
        this.genericEncoder = null;
        this.validSample = false;
        this.pid = new EasyPIDController(coefficients);
        this.targetTolerance = targetTolerance;
        return this;
    }

    public EasyCRServo setPositionalControl(java.util.function.DoubleSupplier positionSupplier, double ticksPerRevolution, EasyPIDCoefficients coefficients, double targetTolerance) {
        this.encoder = null;
        this.genericEncoder = positionSupplier;
        this.encoderFullRotationTicks = ticksPerRevolution;
        this.validSample = false;
        this.pid = new EasyPIDController(coefficients);
        this.targetTolerance = targetTolerance;
        return this;
    }

    @Override
    public EasyCRServo setPositionalControl(EasyAbsoluteAnalogEncoder encoder, EasyPIDCoefficients coefficients, double tolerance, AngleUnit unit) {
        return setPositionalControl(encoder, coefficients, encoder.getUnit().fromUnit(unit, tolerance));
    }

    @Override
    public EasyCRServo setFeedforward(EasyFFCoefficients coefficients) {
        this.ff = coefficients == null ? null : new EasyFFController(coefficients);
        return this;
    }

    public EasyCRServo setMaxRpm(double rpm) {
        if (rpm <= 0) throw new IllegalArgumentException("maxRpm needs to be > 0. Servo: " + name);
        this.maxRpm = rpm;
        return this;
    }

    public EasyCRServo setMinPower(double minimumPower) {
        this.minimumPower = Math.max(0.0, Math.min(0.95, minimumPower));
        return this;
    }

    public EasyCRServo setVelocityControl(EasyAbsoluteAnalogEncoder encoder, EasyPIDCoefficients coefficients) {
        this.encoder = encoder;
        this.genericEncoder = null;
        this.validSample = false;
        this.velPid = new EasyPIDController(coefficients);
        return this;
    }

    public EasyCRServo setVelocityControl(java.util.function.DoubleSupplier positionSupplier, double ticksPerRevolution, EasyPIDCoefficients coefficients) {
        this.encoder = null;
        this.genericEncoder = positionSupplier;
        this.encoderFullRotationTicks = ticksPerRevolution;
        this.validSample = false;
        this.velPid = new EasyPIDController(coefficients);
        return this;
    }

    public EasyCRServo setVelocityFilter(double timeConstantSeconds) {
        this.rpmFilter = new LowPassFilter(timeConstantSeconds);
        return this;
    }

    public EasyCRServo setEncoderInverted(boolean inverted) {
        this.encoderSignal = inverted ? -1.0 : 1.0;
        return this;
    }

    public EasyCRServo setRpmTolerance(double rpm) {
        this.toleranceRpm = Math.abs(rpm);
        return this;
    }

    public void setTargetRpm(double rpm) {
        requireVelocityModel();
        boolean newMode = Double.isNaN(targetRpm);
        targetRpm = Math.max(-maxRpm, Math.min(maxRpm, rpm));
        target = Double.NaN;
        if (newMode && velPid != null) velPid.reset();
        write(rpmToPower(targetRpm));
    }

    public void setTargetAngularVelocity(double velocity, AngleUnit unit) {
        setTargetRpm(unit.toRadians(velocity) * 60.0 / (2.0 * Math.PI));
    }

    public double rpmToPower(double rpm) {
        requireVelocityModel();
        if (rpm == 0.0) return 0.0;
        double fraction = Math.min(1.0, Math.abs(rpm) / maxRpm);
        return Math.signum(rpm) * (minimumPower + (1.0 - minimumPower) * fraction);
    }

    public double powerToRpm(double power) {
        requireVelocityModel();
        double a = Math.abs(power);
        if (a <= minimumPower) return 0.0;
        return Math.signum(power) * (a - minimumPower) / (1.0 - minimumPower) * maxRpm;
    }

    public double getMeasuredRpm() {
        velocitySample();
        return (encoder == null && genericEncoder == null) ? Double.NaN : measuredRpm;
    }

    public double getEstimatedRpm() {
        if (maxRpm <= 0) return Double.NaN;
        return powerToRpm(getPower());
    }

    public double getRpm() {
        double m = getMeasuredRpm();
        return Double.isNaN(m) ? getEstimatedRpm() : m;
    }

    public double getAngularVelocity(AngleUnit unit) {
        double radPerSec = getRpm() * 2.0 * Math.PI / 60.0;
        return unit.fromRadians(radPerSec);
    }

    public double getTargetRpm() { return targetRpm; }

    public double getMaxRpm() { return maxRpm; }

    public boolean isAtTargetRpm() {
        if (Double.isNaN(targetRpm)) return false;
        if (encoder == null && genericEncoder == null) return true;
        double m = getMeasuredRpm();
        return !Double.isNaN(m) && Math.abs(targetRpm - m) <= toleranceRpm;
    }

    @Override
    public void setPower(double power) {
        target = Double.NaN;
        targetRpm = Double.NaN;
        write(power);
    }

    @Override
    public void stop() {
        setPower(0.0);
    }

    private double getCurrentPositionValue() {
        if (encoder != null) return encoder.getAngle();
        if (genericEncoder != null) return genericEncoder.getAsDouble();
        return 0.0;
    }

    private double getFullRotationValue() {
        if (encoder != null) return encoder.getFullRotation();
        return encoderFullRotationTicks;
    }

    private boolean hasEncoder() {
        return encoder != null || genericEncoder != null;
    }

    private boolean sameAngle(double a, double b) {
        double fullRot = getFullRotationValue();
        double difference = (a - b) % fullRot;

        if (difference > fullRot / 2.0) {
            difference -= fullRot;
        }

        if (difference <= -fullRot / 2.0) {
            difference += fullRot;
        }

        return Math.abs(difference) < 1e-9;
    }

    @Override
    public void setTargetAngle(double angle) {
        requirePositional();

        double newTarget = normalize(angle);

        if (Double.isNaN(target) || !sameAngle(target, newTarget)) {
            target = newTarget;
            pid.reset();
        }
    }

    @Override
    public void update() {
        velocitySample();
        if (!Double.isNaN(targetRpm)) {
            executeVelocity();
            return;
        }
        execute(false, 0.0, 0.0);
    }

    @Override
    public void update(double positionRef, double velocityRef, double accelerationRef) {
        requirePositional();
        velocitySample();
        targetRpm = Double.NaN;
        target = normalize(positionRef);
        execute(true, velocityRef, accelerationRef);
    }

    private void execute(boolean profile, double vRef, double aRef) {
        if (!hasEncoder() || pid == null || Double.isNaN(target)) return;

        if (profile != inPerfil) {
            pid.reset();
            inPerfil = profile;
        }

        double e = error();

        if (!profile && Math.abs(e) <= targetTolerance) {
            pid.reset();
            write(0.0);
            return;
        }

        double ffOut = 0.0;
        if (ff != null) {
            ffOut = ff.calculate(vRef, aRef);
            if (vRef == 0.0) {
                ffOut += ff.getCoefficients().kS * Math.signum(e);
            }
        }

        write(pid.calculate(e, 0.0) + ffOut);
    }

    private void executeVelocity() {
        double base = rpmToPower(targetRpm);

        if (velPid == null || !hasEncoder()) {
            write(base);
            return;
        }
        if (targetRpm == 0.0) {
            velPid.reset();
            write(0.0);
            return;
        }
        if (Double.isNaN(measuredRpm)) {
            write(base);
            return;
        }
        double e = (targetRpm - measuredRpm) / maxRpm;
        write(base + velPid.calculate(e, 0.0));
    }

    @Override
    public boolean isAtTarget() {
        return hasEncoder() && !Double.isNaN(target) && Math.abs(error()) <= targetTolerance;
    }

    @Override
    public double getPower() {
        return Double.isNaN(lastPower) ? servo.getPower() : lastPower;
    }

    @Override
    public double getAngle() {
        if (!hasEncoder()) throw new IllegalStateException("No encoder configured. Servo: " + name);
        if (encoder != null) return encoder.getAngle();
        return (genericEncoder.getAsDouble() % encoderFullRotationTicks);
    }

    @Override
    public boolean isInverted() {
        return inverted;
    }

    @Override
    public void disable() {
        servo.setPwmDisable();
    }

    @Override
    public void enable() {
        servo.setPwmEnable();
    }

    @Override
    public boolean isEnabled() {
        return servo.isPwmEnabled();
    }

    @Override
    public String getDeviceType() {
        return "EzCRServo: " + name;
    }

    public CRServoImplEx getNativeServo() {
        return servo;
    }

    void write(double power) {
        double p = Math.max(-1.0, Math.min(1.0, power));
        boolean stopStopping = p == 0.0 && lastPower != 0.0;
        if (!Double.isNaN(lastPower) && Math.abs(p - lastPower) <= cachingTolerance && !stopStopping) {
            return;
        }
        servo.setPower(p);
        lastPower = p;
    }

    private void requirePositional() {
        if (!hasEncoder() || pid == null) {
            throw new IllegalStateException("Configure setPositionalControl() with encoder first. Servo: " + name);
        }
    }

    private void requireVelocityModel() {
        if (maxRpm <= 0) {
            throw new IllegalStateException("Configure setMaxRpm() first. Servo: " + name);
        }
    }

    private void velocitySample() {
        if (!hasEncoder()) return;

        long now = System.nanoTime();
        double currentPos = getCurrentPositionValue();

        if (!validSample) {
            lastPositionValue = currentPos;
            lastTimer = now;
            validSample = true;
            rpmFilter.reset();
            measuredRpm = Double.NaN;
            return;
        }

        double dt = (now - lastTimer) / 1e9;
        if (dt < 0.005) return;

        double revolution = getFullRotationValue();
        double d = (currentPos - lastPositionValue) % revolution;
        if (d > revolution / 2) d -= revolution;
        if (d <= -revolution / 2) d += revolution;

        double raw = encoderSignal * (d / revolution) / dt * 60.0;
        measuredRpm = rpmFilter.calculate(raw, dt);

        lastPositionValue = currentPos;
        lastTimer = now;
    }

    private double error() {
        double revolution = getFullRotationValue();
        double current = getCurrentPositionValue();
        double e = (target - current) % revolution;
        if (e > revolution / 2) e -= revolution;
        if (e <= -revolution / 2) e += revolution;
        return e;
    }

    private double normalize(double angle) {
        double revolution = getFullRotationValue();
        double a = angle % revolution;
        return a < 0 ? a + revolution : a;
    }
}
