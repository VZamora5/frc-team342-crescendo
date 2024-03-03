// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.Constants.IntakeConstants;
import frc.robot.Constants.OperatorConstants;
import frc.robot.commands.Autos;
<<<<<<< HEAD
import frc.robot.commands.Drive.DriveDistance;
import frc.robot.commands.RotateToAngle;
import frc.robot.commands.TimedDrive;
import frc.robot.commands.Drive.DriveWithJoystick;
import frc.robot.commands.Outtake.OuttakeNote;
import frc.robot.subsystems.Outtake;
import frc.robot.subsystems.SwerveDrive;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.PathPlannerAuto;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.Joystick;
=======
import frc.robot.commands.Load;
import frc.robot.commands.MoveWristPercent;
import frc.robot.commands.MoveWristToPosition;
import frc.robot.commands.Drive.DriveWithJoystick;
>>>>>>> da1d78a6d31e038f5f77268f33f42f8881c8589d
import edu.wpi.first.wpilibj.XboxController;

import static frc.robot.Constants.IntakeConstants.*;

import edu.wpi.first.util.sendable.SendableBuilder;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.POVButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Outtake;
import frc.robot.subsystems.SwerveDrive;
import frc.robot.subsystems.Wrist;



/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...

  private final JoystickButton xButton;
  
  private SwerveDrive swerve;
  
  private XboxController driver;
  private XboxController operator;

  private DriveWithJoystick driveWithJoystick;
<<<<<<< HEAD
<<<<<<< HEAD
  private DriveDistance driveDistance;
  private OuttakeNote outtakeNote;
  private TimedDrive driveFoward;
  private RotateToAngle rotate90;
=======
  // private MoveWristToPosition moveWrist;
=======

  private MoveWristToPosition moveWristDown;
  private MoveWristToPosition moveWristUp;
  private MoveWristToPosition moveWristAmp;
  private SequentialCommandGroup wristDownIntake;
>>>>>>> 8f4cd22c60b747839737dab3fdc03569ac8c4f9b

  private Outtake shootVelocity;

  private Load load;
  private Outtake outtake;
>>>>>>> da1d78a6d31e038f5f77268f33f42f8881c8589d

  private JoystickButton goToZeroBtn;
  private JoystickButton rotateToAngleButton;
  private JoystickButton toggleFieldOrientedBtn;
  private JoystickButton toggleSlowModeBtn;
  private JoystickButton timedDriveButton;
  private JoystickButton outtakeNoteBtn;
<<<<<<< HEAD
  private JoystickButton driveDistanceButton;
=======
  private JoystickButton wristButton;
  private JoystickButton intakeBtn;
