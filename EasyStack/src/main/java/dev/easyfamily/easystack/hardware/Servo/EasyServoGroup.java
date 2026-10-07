package dev.easyfamily.easystack.hardware.Servo;

import com.qualcomm.robotcore.hardware.PwmControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


public class EasyServoGroup implements dev.easyfamily.easystack.controllable.EasyServo, Iterable<EasyServo> {
    private final EasyServo[] group;

    public EasyServoGroup(EasyServo leader, EasyServo... followers) {
        if (leader == null) throw new IllegalArgumentException("Leader não pode ser null");
        group = new EasyServo[followers.length + 1];
        group[0] = leader;
        for (int i = 0; i < followers.length; i++) {
            if (followers[i] == null) throw new IllegalArgumentException("Follower null no índice " + i);
            group[i + 1] = followers[i];
        }
    }


    @Override
    public void setPosition(double posicao) {
        for (EasyServo servo : group) servo.setPosition(posicao);
    }

    @Override
    public void rotateBy(double deltaPosicao) {
        for (EasyServo servo : group) servo.rotateBy(deltaPosicao);
    }

    @Override
    public void setAngle(double angulo) {
        for (EasyServo servo : group) servo.setAngle(angulo);
    }

    @Override
    public void setAngle(double angulo, AngleUnit angleUnit) {
        for (EasyServo servo : group) servo.setAngle(angulo, angleUnit);
    }

    @Override
    public void turnByAngle(double angulo) {
        for (EasyServo servo : group) servo.turnByAngle(angulo);
    }

    @Override
    public void turnByAngle(double angulo, AngleUnit angleUnit) {
        for (EasyServo servo : group) servo.turnByAngle(angulo, angleUnit);
    }

    @Override
    public void setState(String nome) {
        for (EasyServo servo : group) servo.setState(nome);
    }

    @Override
    public EasyServoGroup reverse() {
        for (EasyServo servo : group) servo.reverse();
        return this;
    }

    @Override
    public EasyServoGroup setInverted(boolean inverted) {
        for (EasyServo servo : group) servo.setInverted(inverted);
        return this;
    }

    @Override
    public EasyServoGroup setLimits(double min, double max) {
        for (EasyServo servo : group) servo.setLimits(min, max);
        return this;
    }

    @Override
    public EasyServoGroup setAngleRange(double degrees) {
        for (EasyServo servo : group) servo.setAngleRange(degrees);
        return this;
    }

    @Override
    public EasyServoGroup setCachingTolerance(double tolerance) {
        for (EasyServo servo : group) servo.setCachingTolerance(tolerance);
        return this;
    }

    @Override
    public EasyServoGroup setPwmRange(PwmControl.PwmRange pwmRange) {
        for (EasyServo servo : group) servo.setPwmRange(pwmRange);
        return this;
    }

    @Override
    public EasyServoGroup setSpeed(double positionPerSecond) {
        for (EasyServo servo : group) servo.setSpeed(positionPerSecond);
        return this;
    }

    @Override
    public EasyServoGroup addState(String nome, double posicao) {
        for (EasyServo servo : group) servo.addState(nome, posicao);
        return this;
    }


    @Override
    public void disable() {
        for (EasyServo servo : group) servo.disable();
    }

    @Override
    public void enable() {
        for (EasyServo servo : group) servo.enable();
    }

    @Override
    public boolean isEnabled() {
        for (EasyServo servo : group) {
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
        for (EasyServo servo : group) {
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
                .map(EasyServo::getPosition)
                .collect(Collectors.toList());
    }

    @Override
    public Iterator<EasyServo> iterator() {
        return Arrays.asList(group).iterator();
    }
}