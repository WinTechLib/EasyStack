package dev.lagwave.wavestack.controllable;

public interface WaveHardwareDevice {
    void enable();
    void disable();
    boolean isEnabled();
    String getDeviceType();
}