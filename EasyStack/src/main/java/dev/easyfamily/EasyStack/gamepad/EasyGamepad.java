package dev.easyfamily.EasyStack.gamepad;

import com.qualcomm.robotcore.hardware.Gamepad;
public class EasyGamepad {
    public enum Button {
        A, B, X, Y,
        DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT,
        LEFT_BUMPER, RIGHT_BUMPER,
        LEFT_STICK_BUTTON, RIGHT_STICK_BUTTON,
        START, BACK,
        LEFT_TRIGGER, RIGHT_TRIGGER
    }

    private static final Button[] BUTTONS = Button.values();

    private final Gamepad gamepad;
    private final ButtonState[] states = new ButtonState[BUTTONS.length];

    private float stickDeadzone = 0.0f;
    private float triggerThreshold = 0.5f;
    private boolean invertY = false;

    public EasyGamepad(Gamepad gamepad) {
        this.gamepad = gamepad;
        for (int i = 0; i < states.length; i++) {
            states[i] = new ButtonState();
        }
    }
    public void update() {
        long now = System.nanoTime();
        for (Button button : BUTTONS) {
            states[button.ordinal()].update(readRaw(button), now);
        }
    }

    private boolean readRaw(Button button) {
        switch (button) {
            case A: return gamepad.a;
            case B: return gamepad.b;
            case X: return gamepad.x;
            case Y: return gamepad.y;
            case DPAD_UP: return gamepad.dpad_up;
            case DPAD_DOWN: return gamepad.dpad_down;
            case DPAD_LEFT: return gamepad.dpad_left;
            case DPAD_RIGHT: return gamepad.dpad_right;
            case LEFT_BUMPER: return gamepad.left_bumper;
            case RIGHT_BUMPER: return gamepad.right_bumper;
            case LEFT_STICK_BUTTON: return gamepad.left_stick_button;
            case RIGHT_STICK_BUTTON: return gamepad.right_stick_button;
            case START: return gamepad.start;
            case BACK: return gamepad.back;
            case LEFT_TRIGGER: return gamepad.left_trigger > triggerThreshold;
            case RIGHT_TRIGGER: return gamepad.right_trigger > triggerThreshold;
            default: throw new IllegalStateException("Unknown button: " + button);
        }
    }
    public boolean down(Button button) {
        return states[button.ordinal()].isDown();
    }

    public boolean pressed(Button button) {
        return states[button.ordinal()].wasPressed();
    }

    public boolean released(Button button) {
        return states[button.ordinal()].wasReleased();
    }

    public boolean toggled(Button button) {
        return states[button.ordinal()].isToggled();
    }

    public EasyGamepad setToggle(Button button, boolean value) {
        states[button.ordinal()].setToggled(value);
        return this;
    }

    public EasyGamepad resetToggle(Button button) {
        states[button.ordinal()].setToggled(false);
        return this;
    }

    public EasyGamepad resetToggles() {
        for (ButtonState state : states) {
            state.setToggled(false);
        }
        return this;
    }

    public EasyGamepad setDebounce(Button button, long milliseconds) {
        states[button.ordinal()].setDebounce(milliseconds);
        return this;
    }

    public long getDebounce(Button button) {
        return states[button.ordinal()].getDebounce();
    }

    public EasyGamepad setAllDebounce(long milliseconds) {
        for (ButtonState state : states) {
            state.setDebounce(milliseconds);
        }
        return this;
    }
    public boolean anyPressed() {
        for (ButtonState state : states) {
            if (state.wasPressed()) {
                return true;
            }
        }
        return false;
    }
    public boolean a() { return down(Button.A); }
    public boolean aPressed() { return pressed(Button.A); }
    public boolean aReleased() { return released(Button.A); }
    public boolean aToggle() { return toggled(Button.A); }
    public EasyGamepad setADebounce(long ms) { return setDebounce(Button.A, ms); }
    public long getADebounce() { return getDebounce(Button.A); }

    public boolean b() { return down(Button.B); }
    public boolean bPressed() { return pressed(Button.B); }
    public boolean bReleased() { return released(Button.B); }
    public boolean bToggle() { return toggled(Button.B); }
    public EasyGamepad setBDebounce(long ms) { return setDebounce(Button.B, ms); }
    public long getBDebounce() { return getDebounce(Button.B); }

    public boolean x() { return down(Button.X); }
    public boolean xPressed() { return pressed(Button.X); }
    public boolean xReleased() { return released(Button.X); }
    public boolean xToggle() { return toggled(Button.X); }
    public EasyGamepad setXDebounce(long ms) { return setDebounce(Button.X, ms); }
    public long getXDebounce() { return getDebounce(Button.X); }

