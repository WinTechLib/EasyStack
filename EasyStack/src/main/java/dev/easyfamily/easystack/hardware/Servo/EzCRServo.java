package dev.easyfamily.easystack.hardware.Servo;

import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.easyfamily.easystack.control.EasyPIDController;
import dev.easyfamily.easystack.control.PIDCoefficients;
import dev.easyfamily.easystack.controllable.EasyCRServo;
import dev.easyfamily.easystack.controllable.EasyRpmControllable;
import dev.easyfamily.easystack.feedforward.FFController;
import dev.easyfamily.easystack.feedforward.FFCoefficients;
import dev.easyfamily.easystack.util.LowPassFilter;

public class EzCRServo implements EasyCRServo, EasyRpmControllable {
    private final CRServoImplEx servo;
    private final String nome;

    private double cachingTolerance = 0.005;
    private double ultimaPotencia = Double.NaN;
    private boolean invertido = false;

    private EzAbsoluteAnalogEncoder encoder;
    private EasyPIDController pid;
    private FFController ff;
    private double alvo = Double.NaN;
    private double toleranciaAlvo;
    private boolean emPerfil = false;


    private double maxRpm = 0.0;
    private double potenciaMinima = 0.0;
    private EasyPIDController velPid;
    private double rpmAlvo = Double.NaN;
    private double toleranciaRpm = 10.0;
    private double sinalEncoder = 1.0;

    private double rpmMedido = Double.NaN;
    private double ultimoAngulo;
    private long ultimoTempo;
    private boolean amostraValida = false;
    private LowPassFilter filtroRpm = new LowPassFilter(0.05);

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

    public EzCRServo setPositionalControl(EzAbsoluteAnalogEncoder encoder, PIDCoefficients coefficients, double toleranciaAlvo) {
        this.encoder = encoder;
        this.amostraValida = false;
        this.pid = new EasyPIDController(coefficients);
        this.toleranciaAlvo = toleranciaAlvo;
        return this;
    }

    @Override
    public EzCRServo setPositionalControl(EzAbsoluteAnalogEncoder encoder, PIDCoefficients coefficients, double tolerancia, AngleUnit unidade) {
        return setPositionalControl(encoder, coefficients, encoder.getUnit().fromUnit(unidade, tolerancia));
    }

    @Override
    public EzCRServo setFeedforward(FFCoefficients coefficients) {
        this.ff = coefficients == null ? null : new FFController(coefficients);
        return this;
    }


    public EzCRServo setMaxRpm(double rpm) {
        if (rpm <= 0) throw new IllegalArgumentException("maxRpm deve ser > 0. Servo: " + nome);
        this.maxRpm = rpm;
        return this;
    }

    public EzCRServo setMinPower(double potenciaMinima) {
        this.potenciaMinima = Math.max(0.0, Math.min(0.95, potenciaMinima));
        return this;
    }

    public EzCRServo setVelocityControl(EzAbsoluteAnalogEncoder encoder, PIDCoefficients coefficients) {
        this.encoder = encoder;
        this.amostraValida = false;
        this.velPid = new EasyPIDController(coefficients);
        return this;
    }
    public EzCRServo setVelocityFilter(double timeConstantSeconds) {
        this.filtroRpm = new LowPassFilter(timeConstantSeconds);
        return this;
    }

    public EzCRServo setEncoderInverted(boolean inverted) {
        this.sinalEncoder = inverted ? -1.0 : 1.0;
        return this;
    }

    public EzCRServo setRpmTolerance(double rpm) {
        this.toleranciaRpm = Math.abs(rpm);
        return this;
    }


    public void setTargetRpm(double rpm) {
        exigirModeloVelocidade();
        boolean novoModo = Double.isNaN(rpmAlvo);
        rpmAlvo = Math.max(-maxRpm, Math.min(maxRpm, rpm));
        alvo = Double.NaN;
        if (novoModo && velPid != null) velPid.reset();
        escrever(rpmParaPotencia(rpmAlvo));
    }


    public void setTargetAngularVelocity(double velocidade, AngleUnit unidade) {
        setTargetRpm(unidade.toRadians(velocidade) * 60.0 / (2.0 * Math.PI));
    }

    public double rpmToPower(double rpm) {
        exigirModeloVelocidade();
        return rpmParaPotencia(rpm);
    }

    public double powerToRpm(double potencia) {
        exigirModeloVelocidade();
        return potenciaParaRpm(potencia);
    }

