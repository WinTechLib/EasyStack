package dev.lagwave.wavestack.controllable;

import com.qualcomm.robotcore.hardware.PwmControl;

import dev.lagwave.wavestack.control.PIDCoefficients;
import dev.lagwave.wavestack.feedforward.FFCoefficients;
import dev.lagwave.wavestack.hardware.Servo.EzAbsoluteAnalogEncoder;
import dev.lagwave.wavestack.controllable.WaveHardwareDevice;


public interface WaveCRServo extends WaveHardwareDevice {
    void setPower(double power);
    void stop();
    void setTargetAngle(double angle);
    void update();
    void update(double positionRef, double velocityRef, double accelerationRef);

    WaveCRServo setInverted(boolean inverted);
    WaveCRServo reverse();
    WaveCRServo setPwmRange(PwmControl.PwmRange pwmRange);
    WaveCRServo setCachingTolerance(double tolerance);
    WaveCRServo setPositionalControl(EzAbsoluteAnalogEncoder encoder,
                                     PIDCoefficients coefficients,
                                     double targetTolerance);
    WaveCRServo setFeedforward(FFCoefficients coefficients);

    double getPower();
    double getAngle();
    boolean isAtTarget();
    boolean isInverted();
}