    public boolean y() { return down(Button.Y); }
    public boolean yPressed() { return pressed(Button.Y); }
    public boolean yReleased() { return released(Button.Y); }
    public boolean yToggle() { return toggled(Button.Y); }
    public EasyGamepad setYDebounce(long ms) { return setDebounce(Button.Y, ms); }
    public long getYDebounce() { return getDebounce(Button.Y); }
    public boolean dpadUp() { return down(Button.DPAD_UP); }
    public boolean dpadDown() { return down(Button.DPAD_DOWN); }
    public boolean dpadLeft() { return down(Button.DPAD_LEFT); }
    public boolean dpadRight() { return down(Button.DPAD_RIGHT); }

    public boolean dpadUpPressed() { return pressed(Button.DPAD_UP); }
    public boolean dpadDownPressed() { return pressed(Button.DPAD_DOWN); }
    public boolean dpadLeftPressed() { return pressed(Button.DPAD_LEFT); }
    public boolean dpadRightPressed() { return pressed(Button.DPAD_RIGHT); }

    public boolean dpadUpReleased() { return released(Button.DPAD_UP); }
    public boolean dpadDownReleased() { return released(Button.DPAD_DOWN); }
    public boolean dpadLeftReleased() { return released(Button.DPAD_LEFT); }
    public boolean dpadRightReleased() { return released(Button.DPAD_RIGHT); }

    public boolean dpadUpToggle() { return toggled(Button.DPAD_UP); }
    public boolean dpadDownToggle() { return toggled(Button.DPAD_DOWN); }
    public boolean dpadLeftToggle() { return toggled(Button.DPAD_LEFT); }
    public boolean dpadRightToggle() { return toggled(Button.DPAD_RIGHT); }

    public EasyGamepad setDpadUpDebounce(long ms) { return setDebounce(Button.DPAD_UP, ms); }
    public EasyGamepad setDpadDownDebounce(long ms) { return setDebounce(Button.DPAD_DOWN, ms); }
    public EasyGamepad setDpadLeftDebounce(long ms) { return setDebounce(Button.DPAD_LEFT, ms); }
    public EasyGamepad setDpadRightDebounce(long ms) { return setDebounce(Button.DPAD_RIGHT, ms); }
    public boolean leftBumper() { return down(Button.LEFT_BUMPER); }
    public boolean rightBumper() { return down(Button.RIGHT_BUMPER); }

    public boolean leftBumperPressed() { return pressed(Button.LEFT_BUMPER); }
    public boolean rightBumperPressed() { return pressed(Button.RIGHT_BUMPER); }

    public boolean leftBumperReleased() { return released(Button.LEFT_BUMPER); }
    public boolean rightBumperReleased() { return released(Button.RIGHT_BUMPER); }

    public boolean leftBumperToggle() { return toggled(Button.LEFT_BUMPER); }
    public boolean rightBumperToggle() { return toggled(Button.RIGHT_BUMPER); }

    public EasyGamepad setLeftBumperDebounce(long ms) { return setDebounce(Button.LEFT_BUMPER, ms); }
    public EasyGamepad setRightBumperDebounce(long ms) { return setDebounce(Button.RIGHT_BUMPER, ms); }
    public boolean leftStickButton() { return down(Button.LEFT_STICK_BUTTON); }
    public boolean rightStickButton() { return down(Button.RIGHT_STICK_BUTTON); }

    public boolean leftStickButtonPressed() { return pressed(Button.LEFT_STICK_BUTTON); }
    public boolean rightStickButtonPressed() { return pressed(Button.RIGHT_STICK_BUTTON); }

    public boolean leftStickButtonReleased() { return released(Button.LEFT_STICK_BUTTON); }
    public boolean rightStickButtonReleased() { return released(Button.RIGHT_STICK_BUTTON); }

    public boolean leftStickButtonToggle() { return toggled(Button.LEFT_STICK_BUTTON); }
    public boolean rightStickButtonToggle() { return toggled(Button.RIGHT_STICK_BUTTON); }

    public EasyGamepad setLeftStickButtonDebounce(long ms) { return setDebounce(Button.LEFT_STICK_BUTTON, ms); }
    public EasyGamepad setRightStickButtonDebounce(long ms) { return setDebounce(Button.RIGHT_STICK_BUTTON, ms); }
    public boolean start() { return down(Button.START); }
    public boolean startPressed() { return pressed(Button.START); }
    public boolean startReleased() { return released(Button.START); }
    public boolean startToggle() { return toggled(Button.START); }
    public EasyGamepad setStartDebounce(long ms) { return setDebounce(Button.START, ms); }

    public boolean back() { return down(Button.BACK); }
    public boolean backPressed() { return pressed(Button.BACK); }
    public boolean backReleased() { return released(Button.BACK); }
    public boolean backToggle() { return toggled(Button.BACK); }
    public EasyGamepad setBackDebounce(long ms) { return setDebounce(Button.BACK, ms); }
    public float leftTrigger() { return gamepad.left_trigger; }
    public float rightTrigger() { return gamepad.right_trigger; }
    public boolean leftTriggerDown() { return down(Button.LEFT_TRIGGER); }
    public boolean rightTriggerDown() { return down(Button.RIGHT_TRIGGER); }

