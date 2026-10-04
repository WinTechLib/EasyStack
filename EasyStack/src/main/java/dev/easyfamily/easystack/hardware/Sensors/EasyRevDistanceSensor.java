package dev.easyfamily.easystack.hardware.Sensors;

import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.easyfamily.easystack.controllable.EasyDistanceSensor;

public class EasyRevDistanceSensor implements EasyDistanceSensor {
    private final DistanceSensor distanceSensor;
    private final String nome;
    private boolean isOperational = true;
    private final List<DistanceTarget> targets;

    public EasyRevDistanceSensor(HardwareMap hwMap, String name) {
        this.distanceSensor = hwMap.get(DistanceSensor.class, name);
        this.nome = name;
        this.targets = new ArrayList<>();
    }

    public EasyRevDistanceSensor(DistanceSensor distanceSensor) {
        this.distanceSensor = distanceSensor;
        this.nome = "CustomDistanceSensor";
        this.targets = new ArrayList<>();
    }

    @Override
    public double getDistance(DistanceUnit unit) {
        return distanceSensor.getDistance(unit);
    }

    @Override
    public boolean targetReached(DistanceTarget target) {
        double currentDistance = getDistance(target.getUnit());
        return target.atTarget(currentDistance);
    }

    @Override
    public void addTarget(DistanceTarget target) {
        targets.add(target);
    }

    @Override
    public void addTargets(List<DistanceTarget> newTargets) {
        targets.addAll(newTargets);
    }

    @Override
    public Map<DistanceTarget, Boolean> checkAllTargets() {
        Map<DistanceTarget, Boolean> results = new HashMap<>();
        for (DistanceTarget target : targets) {
            results.put(target, targetReached(target));
        }
        return results;
    }

    @Override
    public void enable() {
        isOperational = true;
    }

    @Override
    public void disable() {
        distanceSensor.close();
        isOperational = false;
    }

    @Override
    public boolean isEnabled() {
        return isOperational;
    }

    @Override
    public String getDeviceType() {
        return "EasyRevDistanceSensor: " + nome;
    }

    public DistanceSensor getNativeSensor() {
        return distanceSensor;
    }
}