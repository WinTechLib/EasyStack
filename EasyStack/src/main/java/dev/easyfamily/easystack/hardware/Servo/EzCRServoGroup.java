package dev.easyfamily.easystack.hardware.Servo;

import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import java.util.Arrays;
import java.util.Iterator;

import dev.easyfamily.easystack.control.PIDCoefficients;
import dev.easyfamily.easystack.controllable.EasyCRServo;
import dev.easyfamily.easystack.feedforward.FFCoefficients;

public class EzCRServoGroup implements EasyCRServo, Iterable<EzCRServo> {
    private final EzCRServo[] group;

    public EzCRServoGroup(EzCRServo leader, EzCRServo... followers) {
        if (leader == null) throw new IllegalArgumentException("Leader não pode ser null");
        group = new EzCRServo[followers.length + 1];
        group[0] = leader;
        for (int i = 0; i < followers.length; i++) {
            if (followers[i] == null) throw new IllegalArgumentException("Follower null no índice " + i);
            group[i + 1] = followers[i];
        }
    }

    @Override
    public void setPower(double potencia) {
        for (EzCRServo servo : group) servo.setPower(potencia);
    }

    @Override
    public void stop() {
        for (EzCRServo servo : group) servo.stop();
    }

    @Override
    public void setTargetAngle(double angulo) {
        group[0].setTargetAngle(angulo);
    }

    @Override
    public void update() {
        group[0].update();
        copiarPotencia();
    }

    @Override
    public void update(double positionRef, double velocityRef, double accelerationRef) {
        group[0].update(positionRef, velocityRef, accelerationRef);
        copiarPotencia();
    }

    private void copiarPotencia() {
        double p = group[0].getPower();
        for (int i = 1; i < group.length; i++) group[i].escrever(p);
    }

    @Override
    public EzCRServoGroup reverse() {
        for (EzCRServo servo : group) servo.reverse();
        return this;
    }

    @Override
    public EzCRServoGroup setInverted(boolean inverted) {
        for (EzCRServo servo : group) servo.setInverted(inverted);
        return this;
    }

    @Override
    public EzCRServoGroup setPwmRange(PwmControl.PwmRange range) {
        for (EzCRServo servo : group) servo.setPwmRange(range);
        return this;
    }

    @Override
    public EzCRServoGroup setCachingTolerance(double tolerance) {
        for (EzCRServo servo : group) servo.setCachingTolerance(tolerance);
        return this;
    }


    @Override
    public EzCRServoGroup setPositionalControl(EzAbsoluteAnalogEncoder encoder,
                                               PIDCoefficients coefficients,
                                               double tolerancia,
                                               AngleUnit unidade) {
        group[0].setPositionalControl(encoder, coefficients, tolerancia, unidade);
        return this;
    }


    @Override
    public EzCRServoGroup setFeedforward(FFCoefficients coefficients) {
        group[0].setFeedforward(coefficients);
        return this;
    }


    @Override
    public boolean isAtTarget() {
        return group[0].isAtTarget();
    }

    @Override
    public double getAngle() {
        return group[0].getAngle();
    }

    @Override
    public double getPower() {
        return group[0].getPower();
    }

    @Override
    public boolean isInverted() {
        return group[0].isInverted();
    }

    @Override
    public void disable() {
        for (EzCRServo servo : group) servo.disable();
    }

    @Override
    public void enable() {
        for (EzCRServo servo : group) servo.enable();
    }

    @Override
    public boolean isEnabled() {
        for (EzCRServo servo : group) {
            if (!servo.isEnabled()) return false;
        }
        return true;
    }

    @Override
    public String getDeviceType() {
        return "EzCRServoGroup (" + group.length + ")";
    }

    @Override
    public Iterator<EzCRServo> iterator() {
        return Arrays.asList(group).iterator();
    }
}