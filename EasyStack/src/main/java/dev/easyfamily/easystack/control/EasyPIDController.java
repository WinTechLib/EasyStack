package dev.easyfamily.easystack.control;

import dev.easyfamily.easystack.controllable.EasyController;

public class EasyPIDController implements EasyController {
    private double kP;
    private double kI;
    private double kD;

    private double targetPosition = 0.0;
    private double error = 0.0;
    private double tolerance = 0.0;

    private double lastMeasurement = 0.0;
    private double integralSum = 0.0;
    private double maxIntegralSum = 0.0;

    private long lastTime = 0L;
    private boolean first = true;

    public EasyPIDController(PIDCoefficients coefficients) {
        this(coefficients.kP, coefficients.kI, coefficients.kD);
    }

    public EasyPIDController(double kP, double kI, double kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
    }

    public EasyPIDController setPID(double kP, double kI, double kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
        return this;
    }

    public EasyPIDController setPID(PIDCoefficients coefficients) {
        return setPID(coefficients.kP, coefficients.kI, coefficients.kD);
    }

    public EasyPIDController setMaxIntegralSum(double max) {
        this.maxIntegralSum = Math.abs(max);
        return this;
    }

    public EasyPIDController setTolerance(double tolerance) {
        this.tolerance = Math.abs(tolerance);
        return this;
    }

    @Override
    public void setTargetPosition(double targetPosition) {
        this.targetPosition = targetPosition;
    }

    @Override
    public double getTargetPosition() {
        return targetPosition;
    }

    public double getError() {
        return error;
    }

    public boolean atTarget() {
        return Math.abs(error) <= tolerance;
    }

    @Override
    public double calculate(double currentPosition) {
        error = targetPosition - currentPosition;

        long now = System.nanoTime();
        double dt = first ? 0.0 : (now - lastTime) / 1e9;
        double derivative = 0.0;

        if (!first && dt > 0.0) {
            integralSum += error * dt;

            if (maxIntegralSum > 0) {
                integralSum = Math.max(-maxIntegralSum, Math.min(integralSum, maxIntegralSum));
            }

            derivative = -(currentPosition - lastMeasurement) / dt;
        }

        lastTime = now;
        lastMeasurement = currentPosition;
        first = false;

        return (kP * error) + (kI * integralSum) + (kD * derivative);
    }

    @Override
    public double calculate(double currentPosition, double targetPosition) {
        setTargetPosition(targetPosition);
        return calculate(currentPosition);
    }

    @Override
    public void reset() {
        integralSum = 0.0;
        error = 0.0;
        lastMeasurement = 0.0;
        lastTime = 0L;
        first = true;
    }
}