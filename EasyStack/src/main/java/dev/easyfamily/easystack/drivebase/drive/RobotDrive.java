package dev.easyfamily.easystack.drivebase.drive;

public abstract class RobotDrive implements Drivetrain {

    protected double maxOutput = 1.0;

    public double getMaxOutput() {
        return maxOutput;
    }

    public RobotDrive setMaxOutput(double maxOutput) {
        this.maxOutput = clip(maxOutput);
        return this;
    }

    protected double clip(double value) {
        return Math.max(
                -1.0,
                Math.min(1.0, value)
        );
    }

    protected double squareInput(double value) {
        return Math.copySign(
                value * value,
                value
        );
    }

    protected void normalize(double[] values) {
        double max = 0.0;

        for (double value : values) {
            max = Math.max(
                    max,
                    Math.abs(value)
            );
        }

        if (max > 1.0) {
            for (int i = 0; i < values.length; i++) {
                values[i] /= max;
            }
        }
    }

    protected void normalize(
            double[] values,
            double magnitude
    ) {
        for (int i = 0; i < values.length; i++) {
            values[i] *= magnitude;
        }
    }
}