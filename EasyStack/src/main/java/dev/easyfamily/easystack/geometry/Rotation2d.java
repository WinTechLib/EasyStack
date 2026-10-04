package dev.easyfamily.easystack.geometry;

import java.util.Objects;

/** Rotação 2D imutável (anti-horário positivo). Guarda radianos, cos e sin. */
public final class Rotation2d {
    private final double radians;
    private final double cos;
    private final double sin;

    public Rotation2d() {
        this(0.0);
    }

    public Rotation2d(double radians) {
        this.radians = radians;
        this.cos = Math.cos(radians);
        this.sin = Math.sin(radians);
    }

    /** Constrói a partir de um ponto (x, y) no círculo unitário (é normalizado). */
    public Rotation2d(double x, double y) {
        double norm = Math.hypot(x, y);
        if (norm > 1e-6) {
            this.sin = y / norm;
            this.cos = x / norm;
        } else {
            this.sin = 0.0;
            this.cos = 1.0;
        }
        this.radians = Math.atan2(sin, cos);
    }

    public static Rotation2d fromRadians(double radians) { return new Rotation2d(radians); }

    public static Rotation2d fromDegrees(double degrees) { return new Rotation2d(Math.toRadians(degrees)); }

    public static Rotation2d fromRotations(double rotations) { return new Rotation2d(rotations * 2.0 * Math.PI); }

    public double getRadians() { return radians; }

    /** Radianos normalizados para (-π, π]. */
    public double getRadiansWrapped() { return Math.atan2(sin, cos); }

    public double getDegrees() { return Math.toDegrees(radians); }

    /** Graus normalizados para (-180, 180]. */
    public double getDegreesWrapped() { return Math.toDegrees(getRadiansWrapped()); }

    public double getRotations() { return radians / (2.0 * Math.PI); }

    public double getCos() { return cos; }

    public double getSin() { return sin; }

    public double getTan() { return sin / cos; }

    public Rotation2d plus(Rotation2d other) { return rotateBy(other); }

    public Rotation2d minus(Rotation2d other) { return rotateBy(other.unaryMinus()); }

    public Rotation2d unaryMinus() { return new Rotation2d(-radians); }

    public Rotation2d times(double scalar) { return new Rotation2d(radians * scalar); }

    public Rotation2d div(double scalar) { return times(1.0 / scalar); }

    public Rotation2d rotateBy(Rotation2d other) {
        return new Rotation2d(cos * other.cos - sin * other.sin, cos * other.sin + sin * other.cos);
    }

    /** Versão normalizada em (-π, π]. */
    public Rotation2d normalize() { return new Rotation2d(getRadiansWrapped()); }

    public Rotation2d interpolate(Rotation2d end, double t) {
        return plus(end.minus(this).times(Math.max(0.0, Math.min(1.0, t))));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Rotation2d)) return false;
        Rotation2d r = (Rotation2d) o;
        return Math.abs(cos - r.cos) < 1e-9 && Math.abs(sin - r.sin) < 1e-9;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Math.round(cos * 1e6), Math.round(sin * 1e6));
    }

    @Override
    public String toString() {
        return String.format("Rotation2d(%.4f rad, %.2f deg)", radians, getDegrees());
    }
}
