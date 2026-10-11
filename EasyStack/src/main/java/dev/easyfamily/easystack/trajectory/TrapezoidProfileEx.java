package dev.easyfamily.easystack.trajectory;

public class TrapezoidProfileEx {
    private int m_direction;

    private final Constraints m_constraints;
    private State m_current = new State();

    private double m_endAccel;
    private double m_endFullSpeed;
    private double m_endDecel;

    public static class Constraints {
        public final double maxVelocity;

        public final double maxAcceleration;

        public Constraints(double maxVelocity, double maxAcceleration) {
            if (maxVelocity < 0.0 || maxAcceleration < 0.0) {
                throw new IllegalArgumentException("Constraints must be non-negative");
            }

            this.maxVelocity = maxVelocity;
            this.maxAcceleration = maxAcceleration;
        }
    }

    public static class State {
        public double position;
        public double velocity;
        public State() {}

        public State(double position, double velocity) {
            this.position = position;
            this.velocity = velocity;
        }

    }

    public TrapezoidProfileEx(Constraints constraints) {
        m_constraints = constraints;
    }

    public State calculate(double t, State current, State goal) {
        m_direction = shouldFlipAcceleration(current, goal) ? -1 : 1;
        m_current = direct(current);
        goal = direct(goal);

        if (Math.abs(m_current.velocity) > m_constraints.maxVelocity) {
            m_current.velocity = Math.copySign(m_constraints.maxVelocity, m_current.velocity);
        }

        double cutoffBegin = m_current.velocity / m_constraints.maxAcceleration;
        double cutoffDistBegin = cutoffBegin * cutoffBegin * m_constraints.maxAcceleration / 2.0;

        double cutoffEnd = goal.velocity / m_constraints.maxAcceleration;
        double cutoffDistEnd = cutoffEnd * cutoffEnd * m_constraints.maxAcceleration / 2.0;

        double fullTrapezoidDist =
                cutoffDistBegin + (goal.position - m_current.position) + cutoffDistEnd;
        double accelerationTime = m_constraints.maxVelocity / m_constraints.maxAcceleration;

        double fullSpeedDist =
                fullTrapezoidDist - accelerationTime * accelerationTime * m_constraints.maxAcceleration;

        if (fullSpeedDist < 0) {
            accelerationTime = Math.sqrt(fullTrapezoidDist / m_constraints.maxAcceleration);
            fullSpeedDist = 0;
        }

        m_endAccel = accelerationTime - cutoffBegin;
        m_endFullSpeed = m_endAccel + fullSpeedDist / m_constraints.maxVelocity;
        m_endDecel = m_endFullSpeed + accelerationTime - cutoffEnd;
        State result = new State(m_current.position, m_current.velocity);

        if (t < m_endAccel) {
            result.velocity += t * m_constraints.maxAcceleration;
            result.position += (m_current.velocity + t * m_constraints.maxAcceleration / 2.0) * t;
        } else if (t < m_endFullSpeed) {
            result.velocity = m_constraints.maxVelocity;
            result.position +=
                    (m_current.velocity + m_endAccel * m_constraints.maxAcceleration / 2.0) * m_endAccel
                            + m_constraints.maxVelocity * (t - m_endAccel);
        } else if (t <= m_endDecel) {
            result.velocity = goal.velocity + (m_endDecel - t) * m_constraints.maxAcceleration;
            double timeLeft = m_endDecel - t;
            result.position =
                    goal.position
                            - (goal.velocity + timeLeft * m_constraints.maxAcceleration / 2.0) * timeLeft;
        } else {
            result = goal;
        }

        return direct(result);
    }

    public double timeLeftUntil(double target) {
        double position = m_current.position * m_direction;
        double velocity = m_current.velocity * m_direction;

        double endAccel = m_endAccel * m_direction;
        double endFullSpeed = m_endFullSpeed * m_direction - endAccel;

        if (target < position) {
            endAccel = -endAccel;
            endFullSpeed = -endFullSpeed;
            velocity = -velocity;
        }

        endAccel = Math.max(endAccel, 0);
        endFullSpeed = Math.max(endFullSpeed, 0);

        final double acceleration = m_constraints.maxAcceleration;
        final double deceleration = -m_constraints.maxAcceleration;

        double distToTarget = Math.abs(target - position);
        if (distToTarget < 1e-6) {
            return 0;
        }

        double accelDist = velocity * endAccel + 0.5 * acceleration * endAccel * endAccel;

        double decelVelocity;
        if (endAccel > 0) {
            decelVelocity = Math.sqrt(Math.abs(velocity * velocity + 2 * acceleration * accelDist));
        } else {
            decelVelocity = velocity;
        }

        double fullSpeedDist = m_constraints.maxVelocity * endFullSpeed;
        double decelDist;

        if (accelDist > distToTarget) {
            accelDist = distToTarget;
            fullSpeedDist = 0;
            decelDist = 0;
        } else if (accelDist + fullSpeedDist > distToTarget) {
            fullSpeedDist = distToTarget - accelDist;
            decelDist = 0;
        } else {
            decelDist = distToTarget - fullSpeedDist - accelDist;
        }

        double accelTime =
                (-velocity + Math.sqrt(Math.abs(velocity * velocity + 2 * acceleration * accelDist)))
                        / acceleration;

        double decelTime =
                (-decelVelocity
                        + Math.sqrt(Math.abs(decelVelocity * decelVelocity + 2 * deceleration * decelDist)))
                        / deceleration;

        double fullSpeedTime = fullSpeedDist / m_constraints.maxVelocity;

        return accelTime + fullSpeedTime + decelTime;
    }

    public double totalTime() {
        return m_endDecel;
    }

    public boolean isFinished(double t) {
        return t >= totalTime();
    }

    private static boolean shouldFlipAcceleration(State initial, State goal) {
        return initial.position > goal.position;
    }

    private State direct(State in) {
        State result = new State(in.position, in.velocity);
        result.position = result.position * m_direction;
        result.velocity = result.velocity * m_direction;
        return result;
    }
}
