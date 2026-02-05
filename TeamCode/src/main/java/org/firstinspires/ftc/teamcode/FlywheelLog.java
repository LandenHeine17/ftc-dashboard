package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;


@Disabled
public class FlywheelLog extends OpMode {

    RobotHardware robot;
    private FtcDashboard dashboard;
    DcMotorEx flywheel;

    @Override
    public void init() {
        robot = new RobotHardware(hardwareMap);
        flywheel = robot.flywheelMotor;

        dashboard = FtcDashboard.getInstance();
    }

    @Override
    public void loop() {
        robot.backLeftLaunch.setPosition(0.41);

        double tps = flywheel.getVelocity();          // ticks per second
        double rpm = (tps / 28.0) * 60.0;             // convert to RPM

        flywheel.setPower(gamepad1.right_trigger);
        double backLeftLaunchDistToIdealPosition = Math.abs(robot.backLeftLaunch.getPosition() - 0.41);

        TelemetryPacket packet = new TelemetryPacket();
//        packet.put("flywheelRPM", rpm);
        packet.put("DistToPos", backLeftLaunchDistToIdealPosition);
        dashboard.sendTelemetryPacket(packet);

        telemetry.addData("RPM", rpm);
        telemetry.update();
    }
}
