package dev.easyfamily.easystack.control;

public class EasyPIDFController extends EasyPIDController {
    private double kF;

    public EasyPIDFController(PIDFCoefficients coefficients) {
        this(coefficients.kP, coefficients.kI, coefficients.kD, coefficients.kF);
    }

    public EasyPIDFController(double kP, double kI, double kD, double kF) {
        super(kP, kI, kD);
        this.kF = kF;
    }

    public EasyPIDFController setF(double kF) {
        this.kF = kF;
        return this;
    }

    public EasyPIDFController setPIDF(double kP, double kI, double kD, double kF) {
        setPID(kP, kI, kD);
        this.kF = kF;
        return this;
    }

    public EasyPIDFController setPIDF(PIDFCoefficients coefficients) {
        return setPIDF(coefficients.kP, coefficients.kI, coefficients.kD, coefficients.kF);
    }

    @Override
    public double calculate(double currentPosition) {
        double pidOutput = super.calculate(currentPosition);
        double feedforward = kF * getTargetPosition();
        return pidOutput + feedforward;
    }
}