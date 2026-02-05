package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
@Disabled
public class RobotHardware {

    public DcMotor leftFront, rightFront, leftBack, rightBack;
    public Servo frontRightLaunch, frontLeftLaunch, backRightLaunch, backLeftLaunch, hood;

    public DcMotorEx flywheelMotor;

    public RobotHardware(HardwareMap hardwareMap) {

        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");
        flywheelMotor = hardwareMap.get(DcMotorEx.class, "launchMotor");


        // Servo Names and Variables
        backLeftLaunch = hardwareMap.get(Servo.class, "backLeftLaunch"); // port 0
        backRightLaunch = hardwareMap.get(Servo.class, "backRightLaunch"); // port 1
        frontLeftLaunch = hardwareMap.get(Servo.class, "frontLeftLaunch"); // port 2
        frontRightLaunch = hardwareMap.get(Servo.class, "frontRightLaunch"); // port 3
        hood = hardwareMap.get(Servo.class, "hood"); // port 4


        flywheelMotor.setDirection(DcMotor.Direction.REVERSE);
        flywheelMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheelMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

    }
}
