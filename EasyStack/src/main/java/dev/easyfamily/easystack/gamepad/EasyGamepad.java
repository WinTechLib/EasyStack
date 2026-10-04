package dev.easyfamily.easystack.gamepad;

import com.qualcomm.robotcore.hardware.Gamepad;

public class EasyGamepad {

    private final Gamepad gamepad;

    private final ButtonState a = new ButtonState();
    private final ButtonState b = new ButtonState();
    private final ButtonState x = new ButtonState();
    private final ButtonState y = new ButtonState();

    private final ButtonState dpadUp = new ButtonState();
    private final ButtonState dpadDown = new ButtonState();
    private final ButtonState dpadLeft = new ButtonState();
    private final ButtonState dpadRight = new ButtonState();

    private final ButtonState leftBumper = new ButtonState();
    private final ButtonState rightBumper = new ButtonState();

    public EasyGamepad(Gamepad gamepad) {
        this.gamepad = gamepad;
    }

    public void update() {
        long now = System.nanoTime();

        a.update(gamepad.a, now);
        b.update(gamepad.b, now);
        x.update(gamepad.x, now);
        y.update(gamepad.y, now);

        dpadUp.update(gamepad.dpad_up, now);
        dpadDown.update(gamepad.dpad_down, now);
        dpadLeft.update(gamepad.dpad_left, now);
        dpadRight.update(gamepad.dpad_right, now);

        leftBumper.update(gamepad.left_bumper, now);
        rightBumper.update(gamepad.right_bumper, now);
    }

    public boolean a() {
        return a.isDown();
    }

    public boolean aPressed() {
        return a.wasPressed();
    }

    public boolean aReleased() {
        return a.wasReleased();
    }

    public boolean aToggle() {
        return a.isToggled();
    }

    public EasyGamepad setADebounce(long milliseconds) {
        a.setDebounce(milliseconds);
        return this;
    }

    public long getADebounce() {
        return a.getDebounce();
    }
    public boolean b() {
        return b.isDown();
    }

    public boolean bPressed() {
        return b.wasPressed();
    }

    public boolean bReleased() {
        return b.wasReleased();
    }

    public boolean bToggle() {
        return b.isToggled();
    }

    public EasyGamepad setBDebounce(long milliseconds) {
        b.setDebounce(milliseconds);
        return this;
    }

    public long getBDebounce() {
        return b.getDebounce();
    }

    public boolean x() {
        return x.isDown();
    }

    public boolean xPressed() {
        return x.wasPressed();
    }

    public boolean xReleased() {
        return x.wasReleased();
    }

    public boolean xToggle() {
        return x.isToggled();
    }

    public EasyGamepad setXDebounce(long milliseconds) {
        x.setDebounce(milliseconds);
        return this;
    }

    public long getXDebounce() {
        return x.getDebounce();
    }

    public boolean y() {
        return y.isDown();
    }

    public boolean yPressed() {
        return y.wasPressed();
    }

    public boolean yReleased() {
        return y.wasReleased();
    }

    public boolean yToggle() {
        return y.isToggled();
    }

    public EasyGamepad setYDebounce(long milliseconds) {
        y.setDebounce(milliseconds);
        return this;
    }

    public long getYDebounce() {
        return y.getDebounce();
    }


    public boolean dpadUp() {
        return dpadUp.isDown();
    }

    public boolean dpadDown() {
        return dpadDown.isDown();
    }

    public boolean dpadLeft() {
        return dpadLeft.isDown();
    }

    public boolean dpadRight() {
        return dpadRight.isDown();
    }

    public boolean dpadUpPressed() {
        return dpadUp.wasPressed();
    }

    public boolean dpadDownPressed() {
        return dpadDown.wasPressed();
    }

    public boolean dpadLeftPressed() {
        return dpadLeft.wasPressed();
    }

    public boolean dpadRightPressed() {
        return dpadRight.wasPressed();
    }

    public boolean dpadUpReleased() {
        return dpadUp.wasReleased();
    }

    public boolean dpadDownReleased() {
        return dpadDown.wasReleased();
    }

    public boolean dpadLeftReleased() {
        return dpadLeft.wasReleased();
    }

    public boolean dpadRightReleased() {
        return dpadRight.wasReleased();
    }

    public boolean dpadUpToggle() {
        return dpadUp.isToggled();
    }

    public boolean dpadDownToggle() {
        return dpadDown.isToggled();
    }

    public boolean dpadLeftToggle() {
        return dpadLeft.isToggled();
    }

    public boolean dpadRightToggle() {
        return dpadRight.isToggled();
    }

    public EasyGamepad setDpadUpDebounce(long milliseconds) {
        dpadUp.setDebounce(milliseconds);
        return this;
    }

    public EasyGamepad setDpadDownDebounce(long milliseconds) {
        dpadDown.setDebounce(milliseconds);
        return this;
    }

    public EasyGamepad setDpadLeftDebounce(long milliseconds) {
        dpadLeft.setDebounce(milliseconds);
        return this;
    }

    public EasyGamepad setDpadRightDebounce(long milliseconds) {
        dpadRight.setDebounce(milliseconds);
        return this;
    }

    public boolean leftBumper() {
        return leftBumper.isDown();
    }

    public boolean rightBumper() {
        return rightBumper.isDown();
    }

    public boolean leftBumperPressed() {
        return leftBumper.wasPressed();
    }

    public boolean rightBumperPressed() {
        return rightBumper.wasPressed();
    }

    public boolean leftBumperReleased() {
        return leftBumper.wasReleased();
    }

    public boolean rightBumperReleased() {
        return rightBumper.wasReleased();
    }

    public boolean leftBumperToggle() {
        return leftBumper.isToggled();
    }

    public boolean rightBumperToggle() {
        return rightBumper.isToggled();
    }

    public EasyGamepad setLeftBumperDebounce(long milliseconds) {
        leftBumper.setDebounce(milliseconds);
        return this;
    }

    public EasyGamepad setRightBumperDebounce(long milliseconds) {
        rightBumper.setDebounce(milliseconds);
        return this;
    }

    public float leftStickX() {
        return gamepad.left_stick_x;
    }

    public float leftStickY() {
        return gamepad.left_stick_y;
    }

    public float rightStickX() {
        return gamepad.right_stick_x;
    }

    public float rightStickY() {
        return gamepad.right_stick_y;
    }


    public float leftTrigger() {
        return gamepad.left_trigger;
    }

    public float rightTrigger() {
        return gamepad.right_trigger;
    }

    public EasyGamepad resetToggles() {
        a.resetToggle();
        b.resetToggle();
        x.resetToggle();
        y.resetToggle();

        dpadUp.resetToggle();
        dpadDown.resetToggle();
        dpadLeft.resetToggle();
        dpadRight.resetToggle();

        leftBumper.resetToggle();
        rightBumper.resetToggle();

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
        private long lastPressTime = Long.MIN_VALUE;

        private void update(boolean input, long now) {

            previous = current;
            current = input;

            pressed = false;
            released = false;

            if (current && !previous) {

                if (now - lastPressTime >= debounceNanos) {
                    pressed = true;
                    toggled = !toggled;
                    lastPressTime = now;
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

        private void resetToggle() {
            toggled = false;
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