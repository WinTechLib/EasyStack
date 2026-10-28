package org.firstinspires.ftc.teamcode.ExampleRobot.subsystems;

import com.qualcomm.robotcore.hardware.HardwareMap;

import dev.easyfamily.EasyStack.hardware.Motors.EasyMotor;
import dev.easyfamily.EasyStack.hardware.Motors.EasyMotorGroup;
import dev.easyfamily.EasyStack.subsytem.EasySubsystemBase;

public class Slide extends EasySubsystemBase {

    private EasyMotor elevator1, elevator2;
    private EasyMotorGroup elevator;
    private final int HIGH_BASKET_POSITION = 2000;
    private final int LOW_BASKET_POSITION = 1000;
    private final int LOW_LIMIT_POSITION = 0;
    private final int HIGH_LIMIT_POSITION = 2080;
    @Override
    public void init(HardwareMap hardwareMap) {

        elevator1 = new EasyMotor(hardwareMap, "elevator");
        elevator2 = new EasyMotor(hardwareMap, "elevator1");
        elevator = new EasyMotorGroup(elevator1, elevator2).reverse().brakeMode().setEncoderStrategy(EasyMotorGroup.EncoderStrategy.AVERAGE)
                .setDegreePositionLimits(LOW_LIMIT_POSITION, HIGH_LIMIT_POSITION).setPositionPIDF(0, 0, 0, 0).crossCoupled().stopAndResetEncoder();
    }

    @Override
    public void loop() {
    }
    public void basketLow(){
        elevator.moveToDegrees(LOW_BASKET_POSITION);
    }
    public void basketHigh(){
        elevator.moveToDegrees(HIGH_BASKET_POSITION);
    }
    public void stop(){
        elevator.stop();
    }


}
