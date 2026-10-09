package dev.easyfamily.easystack.control;

public class PIDFCoefficients extends PIDCoefficients {

    public double kF;

    public PIDFCoefficients() {
        this(0, 0, 0, 0);
    }

    public PIDFCoefficients(double kP, double kI, double kD, double kF) {
        super(kP, kI, kD);
        this.kF = kF;
    }
}