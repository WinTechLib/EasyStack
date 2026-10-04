package dev.easyfamily.easystack.trajectory.constraint;

import dev.easyfamily.easystack.geometry.Pose2d;
import dev.easyfamily.easystack.geometry.Translation2d;

/** Aplica outra restrição só quando o robô está dentro de um retângulo (ex.: zona lenta perto de um elemento). */
public class RectangularRegionConstraint implements TrajectoryConstraint {
    private final double minX, maxX, minY, maxY;
    private final TrajectoryConstraint constraint;

    public RectangularRegionConstraint(Translation2d bottomLeft, Translation2d topRight, TrajectoryConstraint constraint) {
        this.minX = Math.min(bottomLeft.getX(), topRight.getX());
        this.maxX = Math.max(bottomLeft.getX(), topRight.getX());
        this.minY = Math.min(bottomLeft.getY(), topRight.getY());
        this.maxY = Math.max(bottomLeft.getY(), topRight.getY());
        this.constraint = constraint;
    }

    private boolean contains(Pose2d pose) {
        return pose.getX() >= minX && pose.getX() <= maxX && pose.getY() >= minY && pose.getY() <= maxY;
    }

    @Override
    public double getMaxVelocity(Pose2d pose, double curvature, double velocity) {
        return contains(pose) ? constraint.getMaxVelocity(pose, curvature, velocity) : Double.POSITIVE_INFINITY;
    }

    @Override
    public MinMax getMinMaxAcceleration(Pose2d pose, double curvature, double velocity) {
        return contains(pose) ? constraint.getMinMaxAcceleration(pose, curvature, velocity) : new MinMax();
    }
}
