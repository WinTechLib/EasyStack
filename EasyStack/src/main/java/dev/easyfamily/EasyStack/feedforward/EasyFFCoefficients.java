package dev.easyfamily.EasyStack.feedforward;

public class EasyFFCoefficients {

    public double kS;
    public double kV;
    public double kA;

    public EasyFFCoefficients(double kS, double kV) {
        this(kS, kV, 0.0);
    }

    public EasyFFCoefficients(double kS, double kV, double kA) {
        this.kS = kS;
        this.kV = kV;
        this.kA = kA;
    }
}