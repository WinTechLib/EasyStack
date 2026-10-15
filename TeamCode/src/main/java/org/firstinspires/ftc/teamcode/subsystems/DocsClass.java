package org.firstinspires.ftc.teamcode.subsystems;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import dev.easyfamily.easystack.control.EasyPIDController;
import dev.easyfamily.easystack.control.PIDCoefficients;
import dev.easyfamily.easystack.hardware.Motors.EasyMotor;

public class DocsClass {
    PIDCoefficients coefficients = new PIDCoefficients(
            0.5,
            0.0,
            0.02
    );
    EasyPIDController pidController = new EasyPIDController(coefficients);

    public void setDesired() {
        pidController.setTargetPosition(1000.0);
    }

    public double  getTarget(){
        return pidController.getTargetPosition();
    }


    public void cal(){

        EasyMotor motor = new EasyMotor(hardwareMap, "motor");
        pidController.setTargetPosition(1000.0);

        double output = pidController.calculate(
                motor.getCurrentPosition()
        );



        pidController.setPID(0.005, 0.001, 0.0002);
        pidController.setMaxIntegralSum(200.0);


    }
}
