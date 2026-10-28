package dev.easyfamily.EasyStack.trajectory;

import dev.easyfamily.EasyStack.controllable.EasyMotionProfile;

/** Adapts TrapezoidProfileEx, whose current and goal states are passed to calculate(). */
public class TrapezoidProfileExAdapter implements EasyMotionProfile {

    private final TrapezoidProfileEx.Constraints constraints;
    private final TrapezoidProfileEx profile;
    private TrapezoidProfileEx.State initialState;
    private TrapezoidProfileEx.State goalState;

    public TrapezoidProfileExAdapter(TrapezoidProfileEx.Constraints constraints) {
        if (constraints == null) {
            throw new IllegalArgumentException("constraints cannot be null");
        }
        this.constraints = constraints;
        this.profile = new TrapezoidProfileEx(constraints);
    }

    @Override
    public void reset(State initial, State goal) {
        if (initial == null || goal == null) {
            throw new IllegalArgumentException("initial and goal cannot be null");
        }
        initialState = new TrapezoidProfileEx.State(initial.position, initial.velocity);
        goalState = new TrapezoidProfileEx.State(goal.position, goal.velocity);
    }

    @Override
    public State sample(double elapsedTime) {
        ensureInitialized();
        double t = Math.max(0.0, elapsedTime);
        TrapezoidProfileEx.State s = profile.calculate(t, initialState, goalState);
        double acceleration = estimateAcceleration(t);
        return new State(s.position, s.velocity, acceleration);
    }

    @Override
    public double totalTime() {
        return profile.totalTime();
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
        double v1 = profile.calculate(t1, initialState, goalState).velocity;
        double v2 = profile.calculate(t2, initialState, goalState).velocity;
        return (v2 - v1) / (t2 - t1);
    }

    private void ensureInitialized() {
        if (initialState == null || goalState == null) {
            throw new IllegalStateException("Call reset(initial, goal) before sample().");
        }
    }
}
