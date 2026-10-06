package dev.easyfamily.easystack.drivebase.swerve.coaxial;

import dev.easyfamily.easystack.drivebase.swerve.ChassisSpeed;
import dev.easyfamily.easystack.geometry.Vector2d;
import dev.easyfamily.easystack.hardware.Motors.EasyMotor;
import dev.easyfamily.easystack.hardware.Servo.EzCRServo;

public class SwerveModule {

    private final EasyMotor driveMotor;
    private final EzCRServo steeringServo;

    private final double maxSpeed;
    private final double tangentialAngle;
    private final double moduleRadius;

    private Vector2d targetVelocity = new Vector2d();

    private double angleError = 0.0;
    private boolean wheelFlipped = false;

    public SwerveModule(
            EasyMotor driveMotor,
            EzCRServo steeringServo,
            Vector2d offset,
            double maxSpeed
    ) {
        if (driveMotor == null) {
            throw new IllegalArgumentException(
                    "driveMotor não pode ser null"
            );
        }

        if (steeringServo == null) {
            throw new IllegalArgumentException(
                    "steeringServo não pode ser null"
            );
        }

        if (offset == null) {
            throw new IllegalArgumentException(
                    "offset não pode ser null"
            );
        }

        if (maxSpeed <= 0) {
            throw new IllegalArgumentException(
                    "maxSpeed deve ser positivo"
            );
        }

        this.driveMotor = driveMotor;
        this.steeringServo = steeringServo;

        this.maxSpeed = maxSpeed;

        /*
         * Mesma lógica da SolversLib:
         *
         * tangentialAngle = offset.angle()
         * circumference = offset.magnitude() * 2π
         *
         * Como:
         *
         * circumference * omega / 2π
         * = radius * omega
         *
         * guardamos diretamente o raio.
         */
        this.moduleRadius = offset.norm();
        this.tangentialAngle = offset.angle();
    }

    /**
     * Calcula a velocidade do módulo a partir da velocidade
     * robot-centric do chassis.
     *
     * Equivalente ao:
     *
     * calculateVectorRobotCentric()
     *
     * da SolversLib.
     */
    public Vector2d calculateVectorRobotCentric(
            ChassisSpeed target
    ) {
        /*
         * SolversLib:
         *
         * turningVectorMagnitude =
         * circumference * omega / (2π)
         *
         * Isso é exatamente:
         *
         * radius * omega
         */
        double turningVectorMagnitude =
                moduleRadius * target.getOmega();

        Vector2d turningVector =
                Vector2d.fromPolar(
                        turningVectorMagnitude,
                        tangentialAngle
                );

        return turningVector.plus(
                target.getTranslation()
        );
    }

    /**
     * Define a velocidade desejada do módulo.
     */
    public void setTargetVelocity(Vector2d velocity) {
        if (velocity == null) {
            throw new IllegalArgumentException(
                    "velocity não pode ser null"
            );
        }

        targetVelocity = velocity;
    }

    /**
     * Atualiza o módulo para seguir a velocidade desejada.
     *
     * Mantém a mesma lógica de otimização de ângulo
     * utilizada pela SolversLib.
     */
    public void updateModule() {

        /*
         * Ângulo que a roda gostaria de assumir.
         */
        double targetAngle =
                targetVelocity.angle();

        /*
         * Ângulo atual absoluto do módulo.
         */
        double currentAngle =
                steeringServo.getAngle();

        /*
         * Erro angular inicial.
         */
        angleError =
                normalizeAngle(
                        targetAngle - currentAngle
                );

        wheelFlipped = false;

        /*
         * Se for necessário girar mais de 90°,
         * fazemos o wheel flip.
         *
         * Exatamente a mesma ideia da SolversLib:
         *
         * angleError += π * -sign(angleError)
         */
        if (Math.abs(angleError) > Math.PI / 2.0) {

            targetAngle =
                    normalizeAngle(
                            targetAngle + Math.PI
                    );

            angleError =
                    normalizeAngle(
                            targetAngle - currentAngle
                    );

            wheelFlipped = true;
        }

        /*
         * Velocidade do módulo.
         */
        double speed =
                targetVelocity.norm();

        /*
         * Mesma equação da SolversLib:
         *
         * speed / maxSpeed * cos(angleError)
         *
         * O cos reduz a velocidade enquanto a roda
         * ainda está girando para o ângulo desejado.
         */
        double motorPower =
                speed
                        / maxSpeed
                        * Math.cos(angleError);

        /*
         * Quando fizemos wheel flip, a roda deve girar
         * na direção oposta.
         */
        if (wheelFlipped) {
            motorPower *= -1.0;
        }

        /*
         * Segurança.
         */
        motorPower =
                Math.max(
                        -1.0,
                        Math.min(1.0, motorPower)
                );

        driveMotor.setPower(motorPower);

        /*
         * Na SolversLib o PID recebe diretamente o
         * angleError.
         *
         * Na EasyStack, o EzCRServo já possui controle
         * posicional. Portanto fornecemos o ângulo
         * absoluto otimizado.
         */
        steeringServo.setTargetAngle(targetAngle);
        steeringServo.update();
    }

    /**
     * Define a velocidade e atualiza o módulo imediatamente.
     */
    public void updateModuleWithVelocity(
            Vector2d velocity
    ) {
        setTargetVelocity(velocity);
        updateModule();
    }

    /**
     * Para completamente o módulo.
     */
    public void stop() {
        driveMotor.setPower(0.0);
        steeringServo.stop();
    }

    /**
     * Configura o caching do motor e do steering servo.
     */
    public SwerveModule setCachingTolerance(
            double motorCachingTolerance,
            double steeringCachingTolerance
    ) {
        driveMotor.setCachingTolerance(
                motorCachingTolerance
        );

        steeringServo.setCachingTolerance(
                steeringCachingTolerance
        );

        return this;
    }

    /**
     * Retorna a velocidade desejada do módulo.
     */
    public Vector2d getTargetVelocity() {
        return targetVelocity;
    }

    /**
     * Retorna o erro angular atual.
     */
    public double getAngleError() {
        return angleError;
    }

    /**
     * Retorna true se o módulo está utilizando
     * wheel flip.
     */
    public boolean isWheelFlipped() {
        return wheelFlipped;
    }

    /**
     * Retorna o motor de tração.
     */
    public EasyMotor getDriveMotor() {
        return driveMotor;
    }

    /**
     * Retorna o servo responsável pelo steering.
     */
    public EzCRServo getSteeringServo() {
        return steeringServo;
    }

    /**
     * Retorna o ângulo atual do módulo.
     */
    public double getCurrentAngle() {
        return steeringServo.getAngle();
    }

    /**
     * Retorna a velocidade máxima do módulo.
     */
    public double getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * Retorna o ângulo tangencial do módulo.
     */
    public double getTangentialAngle() {
        return tangentialAngle;
    }

    /**
     * Retorna a distância do centro do robô até o módulo.
     */
    public double getModuleRadius() {
        return moduleRadius;
    }

    /**
     * Normaliza um ângulo para [-π, π].
     */
    private double normalizeAngle(double angle) {

        while (angle > Math.PI) {
            angle -= 2.0 * Math.PI;
        }

        while (angle <= -Math.PI) {
            angle += 2.0 * Math.PI;
        }

        return angle;
    }
}