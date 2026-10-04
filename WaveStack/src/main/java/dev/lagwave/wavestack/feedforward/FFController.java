package dev.lagwave.wavestack.feedforward;

public class FFController {

    private final FFCoefficients coefficients;

    public FFController(FFCoefficients coefficients) {
        this.coefficients = coefficients;
    }

    public FFController(double kS, double kV) {
        this(kS, kV, 0.0);
    }

    public FFController(double kS, double kV, double kA) {
        this(new FFCoefficients(kS, kV, kA));
    }

    public double calculate(double velocity) {
        return calculate(velocity, 0.0);
    }

    public double calculate(double velocity, double acceleration) {
        return coefficients.kS * Math.signum(velocity)
                + coefficients.kV * velocity
                + coefficients.kA * acceleration;
    }

    public double maxAchievableVelocity(
            double maxVoltage,
            double acceleration
    ) {
        return (maxVoltage
                - coefficients.kS
                - acceleration * coefficients.kA)
                / coefficients.kV;
    }

    public double minAchievableVelocity(
            double maxVoltage,
            double acceleration
    ) {
        return (-maxVoltage
                + coefficients.kS
                - acceleration * coefficients.kA)
                / coefficients.kV;
    }

    public double maxAchievableAcceleration(
            double maxVoltage,
            double velocity
    ) {
        return (maxVoltage
                - coefficients.kS * Math.signum(velocity)
                - velocity * coefficients.kV)
                / coefficients.kA;
    }

    public double minAchievableAcceleration(
            double maxVoltage,
            double velocity
    ) {
        return maxAchievableAcceleration(-maxVoltage, velocity);
    }

    public FFCoefficients getCoefficients() {
        return coefficients;
    }
}