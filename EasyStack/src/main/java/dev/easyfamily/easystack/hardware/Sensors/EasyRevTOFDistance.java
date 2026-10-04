package dev.easyfamily.easystack.hardware.Sensors;

import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.easyfamily.easystack.controllable.EasyDistanceSensor;

public class EasyRevTOFDistance implements EasyDistanceSensor {
    private final DistanceSensor distanceSensor;
    private final String nome;
    private boolean isOperational = true;
    private final List<EasyDistanceSensor.DistanceTarget> targetList;

    public EasyRevTOFDistance(DistanceSensor distanceSensor) {
        this.distanceSensor = distanceSensor;
        this.nome = "CustomTOFDistance";
        this.targetList = new ArrayList<>();
    }

    public EasyRevTOFDistance(HardwareMap hardwareMap, String name) {
        this.distanceSensor = hardwareMap.get(DistanceSensor.class, name);
        this.nome = name;
        this.targetList = new ArrayList<>();
    }

    public EasyRevTOFDistance(DistanceSensor distanceSensor, List<DistanceTarget> targetList) {
        this.distanceSensor = distanceSensor;
        this.nome = "CustomTOFDistance";
        this.targetList = new ArrayList<>(targetList);
    }

    public EasyRevTOFDistance(HardwareMap hardwareMap, String name, List<DistanceTarget> targetList) {
        this.distanceSensor = hardwareMap.get(DistanceSensor.class, name);
        this.nome = name;
        this.targetList = new ArrayList<>(targetList);
    }

    @Override
    public double getDistance(DistanceUnit unit) {
        return distanceSensor.getDistance(unit);
    }

    @Override
    public boolean targetReached(DistanceTarget target) {
        return target.atTarget(getDistance(target.getUnit()));
    }

    @Override
    public void addTarget(DistanceTarget target) {
        if (!targetList.contains(target)) {
            targetList.add(target);
        }
    }

    @Override
    public void addTargets(List<DistanceTarget> targets) {
        for (DistanceTarget target : targets) {
            if (!targetList.contains(target)) {
                targetList.add(target);
            }
        }
    }

    @Override
    public Map<DistanceTarget, Boolean> checkAllTargets() {
        Map<DistanceTarget, Boolean> results = new HashMap<>();
        for (DistanceTarget target : targetList) {
            results.put(target, target.atTarget(getDistance(target.getUnit())));
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
        return "EasyRevTOFDistance: " + nome;
    }

    public DistanceSensor getNativeSensor() {
        return distanceSensor;
    }
}