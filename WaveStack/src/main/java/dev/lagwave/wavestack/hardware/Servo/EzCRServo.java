package dev.lagwave.wavestack.hardware.Servo;

import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.lagwave.wavestack.control.PIDCoefficients;
import dev.lagwave.wavestack.control.PIDController;
import dev.lagwave.wavestack.controllable.WaveCRServo;
import dev.lagwave.wavestack.feedforward.FFController;
import dev.lagwave.wavestack.feedforward.FFCoefficients;

public class EzCRServo implements WaveCRServo {
    private final CRServoImplEx servo;
    private final String nome;

    private double cachingTolerance = 0.005;
    private double ultimaPotencia = Double.NaN;
    private boolean invertido = false;

    private EzAbsoluteAnalogEncoder encoder;
    private PIDController pid;
    private FFController ff;
    private double alvo = Double.NaN;
    private double toleranciaAlvo;
    private boolean emPerfil = false;

    public EzCRServo(HardwareMap hwMap, String nome) {
        this.servo = hwMap.get(CRServoImplEx.class, nome);
        this.nome = nome;
    }

    @Override
    public EzCRServo setInverted(boolean inverted) {
        this.invertido = inverted;
        servo.setDirection(inverted ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
        ultimaPotencia = Double.NaN;
        return this;
    }

    @Override
    public EzCRServo reverse() {
        return setInverted(!invertido);
    }

    @Override
    public EzCRServo setPwmRange(PwmControl.PwmRange range) {
        servo.setPwmRange(range);
        ultimaPotencia = Double.NaN;
        return this;
    }

    @Override
    public EzCRServo setCachingTolerance(double tolerance) {
        this.cachingTolerance = Math.max(0.0, tolerance);
        return this;
    }


    public EzCRServo setPositionalControl(EzAbsoluteAnalogEncoder encoder,
                                          PIDCoefficients coefficients,
                                          double toleranciaAlvo) {
        this.encoder = encoder;
        this.pid = new PIDController(coefficients);
        this.toleranciaAlvo = toleranciaAlvo;
        return this;
    }

    @Override
    public EzCRServo setPositionalControl(EzAbsoluteAnalogEncoder encoder,
                                          PIDCoefficients coefficients,
                                          double tolerancia,
                                          AngleUnit unidade) {
        return setPositionalControl(encoder, coefficients,
                encoder.getUnit().fromUnit(unidade, tolerancia));
    }

    @Override
    public EzCRServo setFeedforward(FFCoefficients coefficients) {
        this.ff = coefficients == null ? null : new FFController(coefficients);
        return this;
    }

    @Override
    public void setPower(double potencia) {
        alvo = Double.NaN;
        escrever(potencia);
    }

    @Override
    public void stop() {
        setPower(0.0);
    }

    @Override
    public void setTargetAngle(double angulo) {
        exigirPosicional();
        alvo = normalizar(angulo);
        pid.reset();
    }

    @Override
    public void update() {
        executar(false, 0.0, 0.0);
    }


    @Override
    public void update(double positionRef, double velocityRef, double accelerationRef) {
        exigirPosicional();
        alvo = normalizar(positionRef);
        executar(true, velocityRef, accelerationRef);
    }

    private void executar(boolean perfil, double vRef, double aRef) {
        if (encoder == null || pid == null || Double.isNaN(alvo)) return;

        if (perfil != emPerfil) {
            pid.reset();
            emPerfil = perfil;
        }

        double e = erro();

        if (!perfil && Math.abs(e) <= toleranciaAlvo) {
            pid.reset();
            escrever(0.0);
            return;
        }

        double ffOut = 0.0;
        if (ff != null) {
            ffOut = ff.calculate(vRef, aRef);
            if (vRef == 0.0) {
                ffOut += ff.getCoefficients().kS * Math.signum(e);
            }
        }

        escrever(pid.calculate(e, 0.0) + ffOut);
    }

    @Override
    public boolean isAtTarget() {
        return encoder != null && !Double.isNaN(alvo) && Math.abs(erro()) <= toleranciaAlvo;
    }

    @Override
    public double getPower() {
        return Double.isNaN(ultimaPotencia) ? servo.getPower() : ultimaPotencia;
    }

    @Override
    public double getAngle() {
        if (encoder == null) throw new IllegalStateException("Sem encoder. Servo: " + nome);
        return encoder.getAngle();
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
        return "EzCRServo: " + nome;
    }

    public CRServoImplEx getNativeServo() {
        return servo;
    }

    void escrever(double potencia) {
        double p = Math.max(-1.0, Math.min(1.0, potencia));
        boolean paraDeParar = p == 0.0 && ultimaPotencia != 0.0;
        if (!Double.isNaN(ultimaPotencia) && Math.abs(p - ultimaPotencia) <= cachingTolerance && !paraDeParar) {
            return;
        }
        servo.setPower(p);
        ultimaPotencia = p;
    }

    private void exigirPosicional() {
        if (encoder == null || pid == null) {
            throw new IllegalStateException("Configure setPositionalControl() antes. Servo: " + nome);
        }
    }

    private double erro() {
        double volta = encoder.getFullRotation();
        double e = (alvo - encoder.getAngle()) % volta;
        if (e > volta / 2) e -= volta;
        if (e <= -volta / 2) e += volta;
        return e;
    }

    private double normalizar(double angulo) {
        double volta = encoder.getFullRotation();
        double a = angulo % volta;
        return a < 0 ? a + volta : a;
    }
}