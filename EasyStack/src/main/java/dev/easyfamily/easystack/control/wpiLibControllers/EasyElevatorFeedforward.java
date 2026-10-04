package dev.easyfamily.easystack.control.wpiLibControllers;

public class EasyElevatorFeedforward {
    public final double kS;
    public final double kG;
    public final double kV;
    public final double kA;

    public EasyElevatorFeedforward(double kS, double kG, double kV, double kA) {
        this.kS = kS;
        this.kG = kG;
        this.kV = kV;
        this.kA = kA;
    }

    public EasyElevatorFeedforward(double kS, double kG, double kV) {
        this(kS, kG, kV, 0.0);
    }

    public double calculate(double velocity, double acceleration) {
        return (kS * Math.signum(velocity)) + kG + (kV * velocity) + (kA * acceleration);
    }

    public double calculate(double velocity) {
        return calculate(velocity, 0.0);
    }

    public double maxAchievableVelocity(double maxVoltage, double acceleration) {
        return (maxVoltage - kS - kG - acceleration * kA) / kV;
    }

    public double minAchievableVelocity(double maxVoltage, double acceleration) {
        return (-maxVoltage + kS - kG - acceleration * kA) / kV;
    }

    public double maxAchievableAcceleration(double maxVoltage, double velocity) {
        return (maxVoltage - kS * Math.signum(velocity) - kG - velocity * kV) / kA;
    }

    public double minAchievableAcceleration(double maxVoltage, double velocity) {
        return maxAchievableAcceleration(-maxVoltage, velocity);
    }
}