package dev.easyfamily.easystack.control.wpiLibControllers;

import dev.easyfamily.easystack.trajectory.TrapezoidProfileEx;
import dev.easyfamily.easystack.util.MathFunctions;

public class EasyProfiledPID {
    private static int instances;

    private EasyPIDControllerEx m_controller;
    private double m_minimumInput;
    private double m_maximumInput;

    private TrapezoidProfileEx.Constraints m_constraints;
    private TrapezoidProfileEx m_profile;
    private TrapezoidProfileEx.State m_goal = new TrapezoidProfileEx.State();
    private TrapezoidProfileEx.State m_setpoint = new TrapezoidProfileEx.State();

    public EasyProfiledPID(
            double Kp, double Ki, double Kd, TrapezoidProfileEx.Constraints constraints) {
        this(Kp, Ki, Kd, constraints, 0.02);
    }

    public EasyProfiledPID(
            double Kp, double Ki, double Kd, TrapezoidProfileEx.Constraints constraints, double period) {
        m_controller = new EasyPIDControllerEx(Kp, Ki, Kd, period);
        m_constraints = constraints;
        m_profile = new TrapezoidProfileEx(m_constraints);
        instances++;
    }

    public void setPID(double Kp, double Ki, double Kd) {
        m_controller.setPID(Kp, Ki, Kd);
    }

    public void setP(double Kp) {
        m_controller.setP(Kp);
    }

    public void setI(double Ki) {
        m_controller.setI(Ki);
    }

    public void setD(double Kd) {
        m_controller.setD(Kd);
    }

    public void setIZone(double iZone) {
        m_controller.setIZone(iZone);
    }

    public double getP() {
        return m_controller.getP();
    }

    public double getI() {
        return m_controller.getI();
    }

    public double getD() {
        return m_controller.getD();
    }

    public double getIZone() {
        return m_controller.getIZone();
    }

    public double getPeriod() {
        return m_controller.getPeriod();
    }

    public double getPositionTolerance() {
        return m_controller.getErrorTolerance();
    }

    public double getVelocityTolerance() {
        return m_controller.getErrorDerivativeTolerance();
    }

    public double getAccumulatedError() {
        return m_controller.getAccumulatedError();
    }

    public void setGoal(TrapezoidProfileEx.State goal) {
        m_goal = goal;
    }

    public void setGoal(double goal) {
        m_goal = new TrapezoidProfileEx.State(goal, 0);
    }

    public TrapezoidProfileEx.State getGoal() {
        return m_goal;
    }

    public boolean atGoal() {
        return atSetpoint() && m_goal.equals(m_setpoint);
    }

    public void setConstraints(TrapezoidProfileEx.Constraints constraints) {
        m_constraints = constraints;
        m_profile = new TrapezoidProfileEx(m_constraints);
    }

    public TrapezoidProfileEx.Constraints getConstraints() {
        return m_constraints;
    }

    public TrapezoidProfileEx.State getSetpoint() {
        return m_setpoint;
    }

    public boolean atSetpoint() {
        return m_controller.atSetpoint();
    }

    public void enableContinuousInput(double minimumInput, double maximumInput) {
        m_controller.enableContinuousInput(minimumInput, maximumInput);
        m_minimumInput = minimumInput;
        m_maximumInput = maximumInput;
    }

    public void disableContinuousInput() {
        m_controller.disableContinuousInput();
    }

    public void setIntegratorRange(double minimumIntegral, double maximumIntegral) {
        m_controller.setIntegratorRange(minimumIntegral, maximumIntegral);
    }

    public void setTolerance(double positionTolerance) {
        setTolerance(positionTolerance, Double.POSITIVE_INFINITY);
    }

    public void setTolerance(double positionTolerance, double velocityTolerance) {
        m_controller.setTolerance(positionTolerance, velocityTolerance);
    }

    public double getPositionError() {
        return m_controller.getError();
    }

    public double getVelocityError() {
        return m_controller.getErrorDerivative();
    }

    public double calculate(double measurement) {
        if (m_controller.isContinuousInputEnabled()) {
            // Get error which is the smallest distance between goal and measurement
            double errorBound = (m_maximumInput - m_minimumInput) / 2.0;
            double goalMinDistance =
                    MathFunctions.inputModulus(m_goal.position - measurement, -errorBound, errorBound);
            double setpointMinDistance =
                    MathFunctions.inputModulus(m_setpoint.position - measurement, -errorBound, errorBound);

            m_goal.position = goalMinDistance + measurement;
            m_setpoint.position = setpointMinDistance + measurement;
        }

        m_setpoint = m_profile.calculate(getPeriod(), m_setpoint, m_goal);
        return m_controller.calculate(measurement, m_setpoint.position);
    }

    public double calculate(double measurement, TrapezoidProfileEx.State goal) {
        setGoal(goal);
        return calculate(measurement);
    }

    public double calculate(double measurement, double goal) {
        setGoal(goal);
        return calculate(measurement);
    }

    public double calculate(
            double measurement, TrapezoidProfileEx.State goal, TrapezoidProfileEx.Constraints constraints) {
        setConstraints(constraints);
        return calculate(measurement, goal);
    }

    public void reset(TrapezoidProfileEx.State measurement) {
        m_controller.reset();
        m_setpoint = measurement;
    }

    public void reset(double measuredPosition, double measuredVelocity) {
        reset(new TrapezoidProfileEx.State(measuredPosition, measuredVelocity));
    }

    public void reset(double measuredPosition) {
        reset(measuredPosition, 0.0);
    }
}
