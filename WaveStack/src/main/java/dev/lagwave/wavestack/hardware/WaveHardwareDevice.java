package dev.lagwave.wavestack.hardware;

public interface WaveHardwareDevice {
    void enable();
    void disable();
    boolean isEnabled();
    String getDeviceType();
}