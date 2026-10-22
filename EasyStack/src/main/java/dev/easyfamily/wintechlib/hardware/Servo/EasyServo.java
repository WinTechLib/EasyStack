package dev.easyfamily.wintechlib.hardware.Servo;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import java.util.HashMap;
import java.util.Map;

public class EasyServo implements dev.easyfamily.wintechlib.controllable.EasyServo {
    private static final double TARGET_TOLERANCE = 0.001;

    private final ServoImplEx servo;
    private final String name;
    private final Map<String, Double> states = new HashMap<>();

    private double degreeRange = 180.0;
    private double min = 0.0;
    private double max = 1.0;
    private double cachingTolerance = 0.0001;
    private boolean inverted = false;
    private double lastRaw = Double.NaN;

    private double speed = 0.0;
    private double target = Double.NaN;
    private double start = Double.NaN;
    private long startNanos = 0L;

    public EasyServo(HardwareMap hwMap, String name) {
        this.servo = hwMap.get(ServoImplEx.class, name);
        this.name = name;
    }

    public EasyServo(HardwareMap hwMap, String name, double degreeRange) {
        this(hwMap, name);
        setAngleRange(degreeRange);
    }

    @Override
    public EasyServo setLimits(double min, double max) {
        if (min < 0.0 || max > 1.0 || min >= max) {
            throw new IllegalArgumentException("Invalid limits: 0 <= min < max <= 1");
        }
        this.min = min;
        this.max = max;
        resetCache();
        return this;
    }

    @Override
    public EasyServo setAngleRange(double degrees) {
        if (degrees <= 0) throw new IllegalArgumentException("Range must be > 0");
        this.degreeRange = degrees;
        return this;
    }

    @Override
    public EasyServo setCachingTolerance(double tolerance) {
        this.cachingTolerance = Math.max(0.0, tolerance);
        return this;
    }

    @Override
    public EasyServo setSpeed(double positionPerSecond) {
        this.speed = positionPerSecond;
        return this;
    }

    @Override
    public EasyServo reverse() {
        return setInverted(!inverted);
    }

    @Override
    public EasyServo setInverted(boolean inverted) {
        this.inverted = inverted;
        servo.setDirection(inverted ? Servo.Direction.REVERSE : Servo.Direction.FORWARD);
        resetCache();
        return this;
    }

    @Override
    public EasyServo setPwmRange(PwmControl.PwmRange pwmRange) {
        servo.setPwmRange(pwmRange);
        resetCache();
        return this;
    }

    @Override
    public EasyServo addState(String name, double position) {
        states.put(name, clamp(position));
        return this;
    }

    @Override
    public void setPosition(double position) {
        double pos = clamp(position);
        double raw = min + pos * (max - min);

        if (!Double.isNaN(lastRaw) && Math.abs(raw - lastRaw) <= cachingTolerance) {
            return;
        }

        startMovement(pos);
        servo.setPosition(raw);
        lastRaw = raw;
    }

    @Override
    public void rotateBy(double deltaPosition) {
        setPosition(getPosition() + deltaPosition);
    }

    @Override
    public void setAngle(double angle) {
        setPosition(angle / degreeRange);
    }

    @Override
    public void setAngle(double angle, AngleUnit angleUnit) {
        setAngle(toDegrees(angle, angleUnit));
    }

    @Override
    public void turnByAngle(double angle) {
        setAngle(getAngle() + angle);
    }

    @Override
    public void turnByAngle(double angle, AngleUnit angleUnit) {
        turnByAngle(toDegrees(angle, angleUnit));
    }

    @Override
    public void setState(String name) {
        Double pos = states.get(name);
        if (pos == null) {
            throw new IllegalArgumentException("Unknown state '" + name + "' in " + this.name);
        }
        setPosition(pos);
    }

    @Override
    public double getPosition() {
        return (getRawPosition() - min) / (max - min);
    }

    @Override
    public double getRawPosition() {
        return !Double.isNaN(lastRaw) ? lastRaw : servo.getPosition();
    }

    @Override
    public double getAngle() {
        return getPosition() * degreeRange;
    }

    @Override
    public double getAngle(AngleUnit angleUnit) {
        double degrees = getAngle();
        return angleUnit == AngleUnit.RADIANS ? Math.toRadians(degrees) : degrees;
    }

    @Override
    public double getEstimatedPosition() {
        if (Double.isNaN(target)) return Double.NaN;
        if (speed <= 0) return target;

        double dist = speed * (System.nanoTime() - startNanos) / 1e9;
        double delta = target - start;
        return Math.abs(delta) <= dist ? target : start + Math.signum(delta) * dist;
    }

    @Override
    public boolean isAtTarget() {
        double est = getEstimatedPosition();
        return Double.isNaN(est) || Math.abs(target - est) <= TARGET_TOLERANCE;
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
        return "EzServo: " + name;
    }

    public Servo getNativeServo() {
        return servo;
    }

    private void resetCache() {
        lastRaw = Double.NaN;
    }

    private void startMovement(double newTarget) {
        double current = getEstimatedPosition();
        start = Double.isNaN(current) ? newTarget : current;
        target = newTarget;
        startNanos = System.nanoTime();
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private static double toDegrees(double angle, AngleUnit unit) {
        return unit == AngleUnit.RADIANS ? Math.toDegrees(angle) : angle;
    }
}
