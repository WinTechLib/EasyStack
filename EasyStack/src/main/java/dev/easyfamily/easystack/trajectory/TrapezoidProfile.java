package dev.easyfamily.easystack.trajectory;
public class TrapezoidProfile {
    public static class Constraints {
        public final double maxVelocity;
        public final double maxAcceleration;

        public Constraints(double maxVelocity, double maxAcceleration) {
            this.maxVelocity = maxVelocity;
            this.maxAcceleration = maxAcceleration;
        }
    }

    public static class State {
        public final double position;
        public final double velocity;

        public State() {
            this(0.0, 0.0);
        }

        public State(double position, double velocity) {
            this.position = position;
            this.velocity = velocity;
        }

        @Override
        public String toString() {
            return "State(pos=" + position + ", vel=" + velocity + ")";
        }
    }

    private final int direction;
    private final double maxVelocity, maxAcceleration;
    private final double curPos, curVel, goalPos, goalVel;
    private final double endAccel, endFullSpeed, endDeccel;

    public TrapezoidProfile(Constraints constraints, State goal) {
        this(constraints, goal, new State());
    }

    public TrapezoidProfile(Constraints constraints, State goal, State initial) {
        this.direction = initial.position > goal.position ? -1 : 1;
        this.maxVelocity = constraints.maxVelocity;
        this.maxAcceleration = constraints.maxAcceleration;

        this.curPos = initial.position * direction;
        this.curVel = Math.min(initial.velocity * direction, maxVelocity);
        this.goalPos = goal.position * direction;
        this.goalVel = goal.velocity * direction;
        double cutoffBegin = curVel / maxAcceleration;
        double cutoffDistBegin = cutoffBegin * cutoffBegin * maxAcceleration / 2.0;
        double cutoffEnd = goalVel / maxAcceleration;
        double cutoffDistEnd = cutoffEnd * cutoffEnd * maxAcceleration / 2.0;

        double fullTrapezoidDist = cutoffDistBegin + (goalPos - curPos) + cutoffDistEnd;
        double accelerationTime = maxVelocity / maxAcceleration;
        double fullSpeedDist = fullTrapezoidDist - accelerationTime * accelerationTime * maxAcceleration;

        if (fullSpeedDist < 0) {
            accelerationTime = Math.sqrt(fullTrapezoidDist / maxAcceleration);
            fullSpeedDist = 0;
        }

        this.endAccel = accelerationTime - cutoffBegin;
        this.endFullSpeed = endAccel + fullSpeedDist / maxVelocity;
        this.endDeccel = endFullSpeed + accelerationTime - cutoffEnd;
    }

    public State calculate(double t) {
        double pos = curPos, vel = curVel;

        if (t < endAccel) {
            vel = curVel + t * maxAcceleration;
            pos = curPos + (curVel + t * maxAcceleration / 2.0) * t;
        } else if (t < endFullSpeed) {
            vel = maxVelocity;
            pos = curPos + (curVel + endAccel * maxAcceleration / 2.0) * endAccel + maxVelocity * (t - endAccel);
        } else if (t <= endDeccel) {
            double timeLeft = endDeccel - t;
            vel = goalVel + timeLeft * maxAcceleration;
            pos = goalPos - (goalVel + timeLeft * maxAcceleration / 2.0) * timeLeft;
        } else {
            pos = goalPos;
            vel = goalVel;
        }
        return new State(pos * direction, vel * direction);
    }

    public double totalTime() { return endDeccel; }

    public boolean isFinished(double t) { return t >= totalTime(); }
}
