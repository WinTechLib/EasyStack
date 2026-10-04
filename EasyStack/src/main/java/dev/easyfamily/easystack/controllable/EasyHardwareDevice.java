package dev.easyfamily.easystack.controllable;

public interface EasyHardwareDevice {
    void enable();
    void disable();
    boolean isEnabled();
    String getDeviceType();
}