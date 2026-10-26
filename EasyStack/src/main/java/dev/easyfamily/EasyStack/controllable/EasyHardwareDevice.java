package dev.easyfamily.EasyStack.controllable;

public interface EasyHardwareDevice {
    void enable();
    void disable();
    boolean isEnabled();
    String getDeviceType();
}