package dev.easyfamily.easystack.control;

public class PIDCoefficients {

    public double kP;
    public double kI;
    public double kD;

    public PIDCoefficients() {
        this(0, 0, 0);
    }

    public PIDCoefficients(double kP, double kI, double kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
    }
}