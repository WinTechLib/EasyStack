package dev.easyfamily.easystack.geometry;

import java.util.Objects;

/** Diferença entre duas poses, expressa no referencial da pose inicial. */
public final class Transform2d {
    private final Translation2d translation;
    private final Rotation2d rotation;

    public Transform2d() {
        this(new Translation2d(), new Rotation2d());
    }

    public Transform2d(Translation2d translation, Rotation2d rotation) {
        this.translation = translation;
        this.rotation = rotation;
    }

    public Transform2d(double x, double y, Rotation2d rotation) {
        this(new Translation2d(x, y), rotation);
    }

    /** Transform que leva a pose "initial" até a pose "last". */
    public Transform2d(Pose2d initial, Pose2d last) {
        this.translation = last.getTranslation().minus(initial.getTranslation()).rotateBy(initial.getRotation().unaryMinus());
        this.rotation = last.getRotation().minus(initial.getRotation());
    }

    public Translation2d getTranslation() { return translation; }

    public double getX() { return translation.getX(); }

    public double getY() { return translation.getY(); }

    public Rotation2d getRotation() { return rotation; }

    public Transform2d times(double scalar) { return new Transform2d(translation.times(scalar), rotation.times(scalar)); }

    public Transform2d div(double scalar) { return times(1.0 / scalar); }

    /** Aplica este transform e depois o outro. */
    public Transform2d plus(Transform2d other) {
        Pose2d origin = new Pose2d();
        return new Transform2d(origin, origin.transformBy(this).transformBy(other));
    }

    public Transform2d inverse() {
        return new Transform2d(translation.unaryMinus().rotateBy(rotation.unaryMinus()), rotation.unaryMinus());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Transform2d)) return false;
        Transform2d t = (Transform2d) o;
        return translation.equals(t.translation) && rotation.equals(t.rotation);
    }

    @Override
    public int hashCode() { return Objects.hash(translation, rotation); }

    @Override
    public String toString() { return "Transform2d(" + translation + ", " + rotation + ")"; }
}
