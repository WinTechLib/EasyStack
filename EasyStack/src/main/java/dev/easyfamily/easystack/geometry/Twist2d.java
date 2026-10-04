package dev.easyfamily.easystack.geometry;

import java.util.Objects;

/** Deslocamento infinitesimal (dx, dy no referencial do robô, dtheta em radianos) seguindo um arco. */
public final class Twist2d {
    public final double dx;
    public final double dy;
    public final double dtheta;

    public Twist2d() {
        this(0.0, 0.0, 0.0);
    }

    public Twist2d(double dx, double dy, double dtheta) {
        this.dx = dx;
        this.dy = dy;
        this.dtheta = dtheta;
    }

    public Twist2d times(double scalar) { return new Twist2d(dx * scalar, dy * scalar, dtheta * scalar); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Twist2d)) return false;
        Twist2d t = (Twist2d) o;
        return Math.abs(dx - t.dx) < 1e-9 && Math.abs(dy - t.dy) < 1e-9 && Math.abs(dtheta - t.dtheta) < 1e-9;
    }

    @Override
    public int hashCode() { return Objects.hash(Math.round(dx * 1e6), Math.round(dy * 1e6), Math.round(dtheta * 1e6)); }

    @Override
    public String toString() { return String.format("Twist2d(dx=%.3f, dy=%.3f, dtheta=%.4f)", dx, dy, dtheta); }
}
