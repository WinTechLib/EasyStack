package dev.easyfamily.easystack.control;

import com.qualcomm.robotcore.util.ElapsedTime;

import dev.easyfamily.easystack.controllable.EasyController;

public class EasySquIDFController implements EasyController {
    private double kP;
    private double kI;
    private double kD;
    private double kF;

    private double targetPosition = 0.0;
    private double prevError = 0.0;
    private double totalError = 0.0;

    private double maxIntegral = 0.0;
    private double decayFactor = 1.0;
    private boolean clearAtSetpoint = false;
    private double positionTolerance = 0.05;

    private final ElapsedTime timer;

    public EasySquIDFController(double kP, double kI, double kD, double kF) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
        this.kF = kF;
        this.timer = new ElapsedTime();
    }

    public EasySquIDFController setPIDF(double kP, double kI, double kD, double kF) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
        this.kF = kF;
        return this;
    }

    public EasySquIDFController setIntegrationBounds(double maxIntegral) {
        this.maxIntegral = Math.abs(maxIntegral);
        return this;
    }

    public EasySquIDFController setDecayFactor(double decayFactor) {
        this.decayFactor = decayFactor;
        return this;
    }

    public EasySquIDFController setClearAtSetpoint(boolean clear, double tolerance) {
        this.clearAtSetpoint = clear;
        this.positionTolerance = tolerance;
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

    public boolean atSetpoint(double currentPosition) {
        return Math.abs(targetPosition - currentPosition) <= positionTolerance;
    }

    @Override
    public double calculate(double currentPosition) {
        double errorVal_p = targetPosition - currentPosition;

        double period = timer.seconds();
        if (period == 0.0) {
            period = 1e-6;
        }
        timer.reset();
        double errorVal_v = 0;
        if (Math.abs(period) > 1E-6) {
            errorVal_v = (errorVal_p - prevError) / period;
        }

        totalError += period * errorVal_p;
        if (maxIntegral > 0) {
            totalError = Math.max(-maxIntegral, Math.min(totalError, maxIntegral));
        }
        if (Math.signum(totalError) != Math.signum(errorVal_p)) {
            totalError *= decayFactor;
        }
        if (clearAtSetpoint && atSetpoint(currentPosition)) {
            totalError = 0;
        }

        prevError = errorVal_p;
        double sqrtWithSig = Math.signum(errorVal_p) * Math.sqrt(Math.abs(errorVal_p));
        return (kP * sqrtWithSig) + (kI * totalError) + (kD * errorVal_v) + (kF * targetPosition);
    }

    @Override
    public double calculate(double currentPosition, double targetPosition) {
        setTargetPosition(targetPosition);
        return calculate(currentPosition);
    }

    @Override
    public void reset() {
        prevError = 0.0;
        totalError = 0.0;
        timer.reset();
    }
}