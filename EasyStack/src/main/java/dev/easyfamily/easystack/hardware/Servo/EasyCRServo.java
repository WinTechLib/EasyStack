package dev.easyfamily.easystack.hardware.Servo;

import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

import dev.easyfamily.easystack.control.EasyPIDController;
import dev.easyfamily.easystack.control.PIDCoefficients;
import dev.easyfamily.easystack.controllable.EasyRpmControllable;
import dev.easyfamily.easystack.feedforward.FFController;
import dev.easyfamily.easystack.feedforward.FFCoefficients;
import dev.easyfamily.easystack.util.LowPassFilter;

public class EasyCRServo implements dev.easyfamily.easystack.controllable.EasyCRServo, EasyRpmControllable {
    private final CRServoImplEx servo;
    private final String name;
    private double cachingTolerance = 0.005;
    private double lastPower = Double.NaN;
    private boolean inverted = false;

    private EasyAbsoluteAnalogEncoder encoder;
    private EasyPIDController pid;
    private FFController ff;
    private double target = Double.NaN;
    private double targetTolerance;
    private boolean inPerfil = false;
    private double maxRpm = 0.0;
    private double minimumPower = 0.0;
    private EasyPIDController velPid;
    private double targetRpm = Double.NaN;
    private double toleranceRpm = 10.0;
    private double encoderSignal = 1.0;

    private double measuredRpm = Double.NaN;
    private double lastAngle;
    private long lastTimer;
    private boolean validSample = false;
    private LowPassFilter rpmFilter = new LowPassFilter(0.05);

    public EasyCRServo(HardwareMap hwMap, String nome) {
        this.servo = hwMap.get(CRServoImplEx.class, nome);
        this.name = nome;
    }

