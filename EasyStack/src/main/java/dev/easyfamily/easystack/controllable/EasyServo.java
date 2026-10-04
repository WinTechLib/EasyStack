package dev.easyfamily.easystack.controllable;

import com.qualcomm.robotcore.hardware.PwmControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public interface EasyServo extends EasyHardwareDevice {

    void setPosition(double position);
    void rotateBy(double deltaPosition);
    void setAngle(double angle);
    void setAngle(double angle, AngleUnit angleUnit);
    void turnByAngle(double angle);
    void turnByAngle(double angle, AngleUnit angleUnit);


    EasyServo setLimits(double min, double max);
    EasyServo setAngleRange(double degrees);
    EasyServo setInverted(boolean inverted);
    EasyServo reverse();
    EasyServo setPwmRange(PwmControl.PwmRange pwmRange);
    EasyServo setCachingTolerance(double tolerance);
    EasyServo setSpeed(double positionPerSecond);

    EasyServo addState(String name, double position);
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