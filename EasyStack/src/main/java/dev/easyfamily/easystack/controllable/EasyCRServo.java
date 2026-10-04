package dev.easyfamily.easystack.controllable;

import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.easyfamily.easystack.control.PIDCoefficients;
import dev.easyfamily.easystack.feedforward.FFCoefficients;
import dev.easyfamily.easystack.hardware.Servo.EzAbsoluteAnalogEncoder;

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
    EasyCRServo setPositionalControl(EzAbsoluteAnalogEncoder encoder,
                                     PIDCoefficients coefficients,
                                     double targetTolerance, AngleUnit toleranceUnit    );
    EasyCRServo setFeedforward(FFCoefficients coefficients);

    double getPower();
    double getAngle();
    boolean isAtTarget();
    boolean isInverted();
}