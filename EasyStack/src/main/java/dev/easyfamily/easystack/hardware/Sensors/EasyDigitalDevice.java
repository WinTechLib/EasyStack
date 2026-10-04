package dev.easyfamily.easystack.hardware.Sensors;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import dev.easyfamily.easystack.controllable.EasyHardwareDevice;

public class EasyDigitalDevice implements EasyHardwareDevice {
    private final DigitalChannel digitalChannel;
    private final String nome;
    private final ElapsedTime timer;

    private double threshold = 0.0;
    private boolean debouncedState = false;
    private boolean lastState = false;
    private boolean isOperational = true;
    private boolean inverted = false;

    public EasyDigitalDevice(HardwareMap hwMap, String name) {
        this.digitalChannel = hwMap.get(DigitalChannel.class, name);
        this.nome = name;
        this.digitalChannel.setMode(DigitalChannel.Mode.INPUT);
        this.timer = new ElapsedTime();
    }

    public EasyDigitalDevice(HardwareMap hwMap, String name, double threshold) {
        this(hwMap, name);
        this.threshold = Math.max(0, threshold);
    }

    public EasyDigitalDevice(DigitalChannel digitalChannel) {
        this.digitalChannel = digitalChannel;
        this.nome = "CustomDigitalDevice";
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
        return "EasyDigitalDevice: " + nome;
    }

    public DigitalChannel getNativeDevice() {
        return digitalChannel;
    }
}