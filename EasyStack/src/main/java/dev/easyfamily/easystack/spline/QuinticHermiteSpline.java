package dev.easyfamily.easystack.spline;

import dev.easyfamily.easystack.geometry.Pose2d;
import dev.easyfamily.easystack.geometry.Rotation2d;

/** Spline quíntica de Hermite em x(t), y(t), t em [0, 1]. Condições: posição, 1ª e 2ª derivadas nas pontas. */
public final class QuinticHermiteSpline {
    // coeficientes de t^5 .. t^0
    private final double[] xc;
    private final double[] yc;

    /** Cada array = {valor, derivada, segunda derivada}. */
    public QuinticHermiteSpline(double[] xInitial, double[] xFinal, double[] yInitial, double[] yFinal) {
        this.xc = coefficients(xInitial, xFinal);
        this.yc = coefficients(yInitial, yFinal);
    }

    /** Spline entre duas poses; o heading de cada pose define a tangente. */
    public static QuinticHermiteSpline fromPoses(Pose2d p0, Pose2d p1) {
        double scale = 1.2 * p0.getTranslation().getDistance(p1.getTranslation());
        Rotation2d r0 = p0.getRotation(), r1 = p1.getRotation();
        return new QuinticHermiteSpline(
                new double[]{p0.getX(), scale * r0.getCos(), 0.0},
                new double[]{p1.getX(), scale * r1.getCos(), 0.0},
                new double[]{p0.getY(), scale * r0.getSin(), 0.0},
                new double[]{p1.getY(), scale * r1.getSin(), 0.0});
    }

    private static double[] coefficients(double[] i, double[] f) {
        double x0 = i[0], dx0 = i[1], ddx0 = i[2];
        double x1 = f[0], dx1 = f[1], ddx1 = f[2];
        double a = -6.0 * x0 - 3.0 * dx0 - 0.5 * ddx0 + 6.0 * x1 - 3.0 * dx1 + 0.5 * ddx1;
        double b = 15.0 * x0 + 8.0 * dx0 + 1.5 * ddx0 - 15.0 * x1 + 7.0 * dx1 - 1.0 * ddx1;
        double c = -10.0 * x0 - 6.0 * dx0 - 1.5 * ddx0 + 10.0 * x1 - 4.0 * dx1 + 0.5 * ddx1;
        double d = 0.5 * ddx0;
        double e = dx0;
        double fc = x0;
        return new double[]{a, b, c, d, e, fc};
    }

    public PoseWithCurvature getPoint(double t) {
        double x = ((((xc[0] * t + xc[1]) * t + xc[2]) * t + xc[3]) * t + xc[4]) * t + xc[5];
        double y = ((((yc[0] * t + yc[1]) * t + yc[2]) * t + yc[3]) * t + yc[4]) * t + yc[5];

        double dx = (((5 * xc[0] * t + 4 * xc[1]) * t + 3 * xc[2]) * t + 2 * xc[3]) * t + xc[4];
        double dy = (((5 * yc[0] * t + 4 * yc[1]) * t + 3 * yc[2]) * t + 2 * yc[3]) * t + yc[4];

        double ddx = ((20 * xc[0] * t + 12 * xc[1]) * t + 6 * xc[2]) * t + 2 * xc[3];
        double ddy = ((20 * yc[0] * t + 12 * yc[1]) * t + 6 * yc[2]) * t + 2 * yc[3];

        double curvature = (dx * ddy - ddx * dy) / ((dx * dx + dy * dy) * Math.hypot(dx, dy));
        return new PoseWithCurvature(new Pose2d(x, y, new Rotation2d(dx, dy)), curvature);
    }
}
