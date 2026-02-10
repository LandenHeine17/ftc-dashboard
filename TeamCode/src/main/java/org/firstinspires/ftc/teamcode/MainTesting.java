package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@Config
@TeleOp
public class MainTesting extends OpMode {
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

    private static final double FRONT_LEFT_DOWN = 0.39; // 0.33
    private static final double FRONT_LEFT_UP   = 0.57;

    private static final double FRONT_RIGHT_DOWN = 0.28; // 0.34
    private static final double FRONT_RIGHT_UP   = 0.10;

    // --- State Variables ---
    private int launchingSequenceActive = 0;
    private int launchingSequenceCounter = 0;
    private double flywheelSpeed = INITIAL_FLYWHEEL_SPEED;
    private boolean waitingForRPMDrop = false;
    private double lastRPM = 0;
    private double lastSlope = 0;
    private double slope = 0;
    double desiredRPM, tps, rpm, motorOffset;

    private double lastP, lastI, lastD, lastF;
    public static double kP = 170;
    public static double kI = 0.0;
    public static double kD = 0.0001;
    public static double kF = 14.1;
    public double stepAmount = 0.001;


    // Dashboard instance
    private FtcDashboard dashboard;

    RobotHardware robot;
    @Override
    public void init() {
        robot = new RobotHardware(hardwareMap);

        // Dashboard
        dashboard = FtcDashboard.getInstance();

        // Initial servo positions
        robot.frontLeftLaunch.setPosition(FRONT_LEFT_DOWN);
        robot.frontRightLaunch.setPosition(FRONT_RIGHT_DOWN);
        robot.backLeftLaunch.setPosition(BACK_LEFT_DOWN);
        robot.backRightLaunch.setPosition(BACK_RIGHT_DOWN);
    }

