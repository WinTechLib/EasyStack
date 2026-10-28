package dev.easyfamily.EasyStack.controllable;

public interface EasyFeedforward {

    double calculate(double velocity, double acceleration);

    default double calculate(double velocity) {
        return calculate(velocity, 0.0);
    }
}
