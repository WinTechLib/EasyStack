package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

//ex abstração
abstract class MecanismoRobo {
    // ex de encapsulamento
    protected String nomeMecanismo;

    // ex de construtor PAI
    public MecanismoRobo(String nomeMecanismo) {
        this.nomeMecanismo = nomeMecanismo;

    }

     //ex metodo abstrato
    public abstract void acionar();
}

// ex classe filho
class BracoMecanico extends MecanismoRobo {
    //encapsulamento
    private int alturaAlvo;

    public BracoMecanico(String nome, int alturaAlvo) { // metodo construtor
        super(nome); // referencia a classe pai
        this.alturaAlvo = alturaAlvo;
    }

    // polimorfismo
    @Override
    public void acionar() {
        System.out.println("Movendo o braço " + nomeMecanismo + " para a altura: " + alturaAlvo);
    }
}

@TeleOp(name = "aaa")
public class aula extends LinearOpMode {

    //ex encapsulamento
    private DcMotor motorDireito;
    private DcMotor motorEsquerdo;

    @Override
    public void runOpMode() throws InterruptedException {
        motorDireito = hardwareMap.get(DcMotor.class, "right_drive");
        motorEsquerdo = hardwareMap.get(DcMotor.class, "left_drive");

        //Objeto
        MecanismoRobo meuBraco = new BracoMecanico("Garra Principal", 500); // ex metodo construtor do objeto

        telemetry.addData("Status", "Robô Inicializado com POO!");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            double potencia = -gamepad1.left_stick_y;
            motorEsquerdo.setPower(potencia);
            motorDireito.setPower(potencia);


            if (gamepad1.a) {
                meuBraco.acionar(); //metodo do objeto
            }

            telemetry.addData("Potência Motores", potencia);
            telemetry.update();
        }
    }
}