    public boolean leftTriggerPressed() { return pressed(Button.LEFT_TRIGGER); }
    public boolean rightTriggerPressed() { return pressed(Button.RIGHT_TRIGGER); }

    public boolean leftTriggerReleased() { return released(Button.LEFT_TRIGGER); }
    public boolean rightTriggerReleased() { return released(Button.RIGHT_TRIGGER); }

    public boolean leftTriggerToggle() { return toggled(Button.LEFT_TRIGGER); }
    public boolean rightTriggerToggle() { return toggled(Button.RIGHT_TRIGGER); }

    public EasyGamepad setLeftTriggerDebounce(long ms) { return setDebounce(Button.LEFT_TRIGGER, ms); }
    public EasyGamepad setRightTriggerDebounce(long ms) { return setDebounce(Button.RIGHT_TRIGGER, ms); }
    public EasyGamepad setTriggerThreshold(float threshold) {
        if (threshold < 0.0f || threshold > 1.0f) {
            throw new IllegalArgumentException("Trigger threshold must be between 0.0 and 1.0.");
        }
        this.triggerThreshold = threshold;
        return this;
    }

    public float getTriggerThreshold() {
        return triggerThreshold;
    }
    public EasyGamepad setStickDeadzone(float deadzone) {
        if (deadzone < 0.0f || deadzone >= 1.0f) {
            throw new IllegalArgumentException("Stick deadzone must be at least 0.0 and less than 1.0.");
        }
        this.stickDeadzone = deadzone;
        return this;
    }

    public float getStickDeadzone() {
        return stickDeadzone;
    }
    public EasyGamepad setInvertY(boolean invert) {
        this.invertY = invert;
        return this;
    }

    public boolean isInvertY() {
        return invertY;
    }
    public float leftStickX() { return applyDeadzone(leftStickXRaw()); }
    public float leftStickY() { return applyDeadzone(leftStickYRaw()); }
    public float rightStickX() { return applyDeadzone(rightStickXRaw()); }
    public float rightStickY() { return applyDeadzone(rightStickYRaw()); }
    public float leftStickXRaw() { return gamepad.left_stick_x; }
    public float leftStickYRaw() { return invertY ? -gamepad.left_stick_y : gamepad.left_stick_y; }
    public float rightStickXRaw() { return gamepad.right_stick_x; }
    public float rightStickYRaw() { return invertY ? -gamepad.right_stick_y : gamepad.right_stick_y; }

    public float leftStickMagnitude() {
        return (float) Math.min(1.0, Math.hypot(leftStickX(), leftStickY()));
    }

    public float rightStickMagnitude() {
        return (float) Math.min(1.0, Math.hypot(rightStickX(), rightStickY()));
    }

    private float applyDeadzone(float value) {
        float abs = Math.abs(value);
        if (abs <= stickDeadzone) {
            return 0.0f;
        }
        float scaled = (abs - stickDeadzone) / (1.0f - stickDeadzone);
        if (scaled > 1.0f) {
            scaled = 1.0f;
        }
        return value < 0.0f ? -scaled : scaled;
    }


    public EasyGamepad rumble(int durationMs) {
        gamepad.rumble(durationMs);
        return this;
    }

    public EasyGamepad rumble(double leftStrength, double rightStrength, int durationMs) {
        gamepad.rumble(leftStrength, rightStrength, durationMs);
        return this;
    }

    public EasyGamepad stopRumble() {
        gamepad.stopRumble();
        return this;
    }
    public Gamepad getGamepad() {
        return gamepad;
    }
    private static class ButtonState {

        private boolean current;
        private boolean previous;

        private boolean pressed;
        private boolean released;

        private boolean toggled;

        private long debounceNanos = 50_000_000L;
        private long lastPressTime;
        private boolean hasPressedBefore = false;

        private void update(boolean input, long now) {
            previous = current;
            current = input;

            pressed = false;
            released = false;

            if (current && !previous) {
                boolean debouncePassed =
                        !hasPressedBefore ||
                                now - lastPressTime >= debounceNanos;

                if (debouncePassed) {
                    pressed = true;
                    toggled = !toggled;

                    lastPressTime = now;
                    hasPressedBefore = true;
                }
            }

            if (!current && previous) {
                released = true;
            }
        }

        private boolean isDown() {
            return current;
        }

        private boolean wasPressed() {
            return pressed;
        }

        private boolean wasReleased() {
            return released;
        }

        private boolean isToggled() {
            return toggled;
        }

        private void setToggled(boolean value) {
            toggled = value;
        }

        private void setDebounce(long milliseconds) {
            if (milliseconds < 0) {
                throw new IllegalArgumentException(
                        "Debounce time cannot be negative."
                );
            }
            debounceNanos = milliseconds * 1_000_000L;
        }

        private long getDebounce() {
            return debounceNanos / 1_000_000L;
        }
    }
}