package dev.easyfamily.easystack.controllable;

import dev.easyfamily.easystack.controllable.EasyHardwareDevice;

public interface EasyColorSensor extends EasyHardwareDevice {
    int red();
    int green();
    int blue();
    int alpha();
    int argb();
    int[] getARGBArray();
    float[] getHSVArray();
    float getHue();
    float getSaturation();
    float getValue();
    EasyColorSensor enableLed(boolean enable);
    boolean isRed();
    boolean isBlue();
    boolean isYellow();
    boolean isGreen();
    String getDominantColor();
    String getHexCode();
}