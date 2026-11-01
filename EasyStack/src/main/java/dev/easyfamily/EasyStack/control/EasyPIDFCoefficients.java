package dev.easyfamily.EasyStack.control;

public class EasyPIDFCoefficients extends EasyPIDCoefficients {

    public double kF;

    public EasyPIDFCoefficients() {
        this(0, 0, 0, 0);
    }

    public EasyPIDFCoefficients(double kP, double kI, double kD, double kF) {
        super(kP, kI, kD);
        this.kF = kF;
    }
}