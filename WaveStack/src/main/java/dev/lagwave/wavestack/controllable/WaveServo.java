package dev.lagwave.wavestack.controllable;

import com.qualcomm.robotcore.hardware.PwmControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public interface WaveServo extends WaveHardwareDevice {

    void setPosition(double position);
    void rotateBy(double deltaPosition);
    void setAngle(double angle);
    void setAngle(double angle, AngleUnit angleUnit);
    void turnByAngle(double angle);
    void turnByAngle(double angle, AngleUnit angleUnit);


    WaveServo setLimits(double min, double max);
    WaveServo setAngleRange(double degrees);
    WaveServo setInverted(boolean inverted);
    WaveServo reverse();
    WaveServo setPwmRange(PwmControl.PwmRange pwmRange);
    WaveServo setCachingTolerance(double tolerance);
    WaveServo setSpeed(double positionPerSecond);

    WaveServo addState(String name, double position);
    void setState(String name);

    // Leitura
    double getPosition();
    double getRawPosition();
    double getAngle();
    double getAngle(AngleUnit angleUnit);
    double getEstimatedPosition();
    boolean isAtTarget();
    boolean isInverted();
}