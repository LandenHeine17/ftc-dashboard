package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@Disabled
public class BasicTeleOp extends LinearOpMode {

    private ElapsedTime runtime = new ElapsedTime();

    // --- Flywheel Constants ---
    private static final double INITIAL_FLYWHEEL_SPEED = 0.5;
    private static final double FLYWHEEL_INCREMENT = 0.05;
    private static final double FLYWHEEL_MIN_SPEED = 0.0;
    private static final double FLYWHEEL_MAX_SPEED = 1.0;

    // --- Servo Positions (from Variables class) ---
    private static final double BACK_LEFT_DOWN = 0.12;
    private static final double BACK_LEFT_UP   = 0.41;

    private static final double BACK_RIGHT_DOWN = 0.96;
    private static final double BACK_RIGHT_UP   = 0.65;

    private static final double FRONT_LEFT_DOWN = 0.33;
    private static final double FRONT_LEFT_UP   = 0.57;

    private static final double FRONT_RIGHT_DOWN = 0.34;
    private static final double FRONT_RIGHT_UP   = 0.10;

    // --- State Variables ---
    private double flywheelSpeed = INITIAL_FLYWHEEL_SPEED;
    private boolean dpadUpPressed = false;
    private boolean dpadDownPressed = false;

    Servo frontLeftLaunch, frontRightLaunch, backLeftLaunch, backRightLaunch;
    DcMotorEx flywheelMotor;

    // Dashboard instance
    private FtcDashboard dashboard;

    @Override
    public void runOpMode() {

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Hardware
        frontLeftLaunch = hardwareMap.get(Servo.class, "frontLeftLaunch");
        frontRightLaunch = hardwareMap.get(Servo.class, "frontRightLaunch");
        backLeftLaunch = hardwareMap.get(Servo.class, "backLeftLaunch");
        backRightLaunch = hardwareMap.get(Servo.class, "backRightLaunch");

        flywheelMotor = hardwareMap.get(DcMotorEx.class, "launchMotor");
        flywheelMotor.setDirection(DcMotorEx.Direction.REVERSE);
        flywheelMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);



        // Dashboard
        dashboard = FtcDashboard.getInstance();

        // Initial servo positions
        frontLeftLaunch.setPosition(FRONT_LEFT_DOWN);
        frontRightLaunch.setPosition(FRONT_RIGHT_DOWN);
        backLeftLaunch.setPosition(BACK_LEFT_DOWN);
        backRightLaunch.setPosition(BACK_RIGHT_DOWN);

        waitForStart();
        runtime.reset();

        if (isStopRequested()) return;

        flywheelMotor.setPower(flywheelSpeed);

        while (opModeIsActive()) {

            handleFlywheelSpeed();
            handleLaunchers();
            updateTelemetry();
            logFlywheelRPM();
        }

        flywheelMotor.setPower(0);
    }

    private void handleFlywheelSpeed() {
        if (gamepad1.dpad_up && !dpadUpPressed) {
            flywheelSpeed += FLYWHEEL_INCREMENT;
        }
        dpadUpPressed = gamepad1.dpad_up;

        if (gamepad1.dpad_down && !dpadDownPressed) {
            flywheelSpeed -= FLYWHEEL_INCREMENT;
        }
        dpadDownPressed = gamepad1.dpad_down;

        flywheelSpeed = Range.clip(flywheelSpeed, FLYWHEEL_MIN_SPEED, FLYWHEEL_MAX_SPEED);
        flywheelMotor.setPower(flywheelSpeed);

        if (gamepad1.a) {
            flywheelSpeed = 0.0;
            flywheelMotor.setPower(flywheelSpeed);
        }
    }

    private void handleLaunchers() {

        // --- X → Back Left UP ---
        if (gamepad1.x) {
            backLeftLaunch.setPosition(BACK_LEFT_UP);
        } else {
            backLeftLaunch.setPosition(BACK_LEFT_DOWN);
        }

        // --- B → Back Right UP ---
        if (gamepad1.b) {
            backRightLaunch.setPosition(BACK_RIGHT_UP);
        } else {
            backRightLaunch.setPosition(BACK_RIGHT_DOWN);
        }

        // --- Y → Both Front UP ---
        if (gamepad1.y) {
            frontLeftLaunch.setPosition(FRONT_LEFT_UP);
            frontRightLaunch.setPosition(FRONT_RIGHT_UP);
        } else {
            frontLeftLaunch.setPosition(FRONT_LEFT_DOWN);
            frontRightLaunch.setPosition(FRONT_RIGHT_DOWN);
        }
    }

    private void updateTelemetry() {
        telemetry.addData("Status", "Run Time: " + runtime.toString());
        telemetry.addData("Flywheel Target Speed", "%.2f (%.0f%%)", flywheelSpeed, flywheelSpeed * 100);
        telemetry.addData("Flywheel Actual Velocity (TPS)", flywheelMotor.getVelocity());
        telemetry.update();
    }

    private void logFlywheelRPM() {
        double tps = flywheelMotor.getVelocity();
        double rpm = (tps / 28.0) * 60.0;

        TelemetryPacket packet = new TelemetryPacket();
        packet.put("flywheelRPM", rpm);
        packet.put("flywheelTPS", tps);
        packet.put("targetPower", flywheelSpeed);

        dashboard.sendTelemetryPacket(packet);
    }
}