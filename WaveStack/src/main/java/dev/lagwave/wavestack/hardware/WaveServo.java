package dev.lagwave.wavestack.hardware;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public interface WaveServo extends WaveHardwareDevice {
    void setPosition(double position);
    void setAngle(double angle);
    void setAngle(double angle, AngleUnit angleUnit);
    void turnByAngle(double angle);
    void turnByAngle(double angle, AngleUnit angleUnit);
    WaveServo setLimits(double min, double max);
    WaveServo setInverted(boolean inverted);
    WaveServo reverse();
    double getPosition();
    double getAngle();
    double getAngle(AngleUnit angleUnit);
}