package dev.easyfamily.easystack.drivebase.drive;

public class DrivePowers {

    private final double forward;
    private final double strafe;
    private final double turn;

    public DrivePowers(
            double forward,
            double strafe,
            double turn
    ) {
        this.forward = forward;
        this.strafe = strafe;
        this.turn = turn;
    }

    public double getForward() {
        return forward;
    }

    public double getStrafe() {
        return strafe;
    }

    public double getTurn() {
        return turn;
    }

    public static DrivePowers zero() {
        return new DrivePowers(
                0.0,
                0.0,
                0.0
        );
    }
}