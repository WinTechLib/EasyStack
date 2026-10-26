package dev.easyfamily.EasyStack.Camera;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EasyLimelight {

    private static final double FOV_X = 54.5;
    private static final double FOV_Y = 42.0;

    private static final class Target {
        final String label;
        final double tx, ty, ta;

        Target(String label, double tx, double ty, double ta) {
            this.label = label == null ? "" : label;
            this.tx = tx;
            this.ty = ty;
            this.ta = ta;
        }
    }

    private static final Comparator<Target> LARGEST_FIRST = new Comparator<Target>() {
        @Override
        public int compare(Target a, Target b) {
            return Double.compare(b.ta, a.ta);
        }
    };

    private final Limelight3A limelight;
    private final String name;

    private boolean running = false;
    private LLResult result = null;
    private List<Target> targets = Collections.emptyList();
    private double ageMs = Double.POSITIVE_INFINITY;

    private long maxResultAgeMs = 150;
    private String ballFilter = "";

    private double aimKP = 0.8;
    private double aimDeadband = 0.04;
    private double aimTolerance = 0.06;
    private double aimMaxPower = 0.5;
    private double aimDirection = 1.0;

    private double mountHeightInches = Double.NaN;
    private double mountPitchDownDegrees = 0.0;
    private double ballRadiusInches = 2.5;

    public EasyLimelight(HardwareMap hardwareMap, String name) {
        this(hardwareMap, name, 100);
    }

    public EasyLimelight(HardwareMap hardwareMap, String name, int pollRateHz) {
        if (hardwareMap == null) {
            throw new IllegalArgumentException("hardwareMap cannot be null");
        }
        this.name = name;
        this.limelight = hardwareMap.get(Limelight3A.class, name);
        this.limelight.setPollRateHz((int) clamp(pollRateHz, 1, 250));
    }

    public EasyLimelight start() {
        if (running) return this;
        limelight.start();
        running = true;
        return this;
    }

    public EasyLimelight stop() {
        running = false;
        clear();
        limelight.stop();
        return this;
    }
    public EasyLimelight pause() {
        limelight.pause();
        clear();
        return this;
    }

    public EasyLimelight resume() {
        limelight.start();
        return this;
    }
    public EasyLimelight update() {
        if (!running) return this;

        LLResult latest = limelight.getLatestResult();
        ageMs = Math.max(0L, limelight.getTimeSinceLastUpdate());

        boolean fresh = latest != null
                && limelight.isConnected()
                && latest.isValid()
                && ageMs <= maxResultAgeMs;

        if (!fresh) {
            clear();
            return this;
        }

        result = latest;
        List<Target> found = new ArrayList<>();

        List<LLResultTypes.DetectorResult> detections = latest.getDetectorResults();
        if (detections != null) {
            for (LLResultTypes.DetectorResult d : detections) {
                if (d == null) continue;
                addIfMatches(found, d.getClassName(),
                        d.getTargetXDegrees(), d.getTargetYDegrees(), d.getTargetArea());
            }
        }

        List<LLResultTypes.ColorResult> colors = latest.getColorResults();
        if (colors != null) {
            for (LLResultTypes.ColorResult c : colors) {
                if (c == null) continue;
                addIfMatches(found, "",
                        c.getTargetXDegrees(), c.getTargetYDegrees(), c.getTargetArea());
            }
        }

        Collections.sort(found, LARGEST_FIRST);
        targets = Collections.unmodifiableList(found);
        return this;
    }

    public boolean isRunning() { return running && limelight.isRunning(); }

    public boolean isConnected() { return limelight.isConnected(); }

    public String getName() { return name; }

      public boolean hasBall() {
        return !targets.isEmpty();
    }

    public int getBallCount() {
        return targets.size();
    }
    public double getBallTx() {
        return hasBall() ? targets.get(0).tx : 0.0;
    }
    public double getBallTy() {
        return hasBall() ? targets.get(0).ty : 0.0;
    }
    public double getBallTa() {
        return hasBall() ? targets.get(0).ta : 0.0;
    }
    public double getBallX() {
        return clamp(getBallTx() / (FOV_X / 2.0), -1.0, 1.0);
    }
    public double getBallY() {
        return clamp(getBallTy() / (FOV_Y / 2.0), -1.0, 1.0);
    }
    public double getBallSize() {
        return clamp(Math.sqrt(Math.max(0.0, getBallTa()) / 100.0), 0.0, 1.0);
    }
    public String getBallClass() {
        return hasBall() ? targets.get(0).label : "";
    }
    public double getBallDistance(DistanceUnit unit) {
        if (!hasBall() || Double.isNaN(mountHeightInches)) return Double.NaN;

        double depression = Math.toRadians(mountPitchDownDegrees - getBallTy());
        if (depression <= 0.0) return Double.NaN;

        double inches = (mountHeightInches - ballRadiusInches) / Math.tan(depression);
        return unit.fromUnit(DistanceUnit.INCH, inches);
    }
    public EasyLimelight setMount(double height, double pitchDownDegrees, DistanceUnit unit) {
        mountHeightInches = DistanceUnit.INCH.fromUnit(unit, Math.abs(height));
        mountPitchDownDegrees = pitchDownDegrees;
        return this;
    }
    public EasyLimelight setBallFilter(String classContains) {
        ballFilter = classContains == null ? "" : classContains.toLowerCase();
        return this;
    }
    public double getAimError() {
        return hasBall() ? getBallX() : Double.NaN;
    }
    public double getAimPower() {
        if (!hasBall()) return 0.0;
        double x = getBallX();
        if (Math.abs(x) <= aimDeadband) return 0.0;
        return clamp(aimDirection * aimKP * x, -aimMaxPower, aimMaxPower);
    }

    public boolean isAligned() {
        return hasBall() && Math.abs(getBallX()) <= aimTolerance;
    }

    public EasyLimelight setAimGain(double kP) {
        aimKP = Math.abs(kP);
        return this;
    }

    public EasyLimelight setAimDeadband(double deadband) {
        aimDeadband = Math.abs(deadband);
        return this;
    }

    public EasyLimelight setAimTolerance(double tolerance) {
        aimTolerance = Math.abs(tolerance);
        return this;
    }

    public EasyLimelight setAimMaxPower(double maxPower) {
        aimMaxPower = clamp(Math.abs(maxPower), 0.0, 1.0);
        return this;
    }
    public EasyLimelight setAimDirection(double direction) {
        if (direction == 0.0) {
            throw new IllegalArgumentException("direction must be +1 or -1");
        }
        aimDirection = direction > 0.0 ? 1.0 : -1.0;
        return this;
    }

    public boolean hasTag() {
        return !getTags().isEmpty();
    }

    public boolean hasTag(int id) {
        return getTag(id) != null;
    }

    public List<LLResultTypes.FiducialResult> getTags() {
        if (result == null) return Collections.emptyList();
        List<LLResultTypes.FiducialResult> tags = result.getFiducialResults();
        return tags == null ? Collections.<LLResultTypes.FiducialResult>emptyList() : tags;
    }
    public LLResultTypes.FiducialResult getTag(int id) {
        for (LLResultTypes.FiducialResult tag : getTags()) {
            if (tag != null && tag.getFiducialId() == id) return tag;
        }
        return null;
    }
    public EasyLimelight updateRobotOrientation(double yawDegrees) {
        limelight.updateRobotOrientation(yawDegrees);
        return this;
    }
    public Pose3D getBotpose() {
        return result == null ? null : result.getBotpose();
    }
    public Pose3D getBotposeMT2() {
        return result == null ? null : result.getBotpose_MT2();
    }

    public int getBotposeTagCount() {
        return result == null ? 0 : result.getBotposeTagCount();
    }

    public EasyLimelight setPipeline(int index) {
        limelight.pipelineSwitch(index);
        return this;
    }
    public int getPipeline() {
        LLStatus status = limelight.getStatus();
        return status == null ? -1 : status.getPipelineIndex();
    }

    public EasyLimelight reloadPipeline() {
        limelight.reloadPipeline();
        return this;
    }

    public EasyLimelight setPollRateHz(int rateHz) {
        limelight.setPollRateHz((int) clamp(rateHz, 1, 250));
        return this;
    }
    public EasyLimelight setMaxResultAgeMs(long ms) {
        maxResultAgeMs = Math.max(1L, ms);
        return this;
    }
    public double getAgeMs() {
        return result == null ? Double.POSITIVE_INFINITY : ageMs;
    }
    public double getLatencyMs() {
        if (result == null) return Double.POSITIVE_INFINITY;
        return result.getCaptureLatency() + result.getTargetingLatency() + result.getParseLatency();
    }
    public double getFps() {
        LLStatus status = limelight.getStatus();
        return status == null ? 0.0 : status.getFps();
    }
    public double getTemp() {
        LLStatus status = limelight.getStatus();
        return status == null ? 0.0 : status.getTemp();
    }
    public EasyLimelight captureSnapshot(String snapshotName) {
        limelight.captureSnapshot(snapshotName);
        return this;
    }

    public EasyLimelight deleteSnapshots() {
        limelight.deleteSnapshots();
        return this;
    }
    public EasyLimelight setPythonInputs(double... inputs) {
        limelight.updatePythonInputs(inputs);
        return this;
    }
    public double[] getPythonOutput() {
        if (result == null) return new double[0];
        double[] out = result.getPythonOutput();
        return out == null ? new double[0] : out;
    }

    public EasyLimelight addTelemetry(Telemetry telemetry) {
        if (telemetry == null) return this;
        telemetry.addData(name + " connected", isConnected());
        telemetry.addData(name + " pipeline", getPipeline());
        telemetry.addData(name + " fps", "%.1f", getFps());
        telemetry.addData(name + " balls", getBallCount());

        if (hasBall()) {
            telemetry.addData(name + " ball", "%s tx=%.1f ty=%.1f ta=%.2f",
                    getBallClass(), getBallTx(), getBallTy(), getBallTa());
            telemetry.addData(name + " aligned", isAligned());
            telemetry.addData(name + " latency", "%.1f ms", getLatencyMs());
        }
        if (hasTag()) {
            telemetry.addData(name + " tags", getTags().size());
        }
        return this;
    }
    public Limelight3A getDevice() { return limelight; }

    private void addIfMatches(List<Target> out, String label, double tx, double ty, double ta) {
        String lower = label == null ? "" : label.toLowerCase();
        if (ballFilter.isEmpty() || lower.isEmpty() || lower.contains(ballFilter)) {
            out.add(new Target(label, tx, ty, ta));
        }
    }

    private void clear() {
        result = null;
        targets = Collections.emptyList();
        ageMs = Double.POSITIVE_INFINITY;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}