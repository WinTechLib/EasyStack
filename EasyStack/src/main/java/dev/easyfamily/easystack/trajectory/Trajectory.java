package dev.easyfamily.easystack.trajectory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.easyfamily.easystack.geometry.Pose2d;
import dev.easyfamily.easystack.geometry.Transform2d;

/** Trajetória amostrada no tempo: lista de estados (t, velocidade, aceleração, pose, curvatura). */
public class Trajectory {
    /** pose.rotation = direção do deslocamento (não é o heading do robô holonômico). */
    public static class State {
        public double time;
        public double velocity;
        public double acceleration;
        public Pose2d pose;
        public double curvature;

        public State(double time, double velocity, double acceleration, Pose2d pose, double curvature) {
            this.time = time;
            this.velocity = velocity;
            this.acceleration = acceleration;
            this.pose = pose;
            this.curvature = curvature;
        }

        public State interpolate(State end, double i) {
            double newT = time + (end.time - time) * i;
            double deltaT = newT - time;
            if (deltaT < 0) return end.interpolate(this, 1.0 - i);

            boolean reversing = velocity < 0 || (Math.abs(velocity) < 1e-9 && acceleration < 0);
            double newV = velocity + acceleration * deltaT;
            double newS = (velocity * deltaT + 0.5 * acceleration * deltaT * deltaT) * (reversing ? -1.0 : 1.0);

            double dist = end.pose.getTranslation().getDistance(pose.getTranslation());
            double frac = dist < 1e-9 ? 0.0 : newS / dist;

            return new State(newT, newV, acceleration,
                    pose.interpolate(end.pose, frac),
                    curvature + (end.curvature - curvature) * frac);
        }

        @Override
        public String toString() {
            return String.format("State(t=%.3f, v=%.3f, a=%.3f, %s, k=%.4f)", time, velocity, acceleration, pose, curvature);
        }
    }

    private final List<State> states;
    private final double totalTime;

    public Trajectory(List<State> states) {
        if (states.isEmpty()) throw new IllegalArgumentException("A trajetória precisa de pelo menos 1 estado.");
        this.states = Collections.unmodifiableList(new ArrayList<>(states));
        this.totalTime = states.get(states.size() - 1).time;
    }

    public List<State> getStates() { return states; }

    public double getTotalTime() { return totalTime; }

    public Pose2d getInitialPose() { return states.get(0).pose; }

    /** Estado no instante t (interpolado entre as amostras). */
    public State sample(double time) {
        if (time <= states.get(0).time) return states.get(0);
        if (time >= totalTime) return states.get(states.size() - 1);

        int low = 1, high = states.size() - 1;
        while (low != high) {
            int mid = (low + high) / 2;
            if (states.get(mid).time < time) low = mid + 1; else high = mid;
        }

        State prev = states.get(low - 1);
        State next = states.get(low);
        if (Math.abs(next.time - prev.time) < 1e-9) return next;
        return prev.interpolate(next, (time - prev.time) / (next.time - prev.time));
    }

    /** Move/rotaciona a trajetória inteira (a 1ª pose recebe o transform). */
    public Trajectory transformBy(Transform2d transform) {
        State first = states.get(0);
        Pose2d firstPose = first.pose;
        Pose2d newFirst = firstPose.plus(transform);

        List<State> out = new ArrayList<>();
        out.add(new State(first.time, first.velocity, first.acceleration, newFirst, first.curvature));
        for (int i = 1; i < states.size(); i++) {
            State s = states.get(i);
            out.add(new State(s.time, s.velocity, s.acceleration,
                    newFirst.plus(s.pose.minus(firstPose)), s.curvature));
        }
        return new Trajectory(out);
    }

    /** Reexpressa todas as poses relativas a "pose". */
    public Trajectory relativeTo(Pose2d pose) {
        List<State> out = new ArrayList<>();
        for (State s : states) {
            out.add(new State(s.time, s.velocity, s.acceleration, s.pose.relativeTo(pose), s.curvature));
        }
        return new Trajectory(out);
    }

    /** Concatena outra trajetória ao final desta (os tempos da outra são deslocados). */
    public Trajectory concatenate(Trajectory other) {
        List<State> out = new ArrayList<>(states);
        double offset = totalTime;
        List<State> o = other.getStates();
        for (int i = 1; i < o.size(); i++) { // pula o 1º: duplicado com o último desta
            State s = o.get(i);
            out.add(new State(s.time + offset, s.velocity, s.acceleration, s.pose, s.curvature));
        }
        return new Trajectory(out);
    }
}