    @Override
    public EasyCRServo setInverted(boolean inverted) {
        this.inverted = inverted;
        servo.setDirection(inverted ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
        lastPower = Double.NaN;
        return this;
    }

    @Override
    public EasyCRServo reverse() {
        return setInverted(!inverted);
    }

    @Override
    public EasyCRServo setPwmRange(PwmControl.PwmRange range) {
        servo.setPwmRange(range);
        lastPower = Double.NaN;
        return this;
    }

    @Override
    public EasyCRServo setCachingTolerance(double tolerance) {
        this.cachingTolerance = Math.max(0.0, tolerance);
        return this;
    }

    public EasyCRServo setPositionalControl(EasyAbsoluteAnalogEncoder encoder, PIDCoefficients coefficients, double targetTolerance) {
        this.encoder = encoder;
        this.validSample = false;
        this.pid = new EasyPIDController(coefficients);
        this.targetTolerance = targetTolerance;
        return this;
    }

    @Override
    public EasyCRServo setPositionalControl(EasyAbsoluteAnalogEncoder encoder, PIDCoefficients coefficients, double tolerancia, AngleUnit unidade) {
        return setPositionalControl(encoder, coefficients, encoder.getUnit().fromUnit(unidade, tolerancia));
    }

    @Override
    public EasyCRServo setFeedforward(FFCoefficients coefficients) {
        this.ff = coefficients == null ? null : new FFController(coefficients);
        return this;
    }


    public EasyCRServo setMaxRpm(double rpm) {
        if (rpm <= 0) throw new IllegalArgumentException("maxRpm needs to be > 0. Servo: " + name);
        this.maxRpm = rpm;
        return this;
    }

    public EasyCRServo setMinPower(double minimumPower) {
        this.minimumPower = Math.max(0.0, Math.min(0.95, minimumPower));
        return this;
    }

    public EasyCRServo setVelocityControl(EasyAbsoluteAnalogEncoder encoder, PIDCoefficients coefficients) {
        this.encoder = encoder;
        this.validSample = false;
        this.velPid = new EasyPIDController(coefficients);
        return this;
    }
    public EasyCRServo setVelocityFilter(double timeConstantSeconds) {
        this.rpmFilter = new LowPassFilter(timeConstantSeconds);
        return this;
    }

    public EasyCRServo setEncoderInverted(boolean inverted) {
        this.encoderSignal = inverted ? -1.0 : 1.0;
        return this;
    }

    public EasyCRServo setRpmTolerance(double rpm) {
        this.toleranceRpm = Math.abs(rpm);
        return this;
    }


    public void setTargetRpm(double rpm) {
        requireVelocityModel();
        boolean novoModo = Double.isNaN(targetRpm);
        targetRpm = Math.max(-maxRpm, Math.min(maxRpm, rpm));
        target = Double.NaN;
        if (novoModo && velPid != null) velPid.reset();
        write(rpmToPower(targetRpm));
    }


    public void setTargetAngularVelocity(double velocity, AngleUnit unity) {
        setTargetRpm(unity.toRadians(velocity) * 60.0 / (2.0 * Math.PI));
    }

    public double rpmToPower(double rpm) {
        requireVelocityModel();
        return rpmToPower(rpm);
    }

    public double powerToRpm(double potencia) {
        requireVelocityModel();
        return powerToRpm(potencia);
    }

    public double getMeasuredRpm() {
        VelocitySample();
        return encoder == null ? Double.NaN : measuredRpm;
    }
    public double getEstimatedRpm() {
        if (maxRpm <= 0) return Double.NaN;
        return powerToRpm(getPower());
    }
    public double getRpm() {
        double m = getMeasuredRpm();
        return Double.isNaN(m) ? getEstimatedRpm() : m;
    }

    public double getAngularVelocity(AngleUnit unity) {
        double radPorSeg = getRpm() * 2.0 * Math.PI / 60.0;
        return unity.fromRadians(radPorSeg);
    }

    public double getTargetRpm() { return targetRpm; }

    public double getMaxRpm() { return maxRpm; }
    public boolean isAtTargetRpm() {
        if (Double.isNaN(targetRpm)) return false;
        if (encoder == null) return true;
        double m = getMeasuredRpm();
        return !Double.isNaN(m) && Math.abs(targetRpm - m) <= toleranceRpm;
    }

    @Override
    public void setPower(double potencia) {
        target = Double.NaN;
        targetRpm = Double.NaN;
        write(potencia);
    }

    @Override
    public void stop() {
        setPower(0.0);
    }

    private boolean sameAngle(double a, double b) {
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
    public void setTargetAngle(double angle) {
        requirePositional();

        double novoAlvo = normalize(angle);

        if (Double.isNaN(target) || !sameAngle(target, novoAlvo)) {
            target = novoAlvo;
            pid.reset();
        }
    }

    @Override
    public void update() {
        VelocitySample();
        if (!Double.isNaN(targetRpm)) {
            executeVelocity();
            return;
        }
        execute(false, 0.0, 0.0);
    }


    @Override
    public void update(double positionRef, double velocityRef, double accelerationRef) {
        requirePositional();
        VelocitySample();
        targetRpm = Double.NaN;
        target = normalize(positionRef);
        execute(true, velocityRef, accelerationRef);
    }

    private void execute(boolean perfil, double vRef, double aRef) {
        if (encoder == null || pid == null || Double.isNaN(target)) return;

        if (perfil != inPerfil) {
            pid.reset();
            inPerfil = perfil;
        }

        double e = error();

        if (!perfil && Math.abs(e) <= targetTolerance) {
            pid.reset();
            write(0.0);
            return;
        }

        double ffOut = 0.0;
        if (ff != null) {
            ffOut = ff.calculate(vRef, aRef);
            if (vRef == 0.0) {
                ffOut += ff.getCoefficients().kS * Math.signum(e);
            }
        }

        write(pid.calculate(e, 0.0) + ffOut);
    }

    private void executeVelocity() {
        double base = rpmToPower(targetRpm);

        if (velPid == null || encoder == null) {
            write(base);
            return;
        }
        if (targetRpm == 0.0) {
            velPid.reset();
            write(0.0);
            return;
        }
        if (Double.isNaN(measuredRpm)) {
            write(base);
            return;
        }
        double e = (targetRpm - measuredRpm) / maxRpm;
        write(base + velPid.calculate(e, 0.0));
    }

    @Override
    public boolean isAtTarget() {
        return encoder != null && !Double.isNaN(target) && Math.abs(error()) <= targetTolerance;
    }

    @Override
    public double getPower() {
        return Double.isNaN(lastPower) ? servo.getPower() : lastPower;
    }

    @Override
    public double getAngle() {
        if (encoder == null) throw new IllegalStateException("Sem encoder. Servo: " + name);
        return encoder.getAngle();
    }


    @Override
    public boolean isInverted() {
        return inverted;
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
        return "EzCRServo: " + name;
    }

    public CRServoImplEx getNativeServo() {
        return servo;
    }

    void write(double power) {
        double p = Math.max(-1.0, Math.min(1.0, power));
        boolean paraDeParar = p == 0.0 && lastPower != 0.0;
        if (!Double.isNaN(lastPower) && Math.abs(p - lastPower) <= cachingTolerance && !paraDeParar) {
            return;
        }
        servo.setPower(p);
        lastPower = p;
    }

    private void requirePositional() {
        if (encoder == null || pid == null) {
            throw new IllegalStateException("Configure setPositionalControl() before. Servo: " + name);
        }
    }

    private void requireVelocityModel() {
        if (maxRpm <= 0) {
            throw new IllegalStateException("Configure setMaxRpm() before. Servo: " + name);
        }
    }

    private double RpmToPower(double rpm) {
        if (rpm == 0.0) return 0.0;
        double fracao = Math.min(1.0, Math.abs(rpm) / maxRpm);
        return Math.signum(rpm) * (minimumPower + (1.0 - minimumPower) * fracao);
    }

    private double PowerToRpm(double potencia) {
        double a = Math.abs(potencia);
        if (a <= minimumPower) return 0.0;
        return Math.signum(potencia) * (a - minimumPower) / (1.0 - minimumPower) * maxRpm;
    }
    private void VelocitySample() {
        if (encoder == null) return;

        long agora = System.nanoTime();
        double angle = encoder.getAngle();

        if (!validSample) {
            lastAngle = angle;
            lastTimer = agora;
            validSample = true;
            rpmFilter.reset();
            measuredRpm = Double.NaN;
            return;
        }

        double dt = (agora - lastTimer) / 1e9;
        if (dt < 0.005) return;

        double volta = encoder.getFullRotation();
        double d = (angle - lastAngle) % volta;
        if (d > volta / 2) d -= volta;
        if (d <= -volta / 2) d += volta;

        double bruto = encoderSignal * (d / volta) / dt * 60.0;
        measuredRpm = rpmFilter.calculate(bruto, dt);

        lastAngle = angle;
        lastTimer = agora;
    }

    private double error() {
        double volta = encoder.getFullRotation();
        double e = (target - encoder.getAngle()) % volta;
        if (e > volta / 2) e -= volta;
        if (e <= -volta / 2) e += volta;
        return e;
    }

    private double normalize(double angulo) {
        double volta = encoder.getFullRotation();
        double a = angulo % volta;
        return a < 0 ? a + volta : a;
    }
}