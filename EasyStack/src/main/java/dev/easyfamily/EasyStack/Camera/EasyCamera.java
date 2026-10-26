package dev.easyfamily.EasyStack.Camera;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.FocusControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.WhiteBalanceControl;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.opencv.Circle;
import org.firstinspires.ftc.vision.opencv.ColorBlobLocatorProcessor;
import org.firstinspires.ftc.vision.opencv.ColorRange;
import org.firstinspires.ftc.vision.opencv.ImageRegion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EasyCamera {

    public enum BallColor { ANY, PURPLE, GREEN }
    public static final class Ball {
        private final BallColor color;
        private final double x, y, size, angle;

        Ball(BallColor color, double x, double y, double size, double angle) {
            this.color = color;
            this.x = x;
            this.y = y;
            this.size = size;
            this.angle = angle;
        }

        public BallColor getColor() { return color; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getSize() { return size; }
        public double getAngle() { return angle; }

        @Override
        public String toString() {
            return String.format(Locale.US, "Ball{%s x=%.2f y=%.2f size=%.2f angle=%.1f}",
                    color, x, y, size, angle);
        }
    }

    private static final Comparator<Ball> LARGEST_FIRST = new Comparator<Ball>() {
        @Override
        public int compare(Ball a, Ball b) {
            return Double.compare(b.size, a.size);
        }
    };

    private final WebcamName webcam;
    private final String name;
    private final int width;
    private final int height;

    private VisionPortal portal;
    private ColorBlobLocatorProcessor purple;
    private ColorBlobLocatorProcessor green;
    private volatile List<Ball> balls = Collections.emptyList();
    private boolean running = false;
    private BallColor color = BallColor.ANY;
    private ColorRange purpleRange = ColorRange.ARTIFACT_PURPLE;
    private ColorRange greenRange = ColorRange.ARTIFACT_GREEN;
    private ImageRegion region = ImageRegion.entireFrame();
    private int blurSize = 5;
    private double minSize = 0.04;
    private double minCircularity = 0.6;
    private double horizontalFov = 60.0;
    private double ballDiameterInches = 5.0;
    private boolean liveView = true;
    private double aimKP = 0.8;
    private double aimDeadband = 0.04;
    private double aimTolerance = 0.06;
    private double aimMaxPower = 0.5;
    private double aimDirection = 1.0;
    private boolean controlsDirty = false;
    private double exposureMs = Double.NaN;
    private int gain = -1;
    private int whiteBalance = -1;
    private double focus = Double.NaN;

    public EasyCamera(HardwareMap hardwareMap, String name) {
        this(hardwareMap, name, 640, 480);
    }

    public EasyCamera(HardwareMap hardwareMap, String name, int width, int height) {
        if (hardwareMap == null) {
            throw new IllegalArgumentException("hardwareMap cannot be null");
        }
        this.name = name;
        this.width = width;
        this.height = height;
        this.webcam = hardwareMap.get(WebcamName.class, name);
    }
    public EasyCamera start() {
        if (running) return this;

        purple = buildProcessor(purpleRange);
        green = buildProcessor(greenRange);

        portal = new VisionPortal.Builder()
                .setCamera(webcam)
                .setCameraResolution(new Size(width, height))
                .addProcessors(purple, green)
                .enableLiveView(liveView)
                .build();

        applyColorEnable();
        controlsDirty = true;
        running = true;
        return this;
    }

    public EasyCamera stop() {
        running = false;
        balls = Collections.emptyList();
        if (portal != null) {
            portal.close();
            portal = null;
        }
        return this;
    }
    public EasyCamera pause() {
        if (portal != null) portal.stopStreaming();
        return this;
    }

    public EasyCamera resume() {
        if (portal != null) portal.resumeStreaming();
        return this;
    }
    public EasyCamera hideLiveView() {
        if (portal != null) portal.stopLiveView();
        return this;
    }

    public EasyCamera showLiveView() {
        if (portal != null) portal.resumeLiveView();
        return this;
    }
    public EasyCamera update() {
        if (!running) return this;

        applyControls();

        List<Ball> found = new ArrayList<>();
        if (color != BallColor.GREEN) collect(purple, BallColor.PURPLE, found);
        if (color != BallColor.PURPLE) collect(green, BallColor.GREEN, found);

        Collections.sort(found, LARGEST_FIRST);
        balls = Collections.unmodifiableList(found);
        return this;
    }

    public boolean isRunning() { return running; }

    public boolean isStreaming() {
        return portal != null
                && portal.getCameraState() == VisionPortal.CameraState.STREAMING;
    }

    public double getFps() {
        return portal == null ? 0.0 : portal.getFps();
    }

    public String getName() { return name; }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    /** Salva o próximo frame no controlador do robô (útil para calibrar cores). */
    public EasyCamera saveFrame(String fileName) {
        if (portal != null) portal.saveNextFrameRaw(fileName);
        return this;
    }

    // ===================== leitura das bolas =====================

    public boolean hasBall() {
        return !balls.isEmpty();
    }

    public int getBallCount() {
        return balls.size();
    }

    /** Bola maior (mais perto), ou null. */
    public Ball getBall() {
        List<Ball> current = balls;
        return current.isEmpty() ? null : current.get(0);
    }

    /** Todas as bolas, da maior para a menor. */
    public List<Ball> getBalls() {
        return balls;
    }

    public double getBallX() {
        Ball b = getBall();
        return b == null ? 0.0 : b.x;
    }

    public double getBallY() {
        Ball b = getBall();
        return b == null ? 0.0 : b.y;
    }

    public double getBallSize() {
        Ball b = getBall();
        return b == null ? 0.0 : b.size;
    }

    public double getBallAngle() {
        Ball b = getBall();
        return b == null ? 0.0 : b.angle;
    }

    public BallColor getBallColor() {
        Ball b = getBall();
        return b == null ? BallColor.ANY : b.color;
    }

    /** Distância estimada até a bola maior. NaN se não houver bola. */
    public double getBallDistance(DistanceUnit unit) {
        Ball b = getBall();
        if (b == null || b.size <= 0.0) return Double.NaN;
        double focalPx = (width / 2.0) / Math.tan(Math.toRadians(horizontalFov / 2.0));
        double inches = ballDiameterInches * focalPx / (b.size * width);
        return unit.fromUnit(DistanceUnit.INCH, inches);
    }

    // ===================== alinhamento =====================

    /** Erro de alinhamento (-1..1). NaN se não houver bola. */
    public double getAimError() {
        Ball b = getBall();
        return b == null ? Double.NaN : b.x;
    }

    /** Potência de giro; positiva quando a bola está à direita. */
    public double getAimPower() {
        Ball b = getBall();
        if (b == null || Math.abs(b.x) <= aimDeadband) return 0.0;
        return clamp(aimDirection * aimKP * b.x, -aimMaxPower, aimMaxPower);
    }

    public boolean isAligned() {
        Ball b = getBall();
        return b != null && Math.abs(b.x) <= aimTolerance;
    }

    public EasyCamera setAimGain(double kP) {
        aimKP = Math.abs(kP);
        return this;
    }

    public EasyCamera setAimDeadband(double deadband) {
        aimDeadband = Math.abs(deadband);
        return this;
    }

    public EasyCamera setAimTolerance(double tolerance) {
        aimTolerance = Math.abs(tolerance);
        return this;
    }

    public EasyCamera setAimMaxPower(double maxPower) {
        aimMaxPower = clamp(Math.abs(maxPower), 0.0, 1.0);
        return this;
    }

    /** +1 ou -1. Use -1 se o robô girar para o lado errado. */
    public EasyCamera setAimDirection(double direction) {
        if (direction == 0.0) {
            throw new IllegalArgumentException("direction must be +1 or -1");
        }
        aimDirection = direction > 0.0 ? 1.0 : -1.0;
        return this;
    }

    // ===================== ajustes de detecção =====================

    /** Cor da bola procurada. Pode mudar a qualquer momento. */
    public EasyCamera setColor(BallColor color) {
        this.color = color == null ? BallColor.ANY : color;
        applyColorEnable();
        return this;
    }

    public BallColor getColor() { return color; }

    /** Troca a faixa de cor (antes de start()). */
    public EasyCamera setColorRange(BallColor ballColor, ColorRange range) {
        requireStopped("setColorRange");
        if (ballColor == BallColor.PURPLE) purpleRange = range;
        else if (ballColor == BallColor.GREEN) greenRange = range;
        return this;
    }

    /** Diâmetro mínimo (0..1 da largura) para contar como bola. */
    public EasyCamera setMinSize(double fraction) {
        minSize = Math.abs(fraction);
        return this;
    }

    /** 0..1. Aumente se detectar coisas que não são bolas. */
    public EasyCamera setMinCircularity(double circularity) {
        minCircularity = clamp(circularity, 0.0, 1.0);
        return this;
    }

    /** Região de interesse, de -1 a 1 (esquerda, topo, direita, base). Antes de start(). */
    public EasyCamera setRegion(double left, double top, double right, double bottom) {
        requireStopped("setRegion");
        region = ImageRegion.asUnityCenterCoordinates(left, top, right, bottom);
        return this;
    }

    /** Desfoque antes da detecção (reduz ruído). Antes de start(). */
    public EasyCamera setBlur(int size) {
        requireStopped("setBlur");
        blurSize = Math.max(0, size);
        return this;
    }

    /** Campo de visão horizontal da webcam em graus (afeta ângulo e distância). */
    public EasyCamera setFov(double horizontalDegrees) {
        horizontalFov = Math.abs(horizontalDegrees);
        return this;
    }

    /** Diâmetro real da bola (padrão 5 pol). Afeta getBallDistance. */
    public EasyCamera setBallDiameter(double diameter, DistanceUnit unit) {
        ballDiameterInches = DistanceUnit.INCH.fromUnit(unit, Math.abs(diameter));
        return this;
    }

    /** Liga/desliga o preview do Driver Station. Antes de start(). */
    public EasyCamera setLiveView(boolean enabled) {
        requireStopped("setLiveView");
        liveView = enabled;
        return this;
    }

    // ===================== controles da câmera =====================
    // Podem ser chamados antes ou depois de start(); são aplicados assim que o stream começa.

    /** Exposição manual em milissegundos (valores baixos reduzem borrão e luz forte). */
    public EasyCamera setExposure(double milliseconds) {
        exposureMs = Math.abs(milliseconds);
        controlsDirty = true;
        return this;
    }

    public EasyCamera setAutoExposure() {
        exposureMs = Double.NaN;
        controlsDirty = true;
        return this;
    }

    public EasyCamera setGain(int gain) {
        this.gain = Math.max(0, gain);
        controlsDirty = true;
        return this;
    }

    /** Balanço de branco manual, em Kelvin (ex.: 4600). */
    public EasyCamera setWhiteBalance(int kelvin) {
        whiteBalance = Math.max(0, kelvin);
        controlsDirty = true;
        return this;
    }

    public EasyCamera setAutoWhiteBalance() {
        whiteBalance = -1;
        controlsDirty = true;
        return this;
    }

    /** Foco manual (a faixa depende da webcam). */
    public EasyCamera setFocus(double focusLength) {
        focus = focusLength;
        controlsDirty = true;
        return this;
    }

    public EasyCamera setAutoFocus() {
        focus = Double.NaN;
        controlsDirty = true;
        return this;
    }

    // ===================== extras =====================

    public EasyCamera addTelemetry(Telemetry telemetry) {
        if (telemetry == null) return this;
        telemetry.addData(name + " streaming", isStreaming());
        telemetry.addData(name + " fps", "%.1f", getFps());
        telemetry.addData(name + " color", color);
        telemetry.addData(name + " balls", getBallCount());

        Ball b = getBall();
        if (b != null) {
            telemetry.addData(name + " ball", b.toString());
            telemetry.addData(name + " aligned", isAligned());
            telemetry.addData(name + " distance", "%.1f in", getBallDistance(DistanceUnit.INCH));
        }
        return this;
    }

    /** Acesso avançado ao VisionPortal. Null se não estiver rodando. */
    public VisionPortal getPortal() { return portal; }

    // ===================== interno =====================

    private ColorBlobLocatorProcessor buildProcessor(ColorRange range) {
        return new ColorBlobLocatorProcessor.Builder()
                .setTargetColorRange(range)
                .setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)
                .setRoi(region)
                .setDrawContours(true)
                .setBlurSize(blurSize)
                .build();
    }

    private void applyColorEnable() {
        if (portal == null) return;
        portal.setProcessorEnabled(purple, color != BallColor.GREEN);
        portal.setProcessorEnabled(green, color != BallColor.PURPLE);
    }

    private void collect(ColorBlobLocatorProcessor processor, BallColor ballColor, List<Ball> out) {
        if (processor == null) return;

        for (ColorBlobLocatorProcessor.Blob blob : processor.getBlobs()) {
            if (blob.getCircularity() < minCircularity) continue;

            Circle circle = blob.getCircle();
            double size = (circle.getRadius() * 2.0) / width;
            if (size < minSize) continue;

            double x = (circle.getX() - width / 2.0) / (width / 2.0);
            double y = (height / 2.0 - circle.getY()) / (height / 2.0);

            out.add(new Ball(ballColor, x, y, clamp(size, 0.0, 1.0), x * horizontalFov / 2.0));
        }
    }

    private void applyControls() {
        if (!controlsDirty || !isStreaming()) return;
        controlsDirty = false;

        try {
            ExposureControl exposure = portal.getCameraControl(ExposureControl.class);
            if (exposure != null && exposure.isExposureSupported()) {
                if (Double.isNaN(exposureMs)) {
                    exposure.setMode(ExposureControl.Mode.AperturePriority);
                } else {
                    exposure.setMode(ExposureControl.Mode.Manual);
                    long min = exposure.getMinExposure(TimeUnit.MILLISECONDS);
                    long max = exposure.getMaxExposure(TimeUnit.MILLISECONDS);
                    long ms = (long) clamp(Math.round(exposureMs), min, max);
                    exposure.setExposure(ms, TimeUnit.MILLISECONDS);
                }
            }

            if (gain >= 0) {
                GainControl gainControl = portal.getCameraControl(GainControl.class);
                if (gainControl != null) {
                    gainControl.setGain((int) clamp(gain,
                            gainControl.getMinGain(), gainControl.getMaxGain()));
                }
            }

            WhiteBalanceControl wb = portal.getCameraControl(WhiteBalanceControl.class);
            if (wb != null) {
                if (whiteBalance < 0) {
                    wb.setMode(WhiteBalanceControl.Mode.AUTO);
                } else {
                    wb.setMode(WhiteBalanceControl.Mode.MANUAL);
                    wb.setWhiteBalanceTemperature((int) clamp(whiteBalance,
                            wb.getMinWhiteBalanceTemperature(), wb.getMaxWhiteBalanceTemperature()));
                }
            }

            FocusControl focusControl = portal.getCameraControl(FocusControl.class);
            if (focusControl != null) {
                if (Double.isNaN(focus)) {
                    focusControl.setMode(FocusControl.Mode.ContinuousAuto);
                } else if (focusControl.isFocusLengthSupported()) {
                    focusControl.setMode(FocusControl.Mode.Fixed);
                    focusControl.setFocusLength(clamp(focus,
                            focusControl.getMinFocusLength(), focusControl.getMaxFocusLength()));
                }
            }
        } catch (RuntimeException ignored) {
            // Algumas webcams não suportam todos os controles; a detecção segue normalmente.
        }
    }

    private void requireStopped(String method) {
        if (running) {
            throw new IllegalStateException("Chame " + method + "() antes de start().");
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}