    public double getMeasuredRpm() {
        amostrarVelocidade();
        return encoder == null ? Double.NaN : rpmMedido;
    }
    public double getEstimatedRpm() {
        if (maxRpm <= 0) return Double.NaN;
        return potenciaParaRpm(getPower());
    }
    public double getRpm() {
        double m = getMeasuredRpm();
        return Double.isNaN(m) ? getEstimatedRpm() : m;
    }

    public double getAngularVelocity(AngleUnit unidade) {
        double radPorSeg = getRpm() * 2.0 * Math.PI / 60.0;
        return unidade.fromRadians(radPorSeg);
    }

    public double getTargetRpm() { return rpmAlvo; }

    public double getMaxRpm() { return maxRpm; }
    public boolean isAtTargetRpm() {
        if (Double.isNaN(rpmAlvo)) return false;
        if (encoder == null) return true;
        double m = getMeasuredRpm();
        return !Double.isNaN(m) && Math.abs(rpmAlvo - m) <= toleranciaRpm;
    }

    @Override
    public void setPower(double potencia) {
        alvo = Double.NaN;
        rpmAlvo = Double.NaN;
        escrever(potencia);
    }

    @Override
    public void stop() {
        setPower(0.0);
    }

    private boolean mesmoAngulo(double a, double b) {
        double diferenca = (a - b) % encoder.getFullRotation();

        if (diferenca > encoder.getFullRotation() / 2.0) {
            diferenca -= encoder.getFullRotation();
        }

        if (diferenca <= -encoder.getFullRotation() / 2.0) {
            diferenca += encoder.getFullRotation();
        }

        return Math.abs(diferenca) < 1e-9;
    }

    @Override
    public void setTargetAngle(double angulo) {
        exigirPosicional();

        double novoAlvo = normalizar(angulo);

        if (Double.isNaN(alvo) || !mesmoAngulo(alvo, novoAlvo)) {
            alvo = novoAlvo;
            pid.reset();
        }
    }

    @Override
    public void update() {
        amostrarVelocidade();
        if (!Double.isNaN(rpmAlvo)) {
            executarVelocidade();
            return;
        }
        executar(false, 0.0, 0.0);
    }


    @Override
    public void update(double positionRef, double velocityRef, double accelerationRef) {
        exigirPosicional();
        amostrarVelocidade();
        rpmAlvo = Double.NaN;
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

    private void executarVelocidade() {
        double base = rpmParaPotencia(rpmAlvo);

        if (velPid == null || encoder == null) { // malha aberta
            escrever(base);
            return;
        }
        if (rpmAlvo == 0.0) {
            velPid.reset();
            escrever(0.0);
            return;
        }
        if (Double.isNaN(rpmMedido)) { // ainda sem medida válida
            escrever(base);
            return;
        }
        double e = (rpmAlvo - rpmMedido) / maxRpm;
        escrever(base + velPid.calculate(e, 0.0));
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

    private void exigirModeloVelocidade() {
        if (maxRpm <= 0) {
            throw new IllegalStateException("Configure setMaxRpm() antes. Servo: " + nome);
        }
    }

    private double rpmParaPotencia(double rpm) {
        if (rpm == 0.0) return 0.0;
        double fracao = Math.min(1.0, Math.abs(rpm) / maxRpm);
        return Math.signum(rpm) * (potenciaMinima + (1.0 - potenciaMinima) * fracao);
    }

    private double potenciaParaRpm(double potencia) {
        double a = Math.abs(potencia);
        if (a <= potenciaMinima) return 0.0;
        return Math.signum(potencia) * (a - potenciaMinima) / (1.0 - potenciaMinima) * maxRpm;
    }
    private void amostrarVelocidade() {
        if (encoder == null) return;

        long agora = System.nanoTime();
        double angulo = encoder.getAngle();

        if (!amostraValida) {
            ultimoAngulo = angulo;
            ultimoTempo = agora;
            amostraValida = true;
            filtroRpm.reset();
            rpmMedido = Double.NaN;
            return;
        }

        double dt = (agora - ultimoTempo) / 1e9;
        if (dt < 0.005) return;

        double volta = encoder.getFullRotation();
        double d = (angulo - ultimoAngulo) % volta;
        if (d > volta / 2) d -= volta;
        if (d <= -volta / 2) d += volta;

        double bruto = sinalEncoder * (d / volta) / dt * 60.0;
        rpmMedido = filtroRpm.calculate(bruto, dt);

        ultimoAngulo = angulo;
        ultimoTempo = agora;
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