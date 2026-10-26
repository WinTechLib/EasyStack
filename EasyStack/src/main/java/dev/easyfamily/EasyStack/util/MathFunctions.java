package dev.easyfamily.EasyStack.util;

public class MathFunctions {

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double normalizeAngle(double angle) {
        if (Double.isInfinite(angle)) {
            return Double.NaN;
        }
        angle = angle % (2 * Math.PI);

        while(angle > Math.PI) {
            angle -= 2 * Math.PI;
        }

        while(angle < -Math.PI) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    public static boolean epsilonEquals(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    public static double applyDeadband(double value, double deadband){
        value = Math.abs(value) > deadband ? value : 0;
        return value;
    }

    public static double inputModulus(double input, double minimumInput, double maximumInput) {
        double modulus = maximumInput - minimumInput;
        if (!(modulus > 0)) {
            throw new IllegalArgumentException(
                    "maximumInput must be greater than minimumInput"
            );
        }

        double wrapped = (input - minimumInput) % modulus;
        if (wrapped < 0) {
            wrapped += modulus;
        }
        if (wrapped >= modulus) {
            wrapped -= modulus;
        }

        return wrapped + minimumInput;
    }

}