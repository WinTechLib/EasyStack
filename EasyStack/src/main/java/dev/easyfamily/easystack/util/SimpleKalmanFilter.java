package dev.easyfamily.easystack.util;
public class SimpleKalmanFilter {
    private final double measurementError;
    private final double processNoise;
    private double estimateError;
    private double estimate;
    private boolean initialized = false;

    public SimpleKalmanFilter(double measurementError, double estimateError, double processNoise) {
        this.measurementError = measurementError;
        this.estimateError = estimateError;
        this.processNoise = processNoise;
    }

    public double update(double measurement) {
        if (!initialized) {
            estimate = measurement;
            initialized = true;
            return estimate;
        }
        double gain = estimateError / (estimateError + measurementError);
        double previous = estimate;
        estimate = previous + gain * (measurement - previous);
        estimateError = (1.0 - gain) * estimateError + Math.abs(previous - estimate) * processNoise;
        return estimate;
    }

    public double getEstimate() { return estimate; }

    public void reset() { initialized = false; }
}
