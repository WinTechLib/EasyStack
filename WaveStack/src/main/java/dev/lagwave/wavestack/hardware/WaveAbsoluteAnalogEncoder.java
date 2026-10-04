package dev.lagwave.wavestack.hardware;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;


public class WaveAbsoluteAnalogEncoder {
    private final AnalogInput input;
    private final double rangeVolts;
    private final AngleUnit unit;
    private final double fullRotation;

    private double offset = 0.0;
    private boolean inverted = false;


    private double ultimoAngulo = Double.NaN;
    private long ultimoNanos = 0L;
    private double velocidade = 0.0;

    public WaveAbsoluteAnalogEncoder(HardwareMap hwMap, String nome) {
        this(hwMap, nome, 3.3, AngleUnit.RADIANS);
    }

    public WaveAbsoluteAnalogEncoder(HardwareMap hwMap, String nome, double rangeVolts, AngleUnit unit) {
        this.input = hwMap.get(AnalogInput.class, nome);
        this.rangeVolts = rangeVolts;
        this.unit = unit;
        this.fullRotation = unit == AngleUnit.RADIANS ? 2 * Math.PI : 360.0;
    }

    public WaveAbsoluteAnalogEncoder setOffset(double offset) {
        this.offset = offset;
        return this;
    }

    public WaveAbsoluteAnalogEncoder zero() {
        this.offset = 0.0;
        this.offset = getAngle();
        return this;
    }

    public WaveAbsoluteAnalogEncoder setInverted(boolean inverted) {
        this.inverted = inverted;
        return this;
    }

    public double getAngle() {
        double raw = input.getVoltage() / rangeVolts * fullRotation;
        if (inverted) raw = fullRotation - raw;
        double a = (raw - offset) % fullRotation;
        a = a < 0 ? a + fullRotation : a;

        long agora = System.nanoTime();
        if (!Double.isNaN(ultimoAngulo)) {
            double dt = (agora - ultimoNanos) / 1e9;
            if (dt > 0) {
                double delta = a - ultimoAngulo;
                if (delta > fullRotation / 2) delta -= fullRotation;
                if (delta < -fullRotation / 2) delta += fullRotation;
                velocidade = delta / dt;
            }
        }
        ultimoAngulo = a;
        ultimoNanos = agora;
        return a;
    }

    public double getVelocity() {
        return velocidade;
    }

    public double getVoltage() {
        return input.getVoltage();
    }

    public double getFullRotation() {
        return fullRotation;
    }

    public AngleUnit getUnit() {
        return unit;
    }
}