package dev.easyfamily.easystack.control.wpiLibControllers;

import dev.easyfamily.easystack.util.MathFunctions;

public class EasyPIDControllerEx {
    private static int instances;
    private double m_kp;
    private double m_ki;
    private double m_kd;

    private double m_iZone = Double.POSITIVE_INFINITY;
    private final double m_period;

    private double m_maximumIntegral = 1.0;

    private double m_minimumIntegral = -1.0;

    private double m_maximumInput;

    private double m_minimumInput;

    private boolean m_continuous;


    private double m_error;
    private double m_errorDerivative;
    private double m_prevError;

    private double m_totalError;

    private double m_errorTolerance = 0.05;
    private double m_errorDerivativeTolerance = Double.POSITIVE_INFINITY;

    private double m_setpoint;
    private double m_measurement;

    private boolean m_haveMeasurement;
    private boolean m_haveSetpoint;

    public EasyPIDControllerEx(double kp, double ki, double kd) {
        this(kp, ki, kd, 0.02);
    }

    @SuppressWarnings("this-escape")
    public EasyPIDControllerEx(double kp, double ki, double kd, double period) {
        m_kp = kp;
        m_ki = ki;
        m_kd = kd;

        if (kp < 0.0) {
            throw new IllegalArgumentException("Kp must be a non-negative number!");
        }
        if (ki < 0.0) {
            throw new IllegalArgumentException("Ki must be a non-negative number!");
        }
        if (kd < 0.0) {
            throw new IllegalArgumentException("Kd must be a non-negative number!");
        }
        if (period <= 0.0) {
            throw new IllegalArgumentException("Controller period must be a positive number!");
        }
        m_period = period;

        instances++;
    }

    public void setPID(double kp, double ki, double kd) {
        m_kp = kp;
        m_ki = ki;
        m_kd = kd;
    }

    public void setP(double kp) {
        m_kp = kp;
    }

    public void setI(double ki) {
        m_ki = ki;
    }

    public void setD(double kd) {
        m_kd = kd;
    }

    public void setIZone(double iZone) {
        if (iZone < 0) {
            throw new IllegalArgumentException("IZone must be a non-negative number!");
        }
        m_iZone = iZone;
    }

    public double getP() {
        return m_kp;
    }

    public double getI() {
        return m_ki;
    }

    public double getD() {
        return m_kd;
    }

    public double getIZone() {
        return m_iZone;
    }

    public double getPeriod() {
        return m_period;
    }

    @Deprecated(forRemoval = true, since = "2025")
    public double getPositionTolerance() {
        return m_errorTolerance;
    }

    @Deprecated(forRemoval = true, since = "2025")
    public double getVelocityTolerance() {
        return m_errorDerivativeTolerance;
    }

    public double getErrorTolerance() {
        return m_errorTolerance;
    }

    public double getErrorDerivativeTolerance() {
        return m_errorDerivativeTolerance;
    }

    public double getAccumulatedError() {
        return m_totalError;
    }

    public void setSetpoint(double setpoint) {
        m_setpoint = setpoint;
        m_haveSetpoint = true;

        if (m_continuous) {
            double errorBound = (m_maximumInput - m_minimumInput) / 2.0;
            m_error = MathFunctions.inputModulus(m_setpoint - m_measurement, -errorBound, errorBound);
        } else {
            m_error = m_setpoint - m_measurement;
        }

        m_errorDerivative = (m_error - m_prevError) / m_period;
    }

    public double getSetpoint() {
        return m_setpoint;
    }

    public boolean atSetpoint() {
        return m_haveMeasurement
                && m_haveSetpoint
                && Math.abs(m_error) < m_errorTolerance
                && Math.abs(m_errorDerivative) < m_errorDerivativeTolerance;
    }

    public void enableContinuousInput(double minimumInput, double maximumInput) {
        m_continuous = true;
        m_minimumInput = minimumInput;
        m_maximumInput = maximumInput;
    }

    public void disableContinuousInput() {
        m_continuous = false;
    }

    public boolean isContinuousInputEnabled() {
        return m_continuous;
    }

    public void setIntegratorRange(double minimumIntegral, double maximumIntegral) {
        m_minimumIntegral = minimumIntegral;
        m_maximumIntegral = maximumIntegral;
    }

    public void setTolerance(double errorTolerance) {
        setTolerance(errorTolerance, Double.POSITIVE_INFINITY);
    }

    public void setTolerance(double errorTolerance, double errorDerivativeTolerance) {
        m_errorTolerance = errorTolerance;
        m_errorDerivativeTolerance = errorDerivativeTolerance;
    }

    @Deprecated(forRemoval = true, since = "2025")
    public double getPositionError() {
        return m_error;
    }

    @Deprecated(forRemoval = true, since = "2025")
    public double getVelocityError() {
        return m_errorDerivative;
    }

    public double getError() {
        return m_error;
    }

    public double getErrorDerivative() {
        return m_errorDerivative;
    }

    public double calculate(double measurement, double setpoint) {
        m_setpoint = setpoint;
        m_haveSetpoint = true;
        return calculate(measurement);
    }

    public double calculate(double measurement) {
        m_measurement = measurement;
        m_prevError = m_error;
        m_haveMeasurement = true;

        if (m_continuous) {
            double errorBound = (m_maximumInput - m_minimumInput) / 2.0;
            m_error = MathFunctions.inputModulus(m_setpoint - m_measurement, -errorBound, errorBound);
        } else {
            m_error = m_setpoint - m_measurement;
        }

        m_errorDerivative = (m_error - m_prevError) / m_period;

        // If the absolute value of the position error is greater than IZone, reset the total error
        if (Math.abs(m_error) > m_iZone) {
            m_totalError = 0;
        } else if (m_ki != 0) {
            m_totalError =
                    MathFunctions.clamp(
                            m_totalError + m_error * m_period,
                            m_minimumIntegral / m_ki,
                            m_maximumIntegral / m_ki);
        }

        return m_kp * m_error + m_ki * m_totalError + m_kd * m_errorDerivative;
    }

    /** Resets the previous error and the integral term. */
    public void reset() {
        m_error = 0;
        m_prevError = 0;
        m_totalError = 0;
        m_errorDerivative = 0;
        m_haveMeasurement = false;
    }
}
