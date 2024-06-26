// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.util.function.Supplier;

import com.kauailabs.navx.frc.AHRS;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.util.PathPlannerLogging;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.SwerveModule;
import frc.robot.Constants.DriveConstants;
import frc.robot.commands.RotateToAngle;

import static frc.robot.Constants.DriveConstants.*;

public class SwerveDrive extends SubsystemBase {

  private SwerveModule frontLeft;
  private SwerveModule frontRight;
  private SwerveModule backLeft;
  private SwerveModule backRight;

  private AHRS gyro;

  private SwerveDriveOdometry swerveOdometry;
  
  private SwerveModulePosition[] positions;
  private SwerveModuleState[] states;

  private ChassisSpeeds chassisSpeeds;
  private Supplier<Pose2d> poseSupplier;
  private Consumer<Pose2d> resetPoseConsumer;
  private Consumer<ChassisSpeeds> robotRelativeOutput;
  private Supplier<ChassisSpeeds> chassisSpeedSupplier;
  private BooleanSupplier shouldFlipSupplier;
  private Field2d field;
  private double lastAngle;

  private SlewRateLimiter xLimiter = new SlewRateLimiter(3);
  private SlewRateLimiter yLimiter = new SlewRateLimiter(3);
  private SlewRateLimiter rotateLimiter = new SlewRateLimiter(3);

  private boolean fieldOriented;
  public boolean slowMode;
  public boolean ninetyLock;
  public boolean zeroLock;

  /** Creates a new SwerveDrive. */
  public SwerveDrive() {

    frontLeft = new SwerveModule(
      DriveConstants.FRONT_LEFT[0],
      DriveConstants.FRONT_LEFT[1],
      DriveConstants.FL_ENCODER_PORT,
      false, false,
      DriveConstants.FRONT_LEFT_OFFSET,
      DriveConstants.FL_PID_VALUES);

    frontRight = new SwerveModule(
      DriveConstants.FRONT_RIGHT[0], 
      DriveConstants.FRONT_RIGHT[1],
      DriveConstants.FR_ENCODER_PORT,
      false, true,
      DriveConstants.FRONT_RIGHT_OFFSET,
      DriveConstants.FR_PID_VALUES);
   
    backLeft = new SwerveModule(
      DriveConstants.BACK_LEFT[0],
      DriveConstants.BACK_LEFT[1],
      DriveConstants.BL_ENCODER_PORT,
      false, false,
      DriveConstants.BACK_LEFT_OFFSET,
      DriveConstants.BL_PID_VALUES);  
      
    backRight = new SwerveModule(
      DriveConstants.BACK_RIGHT[0],
      DriveConstants.BACK_RIGHT[1],
      DriveConstants.BR_ENCODER_PORT,
      false, true,
      DriveConstants.BACK_RIGHT_OFFSET,
      DriveConstants.BR_PID_VALUES);

    gyro = new AHRS(SerialPort.Port.kUSB);

    states = new SwerveModuleState[] {
      frontLeft.getState(),
      frontRight.getState(),
      backLeft.getState(),
      backRight.getState()
    };

    swerveOdometry = new SwerveDriveOdometry(KINEMATICS, new Rotation2d(gyro.getAngle()), getModulePositions());
    chassisSpeeds = new ChassisSpeeds();

    poseSupplier = () -> getPose();
    resetPoseConsumer = pose -> resetOdometry(pose);
    robotRelativeOutput = inputSpeed -> drive(inputSpeed, SLOWER_DRIVE_SPEED);
    chassisSpeedSupplier = () -> getChassisSpeeds();
    shouldFlipSupplier = () -> false;

    fieldOriented = true; // Set to true for driver preference
    slowMode = false; // Whether the robot is running at its maximium speed or not
    ninetyLock = false; // Whether the angle of the wheels are locked at ninety degrees or not
    zeroLock = false; // Whether the angle of the wheels are locked at zero degrees or not

    field = new Field2d();

    // Magic that zeros the gyro upon startup.
    new Thread(() -> {
      try {
        Thread.sleep(1000);
        zeroHeading();
      } catch (Exception e) {}
    }).start();
    
    configureAutoBuilder();
  } 

  /**
   * Zero the modules
   */
  public void zeroModules() {
    frontLeft.resetEncoder();
    frontRight.resetEncoder();
    backLeft.resetEncoder();
    backRight.resetEncoder();
  }

