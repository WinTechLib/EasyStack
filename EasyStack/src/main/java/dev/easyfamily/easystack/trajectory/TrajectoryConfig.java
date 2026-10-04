package dev.easyfamily.easystack.trajectory;

import java.util.ArrayList;
import java.util.List;

import dev.easyfamily.easystack.trajectory.constraint.TrajectoryConstraint;

/**
 * Configuração da geração. Unidades livres (ex.: polegadas, pol/s, pol/s²).
 * As tolerâncias da spline têm padrão em polegadas; ajuste com setSplineTolerance se usar outra unidade.
 */
public class TrajectoryConfig {
    private final double maxVelocity;
    private final double maxAcceleration;
    private final List<TrajectoryConstraint> constraints = new ArrayList<>();
    private double startVelocity = 0.0;
    private double endVelocity = 0.0;
    private boolean reversed = false;

    private double splineMaxDx = 5.0;
    private double splineMaxDy = 0.05;
    private double splineMaxDtheta = 0.0872;

    public TrajectoryConfig(double maxVelocity, double maxAcceleration) {
        this.maxVelocity = maxVelocity;
        this.maxAcceleration = maxAcceleration;
    }

    public TrajectoryConfig setStartVelocity(double v) { startVelocity = v; return this; }

    public TrajectoryConfig setEndVelocity(double v) { endVelocity = v; return this; }

    public TrajectoryConfig setReversed(boolean reversed) { this.reversed = reversed; return this; }

    public TrajectoryConfig addConstraint(TrajectoryConstraint constraint) { constraints.add(constraint); return this; }

    public TrajectoryConfig setSplineTolerance(double maxDx, double maxDy, double maxDtheta) {
        splineMaxDx = maxDx;
        splineMaxDy = maxDy;
        splineMaxDtheta = maxDtheta;
        return this;
    }

    public double getMaxVelocity() { return maxVelocity; }

    public double getMaxAcceleration() { return maxAcceleration; }

    public double getStartVelocity() { return startVelocity; }

    public double getEndVelocity() { return endVelocity; }

    public boolean isReversed() { return reversed; }

    public List<TrajectoryConstraint> getConstraints() { return constraints; }

    public double getSplineMaxDx() { return splineMaxDx; }

    public double getSplineMaxDy() { return splineMaxDy; }

    public double getSplineMaxDtheta() { return splineMaxDtheta; }
}
