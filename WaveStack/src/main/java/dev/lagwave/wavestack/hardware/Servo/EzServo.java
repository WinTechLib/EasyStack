package dev.lagwave.wavestack.hardware.Servo;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoControllerEx;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.lagwave.wavestack.hardware.WaveServo;

public class EzServo implements WaveServo {
    private final Servo servo;
    private final String nome;

    private double rangeGraus = 180.0;
    private double min = 0.0;
    private double max = 1.0;
    private double cachingTolerance = 0.0001;
    private double ultimaPosicao = Double.NaN;

    public EzServo(HardwareMap hwMap, String nome) {
        this.servo = hwMap.get(Servo.class, nome);
        this.nome = nome;
    }

    public EzServo(HardwareMap hwMap, String nome, double rangeGraus) {
        this.servo = hwMap.get(Servo.class, nome);
        this.nome = nome;
        this.rangeGraus = rangeGraus;
    }

    @Override
    public EzServo setLimits(double min, double max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public EzServo setCachingTolerance(double tolerance) {
        this.cachingTolerance = tolerance;
        return this;
    }

    @Override
    public void setPosition(double posicao) {
        double posCalculada = (posicao - min) / (max - min);

        if (!Double.isNaN(ultimaPosicao) && Math.abs(posCalculada - ultimaPosicao) <= cachingTolerance) {
            return;
        }

        servo.setPosition(posCalculada);
        ultimaPosicao = posCalculada;
    }

    @Override
    public void setAngle(double angulo) {
        double posicao = angulo / rangeGraus;
        setPosition(posicao);
    }

    @Override
    public void setAngle(double angulo, AngleUnit angleUnit) {
        double anguloEmGraus = angleUnit == AngleUnit.RADIANS ? Math.toDegrees(angulo) : angulo;
        setAngle(anguloEmGraus);
    }

    @Override
    public void turnByAngle(double angulo) {
        setAngle(getAngle() + angulo);
    }

    @Override
    public void turnByAngle(double angulo, AngleUnit angleUnit) {
        double anguloEmGraus = angleUnit == AngleUnit.RADIANS ? Math.toDegrees(angulo) : angulo;
        turnByAngle(anguloEmGraus);
    }

    @Override
    public EzServo reverse() {
        servo.setDirection(servo.getDirection() == Servo.Direction.FORWARD ? Servo.Direction.REVERSE : Servo.Direction.FORWARD);
        return this;
    }

    @Override
    public EzServo setInverted(boolean inverted) {
        servo.setDirection(inverted ? Servo.Direction.REVERSE : Servo.Direction.FORWARD);
        return this;
    }

    public EzServo setPwmRange(PwmControl.PwmRange pwmRange) {
        getController().setServoPwmRange(servo.getPortNumber(), pwmRange);
        return this;
    }

    @Override
    public double getPosition() {
        return !Double.isNaN(ultimaPosicao) ? (ultimaPosicao * (max - min) + min) : servo.getPosition();
    }

    public double getRawPosition() {
        return servo.getPosition();
    }

    @Override
    public double getAngle() {
        return getPosition() * rangeGraus;
    }

    @Override
    public double getAngle(AngleUnit angleUnit) {
        double anguloGraus = getAngle();
        return angleUnit == AngleUnit.RADIANS ? Math.toRadians(anguloGraus) : anguloGraus;
    }

    @Override
    public void disable() {
        getController().pwmDisable();
    }

    public void enable() {
        getController().pwmEnable();
    }

    @Override
    public String getDeviceType() {
        return "EzServo: " + nome + " na porta " + servo.getPortNumber();
    }

    public ServoControllerEx getController() {
        return (ServoControllerEx) servo.getController();
    }

    public Servo getNativeServo() {
        return servo;
    }
}