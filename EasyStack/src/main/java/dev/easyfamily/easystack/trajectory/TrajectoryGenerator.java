package dev.easyfamily.easystack.trajectory;

import java.util.ArrayList;
import java.util.List;

import dev.easyfamily.easystack.geometry.Pose2d;
import dev.easyfamily.easystack.geometry.Rotation2d;
import dev.easyfamily.easystack.geometry.Transform2d;
import dev.easyfamily.easystack.geometry.Translation2d;
import dev.easyfamily.easystack.spline.PoseWithCurvature;
import dev.easyfamily.easystack.spline.QuinticHermiteSpline;
import dev.easyfamily.easystack.spline.SplineParameterizer;

/**
 * Gera trajetórias a partir de waypoints (pose com heading = direção de deslocamento no ponto).
 * Ex.: TrajectoryGenerator.generate(Arrays.asList(new Pose2d(0,0,0), new Pose2d(48,24,0)), config)
 */
public final class TrajectoryGenerator {
    private TrajectoryGenerator() {}

    public static Trajectory generate(List<Pose2d> waypoints, TrajectoryConfig config) {
        if (waypoints.size() < 2) {
            throw new IllegalArgumentException("São necessários pelo menos 2 waypoints.");
        }

        Transform2d flip = new Transform2d(new Translation2d(), Rotation2d.fromDegrees(180));

        List<Pose2d> wp = new ArrayList<>(waypoints.size());
        for (Pose2d p : waypoints) wp.add(config.isReversed() ? p.plus(flip) : p);

        List<PoseWithCurvature> points = new ArrayList<>();
        for (int i = 0; i < wp.size() - 1; i++) {
            QuinticHermiteSpline spline = QuinticHermiteSpline.fromPoses(wp.get(i), wp.get(i + 1));
            List<PoseWithCurvature> segment = SplineParameterizer.parameterize(spline,
                    config.getSplineMaxDx(), config.getSplineMaxDy(), config.getSplineMaxDtheta());
            // pula o 1º ponto dos segmentos seguintes (duplicado com o último do anterior)
            points.addAll(i == 0 ? segment : segment.subList(1, segment.size()));
        }

        if (config.isReversed()) {
            for (PoseWithCurvature p : points) {
                p.pose = p.pose.plus(flip);
                p.curvature *= -1;
            }
        }

        return TrajectoryParameterizer.timeParameterize(points, config.getConstraints(),
                config.getStartVelocity(), config.getEndVelocity(),
                config.getMaxVelocity(), config.getMaxAcceleration(), config.isReversed());
    }
}
