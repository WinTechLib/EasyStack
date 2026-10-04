package dev.easyfamily.easystack.trajectory;

import java.util.ArrayList;
import java.util.List;

import dev.easyfamily.easystack.spline.PoseWithCurvature;
import dev.easyfamily.easystack.trajectory.constraint.TrajectoryConstraint;

/**
 * Parametrização de tempo: transforma uma lista de pontos geométricos em uma trajetória no tempo,
 * respeitando velocidade/aceleração máximas e as restrições (passe para frente + passe para trás).
 */
public final class TrajectoryParameterizer {
    private TrajectoryParameterizer() {}

    private static class ConstrainedState {
        PoseWithCurvature pose;
        double distance;
        double maxVelocity;
        double minAcceleration;
        double maxAcceleration;

        ConstrainedState() {}

        ConstrainedState(PoseWithCurvature pose, double distance, double maxVelocity,
                         double minAcceleration, double maxAcceleration) {
            this.pose = pose;
            this.distance = distance;
            this.maxVelocity = maxVelocity;
            this.minAcceleration = minAcceleration;
            this.maxAcceleration = maxAcceleration;
        }
    }

    public static Trajectory timeParameterize(List<PoseWithCurvature> points,
                                              List<TrajectoryConstraint> constraints,
                                              double startVelocity, double endVelocity,
                                              double maxVelocity, double maxAcceleration,
                                              boolean reversed) {
        List<ConstrainedState> cs = new ArrayList<>(points.size());
        ConstrainedState predecessor = new ConstrainedState(points.get(0), 0.0, startVelocity, -maxAcceleration, maxAcceleration);

        // ---- passe para frente ----
        for (int i = 0; i < points.size(); i++) {
            ConstrainedState state = new ConstrainedState();
            cs.add(state);
            state.pose = points.get(i);

            double ds = state.pose.pose.getTranslation().getDistance(predecessor.pose.pose.getTranslation());
            state.distance = predecessor.distance + ds;

            while (true) {
                // vf = sqrt(vi^2 + 2 a d)
                state.maxVelocity = Math.min(maxVelocity,
                        Math.sqrt(predecessor.maxVelocity * predecessor.maxVelocity
                                + predecessor.maxAcceleration * ds * 2.0));
                state.minAcceleration = -maxAcceleration;
                state.maxAcceleration = maxAcceleration;

                for (TrajectoryConstraint c : constraints) {
                    state.maxVelocity = Math.min(state.maxVelocity,
                            c.getMaxVelocity(state.pose.pose, state.pose.curvature, state.maxVelocity));
                }

                enforceAccelerationLimits(reversed, constraints, state);

                if (ds < 1e-6) break;

                double actual = (state.maxVelocity * state.maxVelocity
                        - predecessor.maxVelocity * predecessor.maxVelocity) / (ds * 2.0);

                if (state.maxAcceleration < actual - 1e-6) {
                    predecessor.maxAcceleration = state.maxAcceleration;
                } else {
                    if (actual > predecessor.minAcceleration + 1e-6) {
                        predecessor.maxAcceleration = actual;
                    }
                    break;
                }
            }
            predecessor = state;
        }

        ConstrainedState last = cs.get(cs.size() - 1);
        ConstrainedState successor = new ConstrainedState(points.get(points.size() - 1), last.distance,
                endVelocity, -maxAcceleration, maxAcceleration);

        // ---- passe para trás ----
        for (int i = cs.size() - 1; i >= 0; i--) {
            ConstrainedState state = cs.get(i);
            double ds = state.distance - successor.distance; // negativo

            while (true) {
                double newMaxVelocity = Math.sqrt(successor.maxVelocity * successor.maxVelocity
                        + successor.minAcceleration * ds * 2.0);

                if (newMaxVelocity >= state.maxVelocity) break;

                state.maxVelocity = newMaxVelocity;
                enforceAccelerationLimits(reversed, constraints, state);

                if (ds > -1e-6) break;

                double actual = (state.maxVelocity * state.maxVelocity
                        - successor.maxVelocity * successor.maxVelocity) / (ds * 2.0);

                if (state.minAcceleration > actual + 1e-6) {
                    successor.minAcceleration = state.minAcceleration;
                } else {
                    successor.minAcceleration = actual;
                    break;
                }
            }
            successor = state;
        }

        // ---- integra no tempo ----
        List<Trajectory.State> states = new ArrayList<>(cs.size());
        double time = 0.0, distance = 0.0, velocity = 0.0;

        for (int i = 0; i < cs.size(); i++) {
            ConstrainedState s = cs.get(i);
            double ds = s.distance - distance;
            double accel = (s.maxVelocity * s.maxVelocity - velocity * velocity) / (ds * 2.0);

            double dt = 0.0;
            if (i > 0) {
                states.get(i - 1).acceleration = reversed ? -accel : accel;
                if (Math.abs(accel) > 1e-6) {
                    dt = (s.maxVelocity - velocity) / accel;
                } else if (Math.abs(velocity) > 1e-6) {
                    dt = ds / velocity;
                } else {
                    throw new IllegalStateException("Falha na parametrização de tempo no ponto " + i
                            + " (velocidade 0 e aceleração 0). Verifique as restrições.");
                }
            }

            velocity = s.maxVelocity;
            distance = s.distance;
            time += dt;

            states.add(new Trajectory.State(time, reversed ? -velocity : velocity, reversed ? -accel : accel,
                    s.pose.pose, s.pose.curvature));
        }
        return new Trajectory(states);
    }

    private static void enforceAccelerationLimits(boolean reverse, List<TrajectoryConstraint> constraints,
                                                  ConstrainedState state) {
        for (TrajectoryConstraint c : constraints) {
            double factor = reverse ? -1.0 : 1.0;
            TrajectoryConstraint.MinMax mm = c.getMinMaxAcceleration(state.pose.pose, state.pose.curvature,
                    state.maxVelocity * factor);
            if (mm.min > mm.max) {
                throw new IllegalStateException("Restrição inválida: aceleração mínima maior que a máxima.");
            }
            state.minAcceleration = Math.max(state.minAcceleration, reverse ? -mm.max : mm.min);
            state.maxAcceleration = Math.min(state.maxAcceleration, reverse ? -mm.min : mm.max);
        }
    }
}
