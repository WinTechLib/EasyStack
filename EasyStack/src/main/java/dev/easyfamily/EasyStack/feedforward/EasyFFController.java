package dev.easyfamily.EasyStack.feedforward;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import dev.easyfamily.EasyStack.controllable.EasyFeedforward;

public class EasyFFController implements EasyFeedforward {

    private final EasyFFCoefficients coefficients;
    private VoltageSensor voltageSensor = null;

    public EasyFFController(EasyFFCoefficients coefficients) {
        if (coefficients == null) {
            throw new IllegalArgumentException("coefficients cannot be null");
        }
        this.coefficients = coefficients;
    }
    public EasyFFController(EasyFFCoefficients coefficients, HardwareMap hardwareMap) {
        this(coefficients);
        if (hardwareMap != null && hardwareMap.voltageSensor.iterator().hasNext()) {
            this.voltageSensor = hardwareMap.voltageSensor.iterator().next();
        }
    }

    public EasyFFController(double kS, double kV) {
        this(kS, kV, 0.0);
    }

    public EasyFFController(double kS, double kV, double kA) {
        this(new EasyFFCoefficients(kS, kV, kA));
    }
    public EasyFFController(double kS, double kV, HardwareMap hardwareMap) {
        this(new EasyFFCoefficients(kS, kV, 0.0), hardwareMap);
    }

    public EasyFFController(double kS, double kV, double kA, HardwareMap hardwareMap) {
        this(new EasyFFCoefficients(kS, kV, kA), hardwareMap);
    }

    @Override
    public double calculate(double velocity) {
        return calculate(velocity, 0.0);
    }

    @Override
    public double calculate(double velocity, double acceleration) {
        double targetVoltage = (coefficients.kS * Math.signum(velocity))
                + (coefficients.kV * velocity)
                + (coefficients.kA * acceleration);
        if (voltageSensor != null) {
            double currentBatteryVoltage = voltageSensor.getVoltage();
            if (currentBatteryVoltage > 0.01) {
                return targetVoltage / currentBatteryVoltage;
            }
            return 0.0;
        }
        return targetVoltage;
    }

    public double maxAchievableVelocity(
            double maxVoltage,
            double acceleration
    ) {
        if (coefficients.kV == 0.0) {
            return Double.NaN;
        }
        return (maxVoltage - coefficients.kS - (acceleration * coefficients.kA))
                / coefficients.kV;
    }

    public double minAchievableVelocity(
            double maxVoltage,
            double acceleration
    ) {
        if (coefficients.kV == 0.0) {
            return Double.NaN;
        }
        return (-maxVoltage + coefficients.kS - (acceleration * coefficients.kA))
                / coefficients.kV;
    }

    public double maxAchievableAcceleration(
            double maxVoltage,
            double velocity
    ) {
        if (coefficients.kA == 0.0) {
            return Double.NaN;
        }
        return (maxVoltage - (coefficients.kS * Math.signum(velocity)) - (velocity * coefficients.kV))
                / coefficients.kA;
    }

    public double minAchievableAcceleration(
            double maxVoltage,
            double velocity
    ) {
        return maxAchievableAcceleration(-maxVoltage, velocity);
    }

    public EasyFFCoefficients getCoefficients() {
        return coefficients;
    }
}