package dev.easyfamily.EasyStack.util;
public class SimpleKalmanFilter {
    private final double measurementError;
    private final double processNoise;
    private final double initialEstimateError;
    private double estimateError;
    private double estimate;
    private boolean initialized = false;

    public SimpleKalmanFilter(double measurementError, double estimateError, double processNoise) {
        if (measurementError < 0 || estimateError < 0 || processNoise < 0) {
            throw new IllegalArgumentException(
                    "measurementError, estimateError and processNoise must not be negative."
            );
        }
        this.measurementError = measurementError;
        this.estimateError = estimateError;
        this.initialEstimateError = estimateError;
        this.processNoise = processNoise;
    }

    public double update(double measurement) {
        if (!initialized) {
            estimate = measurement;
            initialized = true;
            return estimate;
        }
        double denominator = estimateError + measurementError;
        double gain = denominator == 0.0 ? 1.0 : estimateError / denominator;
        double previous = estimate;
        estimate = previous + gain * (measurement - previous);
        estimateError = (1.0 - gain) * estimateError + Math.abs(previous - estimate) * processNoise;
        return estimate;
    }

    public double getEstimate() { return estimate; }

    public void reset() {
        initialized = false;
        estimateError = initialEstimateError;
    }
}