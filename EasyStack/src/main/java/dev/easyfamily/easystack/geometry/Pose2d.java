package dev.easyfamily.easystack.geometry;

import java.util.Objects;

/** Pose 2D imutável: posição (Translation2d) + direção (Rotation2d). */
public final class Pose2d {
    private final Translation2d translation;
    private final Rotation2d rotation;

    public Pose2d() {
        this(new Translation2d(), new Rotation2d());
    }

    public Pose2d(Translation2d translation, Rotation2d rotation) {
        this.translation = translation;
        this.rotation = rotation;
    }

    public Pose2d(double x, double y, Rotation2d rotation) {
        this(new Translation2d(x, y), rotation);
    }

    /** heading em radianos. */
    public Pose2d(double x, double y, double headingRadians) {
        this(new Translation2d(x, y), new Rotation2d(headingRadians));
    }

    public Translation2d getTranslation() { return translation; }

    public double getX() { return translation.getX(); }

    public double getY() { return translation.getY(); }

    public Rotation2d getRotation() { return rotation; }

    /** Heading em radianos normalizado para (-π, π]. */
    public double getHeading() { return rotation.getRadiansWrapped(); }

    public double getDistance(Pose2d other) { return translation.getDistance(other.translation); }

    /** Pose "this" vista a partir de other (mesma coisa que new Transform2d(other, this) como Pose2d). */
    public Pose2d relativeTo(Pose2d other) {
        Transform2d t = new Transform2d(other, this);
        return new Pose2d(t.getTranslation(), t.getRotation());
    }

    /** Diferença this - other como Transform2d. */
    public Transform2d minus(Pose2d other) {
        return new Transform2d(other, this);
    }

    public Pose2d plus(Transform2d other) { return transformBy(other); }

    public Pose2d transformBy(Transform2d other) {
        return new Pose2d(translation.plus(other.getTranslation().rotateBy(rotation)),
                other.getRotation().plus(rotation));
    }

    public Pose2d rotateBy(Rotation2d other) {
        return new Pose2d(translation.rotateBy(other), rotation.rotateBy(other));
    }

    public Pose2d times(double scalar) { return new Pose2d(translation.times(scalar), rotation.times(scalar)); }

    public Pose2d div(double scalar) { return times(1.0 / scalar); }

    /** Aplica um twist (arco de círculo) a esta pose. Útil para odometria. */
    public Pose2d exp(Twist2d twist) {
        double dtheta = twist.dtheta;
        double sinTheta = Math.sin(dtheta);
        double cosTheta = Math.cos(dtheta);

        double s, c;
        if (Math.abs(dtheta) < 1e-9) {
            s = 1.0 - dtheta * dtheta / 6.0;
            c = 0.5 * dtheta;
        } else {
            s = sinTheta / dtheta;
            c = (1.0 - cosTheta) / dtheta;
        }
        Transform2d transform = new Transform2d(
                new Translation2d(twist.dx * s - twist.dy * c, twist.dx * c + twist.dy * s),
                new Rotation2d(cosTheta, sinTheta));
        return this.plus(transform);
    }

    /** Twist que leva esta pose até "end" (inverso de exp). */
    public Twist2d log(Pose2d end) {
        Pose2d transform = end.relativeTo(this);
        double dtheta = transform.getRotation().getRadiansWrapped();
        double halfDtheta = dtheta / 2.0;

        double cosMinusOne = transform.getRotation().getCos() - 1.0;
        double halfThetaByTanOfHalfDtheta;
        if (Math.abs(cosMinusOne) < 1e-9) {
            halfThetaByTanOfHalfDtheta = 1.0 - dtheta * dtheta / 12.0;
        } else {
            halfThetaByTanOfHalfDtheta = -(halfDtheta * transform.getRotation().getSin()) / cosMinusOne;
        }

        Translation2d part = transform.getTranslation()
                .rotateBy(new Rotation2d(halfThetaByTanOfHalfDtheta, -halfDtheta))
                .times(Math.hypot(halfThetaByTanOfHalfDtheta, halfDtheta));
        return new Twist2d(part.getX(), part.getY(), dtheta);
    }

    /** Interpola seguindo um arco (t limitado a [0, 1]). */
    public Pose2d interpolate(Pose2d end, double t) {
        if (t <= 0) return this;
        if (t >= 1) return end;
        return exp(log(end).times(t));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pose2d)) return false;
        Pose2d p = (Pose2d) o;
        return translation.equals(p.translation) && rotation.equals(p.rotation);
    }

    @Override
    public int hashCode() { return Objects.hash(translation, rotation); }

    @Override
    public String toString() { return "Pose2d(" + translation + ", " + rotation + ")"; }
}