  /**
   * Returns an AHRS object representing the gyro.
   */
  public AHRS getGyro() {
    return gyro;
  }

  /**
   * @return heading of the robot based upon the gyro.
   */
  public double getHeading() {
    return Math.IEEEremainder(gyro.getAngle(), 360);
  }

  /**
   * Zeros the gyro
   */
  private void zeroHeading() {
    gyro.reset();
  }

  /**
   * @return A Rotation2d object representing the robot's heading from radians.
   */
  public Rotation2d getRotation2d() {
    return Rotation2d.fromRadians(getHeading() * (Math.PI / 180));
  }

  /**
   * @return A Pose2d object representing the position of the robot on the field.
   */
  public Pose2d getPose() {
    return swerveOdometry.getPoseMeters();
  }

  /**
   * Reset the odometry based on the angle, module positions, and pose.
   * @param pose
   */
  public void resetOdometry(Pose2d pose) {
    swerveOdometry.resetPosition(getRotation2d(), getModulePositions(), pose);
  }

  /**
   * Sets modules to speeds within the allowed range
   * @param desiredStates
   */
  public void setModuleStates(SwerveModuleState[] desiredStates, double maxDriveSpeed) {
    SwerveDriveKinematics.desaturateWheelSpeeds(desiredStates, maxDriveSpeed);
    frontLeft.setDesiredState(desiredStates[0]);
    frontRight.setDesiredState(desiredStates[1]);
    backLeft.setDesiredState(desiredStates[2]);
    backRight.setDesiredState(desiredStates[3]);
  }

  /**
   * @return An array of SwerveModulePosition objects containing the current drive and rotate motor positions for their respective modules.
   */
  public SwerveModulePosition[] getModulePositions() {
    SwerveModulePosition[] positions = {new SwerveModulePosition(), new SwerveModulePosition(), new SwerveModulePosition(), new SwerveModulePosition()};
    positions[0] = new SwerveModulePosition(frontLeft.getDrivePosition(), new Rotation2d(frontLeft.getRotatePosition()));
    positions[1] = new SwerveModulePosition(frontRight.getDrivePosition(), new Rotation2d(frontRight.getRotatePosition()));
    positions[2] = new SwerveModulePosition(backLeft.getDrivePosition(), new Rotation2d(backLeft.getRotatePosition()));
    positions[3] = new SwerveModulePosition(backRight.getDrivePosition(), new Rotation2d(backRight.getRotatePosition()));
    return positions;
  }

  /**
   * @return An array of SwerveModuleState objects containing the current drive and rotate motor velocities for their respective modules.
   */
  public SwerveModuleState[] getModuleStates() {
    SwerveModuleState[] states = {new SwerveModuleState(), new SwerveModuleState(), new SwerveModuleState(), new SwerveModuleState()};
    states[0] = new SwerveModuleState(frontLeft.getDriveVelocity(), new Rotation2d(frontLeft.getRotatePosition()));
    states[1] = new SwerveModuleState(frontRight.getDriveVelocity(), new Rotation2d(frontRight.getRotatePosition()));
    states[2] = new SwerveModuleState(backLeft.getDriveVelocity(), new Rotation2d(backLeft.getRotatePosition()));
    states[3] = new SwerveModuleState(backRight.getDriveVelocity(), new Rotation2d(backRight.getRotatePosition()));
    return states;
  }

  /**
   * Set the module states all to zero degrees and zero velocity.
   */
  public Command goToZero() {
    return run(() -> {
      SwerveModuleState[] zeroStates = {new SwerveModuleState(), new SwerveModuleState(), new SwerveModuleState(), new SwerveModuleState()};
      setModuleStates(zeroStates, MAX_DRIVE_SPEED);
    });
  }

  /**
   * Rotate the robot toward the amp (nintey degrees or -ninety degrees) depending on the alliance.
   */
  public Command rotateToAmp() {
    boolean redAlliance = shouldFlip(); // Depending on the starting alliance side, the amp would be at different angle. Account for that.
    RotateToAngle rotate = redAlliance ? new RotateToAngle(90, this) : new RotateToAngle(-90, this); // If red, go to 90. If blue, go to -90.

    return rotate;
  }

  /**
   * Rotate the robot toward the speaker (zero degrees)
   */
  public Command rotateToSpeaker() {
    return new RotateToAngle(0, this);
  }

