package dev.lagwave.wavestack.hardware.Servo;

import com.qualcomm.robotcore.hardware.PwmControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import dev.lagwave.wavestack.hardware.WaveServo;


public class EzServoGroup implements WaveServo, Iterable<EzServo> {
    private final EzServo[] group;

    public EzServoGroup(EzServo leader, EzServo... followers) {
        if (leader == null) throw new IllegalArgumentException("Leader não pode ser null");
        group = new EzServo[followers.length + 1];
        group[0] = leader;
        for (int i = 0; i < followers.length; i++) {
            if (followers[i] == null) throw new IllegalArgumentException("Follower null no índice " + i);
            group[i + 1] = followers[i];
        }
    }


    @Override
    public void setPosition(double posicao) {
        for (EzServo servo : group) servo.setPosition(posicao);
    }

    @Override
    public void rotateBy(double deltaPosicao) {
        for (EzServo servo : group) servo.rotateBy(deltaPosicao);
    }

    @Override
    public void setAngle(double angulo) {
        for (EzServo servo : group) servo.setAngle(angulo);
    }

    @Override
    public void setAngle(double angulo, AngleUnit angleUnit) {
        for (EzServo servo : group) servo.setAngle(angulo, angleUnit);
    }

    @Override
    public void turnByAngle(double angulo) {
        for (EzServo servo : group) servo.turnByAngle(angulo);
    }

    @Override
    public void turnByAngle(double angulo, AngleUnit angleUnit) {
        for (EzServo servo : group) servo.turnByAngle(angulo, angleUnit);
    }

    @Override
    public void setState(String nome) {
        for (EzServo servo : group) servo.setState(nome);
    }

    @Override
    public EzServoGroup reverse() {
        for (EzServo servo : group) servo.reverse();
        return this;
    }

    @Override
    public EzServoGroup setInverted(boolean inverted) {
        for (EzServo servo : group) servo.setInverted(inverted);
        return this;
    }

    @Override
    public EzServoGroup setLimits(double min, double max) {
        for (EzServo servo : group) servo.setLimits(min, max);
        return this;
    }

    @Override
    public EzServoGroup setAngleRange(double degrees) {
        for (EzServo servo : group) servo.setAngleRange(degrees);
        return this;
    }

    @Override
    public EzServoGroup setCachingTolerance(double tolerance) {
        for (EzServo servo : group) servo.setCachingTolerance(tolerance);
        return this;
    }

    @Override
    public EzServoGroup setPwmRange(PwmControl.PwmRange pwmRange) {
        for (EzServo servo : group) servo.setPwmRange(pwmRange);
        return this;
    }

    @Override
    public EzServoGroup setSpeed(double positionPerSecond) {
        for (EzServo servo : group) servo.setSpeed(positionPerSecond);
        return this;
    }

    @Override
    public EzServoGroup addState(String nome, double posicao) {
        for (EzServo servo : group) servo.addState(nome, posicao);
        return this;
    }


    @Override
    public void disable() {
        for (EzServo servo : group) servo.disable();
    }

    @Override
    public void enable() {
        for (EzServo servo : group) servo.enable();
    }

    @Override
    public boolean isEnabled() {
        for (EzServo servo : group) {
            if (!servo.isEnabled()) return false;
        }
        return true;
    }



    @Override
    public double getPosition() {
        return group[0].getPosition();
    }

    @Override
    public double getRawPosition() {
        return group[0].getRawPosition();
    }

    @Override
    public double getAngle() {
        return group[0].getAngle();
    }

    @Override
    public double getAngle(AngleUnit angleUnit) {
        return group[0].getAngle(angleUnit);
    }

    @Override
    public double getEstimatedPosition() {
        return group[0].getEstimatedPosition();
    }


    @Override
    public boolean isAtTarget() {
        for (EzServo servo : group) {
            if (!servo.isAtTarget()) return false;
        }
        return true;
    }

    @Override
    public boolean isInverted() {
        return group[0].isInverted();
    }

    @Override
    public String getDeviceType() {
        return "EzServoGroup (" + group.length + ")";
    }

    public List<Double> getPositions() {
        return Arrays.stream(group)
                .map(EzServo::getPosition)
                .collect(Collectors.toList());
    }

    @Override
    public Iterator<EzServo> iterator() {
        return Arrays.asList(group).iterator();
    }
}