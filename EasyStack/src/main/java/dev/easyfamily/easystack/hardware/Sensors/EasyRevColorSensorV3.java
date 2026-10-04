package dev.easyfamily.easystack.hardware.Sensors;

import android.graphics.Color;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.easyfamily.easystack.controllable.EasyColorSensor;
import dev.easyfamily.easystack.controllable.EasyDistanceSensor;

public class EasyRevColorSensorV3 implements EasyColorSensor, EasyDistanceSensor {
    private final RevColorSensorV3 colorSensor;
    private final String nome;
    private DistanceUnit distanceUnit = DistanceUnit.CM;
    private boolean isOperational = true;
    private final List<DistanceTarget> targets;

    public EasyRevColorSensorV3(HardwareMap hwMap, String name) {
        this.colorSensor = hwMap.get(RevColorSensorV3.class, name);
        this.nome = name;
        this.targets = new ArrayList<>();
    }

    public EasyRevColorSensorV3(HardwareMap hwMap, String name, DistanceUnit distanceUnit) {
        this(hwMap, name);
        this.distanceUnit = distanceUnit;
    }

    public EasyRevColorSensorV3(RevColorSensorV3 colorSensor) {
        this.colorSensor = colorSensor;
        this.nome = "CustomRevColorSensorV3";
        this.targets = new ArrayList<>();
    }

    public EasyRevColorSensorV3 setDefaultDistanceUnit(DistanceUnit unit) {
        this.distanceUnit = unit;
        return this;
    }

    @Override
    public int red() {
        return colorSensor.red();
    }

    @Override
    public int green() {
        return colorSensor.green();
    }

    @Override
    public int blue() {
        return colorSensor.blue();
    }

    @Override
    public int alpha() {
        return colorSensor.alpha();
    }

    @Override
    public int argb() {
        return colorSensor.argb();
    }

    @Override
    public int[] getARGBArray() {
        return new int[]{alpha(), red(), green(), blue()};
    }

    @Override
    public float[] getHSVArray() {
        float[] hsv = new float[3];
        Color.RGBToHSV(red(), green(), blue(), hsv);
        return hsv;
    }

    @Override
    public float getHue() {
        return getHSVArray()[0];
    }

    @Override
    public float getSaturation() {
        return getHSVArray()[1];
    }

    @Override
    public float getValue() {
        return getHSVArray()[2];
    }

    @Override
    public EasyColorSensor enableLed(boolean enable) {
        colorSensor.enableLed(enable);
        return this;
    }

    @Override
    public boolean isRed() {
        float hue = getHue();
        return (hue >= 330 || hue <= 30) && getSaturation() > 0.3f;
    }

    @Override
    public boolean isBlue() {
        float hue = getHue();
        return hue >= 200 && hue <= 270 && getSaturation() > 0.3f;
    }

    @Override
    public boolean isYellow() {
        float hue = getHue();
        return hue >= 40 && hue <= 90 && getSaturation() > 0.3f;
    }

    @Override
    public boolean isGreen() {
        float hue = getHue();
        return hue > 90 && hue < 160 && getSaturation() > 0.3f;
    }

    @Override
    public String getDominantColor() {
        if (isRed()) return "RED";
        if (isBlue()) return "BLUE";
        if (isYellow()) return "YELLOW";
        if (isGreen()) return "GREEN";
        return "UNKNOWN";
    }

    @Override
    public String getHexCode() {
        return String.format("#%06X", (0xFFFFFF & argb()));
    }

    @Override
    public double getDistance(DistanceUnit unit) {
        return colorSensor.getDistance(unit);
    }

    public double getDistance() {
        return colorSensor.getDistance(distanceUnit);
    }

    @Override
    public boolean targetReached(DistanceTarget target) {
        double currentDistance = getDistance(target.getUnit());
        return target.atTarget(currentDistance);
    }

    @Override
    public void addTarget(DistanceTarget target) {
        targets.add(target);
    }

    @Override
    public void addTargets(List<DistanceTarget> newTargets) {
        targets.addAll(newTargets);
    }

    @Override
    public Map<DistanceTarget, Boolean> checkAllTargets() {
        Map<DistanceTarget, Boolean> results = new HashMap<>();
        for (DistanceTarget target : targets) {
            results.put(target, targetReached(target));
        }
        return results;
    }

    @Override
    public void enable() {
        colorSensor.enableLed(true);
        isOperational = true;
    }

    @Override
    public void disable() {
        colorSensor.enableLed(false);
        colorSensor.close();
        isOperational = false;
    }

    @Override
    public boolean isEnabled() {
        return isOperational;
    }

    @Override
    public String getDeviceType() {
        return "EasyRevColorSensorV3: " + nome;
    }

    public RevColorSensorV3 getNativeSensor() {
        return colorSensor;
    }
}