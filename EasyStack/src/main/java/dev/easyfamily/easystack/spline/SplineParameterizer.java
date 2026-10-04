package dev.easyfamily.easystack.spline;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import dev.easyfamily.easystack.geometry.Twist2d;

/** Discretiza uma spline em pontos, subdividindo até o erro (dx, dy, dtheta) ficar abaixo das tolerâncias. */
public final class SplineParameterizer {
    private static final int MAX_ITERATIONS = 5000;

    private SplineParameterizer() {}

    public static List<PoseWithCurvature> parameterize(QuinticHermiteSpline spline,
                                                       double maxDx, double maxDy, double maxDtheta) {
        List<PoseWithCurvature> points = new ArrayList<>();
        points.add(spline.getPoint(0.0));

        Deque<double[]> stack = new ArrayDeque<>();
        stack.addFirst(new double[]{0.0, 1.0});

        int iterations = 0;
        while (!stack.isEmpty()) {
            double[] cur = stack.removeFirst();
            PoseWithCurvature start = spline.getPoint(cur[0]);
            PoseWithCurvature end = spline.getPoint(cur[1]);
            Twist2d twist = start.pose.log(end.pose);

            if (Math.abs(twist.dy) > maxDy || Math.abs(twist.dx) > maxDx || Math.abs(twist.dtheta) > maxDtheta) {
                double mid = (cur[0] + cur[1]) / 2.0;
                stack.addFirst(new double[]{mid, cur[1]});
                stack.addFirst(new double[]{cur[0], mid});
            } else {
                points.add(end);
            }

            if (++iterations >= MAX_ITERATIONS) {
                throw new IllegalStateException("Não foi possível parametrizar a spline (tolerâncias muito pequenas?).");
            }
        }
        return points;
    }
}