  /**
   * Set all motor velocities to zero.
   */
  public void stopModules() {
    frontLeft.stop();
    frontRight.stop();
    backLeft.stop();
    backRight.stop();
  }

  /**
   * Toggle field oriented
   */
  public Command toggleFieldOriented() {
    return runEnd(()-> {}, () -> {fieldOriented = !fieldOriented;});
  }

  /**
   * @return The current state of field oriented mode
   */
  public boolean getFieldOriented() {
    return fieldOriented;
  }

  public Command toggleSlowMode() {
    return runEnd(() -> {}, () -> slowMode = !slowMode);
  }

  /**
   * @return The current state of slow mode.
   */
  public boolean getSlowMode() {
    return slowMode;
  }

  /**
   * Toggle ninety lock mode.
   */
  public Command toggleNinetyLock() {
    return runEnd(() -> {
      ninetyLock = true;
    }, () -> {
      ninetyLock = false;
    });
  }

  /**
   * Toggle zero lock mode.
   */
  public Command toggleZeroLock() {
    return runEnd(() -> {
      zeroLock = true;
    }, () -> {
      zeroLock = false;
    });
  }

  /**
   * @return A ChassisSpeeds object representing the current x and y velocity in meters/second and the rotation velocity in radians/second
   */
  public ChassisSpeeds getChassisSpeeds() {
    return new ChassisSpeeds(chassisSpeeds.vxMetersPerSecond, chassisSpeeds.vyMetersPerSecond, chassisSpeeds.omegaRadiansPerSecond);
  }
   
  /**
   * Drive each motor at a set speed without going over the set maximum.
   * @param speeds to input into the motors.
   * @param maxDriveSpeed the highest velocity the robot is allowed to drive during function.
   */
  public void drive(ChassisSpeeds speeds, double maxDriveSpeed) {
    chassisSpeeds = new ChassisSpeeds(-speeds.vxMetersPerSecond, -speeds.vyMetersPerSecond, speeds.omegaRadiansPerSecond);
    chassisSpeeds = ChassisSpeeds.discretize(chassisSpeeds, 0.02);
    
    SwerveModuleState moduleStates[] = KINEMATICS.toSwerveModuleStates(chassisSpeeds);
    setModuleStates(moduleStates, maxDriveSpeed);
  }

  /**
   * @return True when the robot's alliance is red. False when the robot's alliance is blue.
   */
  public boolean shouldFlip() {
    var alliance = DriverStation.getAlliance();
    if (alliance.isPresent()) {
      return alliance.get() == DriverStation.Alliance.Red;
    }
    return false;
  }

  /**
   * Set all motors to brake mode.
   */
  public void setBrakeMode() {
    frontLeft.setBrakeMode();
    frontRight.setBrakeMode();
    backLeft.setBrakeMode();
    backRight.setBrakeMode();
  }

  /**
   * Set all motors to coast mode.
   */
  public void setCoastMode() {
    frontLeft.setCoastMode();
    frontRight.setCoastMode();
    backLeft.setCoastMode();
    backRight.setCoastMode();
  }

  public double get5V() {
    return RobotController.getVoltage5V();
  }

  public void configureAutoBuilder() {
    AutoBuilder.configureHolonomic(
      poseSupplier, 
      resetPoseConsumer,
      chassisSpeedSupplier,
      robotRelativeOutput,
      PATH_CONFIG,
      shouldFlipSupplier,
      this
      );
  }

  public void putOffsets(SendableBuilder sendableBuilder) {
    sendableBuilder.addDoubleProperty("Front Left", ()-> frontLeft.getOffsets(), null);
    sendableBuilder.addDoubleProperty("Front Right", ()-> frontRight.getOffsets(), null);
    sendableBuilder.addDoubleProperty("Back Left", ()-> backLeft.getOffsets(), null);
    sendableBuilder.addDoubleProperty("Back Right", ()-> backRight.getOffsets(), null);
  }

  @Override
  public void initSendable(SendableBuilder sendableBuilder) {
    sendableBuilder.setSmartDashboardType("Swerve drive");
    sendableBuilder.addBooleanProperty("Field Oriented", () -> fieldOriented, null);
    sendableBuilder.addBooleanProperty("Slow mode", () -> getSlowMode(), null);
    sendableBuilder.addDoubleProperty("Heading", () -> getHeading(), null);
    putOffsets(sendableBuilder);
   }  

  @Override
  public void periodic() {
    swerveOdometry.update(getRotation2d(), getModulePositions());
  }
}