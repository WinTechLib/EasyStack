package dev.easyfamily.easystack.control;

import androidx.core.math.MathUtils;

public abstract class EasyCotroller {
    private double minOutput = 0;
    private double maxOutput = Double.POSITIVE_INFINITY;
    private double openF = 0;
    protected double setPoint;
    protected double measuredValue;

    protected double errorVal_p;
    protected double errorVal_v;
    protected double errorTolerance_p = 0.05;
    protected double errorTolerance_v = Double.POSITIVE_INFINITY;

    protected double prevErrorVal;
    protected double lastTimeStamp;
    protected double period;

    public EasyCotroller() {
        reset();
        period = 0;
    }
    public void reset() {
        prevErrorVal = 0;
        lastTimeStamp = 0;
    }
    protected abstract double calculateOutput(double pv);
    public double calculate(double pv) {
        double output = calculateOutput(pv);
        output += Math.signum(errorVal_p) * openF;
        if (atSetPoint()) {
            return output;
        } else {
            return MathUtils.clamp(Math.abs(output), minOutput, maxOutput) * Math.signum(output);
        }
    }

    public double calculate(double pv, double sp) {
        setSetPoint(sp);
        return calculate(pv);
    }


    public double calculate() {
        return calculate(measuredValue);
    }

    public void setSetPoint(double sp) {
        setPoint = sp;
        errorVal_p = setPoint - measuredValue;
        errorVal_v = (errorVal_p - prevErrorVal) / period;
    }


    public double getSetPoint() {
        return setPoint;
    }


    public boolean atSetPoint() {
        return Math.abs(errorVal_p) < errorTolerance_p
                && Math.abs(errorVal_v) < errorTolerance_v;
    }


    public double getPositionError() {
        return errorVal_p;
    }

    public double getVelocityError() {
        return errorVal_v;
    }
    public void setTolerance(double positionTolerance) {
        setTolerance(positionTolerance, Double.POSITIVE_INFINITY);
    }
    public void setTolerance(double positionTolerance, double velocityTolerance) {
        errorTolerance_p = positionTolerance;
        errorTolerance_v = velocityTolerance;
    }
    public double[] getTolerance() {
        return new double[]{errorTolerance_p, errorTolerance_v};
    }

    public double getPeriod() {
        return period;
    }
    public EasyCotroller setMinOutput(double minOutput) {
        this.minOutput = Math.abs(minOutput);
        return this;
    }
    public double getMinOutput() {
        return minOutput;
    }
    public EasyCotroller setMaxOutput(double maxOutput) {
        this.maxOutput = maxOutput;
        return this;
    }
    public double getMaxOutput() {
        return maxOutput;
    }
    public EasyCotroller setOpenF(double f) {
        this.openF = f;
        return this;
    }
}
