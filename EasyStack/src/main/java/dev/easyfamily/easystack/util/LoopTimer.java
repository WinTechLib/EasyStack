package dev.easyfamily.easystack.util;
public class LoopTimer {
    private long lastNanos = 0;
    private double dt = 0.0;
    private double avgDt = 0.0;
    private final double smoothing;

    public LoopTimer() {
        this(0.1);
    }
    public LoopTimer(double smoothing) {
        this.smoothing = smoothing;
    }

    public void tick() {
        long now = System.nanoTime();
        if (lastNanos != 0) {
            dt = (now - lastNanos) / 1e9;
            avgDt = avgDt == 0.0 ? dt : avgDt + smoothing * (dt - avgDt);
        }
        lastNanos = now;
    }
    public double getDt() { return dt; }
    public double getHz() { return avgDt > 0 ? 1.0 / avgDt : 0.0; }

    public double getAverageMillis() { return avgDt * 1000.0; }
}