    @Override
    public void loop() {
        handleFlywheelSpeed();
        handleLaunchers();
        updateTelemetry();
        logFlywheelRPM();
        updatePIDF();
    }
    private void updatePIDF() {

        if (gamepad1.dpadUpWasPressed()) {
            stepAmount *= 10;
        } else if (gamepad1.dpadDownWasPressed()) {
            stepAmount *= 0.1;
        }


        if (kP != lastP || kI != lastI || kD != lastD || kF != lastF) {
            robot.flywheelMotor.setVelocityPIDFCoefficients(kP, kI, kD, kF);

            lastP = kP;
            lastI = kI;
            lastD = kD;
            lastF = kF;
        }
    }
    private void handleLaunchers() {
        if (gamepad1.rightBumperWasPressed()) {

            // Toggles sequence on/off
            if (launchingSequenceActive == 0) {
                launchingSequenceActive = 1;
            } else if (launchingSequenceActive == 1) {
                launchingSequenceCounter = 0;
                launchingSequenceActive = 0;
            }

            // Runs once at the start of the sequence
            if (launchingSequenceActive == 1) {
                runtime.reset();

            }
        }

        // runs if launchingSequenceActive
        if (launchingSequenceActive == 1) {
            switch (launchingSequenceCounter) {
                case 0:
                    launchingSequenceCounter++;
                    waitingForRPMDrop = true;
                    break;
                // step 1 HANDLES LIFTING FOR 1st BALL AND ENDS WHEN IS LAUNCHED
                case 1:
                    robot.backLeftLaunch.setPosition(BACK_LEFT_UP);
                    if (runtime.seconds() > 0.5 || !waitingForRPMDrop) {
                        runtime.reset();
                        launchingSequenceCounter++;
                    }
                    break;
                // step 2 DROPS THE FLICKER, GUARANTEES A 0.1s BUFFER
                case 2:
                    robot.backLeftLaunch.setPosition(BACK_LEFT_DOWN);
                    // calculates accuracy of the motor
                    desiredRPM = flywheelSpeed * 2360 / 28 * 60;
                    tps = robot.flywheelMotor.getVelocity();
                    rpm = (tps / 28.0) * 60.0;
                    motorOffset = Math.abs(desiredRPM - rpm);

                    if (runtime.seconds() > 0.1 && motorOffset < 250) {
                        runtime.reset();
                        launchingSequenceCounter++;
                        waitingForRPMDrop = true;
                    }
                    break;
                // step 3 HANDLES LIFTING FOR 2nd BALL AND ENDS WHEN IS LAUNCHED
                case 3:
                    robot.backRightLaunch.setPosition(BACK_RIGHT_UP);

                    if (runtime.seconds() > 0.5 || !waitingForRPMDrop) {
                        runtime.reset();
                        launchingSequenceCounter++;
                    }
                    break;
                // step 4 DROPS THE FLICKER, //GUARANTEES A 0.1s BUFFER
                case 4:
                    robot.backRightLaunch.setPosition(BACK_RIGHT_DOWN);

                    // calculates accuracy of the motor
                    desiredRPM = flywheelSpeed * 2360 / 28 * 60;
                    tps = robot.flywheelMotor.getVelocity();
                    rpm = (tps / 28.0) * 60.0;
                    motorOffset = Math.abs(desiredRPM - rpm);

                    if (runtime.seconds() > 0 && motorOffset < 450) {
                        runtime.reset();
                        launchingSequenceCounter++;
                    }
                    break;
                // step 5 STARTS LIFTING FOR 3rd BALL
                case 5:
                    robot.frontLeftLaunch.setPosition(FRONT_LEFT_UP);
                    robot.frontRightLaunch.setPosition(FRONT_RIGHT_UP);
                    if (runtime.seconds() > 0.15) {
                        runtime.reset();
                        launchingSequenceCounter++;
                    }
                    break;
                // step 6 CONTINUES LIFTING FOR 3rd BALL
                case 6:
                    robot.backLeftLaunch.setPosition(BACK_LEFT_UP);
                    robot.backRightLaunch.setPosition(BACK_RIGHT_UP);
                    if (runtime.seconds() > 0.35) {
                        runtime.reset();
                        launchingSequenceCounter++;
                    }
                    break;
                // step 7
                case 7:
                    robot.frontLeftLaunch.setPosition(FRONT_LEFT_DOWN);
                    robot.frontRightLaunch.setPosition(FRONT_RIGHT_DOWN);
                    robot.backRightLaunch.setPosition(BACK_RIGHT_DOWN);
                    robot.backLeftLaunch.setPosition(BACK_LEFT_DOWN);
                    if (runtime.seconds() > 0.5) {
                        runtime.reset();
                        launchingSequenceCounter++;
                    }
                    break;
                // reset
                case 8:
                    launchingSequenceCounter = 0;
                    launchingSequenceActive = 0;
                    break;
            }
        }else {
            if (gamepad1.b) {
                robot.backRightLaunch.setPosition(BACK_RIGHT_UP);
            } else {
                robot.backRightLaunch.setPosition(BACK_RIGHT_DOWN);
            }
            if (gamepad1.x) {
                robot.backLeftLaunch.setPosition(BACK_LEFT_UP);
            } else {
                robot.backLeftLaunch.setPosition(BACK_LEFT_DOWN);
            }

            if (gamepad1.y) {
                robot.frontRightLaunch.setPosition(FRONT_RIGHT_UP);
                robot.frontLeftLaunch.setPosition(FRONT_LEFT_UP);
            } else {
                robot.frontRightLaunch.setPosition(FRONT_RIGHT_DOWN);
                robot.frontLeftLaunch.setPosition(FRONT_LEFT_DOWN);

            }
        }

    }
    private void handleFlywheelSpeed() {
        if (gamepad1.dpadUpWasPressed()) {
            flywheelSpeed += FLYWHEEL_INCREMENT;
        }

        if (gamepad1.dpadDownWasPressed()) {
            flywheelSpeed -= FLYWHEEL_INCREMENT;
        }
        flywheelSpeed = Range.clip(flywheelSpeed, FLYWHEEL_MIN_SPEED, FLYWHEEL_MAX_SPEED);
        robot.flywheelMotor.setPower(flywheelSpeed);

        if (gamepad1.a) {
            robot.flywheelMotor.setPower(0);
        }
    }
    private void updateTelemetry() {
        telemetry.addData("Status", "Run Time: " + runtime.toString());
        telemetry.addData("Flywheel Target Speed", "%.2f (%.0f%%)", flywheelSpeed, flywheelSpeed * 100);
        telemetry.addData("Flywheel Actual Velocity (TPS)", robot.flywheelMotor.getVelocity());
        telemetry.addData("PIDF COEFFICIETS:\nP", kP);
        telemetry.addData("I", kI);
        telemetry.addData("D", kD);
        telemetry.addData("F", kF);
        telemetry.addData("STEP AMOUNT", stepAmount);
        telemetry.update();
    }
    private void logFlywheelRPM() {
        double tps = robot.flywheelMotor.getVelocity();
        double rpm = (tps / 28.0) * 60.0;
        // Detects drop in rpm meaning an artifact was launched
        if (waitingForRPMDrop) {
            slope = rpm - lastRPM;

            // Relative + absolute drop detection
            boolean significantDrop = slope < -50;
//                    delta > 150 &&                     // at least 150 RPM drop
//                            (delta / Math.max(lastRPM, 1)) > 0.12;  // AND at least 12% drop

            if (significantDrop) {
                waitingForRPMDrop = false;
                telemetry.addData("Launch Detected!", true);
            }
        }
        lastRPM = rpm;
        lastSlope = slope;


        TelemetryPacket packet = new TelemetryPacket();
        packet.put("flywheelRPM", rpm);
        packet.put("flywheelTPS", tps);
        packet.put("targetPower", flywheelSpeed);
        packet.put("launchingSequenceCounter", launchingSequenceCounter * 100 + 3400);
        packet.put("Slope", slope);

        dashboard.sendTelemetryPacket(packet);
    }
}
