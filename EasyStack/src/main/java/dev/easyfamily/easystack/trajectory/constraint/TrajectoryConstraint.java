package dev.easyfamily.easystack.trajectory.constraint;

import dev.easyfamily.easystack.geometry.Pose2d;

/** Restrição aplicada ponto a ponto na parametrização de tempo da trajetória. */
public interface TrajectoryConstraint {
    /** Velocidade máxima permitida neste ponto (use Double.POSITIVE_INFINITY para "sem limite"). */
    double getMaxVelocity(Pose2d pose, double curvature, double velocity);

    /** Aceleração mínima/máxima permitida neste ponto. */
    MinMax getMinMaxAcceleration(Pose2d pose, double curvature, double velocity);

    class MinMax {
        public final double min;
        public final double max;

        public MinMax() {
            this(-Double.MAX_VALUE, Double.MAX_VALUE);
        }

        public MinMax(double min, double max) {
            this.min = min;
            this.max = max;
        }
    }
}
