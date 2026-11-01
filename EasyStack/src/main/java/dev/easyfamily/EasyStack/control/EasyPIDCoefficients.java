package dev.easyfamily.EasyStack.control;

public class EasyPIDCoefficients {

    public double kP;
    public double kI;
    public double kD;

    public EasyPIDCoefficients() {
        this(0, 0, 0);
    }

    public EasyPIDCoefficients(double kP, double kI, double kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
    }
}