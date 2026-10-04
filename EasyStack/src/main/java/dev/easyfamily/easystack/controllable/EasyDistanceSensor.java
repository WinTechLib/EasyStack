package dev.easyfamily.easystack.controllable;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import dev.easyfamily.easystack.controllable.EasyHardwareDevice;
import java.util.List;
import java.util.Map;

public interface EasyDistanceSensor extends EasyHardwareDevice {

    double getDistance(DistanceUnit unit);
    boolean targetReached(DistanceTarget target);
    void addTarget(DistanceTarget target);
    void addTargets(List<DistanceTarget> targets);
    Map<DistanceTarget, Boolean> checkAllTargets();

    class DistanceTarget {
        private double target;
        private double minThreshold;
        private double maxThreshold;
        private String name;
        private DistanceUnit unit;

        public DistanceTarget(DistanceUnit unit, double target) {
            this(unit, target - 5.0, target + 5.0);
        }

        public DistanceTarget(DistanceUnit unit, double minThreshold, double maxThreshold) {
            this(unit, minThreshold, maxThreshold, "Distance Target");
        }

        public DistanceTarget(DistanceUnit unit, double minThreshold, double maxThreshold, String name) {
            if (minThreshold < 0 || maxThreshold < 0 || minThreshold > maxThreshold) {
                throw new IllegalArgumentException("Valores de threshold inválidos.");
            }

            this.unit = unit;
            this.target = (minThreshold + maxThreshold) / 2.0;
            this.minThreshold = minThreshold;
            this.maxThreshold = maxThreshold;
            this.name = name;
        }

        public boolean atTarget(double currentDistanceInTargetUnit) {
            return (currentDistanceInTargetUnit >= minThreshold) && (currentDistanceInTargetUnit <= maxThreshold);
        }

        public void setTarget(double target) {
            this.target = target;
        }

        public void setThreshold(double threshold) {
            this.minThreshold = Math.max(target - threshold, 0);
            this.maxThreshold = target + threshold;
        }

        public void setMinThreshold(double minThreshold) {
            if (minThreshold > maxThreshold) throw new IllegalArgumentException("Min > Max");
            this.minThreshold = minThreshold;
        }

        public void setMaxThreshold(double maxThreshold) {
            if (minThreshold > maxThreshold) throw new IllegalArgumentException("Max < Min");
            this.maxThreshold = maxThreshold;
        }

        public void setUnit(DistanceUnit unit) {
            this.target = unit.fromUnit(this.unit, target);
            this.minThreshold = unit.fromUnit(this.unit, minThreshold);
            this.maxThreshold = unit.fromUnit(this.unit, maxThreshold);
            this.unit = unit;
        }

        public void setName(String name) {
            this.name = name;
        }

        public DistanceUnit getUnit() {
            return unit;
        }

        public double getThreshold() {
            return maxThreshold - minThreshold;
        }

        public double getMinThreshold() {
            return minThreshold;
        }

        public double getMaxThreshold() {
            return maxThreshold;
        }

        public double getTarget() {
            return target;
        }

        public String getName() {
            return name;
        }
    }
}