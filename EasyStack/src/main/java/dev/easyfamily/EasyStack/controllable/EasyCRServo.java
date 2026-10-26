package dev.easyfamily.EasyStack.controllable;

import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.easyfamily.EasyStack.control.EasyPIDCoefficients;
import dev.easyfamily.EasyStack.feedforward.EasyFFCoefficients;
import dev.easyfamily.EasyStack.hardware.Servo.EasyAbsoluteAnalogEncoder;

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
                                     EasyPIDCoefficients coefficients,
                                     double targetTolerance, AngleUnit toleranceUnit    );
    EasyCRServo setFeedforward(EasyFFCoefficients coefficients);

    double getPower();
    double getAngle();
    boolean isAtTarget();
    boolean isInverted();
}