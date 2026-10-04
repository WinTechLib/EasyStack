package dev.easyfamily.easystack.controllable;


public interface EasyGyro extends EasyHardwareDevice {
    void init();
    void invertGyro();
    double getHeading();
    double getAbsoluteHeading();
    double[] getAngles();
    void reset();
}