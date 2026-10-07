package dev.easyfamily.easystack.hardware.Servo;

import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import java.util.Arrays;
import java.util.Iterator;

import dev.easyfamily.easystack.control.PIDCoefficients;
import dev.easyfamily.easystack.controllable.EasyRpmControllable;
import dev.easyfamily.easystack.feedforward.FFCoefficients;

public class EasyCRServoGroup implements dev.easyfamily.easystack.controllable.EasyCRServo, EasyRpmControllable, Iterable<EasyCRServo> {
    private final EasyCRServo[] group;

    public EasyCRServoGroup(EasyCRServo leader, EasyCRServo... followers) {
        if (leader == null) throw new IllegalArgumentException("Leader não pode ser null");
        group = new EasyCRServo[followers.length + 1];
        group[0] = leader;
        for (int i = 0; i < followers.length; i++) {
            if (followers[i] == null) throw new IllegalArgumentException("Follower null no índice " + i);
            group[i + 1] = followers[i];
        }
    }

    @Override
    public void setPower(double potencia) {
        for (EasyCRServo servo : group) servo.setPower(potencia);
    }

    @Override
    public void stop() {
        for (EasyCRServo servo : group) servo.stop();
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
    public EasyCRServoGroup setMaxRpm(double rpm) {
        group[0].setMaxRpm(rpm);
        return this;
    }

    public EasyCRServoGroup setMinPower(double potenciaMinima) {
        group[0].setMinPower(potenciaMinima);
        return this;
    }
    public EasyCRServoGroup setVelocityControl(EasyAbsoluteAnalogEncoder encoder, PIDCoefficients coefficients) {
        group[0].setVelocityControl(encoder, coefficients);
        return this;
    }

    public EasyCRServoGroup setVelocityFilter(double timeConstantSeconds) {
        group[0].setVelocityFilter(timeConstantSeconds);
        return this;
    }

    public EasyCRServoGroup setEncoderInverted(boolean inverted) {
        group[0].setEncoderInverted(inverted);
        return this;
    }

    public EasyCRServoGroup setRpmTolerance(double rpm) {
        group[0].setRpmTolerance(rpm);
        return this;
    }

    @Override
    public void setTargetRpm(double rpm) {
        group[0].setTargetRpm(rpm);
        copiarPotencia();
    }

    public double rpmToPower(double rpm) {
        return group[0].rpmToPower(rpm);
    }

    public double powerToRpm(double potencia) {
        return group[0].powerToRpm(potencia);
    }

    public double getMeasuredRpm() {
        return group[0].getMeasuredRpm();
    }

    public double getEstimatedRpm() {
        return group[0].getEstimatedRpm();
    }

    @Override
    public double getRpm() {
        return group[0].getRpm();
    }

    @Override
    public double getTargetRpm() {
        return group[0].getTargetRpm();
    }

    @Override
    public double getMaxRpm() {
        return group[0].getMaxRpm();
    }

    @Override
    public boolean isAtTargetRpm() {
        return group[0].isAtTargetRpm();
    }

    @Override
    public EasyCRServoGroup reverse() {
        for (EasyCRServo servo : group) servo.reverse();
        return this;
    }

    @Override
    public EasyCRServoGroup setInverted(boolean inverted) {
        for (EasyCRServo servo : group) servo.setInverted(inverted);
        return this;
    }

    @Override
    public EasyCRServoGroup setPwmRange(PwmControl.PwmRange range) {
        for (EasyCRServo servo : group) servo.setPwmRange(range);
        return this;
    }

    @Override
    public EasyCRServoGroup setCachingTolerance(double tolerance) {
        for (EasyCRServo servo : group) servo.setCachingTolerance(tolerance);
        return this;
    }


    @Override
    public EasyCRServoGroup setPositionalControl(EasyAbsoluteAnalogEncoder encoder,
                                                 PIDCoefficients coefficients,
                                                 double tolerancia,
                                                 AngleUnit unidade) {
        group[0].setPositionalControl(encoder, coefficients, tolerancia, unidade);
        return this;
    }


    @Override
    public EasyCRServoGroup setFeedforward(FFCoefficients coefficients) {
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
        for (EasyCRServo servo : group) servo.disable();
    }

    @Override
    public void enable() {
        for (EasyCRServo servo : group) servo.enable();
    }

    @Override
    public boolean isEnabled() {
        for (EasyCRServo servo : group) {
            if (!servo.isEnabled()) return false;
        }
        return true;
    }

    @Override
    public String getDeviceType() {
        return "EzCRServoGroup (" + group.length + ")";
    }

    @Override
    public Iterator<EasyCRServo> iterator() {
        return Arrays.asList(group).iterator();
    }
}