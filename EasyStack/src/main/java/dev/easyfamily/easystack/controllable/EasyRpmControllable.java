package dev.easyfamily.easystack.controllable;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public interface EasyRpmControllable {
    void setTargetRpm(double rpm);
    double getRpm();
    double getTargetRpm();
    double getMaxRpm();

    boolean isAtTargetRpm();

    default void setTargetAngularVelocity(double velocidade, AngleUnit unidade) {
        setTargetRpm(unidade.toRadians(velocidade) * 60.0 / (2.0 * Math.PI));
    }

    default double getAngularVelocity(AngleUnit unidade) {
        return unidade.fromRadians(getRpm() * 2.0 * Math.PI / 60.0);
    }
}