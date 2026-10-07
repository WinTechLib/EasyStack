package dev.easyfamily.easystack.hardware.Servo;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
public class EasyAbsoluteAnalogEncoder {
    private final AnalogInput input;
    private final double rangeVolts;
    private final AngleUnit unit;
    private final double fullRotation;

    private double offset = 0.0;
    private boolean inverted = false;

    public EasyAbsoluteAnalogEncoder(HardwareMap hwMap, String nome) {
        this(hwMap, nome, 3.3, AngleUnit.RADIANS);
    }

    public EasyAbsoluteAnalogEncoder(HardwareMap hwMap, String nome, double rangeVolts, AngleUnit unit) {
        this.input = hwMap.get(AnalogInput.class, nome);
        this.rangeVolts = rangeVolts;
        this.unit = unit;
        this.fullRotation = unit == AngleUnit.RADIANS ? 2 * Math.PI : 360.0;
    }

    public double getAngle(AngleUnit outraUnidade) {
        return outraUnidade.fromUnit(unit, getAngle());
    }

    public double getAngleDegrees() {
        return getAngle(AngleUnit.DEGREES);
    }

    public double getAngleRadians() {
        return getAngle(AngleUnit.RADIANS);
    }

    public EasyAbsoluteAnalogEncoder setOffset(double offset) {
        this.offset = offset;
        return this;
    }

    public EasyAbsoluteAnalogEncoder zero() {
        this.offset = 0.0;
        this.offset = getAngle();
        return this;
    }

    public EasyAbsoluteAnalogEncoder setInverted(boolean inverted) {
        this.inverted = inverted;
        return this;
    }

    public double getAngle() {
        double raw = input.getVoltage() / rangeVolts * fullRotation;
        if (inverted) raw = fullRotation - raw;
        double a = (raw - offset) % fullRotation;
        return a < 0 ? a + fullRotation : a;
    }

    public double getFullRotation() {
        return fullRotation;
    }

    public AngleUnit getUnit() {
        return unit;
    }
}