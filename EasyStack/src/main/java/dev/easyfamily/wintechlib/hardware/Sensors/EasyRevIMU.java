package dev.easyfamily.wintechlib.hardware.Sensors;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.Quaternion;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

import dev.easyfamily.wintechlib.controllable.EasyGyro;

public class EasyRevIMU implements EasyGyro {
    private final IMU imu;
    private final String name;

    private double yawOffset = 0.0;
    private double pitchOffset = 0.0;
    private double rollOffset = 0.0;
    private int multiplier = 1;

    private YawPitchRollAngles cachedAngles;
    private AngularVelocity cachedVelocity;
    private AngularVelocity previousVelocity;

    private AngleUnit lastVelocityUnit = AngleUnit.DEGREES;
    private long lastAngleReadTime = 0;
    private long lastVelocityReadTime = 0;
    private final long CACHE_THRESHOLD_NS = 2000000;

    public EasyRevIMU(HardwareMap hwMap, String name, RevHubOrientationOnRobot.LogoFacingDirection logo, RevHubOrientationOnRobot.UsbFacingDirection usb) {
        this.imu = hwMap.get(IMU.class, name);
        this.name = name;
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(logo, usb));
        this.imu.initialize(parameters);
        this.previousVelocity = new AngularVelocity();
    }

    @Override
    public void init() {
        imu.resetYaw();
        yawOffset = 0.0;
        pitchOffset = 0.0;
        rollOffset = 0.0;
        clearCache();
    }

    @Override
    public void invertGyro() {
        multiplier *= -1;
    }

    @Override
    public double getHeading() {
        return getHeading(AngleUnit.DEGREES);
    }

    public double getHeading(AngleUnit unit) {
        double raw = getAbsoluteHeading(unit);
        double offsetConverted = unit == AngleUnit.DEGREES ? yawOffset : Math.toRadians(yawOffset);
        return raw - offsetConverted;
    }

    @Override
    public double getAbsoluteHeading() {
        return getAbsoluteHeading(AngleUnit.DEGREES);
    }

    public double getAbsoluteHeading(AngleUnit unit) {
        updateAnglesCache();
        return cachedAngles.getYaw(unit) * multiplier;
    }

    public double getPitch(AngleUnit unit) {
        updateAnglesCache();
        double raw = cachedAngles.getPitch(unit);
        double offsetConverted = unit == AngleUnit.DEGREES ? pitchOffset : Math.toRadians(pitchOffset);
        return raw - offsetConverted;
    }

    public double getRoll(AngleUnit unit) {
        updateAnglesCache();
        double raw = cachedAngles.getRoll(unit);
        double offsetConverted = unit == AngleUnit.DEGREES ? rollOffset : Math.toRadians(rollOffset);
        return raw - offsetConverted;
    }

    @Override
    public double[] getAngles() {
        return getAngles(AngleUnit.DEGREES);
    }

    public double[] getAngles(AngleUnit unit) {
        return new double[]{
                getHeading(unit),
                getPitch(unit),
                getRoll(unit)
        };
    }

    public double getAngularVelocity() {
        return getAngularVelocity(AngleUnit.DEGREES);
    }

    public double getAngularVelocity(AngleUnit unit) {
        updateVelocityCache(unit);
        return cachedVelocity.zRotationRate * multiplier;
    }

    public double[] getAngularVelocities(AngleUnit unit) {
        updateVelocityCache(unit);
        return new double[]{
                cachedVelocity.xRotationRate,
                cachedVelocity.yRotationRate,
                cachedVelocity.zRotationRate * multiplier
        };
    }

    public double getRotationalJerk(AngleUnit unit) {
        updateVelocityCache(unit);
        if (previousVelocity.acquisitionTime == 0 || cachedVelocity.acquisitionTime == previousVelocity.acquisitionTime) {
            return 0.0;
        }
        double dt = (cachedVelocity.acquisitionTime - previousVelocity.acquisitionTime) / 1e9;
        return (cachedVelocity.zRotationRate - previousVelocity.zRotationRate) / dt;
    }

    public boolean detectCollision(double jerkThreshold, AngleUnit unit) {
        return Math.abs(getRotationalJerk(unit)) > jerkThreshold;
    }

    public boolean isTipping(double criticalAngle, AngleUnit unit) {
        return Math.abs(getPitch(unit)) > criticalAngle || Math.abs(getRoll(unit)) > criticalAngle;
    }

    public boolean isStationary(double velocityThreshold, AngleUnit unit) {
        updateVelocityCache(unit);
        double angularMagnitude = Math.abs(cachedVelocity.xRotationRate) + Math.abs(cachedVelocity.yRotationRate) + Math.abs(cachedVelocity.zRotationRate);
        return angularMagnitude < velocityThreshold;
    }

    public double getFusedHeading(double externalHeading, double imuWeight, AngleUnit unit) {
        double currentImu = getHeading(unit);
        double externalWeight = 1.0 - Math.max(0.0, Math.min(1.0, imuWeight));

        double imuRad = unit == AngleUnit.DEGREES ? Math.toRadians(currentImu) : currentImu;
        double extRad = unit == AngleUnit.DEGREES ? Math.toRadians(externalHeading) : externalHeading;

        double sinSum = (imuWeight * Math.sin(imuRad)) + (externalWeight * Math.sin(extRad));
        double cosSum = (imuWeight * Math.cos(imuRad)) + (externalWeight * Math.cos(extRad));

        double fusedRad = Math.atan2(sinSum, cosSum);
        return unit == AngleUnit.DEGREES ? Math.toDegrees(fusedRad) : fusedRad;
    }

    public Quaternion getQuaternion() {
        return imu.getRobotOrientationAsQuaternion();
    }

    @Override
    public void reset() {
        yawOffset += getHeading(AngleUnit.DEGREES);
    }

    public void resetPitch() {
        pitchOffset += getPitch(AngleUnit.DEGREES);
    }

    public void resetRoll() {
        rollOffset += getRoll(AngleUnit.DEGREES);
    }

    public void setYawOffset(double offset, AngleUnit unit) {
        this.yawOffset = unit == AngleUnit.RADIANS ? Math.toDegrees(offset) : offset;
    }

    private void updateAnglesCache() {
        long now = System.nanoTime();
        if (cachedAngles == null || (now - lastAngleReadTime) > CACHE_THRESHOLD_NS) {
            cachedAngles = imu.getRobotYawPitchRollAngles();
            lastAngleReadTime = now;
        }
    }

    private void updateVelocityCache(AngleUnit unit) {
        long now = System.nanoTime();
        if (cachedVelocity == null || (now - lastVelocityReadTime) > CACHE_THRESHOLD_NS || lastVelocityUnit != unit) {
            if (cachedVelocity != null) {
                previousVelocity = cachedVelocity;
            }
            cachedVelocity = imu.getRobotAngularVelocity(unit);
            lastVelocityReadTime = now;
            lastVelocityUnit = unit;
        }
    }

    private void clearCache() {
        cachedAngles = null;
        cachedVelocity = null;
    }

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
        imu.close();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getDeviceType() {
        return "EasyRevIMU: " + name;
    }

    public IMU getNativeIMU() {
        return imu;
    }
}
