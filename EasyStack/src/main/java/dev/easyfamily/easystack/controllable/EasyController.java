package dev.easyfamily.easystack.controllable;

public interface EasyController {
    void setTargetPosition(double targetPosition);
    double getTargetPosition();
    double calculate(double currentPosition);
    double calculate(double currentPosition, double targetPosition);
    void reset();
}