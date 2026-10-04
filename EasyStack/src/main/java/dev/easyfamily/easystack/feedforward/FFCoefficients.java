package dev.easyfamily.easystack.feedforward;

public class FFCoefficients {

    public double kS;
    public double kV;
    public double kA;

    public FFCoefficients(double kS, double kV) {
        this(kS, kV, 0.0);
    }

    public FFCoefficients(double kS, double kV, double kA) {
        this.kS = kS;
        this.kV = kV;
        this.kA = kA;
    }
}