>>>>>>> da1d78a6d31e038f5f77268f33f42f8881c8589d

  private POVButton wristDownBtn;
  private POVButton wristUpBtn;
  private POVButton wristRightBtn;

  private Intake intake;
  private Wrist wrist;
  private JoystickButton loadButton;

  private MoveWristPercent moveWristPercent;

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {

    intake = new Intake();
    outtake = new Outtake();
    swerve = new SwerveDrive();

<<<<<<< HEAD
<<<<<<< HEAD
    joy = new XboxController(0);
    driveWithJoystick = new DriveWithJoystick(swerve, joy);
    // outtakeNote = new OuttakeNote(0.5, outtake);
    driveDistance = new DriveDistance(1, 5, swerve);

    //driveWithJoystick = new DriveWithJoystick(swerve, joy, swerve.getFieldOriented());
    timedDriveButton = new JoystickButton(joy,  XboxController.Button.kY.value);
    driveFoward = new TimedDrive(swerve, 2, 0, 0);
    rotate90 = new RotateToAngle( 270, swerve);
    rotateToAngleButton = new JoystickButton(joy, XboxController.Button.kB.value);

    
    swerve.setDefaultCommand(driveWithJoystick);
    toggleFieldOrientedBtn = new JoystickButton(joy, XboxController.Button.kA.value);
    toggleSlowModeBtn = new JoystickButton(joy, XboxController.Button.kX.value);
    // outtakeNoteBtn = new JoystickButton(joy, XboxController.Button.kB.value);
    driveDistanceButton = new JoystickButton(joy, XboxController.Button.kY.value);

=======
=======
>>>>>>> 8f4cd22c60b747839737dab3fdc03569ac8c4f9b
    driver = new XboxController(0);
    operator = new XboxController(1);

    xButton = new JoystickButton(operator, XboxController.Button.kX.value);
    wristButton = new JoystickButton(operator, XboxController.Button.kY.value);
    loadButton = new JoystickButton(operator, XboxController.Button.kB.value);
    intakeBtn = new JoystickButton(operator, XboxController.Button.kA.value);
    outtakeNoteBtn = new JoystickButton(operator, XboxController.Button.kA.value);
    wristDownBtn = new POVButton(operator, 180);
    wristUpBtn = new POVButton(operator, 0);
    wristRightBtn = new POVButton(operator, 270);

    toggleFieldOrientedBtn = new JoystickButton(driver, XboxController.Button.kA.value);
    toggleSlowModeBtn = new JoystickButton(driver, XboxController.Button.kX.value);
    
    driveWithJoystick = new DriveWithJoystick(swerve, driver);

    moveWristDown = new MoveWristToPosition(wrist, IntakeConstants.LOW_WRIST_POS);
    moveWristUp = new MoveWristToPosition(wrist, IntakeConstants.HIGH_WRIST_POS);
    moveWristAmp = new MoveWristToPosition(wrist, IntakeConstants.AMP_POS);

    wristDownIntake = new SequentialCommandGroup(moveWristDown, intake.spinIntake().until(() -> !intake.getIntakeSensor()));

    moveWristPercent = new MoveWristPercent(operator, wrist);
    wrist.setDefaultCommand(moveWristPercent);

    outtakeNoteBtn = new JoystickButton(operator, XboxController.Button.kA.value);
    wristDownBtn = new POVButton(operator, 180);
    wristUpBtn = new POVButton(operator, 0);
    wristRightBtn = new POVButton(operator, 90);

    swerve.setDefaultCommand(driveWithJoystick);
>>>>>>> da1d78a6d31e038f5f77268f33f42f8881c8589d

   SmartDashboard.putData(swerve);
   SmartDashboard.putData(outtake);
   SmartDashboard.putData(intake);

    configureBindings();
  } 

  /**
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary
   * predicate, or via the named factories in {@link
   * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for {@link
   * CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
   * PS4} controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
   * joysticks}.
   */
  private void configureBindings() {
<<<<<<< HEAD
<<<<<<< HEAD
    toggleFieldOrientedBtn.whileTrue(swerve.toggleFieldOriented());
    toggleSlowModeBtn.whileTrue(swerve.toggleSlowMode());
    // outtakeNoteBtn.whileTrue(outtakeNote);
    goToZeroBtn.whileTrue(swerve.goToZero());
    timedDriveButton.whileTrue(driveFoward);
    rotateToAngleButton.whileTrue(rotate90);
    
=======
   xButton.whileTrue(intake.spinIntake());
  //  aButton.whileTrue(intake.getSensors());
  //  wristButton.whileTrue(moveWrist);
   loadButton.whileTrue(load);
   intakeBtn.whileTrue(intake.spinIntake());
>>>>>>> da1d78a6d31e038f5f77268f33f42f8881c8589d
=======
   xButton.whileTrue(intake.outtake()); // X
   loadButton.whileTrue(load);
   intakeBtn.whileTrue(intake.spinIntake()); // A
   wristDownBtn.onTrue(wristDownIntake);
   wristUpBtn.onTrue(moveWristUp);
   wristRightBtn.onTrue(moveWristAmp);

   toggleFieldOrientedBtn.whileTrue(swerve.toggleFieldOriented());
   toggleSlowModeBtn.whileTrue(swerve.toggleSlowMode());
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An example command will be run in autonomous
    return null;
  }
}
