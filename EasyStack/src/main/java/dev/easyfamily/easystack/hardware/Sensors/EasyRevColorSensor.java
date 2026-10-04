package dev.easyfamily.easystack.hardware.Sensors;

import android.graphics.Color;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.easyfamily.easystack.controllable.EasyColorSensor;

public class EasyRevColorSensor implements EasyColorSensor {
    private final ColorSensor colorSensor;
    private final String nome;
    private boolean isOperational = true;

    public EasyRevColorSensor(HardwareMap hwMap, String name) {
        this.colorSensor = hwMap.get(ColorSensor.class, name);
        this.nome = name;
    }

    public EasyRevColorSensor(ColorSensor colorSensor) {
        this.colorSensor = colorSensor;
        this.nome = "CustomColorSensor";
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
        return "EasyRevColorSensor: " + nome;
    }

    public ColorSensor getNativeSensor() {
        return colorSensor;
    }
}