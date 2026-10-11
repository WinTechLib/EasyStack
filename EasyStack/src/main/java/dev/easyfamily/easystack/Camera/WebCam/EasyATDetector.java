package dev.easyfamily.easystack.Camera.WebCam;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.concurrent.TimeUnit;

public class EasyATDetector {
    public AprilTagProcessor detectionProcessor;
    public VisionPortal visionPortal;
    public static double x;
    public static double y;
    public static double z;
    public static double yaw;
    public static double pitch;
    public static double roll;
    public static double bearing;
    public static double range;
    public static double elevation;
    public static double id;
    public static boolean detection;
    public static double xProof;
    public static double yProof;
    public static double zProof;
    public static double yawProof;
    public static double pitchProof;
    public static double rollProof;
    public static double bearingProof;
    public static double rangeProof;
    public static double elevationProof;
    public static double idProof;
    public static boolean detectionProof;


    public EasyATDetector(HardwareMap hardwareMap, Position cameraPosition, YawPitchRollAngles cameraOrientation) {
        initCam(hardwareMap, cameraPosition, cameraOrientation);
    }

    public EasyATDetector(HardwareMap hardwareMap, Position cameraPosition){
        initCam(
                hardwareMap,
                cameraPosition,
                new YawPitchRollAngles(
                        AngleUnit.DEGREES,
                        0,
                        0,
                        0,
                        0
                )
        );
    }

    public EasyATDetector(HardwareMap hardwareMap, YawPitchRollAngles cameraOrientation){
        initCam(
                hardwareMap,
                new Position(),
                cameraOrientation
        );
    }

    public void initCam (HardwareMap hardwareMap, Position cameraPosition, YawPitchRollAngles cameraOrientation){
        detectionProcessor = new AprilTagProcessor.Builder()
                .setCameraPose(cameraPosition, cameraOrientation)
                .setDrawTagID(true)
                .setDrawCubeProjection(true)
                .setDrawAxes(true)
                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
                .setTagLibrary(AprilTagGameDatabase.getBioBuzzTagLibrary())
                .setOutputUnits(DistanceUnit.CM, AngleUnit.DEGREES)
                .build();


        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "WebCam"))
                .addProcessors(detectionProcessor)
                .setCameraResolution(new Size(1280,720))
                .setStreamFormat(VisionPortal.StreamFormat.MJPEG)
                .setAutoStartStreamOnBuild(true)
                .build();
    }

    public boolean setManualExposure(int exposureMS, int gain) {
        if (visionPortal == null || visionPortal.getCameraState() != VisionPortal.CameraState.STREAMING) {
            return false;
        }
        ExposureControl exposureControl = visionPortal.getCameraControl(ExposureControl.class);

        if (exposureControl.getMode() != ExposureControl.Mode.Manual) {
            exposureControl.setMode(ExposureControl.Mode.Manual);

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        exposureControl.setExposure((long)exposureMS, TimeUnit.MILLISECONDS);

        GainControl gainControl = visionPortal.getCameraControl(GainControl.class);
        gainControl.setGain(gain);

        return true;
    }

    public void CameraDetection() {
        if (detectionProcessor.getDetections().isEmpty()) {
            detection = false;
        } else {
            for (AprilTagDetection detection : detectionProcessor.getDetections()) {
                //AprilTagSingleDetection idDetection = (AprilTagSingleDetection) detection;
                //id = idDetection.id;

                x = detection.ftcPose.x;
                y = detection.ftcPose.y;
                z = detection.ftcPose.z;

                yaw = detection.ftcPose.yaw;
                pitch = detection.ftcPose.pitch;
                roll = detection.ftcPose.roll;

                range = detection.ftcPose.range;
                bearing = detection.ftcPose.bearing;
                elevation = detection.ftcPose.elevation;
            }
            detection = true;
        }
    }
}
