package dev.easyfamily.EasyStack.trajectory;

import dev.easyfamily.EasyStack.controllable.EasyMotionProfile;

/** Adapts the fixed-goal TrapezoidProfile to EasyMotor's common profile API. */
public class TrapezoidProfileAdapter implements EasyMotionProfile {

    private final TrapezoidProfile.Constraints constraints;
    private TrapezoidProfile profile;

    public TrapezoidProfileAdapter(TrapezoidProfile.Constraints constraints) {
        if (constraints == null) {
            throw new IllegalArgumentException("constraints cannot be null");
        }
        this.constraints = constraints;
    }

    @Override
    public void reset(State initial, State goal) {
        if (initial == null || goal == null) {
            throw new IllegalArgumentException("initial and goal cannot be null");
        }
        profile = new TrapezoidProfile(
                constraints,
                new TrapezoidProfile.State(goal.position, goal.velocity),
                new TrapezoidProfile.State(initial.position, initial.velocity)
        );
    }

    @Override
    public State sample(double elapsedTime) {
        ensureInitialized();
        double t = clampTime(elapsedTime);
        TrapezoidProfile.State s = profile.calculate(t);
        double acceleration = estimateAcceleration(t);
        return new State(s.position, s.velocity, acceleration);
    }

    @Override
    public double totalTime() {
        return profile == null ? 0.0 : profile.totalTime();
    }

    private double estimateAcceleration(double t) {
        double total = profile.totalTime();
        if (total <= 0.0) {
            return 0.0;
        }
        final double h = 0.001;
        double t1 = Math.max(0.0, t - h);
        double t2 = Math.min(total, t + h);
        if (t2 <= t1) {
            return 0.0;
        }
        double v1 = profile.calculate(t1).velocity;
        double v2 = profile.calculate(t2).velocity;
        return (v2 - v1) / (t2 - t1);
    }

    private double clampTime(double t) {
        return Math.max(0.0, Math.min(profile.totalTime(), t));
    }

    private void ensureInitialized() {
        if (profile == null) {
            throw new IllegalStateException("Call reset(initial, goal) before sample().");
        }
    }
}
