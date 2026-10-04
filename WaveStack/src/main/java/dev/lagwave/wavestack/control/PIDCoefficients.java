package dev.lagwave.wavestack.control;

public class PIDCoefficients {

    public static double kP;
    public static double kI;
    public static double kD;

    public PIDCoefficients(double kP, double kI, double kD) {
        this.kP = kP; this.kI = kI; this.kD = kD;
    }

}
