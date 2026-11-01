package dev.easyfamily.EasyStack.hardware.Sensors;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import dev.easyfamily.EasyStack.controllable.EasyHardwareDevice;

public class EasyDigitalDevice implements EasyHardwareDevice {
    private final DigitalChannel digitalChannel;
    private final String name;
    private final ElapsedTime timer;

    private double threshold = 0.0;
    private boolean debouncedState = false;
    private boolean lastState = false;
    private boolean isOperational = true;
    private boolean inverted = false;

    public EasyDigitalDevice(HardwareMap hwMap, String name) {
        this.digitalChannel = hwMap.get(DigitalChannel.class, name);
        this.name = name;
        this.digitalChannel.setMode(DigitalChannel.Mode.INPUT);
        this.timer = new ElapsedTime();
    }

    public EasyDigitalDevice(HardwareMap hwMap, String name, double threshold) {
        this(hwMap, name);
        this.threshold = Math.max(0, threshold);
    }

    public EasyDigitalDevice(DigitalChannel digitalChannel) {
        this.digitalChannel = digitalChannel;
        this.name = "CustomDigitalDevice";
        this.digitalChannel.setMode(DigitalChannel.Mode.INPUT);
        this.timer = new ElapsedTime();
    }

    public EasyDigitalDevice setThreshold(double threshold) {
        this.threshold = Math.max(0, threshold);
        return this;
    }

    public EasyDigitalDevice setInverted(boolean inverted) {
        this.inverted = inverted;
        return this;
    }

    public EasyDigitalDevice setMode(DigitalChannel.Mode mode) {
        digitalChannel.setMode(mode);
        return this;
    }

    public DigitalChannel.Mode getMode() {
        return digitalChannel.getMode();
    }

    public double getThreshold() {
        return threshold;
    }

    public void update() {
        boolean rawState = digitalChannel.getState();
        boolean state = inverted != rawState;

        if (threshold == 0) {
            debouncedState = state;
            lastState = state;
            return;
        }

        if (state) {
            if (!lastState) {
                timer.reset();
            }

            if (timer.milliseconds() >= threshold) {
                debouncedState = true;
            }
        } else {
            debouncedState = false;
            timer.reset();
        }

        lastState = state;
    }

    public boolean getState() {
        if (threshold == 0) {
            boolean rawState = digitalChannel.getState();
            return inverted != rawState;
        }
        return debouncedState && (inverted != digitalChannel.getState());
    }

    @Override
    public void enable() {
        isOperational = true;
    }

    @Override
    public void disable() {
        digitalChannel.close();
        isOperational = false;
    }

    @Override
    public boolean isEnabled() {
        return isOperational;
    }

    @Override
    public String getDeviceType() {
        return "EasyDigitalDevice: " + name;
    }

    public DigitalChannel getNativeDevice() {
        return digitalChannel;
    }
}
