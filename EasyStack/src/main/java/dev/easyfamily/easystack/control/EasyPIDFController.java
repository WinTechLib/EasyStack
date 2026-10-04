package dev.easyfamily.easystack.control;

public class EasyPIDFController extends EasyPIDController {
    private double kF;

    public EasyPIDFController(double kP, double kI, double kD, double kF) {
        super(kP, kI, kD);
        this.kF = kF;
    }

    public EasyPIDFController setF(double kF) {
        this.kF = kF;
        return this;
    }

    @Override
    public double calculate(double currentPosition) {
        double pidOutput = super.calculate(currentPosition);
        double feedforward = kF * getTargetPosition();
        return pidOutput + feedforward;
    }

    @Override
    public double calculate(double currentPosition, double targetPosition) {
        setTargetPosition(targetPosition);
        return calculate(currentPosition);
    }
}