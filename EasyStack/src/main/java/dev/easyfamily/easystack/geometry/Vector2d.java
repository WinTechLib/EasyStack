package dev.easyfamily.easystack.geometry;

import java.util.Objects;

/** Vetor 2D imutável para contas (velocidades, direções, projeções). Para posições use Translation2d. */
public final class Vector2d {
    private final double x;
    private final double y;

    public Vector2d() {
        this(0.0, 0.0);
    }

    public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2d fromPolar(double magnitude, double angleRadians) {
        return new Vector2d(magnitude * Math.cos(angleRadians), magnitude * Math.sin(angleRadians));
    }

    public double getX() { return x; }

    public double getY() { return y; }

    public double norm() { return Math.hypot(x, y); }

    /** Ângulo em radianos (atan2). */
    public double angle() { return Math.atan2(y, x); }

    public Vector2d plus(Vector2d o) { return new Vector2d(x + o.x, y + o.y); }

    public Vector2d minus(Vector2d o) { return new Vector2d(x - o.x, y - o.y); }

    public Vector2d unaryMinus() { return new Vector2d(-x, -y); }

    public Vector2d times(double s) { return new Vector2d(x * s, y * s); }

    public Vector2d div(double s) { return new Vector2d(x / s, y / s); }

    public double dot(Vector2d o) { return x * o.x + y * o.y; }

    /** Componente z do produto vetorial 3D (positivo se o está no sentido anti-horário de this). */
    public double cross(Vector2d o) { return x * o.y - y * o.x; }

    /** Vetor unitário (retorna zero se a norma for ~0). */
    public Vector2d unit() {
        double n = norm();
        return n < 1e-12 ? new Vector2d() : div(n);
    }

    public Vector2d rotated(double radians) {
        double c = Math.cos(radians), s = Math.sin(radians);
        return new Vector2d(x * c - y * s, x * s + y * c);
    }

    public Vector2d rotateBy(Rotation2d r) {
        return new Vector2d(x * r.getCos() - y * r.getSin(), x * r.getSin() + y * r.getCos());
    }

    public Vector2d projectOnto(Vector2d o) {
        double d = o.dot(o);
        return d < 1e-12 ? new Vector2d() : o.times(dot(o) / d);
    }

    /** Limita a norma a no máximo maxNorm (mantém a direção). */
    public Vector2d clampNorm(double maxNorm) {
        double n = norm();
        return n > maxNorm ? times(maxNorm / n) : this;
    }

    public Translation2d toTranslation() { return new Translation2d(x, y); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Vector2d)) return false;
        Vector2d v = (Vector2d) o;
        return Math.abs(x - v.x) < 1e-9 && Math.abs(y - v.y) < 1e-9;
    }

    @Override
    public int hashCode() { return Objects.hash(Math.round(x * 1e6), Math.round(y * 1e6)); }

    @Override
    public String toString() { return String.format("Vector2d(%.3f, %.3f)", x, y); }
}
