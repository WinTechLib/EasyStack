package dev.easyfamily.wintechlib.controllable;

import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.easyfamily.wintechlib.control.PIDCoefficients;
import dev.easyfamily.wintechlib.feedforward.FFCoefficients;
import dev.easyfamily.wintechlib.hardware.Servo.EasyAbsoluteAnalogEncoder;

public interface EasyCRServo extends EasyHardwareDevice {
    void setPower(double power);
    void stop();
    void setTargetAngle(double angle);
    void update();
    void update(double positionRef, double velocityRef, double accelerationRef);

    EasyCRServo setInverted(boolean inverted);
    EasyCRServo reverse();
    EasyCRServo setPwmRange(PwmControl.PwmRange pwmRange);
    EasyCRServo setCachingTolerance(double tolerance);
    EasyCRServo setPositionalControl(EasyAbsoluteAnalogEncoder encoder,
                                     PIDCoefficients coefficients,
                                     double targetTolerance, AngleUnit toleranceUnit    );
    EasyCRServo setFeedforward(FFCoefficients coefficients);

    double getPower();
    double getAngle();
    boolean isAtTarget();
    boolean isInverted();
}