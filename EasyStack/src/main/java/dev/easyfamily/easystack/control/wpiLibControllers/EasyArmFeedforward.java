package dev.easyfamily.easystack.control.wpiLibControllers;

public class EasyArmFeedforward {
    public final double kS;
    public final double kCos;
    public final double kV;
    public final double kA;

    public EasyArmFeedforward(double kS, double kCos, double kV, double kA) {
        this.kS = kS;
        this.kCos = kCos;
        this.kV = kV;
        this.kA = kA;
    }

    public EasyArmFeedforward(double kS, double kCos, double kV) {
        this(kS, kCos, kV, 0.0);
    }

    public double calculate(double positionRadians, double velocityRadPerSec, double accelRadPerSecSquared) {
        return (kS * Math.signum(velocityRadPerSec))
                + (kCos * Math.cos(positionRadians))
                + (kV * velocityRadPerSec)
                + (kA * accelRadPerSecSquared);
    }

    public double calculate(double positionRadians, double velocity) {
        return calculate(positionRadians, velocity, 0.0);
    }

    public double maxAchievableVelocity(double maxVoltage, double angle, double acceleration) {
        return (maxVoltage - kS - Math.cos(angle) * kCos - acceleration * kA) / kV;
    }

    public double minAchievableVelocity(double maxVoltage, double angle, double acceleration) {
        return (-maxVoltage + kS - Math.cos(angle) * kCos - acceleration * kA) / kV;
    }

    public double maxAchievableAcceleration(double maxVoltage, double angle, double velocity) {
        return (maxVoltage - kS * Math.signum(velocity) - Math.cos(angle) * kCos - velocity * kV) / kA;
    }

    public double minAchievableAcceleration(double maxVoltage, double angle, double velocity) {
        return maxAchievableAcceleration(-maxVoltage, angle, velocity);
    }
}