package dev.easyfamily.EasyStack.controllable;
public interface EasyMotionProfile {

    /** Rebuilds/restarts the profile from initial state to goal state. */
    void reset(State initial, State goal);

    /** Samples the profile at elapsedTime seconds after reset. */
    State sample(double elapsedTime);

    /** Total profile duration in seconds. */
    double totalTime();

    default boolean isFinished(double elapsedTime) {
        return elapsedTime >= totalTime();
    }

    final class State {
        public final double position;
        public final double velocity;
        public final double acceleration;

        public State(double position, double velocity) {
            this(position, velocity, 0.0);
        }

        public State(double position, double velocity, double acceleration) {
            this.position = position;
            this.velocity = velocity;
            this.acceleration = acceleration;
        }
    }
}
