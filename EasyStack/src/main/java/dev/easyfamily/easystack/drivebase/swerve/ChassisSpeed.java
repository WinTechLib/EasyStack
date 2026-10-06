package dev.easyfamily.easystack.drivebase.swerve;

import dev.easyfamily.easystack.geometry.Vector2d;

public class ChassisSpeed {

    private final double vx;
    private final double vy;
    private final double omega;

    public ChassisSpeed() {
        this(0.0, 0.0, 0.0);
    }

    public ChassisSpeed(double vx, double vy, double omega) {
        this.vx = vx;
        this.vy = vy;
        this.omega = omega;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getOmega() {
        return omega;
    }

    public Vector2d getTranslation() {
        return new Vector2d(vx, vy);
    }

    public double translationNorm() {
        return Math.hypot(vx, vy);
    }

    public ChassisSpeed times(double scalar) {
        return new ChassisSpeed(
                vx * scalar,
                vy * scalar,
                omega * scalar
        );
    }

    @Override
    public String toString() {
        return String.format(
                "ChassisSpeeds(vx=%.3f, vy=%.3f, omega=%.3f)",
                vx, vy, omega
        );
    }
}