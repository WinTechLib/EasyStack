package dev.easyfamily.wintechlib.wpiLibControllers;

public class EasyBangBangController {

    private double m_tolerance;

    private double m_setpoint;
    private double m_measurement;

    public EasyBangBangController(double tolerance) {
        setTolerance(tolerance);
    }

    public EasyBangBangController() {
        this(Double.POSITIVE_INFINITY);
    }

    public void setSetpoint(double setpoint) {
        m_setpoint = setpoint;
    }

    public double getSetpoint() {
        return m_setpoint;
    }

    public boolean atSetpoint() {
        return Math.abs(m_setpoint - m_measurement) < m_tolerance;
    }

    public final void setTolerance(double tolerance) {
        m_tolerance = tolerance;
    }

    public double getTolerance() {
        return m_tolerance;
    }

    public double getMeasurement() {
        return m_measurement;
    }

    public double getError() {
        return m_setpoint - m_measurement;
    }

    public double calculate(double measurement, double setpoint) {
        m_measurement = measurement;
        m_setpoint = setpoint;

        return measurement < setpoint ? 1 : 0;
    }

    public double calculate(double measurement) {
        return calculate(measurement, m_setpoint);
    }
}
