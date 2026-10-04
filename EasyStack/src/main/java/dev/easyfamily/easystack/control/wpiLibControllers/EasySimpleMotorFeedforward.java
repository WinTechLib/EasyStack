package dev.easyfamily.easystack.control.wpiLibControllers;

public class EasySimpleMotorFeedforward {
    public final double kS;
    public final double kV;
    public final double kA;

    public EasySimpleMotorFeedforward(double kS, double kV, double kA) {
        this.kS = kS;
        this.kV = kV;
        this.kA = kA;
    }

    public EasySimpleMotorFeedforward(double kS, double kV) {
        this(kS, kV, 0.0);
    }

    public double calculate(double velocity, double acceleration) {
        return (kS * Math.signum(velocity)) + (kV * velocity) + (kA * acceleration);
    }

    public double calculate(double velocity) {
        return calculate(velocity, 0.0);
    }

    public double maxAchievableVelocity(double maxVoltage, double acceleration) {
        return (maxVoltage - kS - acceleration * kA) / kV;
    }

    public double minAchievableVelocity(double maxVoltage, double acceleration) {
        return (-maxVoltage + kS - acceleration * kA) / kV;
    }

    public double maxAchievableAcceleration(double maxVoltage, double velocity) {
        return (maxVoltage - kS * Math.signum(velocity) - velocity * kV) / kA;
    }

    public double minAchievableAcceleration(double maxVoltage, double velocity) {
        return maxAchievableAcceleration(-maxVoltage, velocity);
    }
}