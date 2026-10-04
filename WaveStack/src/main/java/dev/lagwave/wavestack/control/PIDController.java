package dev.lagwave.wavestack.control;

public class PIDController {

    private final PIDCoefficients coefficients;


    private double sumError = 0;
    private double lastError = 0;
    private double lastTime = 0;

    public PIDController(PIDCoefficients coefficients) {
        this.coefficients = coefficients;
    }

    public void reset() {
        sumError = 0;
        lastError = 0;
        lastTime = 0;
    }

    public double calculate(double target, double current) {
        double currentTime = System.nanoTime() / 1e9;
        double deltaTime = (lastTime == 0) ? 0 : (currentTime - lastTime);

        double error = target - current;

        double P = coefficients.kP * error;

        if (deltaTime > 0) {
            sumError += error * deltaTime;
        }
        double I = coefficients.kI * sumError;

        double derivative = (deltaTime > 0) ? (error - lastError) / deltaTime : 0;
        double D = coefficients.kD * derivative;

        lastError = error;
        lastTime = currentTime;

        return P + I + D;
    }
}
