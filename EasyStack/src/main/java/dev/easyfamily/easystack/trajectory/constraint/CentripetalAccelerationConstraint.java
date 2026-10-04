package dev.easyfamily.easystack.trajectory.constraint;

import dev.easyfamily.easystack.geometry.Pose2d;

/** Limita a velocidade nas curvas: v = sqrt(aCentripeta / |curvatura|). */
public class CentripetalAccelerationConstraint implements TrajectoryConstraint {
    private final double maxCentripetalAcceleration;

    public CentripetalAccelerationConstraint(double maxCentripetalAcceleration) {
        this.maxCentripetalAcceleration = maxCentripetalAcceleration;
    }

    @Override
    public double getMaxVelocity(Pose2d pose, double curvature, double velocity) {
        return Math.sqrt(Math.abs(maxCentripetalAcceleration / curvature));
    }

    @Override
    public MinMax getMinMaxAcceleration(Pose2d pose, double curvature, double velocity) {
        return new MinMax();
    }
}
