package dev.easyfamily.easystack.geometry;

import java.util.Objects;

/** Posição 2D imutável. Use a mesma unidade em todo o código (ex.: polegadas ou metros). */
public final class Translation2d {
    private final double x;
    private final double y;

    public Translation2d() {
        this(0.0, 0.0);
    }

    public Translation2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** Coordenadas polares. */
    public Translation2d(double distance, Rotation2d angle) {
        this(distance * angle.getCos(), distance * angle.getSin());
    }

    public double getX() { return x; }

    public double getY() { return y; }

    public double getNorm() { return Math.hypot(x, y); }

    public double getDistance(Translation2d other) { return Math.hypot(other.x - x, other.y - y); }

    /** Ângulo do vetor (0,0) -> (x,y). */
    public Rotation2d getAngle() { return new Rotation2d(x, y); }

    public Translation2d rotateBy(Rotation2d other) {
        return new Translation2d(x * other.getCos() - y * other.getSin(), x * other.getSin() + y * other.getCos());
    }

    public Translation2d plus(Translation2d other) { return new Translation2d(x + other.x, y + other.y); }

    public Translation2d minus(Translation2d other) { return new Translation2d(x - other.x, y - other.y); }

    public Translation2d unaryMinus() { return new Translation2d(-x, -y); }

    public Translation2d times(double scalar) { return new Translation2d(x * scalar, y * scalar); }

    public Translation2d div(double scalar) { return new Translation2d(x / scalar, y / scalar); }

    public Translation2d interpolate(Translation2d end, double t) {
        double k = Math.max(0.0, Math.min(1.0, t));
        return new Translation2d(x + (end.x - x) * k, y + (end.y - y) * k);
    }

    public Vector2d toVector() { return new Vector2d(x, y); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Translation2d)) return false;
        Translation2d t = (Translation2d) o;
        return Math.abs(x - t.x) < 1e-9 && Math.abs(y - t.y) < 1e-9;
    }

    @Override
    public int hashCode() { return Objects.hash(Math.round(x * 1e6), Math.round(y * 1e6)); }

    @Override
    public String toString() { return String.format("Translation2d(%.3f, %.3f)", x, y); }
}
