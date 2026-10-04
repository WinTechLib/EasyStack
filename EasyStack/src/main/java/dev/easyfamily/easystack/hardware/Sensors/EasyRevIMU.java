package dev.easyfamily.easystack.hardware.Sensors;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

import dev.easyfamily.easystack.controllable.EasyGyro;

public class EasyRevIMU implements EasyGyro {
    private final IMU imu;
    private final String nome;

    private double offset;
    private int multiplier;

    public EasyRevIMU(HardwareMap hwMap, String name, RevHubOrientationOnRobot.LogoFacingDirection logo, RevHubOrientationOnRobot.UsbFacingDirection usb) {
        this.imu = hwMap.get(IMU.class, name);
        this.nome = name;
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(logo, usb));
        this.imu.initialize(parameters);
        this.multiplier = 1;
        this.offset = 0.0;
    }

    @Override
    public void init() {
        imu.resetYaw();
        offset = 0.0;
    }

    @Override
    public void invertGyro() {
        multiplier *= -1;
    }

    @Override
    public double getHeading() {
        return getAbsoluteHeading() - offset;
    }

    @Override
    public double getAbsoluteHeading() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES) * multiplier;
    }

    @Override
    public double[] getAngles() {
        YawPitchRollAngles angles = imu.getRobotYawPitchRollAngles();
        return new double[]{
                angles.getYaw(AngleUnit.DEGREES),
                angles.getPitch(AngleUnit.DEGREES),
                angles.getRoll(AngleUnit.DEGREES)
        };
    }

    @Override
    public void reset() {
        offset += getHeading();
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {
        imu.close();
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String getDeviceType() {
        return "EzRevIMU: " + nome;
    }

    public IMU getNativeIMU() {
        return imu;
    }
}