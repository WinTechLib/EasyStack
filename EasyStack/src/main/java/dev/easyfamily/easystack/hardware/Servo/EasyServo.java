package dev.easyfamily.easystack.hardware.Servo;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import java.util.HashMap;
import java.util.Map;

public class EasyServo implements dev.easyfamily.easystack.controllable.EasyServo {
    private static final double TOLERANCIA_ALVO = 0.001;

    private final ServoImplEx servo;
    private final String nome;
    private final Map<String, Double> estados = new HashMap<>();

    private double rangeGraus = 180.0;
    private double min = 0.0;
    private double max = 1.0;
    private double cachingTolerance = 0.0001;
    private boolean invertido = false;
    private double ultimoRaw = Double.NaN;

    private double velocidade = 0.0;
    private double alvo = Double.NaN;
    private double inicio = Double.NaN;
    private long inicioNanos = 0L;

    public EasyServo(HardwareMap hwMap, String nome) {
        this.servo = hwMap.get(ServoImplEx.class, nome);
        this.nome = nome;
    }

    public EasyServo(HardwareMap hwMap, String nome, double rangeGraus) {
        this(hwMap, nome);
        setAngleRange(rangeGraus);
    }

    @Override
    public EasyServo setLimits(double min, double max) {
        if (min < 0.0 || max > 1.0 || min >= max) {
            throw new IllegalArgumentException("Limites inválidos: 0 <= min < max <= 1");
        }
        this.min = min;
        this.max = max;
        resetarCache();
        return this;
    }
    @Override
    public EasyServo setAngleRange(double degrees) {
        if (degrees <= 0) throw new IllegalArgumentException("Range deve ser > 0");
        this.rangeGraus = degrees;
        return this;
    }
    @Override
    public EasyServo setCachingTolerance(double tolerance) {
        this.cachingTolerance = Math.max(0.0, tolerance);
        return this;
    }
    @Override
    public EasyServo setSpeed(double positionPerSecond) {
        this.velocidade = positionPerSecond;
        return this;
    }

    @Override
    public EasyServo reverse() {
        return setInverted(!invertido);
    }

    @Override
    public EasyServo setInverted(boolean inverted) {
        this.invertido = inverted;
        servo.setDirection(inverted ? Servo.Direction.REVERSE : Servo.Direction.FORWARD);
        resetarCache();
        return this;
    }

    @Override
    public EasyServo setPwmRange(PwmControl.PwmRange pwmRange) {
        servo.setPwmRange(pwmRange);
        resetarCache();
        return this;
    }

    @Override
    public EasyServo addState(String nome, double posicao) {
        estados.put(nome, clamp(posicao));
        return this;
    }


    @Override
    public void setPosition(double posicao) {
        double pos = clamp(posicao);
        double raw = min + pos * (max - min);

        if (!Double.isNaN(ultimoRaw) && Math.abs(raw - ultimoRaw) <= cachingTolerance) {
            return;
        }

        iniciarMovimento(pos);
        servo.setPosition(raw);
        ultimoRaw = raw;
    }

    @Override
    public void rotateBy(double deltaPosicao) {
        setPosition(getPosition() + deltaPosicao);
    }

    @Override
    public void setAngle(double angulo) {
        setPosition(angulo / rangeGraus);
    }

    @Override
    public void setAngle(double angulo, AngleUnit angleUnit) {
        setAngle(paraGraus(angulo, angleUnit));
    }

    @Override
    public void turnByAngle(double angulo) {
        setAngle(getAngle() + angulo);
    }

    @Override
    public void turnByAngle(double angulo, AngleUnit angleUnit) {
        turnByAngle(paraGraus(angulo, angleUnit));
    }

    @Override
    public void setState(String nome) {
        Double pos = estados.get(nome);
        if (pos == null) {
            throw new IllegalArgumentException("Estado desconhecido '" + nome + "' em " + this.nome);
        }
        setPosition(pos);
    }
    @Override
    public double getPosition() {
        return (getRawPosition() - min) / (max - min);
    }

    @Override
    public double getRawPosition() {
        return !Double.isNaN(ultimoRaw) ? ultimoRaw : servo.getPosition();
    }

    @Override
    public double getAngle() {
        return getPosition() * rangeGraus;
    }

    @Override
    public double getAngle(AngleUnit angleUnit) {
        double graus = getAngle();
        return angleUnit == AngleUnit.RADIANS ? Math.toRadians(graus) : graus;
    }

    @Override
    public double getEstimatedPosition() {
        if (Double.isNaN(alvo)) return Double.NaN;
        if (velocidade <= 0) return alvo;

        double dist = velocidade * (System.nanoTime() - inicioNanos) / 1e9;
        double delta = alvo - inicio;
        return Math.abs(delta) <= dist ? alvo : inicio + Math.signum(delta) * dist;
    }

    @Override
    public boolean isAtTarget() {
        double est = getEstimatedPosition();
        return Double.isNaN(est) || Math.abs(alvo - est) <= TOLERANCIA_ALVO;
    }

    @Override
    public boolean isInverted() {
        return invertido;
    }

    @Override
    public void disable() {
        servo.setPwmDisable();
    }

    @Override
    public void enable() {
        servo.setPwmEnable();
    }

    @Override
    public boolean isEnabled() {
        return servo.isPwmEnabled();
    }

    @Override
    public String getDeviceType() {
        return "EzServo: " + nome;
    }

    public Servo getNativeServo() {
        return servo;
    }



    private void resetarCache() {
        ultimoRaw = Double.NaN;
    }

    private void iniciarMovimento(double novoAlvo) {
        double atual = getEstimatedPosition();
        inicio = Double.isNaN(atual) ? novoAlvo : atual;
        alvo = novoAlvo;
        inicioNanos = System.nanoTime();
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private static double paraGraus(double angulo, AngleUnit unit) {
        return unit == AngleUnit.RADIANS ? Math.toDegrees(angulo) : angulo;
    }
}