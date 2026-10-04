package dev.easyfamily.easystack.control;

import com.qualcomm.robotcore.util.ElapsedTime;

import dev.easyfamily.easystack.controllable.EasyController;

public class EasyPIDController implements EasyController {
    private double kP;
    private double kI;
    private double kD;

    private double targetPosition = 0.0;
    private double lastError = 0.0;
    private double integralSum = 0.0;
    private double maxIntegralSum = 0.0;

    private final ElapsedTime timer;

    public EasyPIDController(PIDCoefficients coefficients) {
        this(coefficients.kP, coefficients.kI, coefficients.kD);
    }

    public EasyPIDController(double kP, double kI, double kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
        this.timer = new ElapsedTime();
    }

    public EasyPIDController setPID(double kP, double kI, double kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
        return this;
    }

    public EasyPIDController setMaxIntegralSum(double max) {
        this.maxIntegralSum = max;
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

    @Override
    public double calculate(double currentPosition) {
        double error = targetPosition - currentPosition;
        double dt = timer.seconds();

        if (dt == 0.0) {
            dt = 1e-6;
        }

        integralSum += error * dt;

        if (maxIntegralSum > 0) {
            integralSum = Math.max(-maxIntegralSum, Math.min(integralSum, maxIntegralSum));
        }

        double derivative = (error - lastError) / dt;

        timer.reset();
        lastError = error;

        return (kP * error) + (kI * integralSum) + (kD * derivative);
    }

    @Override
    public double calculate(double currentPosition, double targetPosition) {
        setTargetPosition(targetPosition);
        return calculate(currentPosition);
    }

    @Override
    public void reset() {
        lastError = 0.0;
        integralSum = 0.0;
        timer.reset();
    }
}