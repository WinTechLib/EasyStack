package dev.easyfamily.easystack.trajectory.constraint;

import dev.easyfamily.easystack.geometry.Pose2d;

public class MaxVelocityConstraint implements TrajectoryConstraint {
    private final double maxVelocity;

    public MaxVelocityConstraint(double maxVelocity) {
        this.maxVelocity = Math.abs(maxVelocity);
    }

    @Override
    public double getMaxVelocity(Pose2d pose, double curvature, double velocity) {
        return maxVelocity;
    }

    @Override
    public MinMax getMinMaxAcceleration(Pose2d pose, double curvature, double velocity) {
        return new MinMax();
    }
}
