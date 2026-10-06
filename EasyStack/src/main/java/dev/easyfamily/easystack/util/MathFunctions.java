package dev.easyfamily.easystack.util;

public class MathFunctions {

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double normalizeAngle(double angle) {
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

}
