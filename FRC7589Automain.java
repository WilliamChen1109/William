/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.hal.DIOJNI;
import edu.wpi.first.hal.sim.DIOSim;
import edu.wpi.first.hal.sim.mockdata.DIODataJNI;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.POVButton;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends TimedRobot {

  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  private DifferentialDrive m_Drive ;

  private XboxController stick;
  private XboxController stick2;

  private WPI_VictorSPX baseleft1;
  private WPI_VictorSPX baseleft2;
  private WPI_VictorSPX baseright1;
  private WPI_VictorSPX baseright2;

  
  private WPI_VictorSPX hatchpanel;

  private WPI_VictorSPX vertical1;        
  private WPI_VictorSPX vertical2;        

  private WPI_VictorSPX standard;  
         
  private SpeedControllerGroup baseleft, baseright;

  private WPI_VictorSPX gate1;
  private WPI_VictorSPX gate2;
  private SpeedControllerGroup cargo;

  private Encoder enc;

  public DigitalInput limitswitch;

  public DigitalOutput light1;
  public DigitalOutput light2;
  public DigitalOutput light3;

  private POVButton pov0;
  private POVButton pov90;
  private POVButton pov180;
  private POVButton pov270;

  private boolean m_LimelightHasValidTarget = false;
  private double m_LimelightDriveCommand = 0.0;
  private double m_LimelightSteerCommand = 0.0;

  int gearbox = 1;
  int gearbox2 = 1;
  int level = 1;
  double speed = 1.0;
  double speed2 = 1.0;
  double baseleftspeed;
  double baserightspeed;
  int pov;
  boolean limit = false;
  boolean auto ;

  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    stick = new XboxController(0);
    stick2 = new XboxController(1);
    
    baseleft1 = new WPI_VictorSPX(0);
    baseleft2 = new WPI_VictorSPX(2);
    baseright1 = new WPI_VictorSPX(1);
    baseright2 = new WPI_VictorSPX(3);
    
    hatchpanel = new WPI_VictorSPX(6);

    vertical1 = new WPI_VictorSPX(5);
    vertical2 = new WPI_VictorSPX(7);

    standard = new WPI_VictorSPX(4);

    gate1 = new WPI_VictorSPX(9);
    gate2 = new WPI_VictorSPX(8);
    cargo = new SpeedControllerGroup(gate1, gate2);
    
    baseleft = new SpeedControllerGroup(baseleft1, baseleft2);
    baseright = new SpeedControllerGroup(baseright1, baseright2);

    pov = stick.getPOV();

    enc = new Encoder(3, 2);

    limitswitch = new DigitalInput(1);

    light1 = new DigitalOutput(4);
    light2 = new DigitalOutput(5);
    light3 = new DigitalOutput(6);

    pov0 = new POVButton(stick, 0);
    pov90 = new POVButton(stick, 90);
    pov180 = new POVButton(stick, 180);
    pov270 = new POVButton(stick, 270);
    
    m_Drive = new DifferentialDrive(baseleft, baseright);
  }

  @Override
  public void robotPeriodic() {

  }

  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
  }

  @Override
  public void autonomousPeriodic() {
    limit = limitswitch.get();
    baseleftspeed = stick.getY(Hand.kLeft)*speed;
    baserightspeed = stick.getY(Hand.kRight)*speed;

    if(gearbox == 0){
      gearbox = 1;
    }
    else if(gearbox == 4){
      gearbox = 3;
    }
    else if(stick.getBumperPressed(Hand.kRight)){
      gearbox++;
    }
    else if(stick.getBumperPressed(Hand.kLeft)){
      gearbox--;
    }

    if(gearbox == 1){
      speed = 0.1;
    }
    else if(gearbox == 2){
      speed = 0.2;
    }
    else if(gearbox == 3){
      speed = 0.4;
    }

    if(gearbox2 == 3){
      gearbox2 = 2;
    }
    else if(gearbox2 == 0){
      gearbox2 = 1;
    }
    else if(stick.getStickButton(Hand.kRight)){
      gearbox2++;
    }
    else if(stick.getStickButton(Hand.kLeft)){
      gearbox2--;
    }

    if(gearbox2 == 1){
      speed2 = 0.5;
    }
    else if(gearbox2 == 2){
      speed2 = 1.0;
    }
    //
    //
    //
    //Left and Right 
    if(pov90.get()){
      standard.set(ControlMode.PercentOutput , 1.0);
    }
    else if(pov270.get()){
      standard.set(ControlMode.PercentOutput , -1.0);
    }
    else{
      standard.set(ControlMode.PercentOutput , 0.0);
    }
    //
    //
    //
    //
    //Up and Down
    if(pov0.get()){                                   //Up
      vertical1.set(ControlMode.PercentOutput , 0.2);
      vertical2.set(ControlMode.PercentOutput , 0.2);
    }
    else if(stick.getXButton() && enc.get() < 7){    //second level
      vertical1.set(ControlMode.PercentOutput , 0.8);
      vertical2.set(ControlMode.PercentOutput , 0.8);
    }
    else if(stick.getYButton() && enc.get() < 13){    //Third lavel
      vertical1.set(ControlMode.PercentOutput , 0.8);
      vertical2.set(ControlMode.PercentOutput , 0.8);
    }
    else if(pov180.get() && limit == false){           //Down
      vertical1.set(ControlMode.PercentOutput , -0.1);
      vertical2.set(ControlMode.PercentOutput , -0.1);
    }
    else if(stick.getBackButton() && limit == false){
      vertical1.set(ControlMode.PercentOutput , -0.8);
      vertical2.set(ControlMode.PercentOutput , -0.8);
    }
    else{                                             //Stop
      vertical1.set(ControlMode.PercentOutput , 0.0);
      vertical2.set(ControlMode.PercentOutput , 0.0);
    }

    //Up and Down
    //
    //
    //
    //Hatch Panel
    if(stick.getTriggerAxis(Hand.kLeft) > 0.1){
      hatchpanel.set(ControlMode.PercentOutput , 0.2);
    }
    else if(stick.getTriggerAxis(Hand.kRight) > 0.1){
      hatchpanel.set(ControlMode.PercentOutput , -0.5*speed2);
    }
    else{
      hatchpanel.set(ControlMode.PercentOutput , 0.0);
    }
    //Hatch Panel
    //
    //
    //

    if(stick.getAButton()){
      cargo.set(1.0);
    }
    else if(stick.getBButton()){
      cargo.set(-1.0);
    }
    else{
      cargo.set(0.0);
    }
      
    if(limit){
      enc.reset();   
    }

    if(stick.getStartButton()){
      enc.reset();
    }

    if(gearbox == 1) light1.set(false);
    else light1.set(true);

    if(gearbox == 2) light2.set(false);
    else light2.set(true);

    if(gearbox == 3) light3.set(false);
    else light3.set(true);

      System.out.println(enc.get());

    auto = stick2.getAButton();

      if (auto)
      {
        if (m_LimelightHasValidTarget)
        {
              m_Drive.arcadeDrive(m_LimelightDriveCommand,m_LimelightSteerCommand);
        }
        else
        {
              m_Drive.arcadeDrive(0.0,0.0);
        }
      }
      else
      {
        if(Math.abs(stick.getY(Hand.kRight)) > 0.1){
          baseright.set(baserightspeed);
        }
        else{
          baseright.set(0.0);
        }
        if(Math.abs(stick.getY(Hand.kLeft)) > 0.1){
          baseleft.set(-baseleftspeed);
        }
        else{
          baseleft.set(0.0);
        }
      }
  }

  @Override
  public void teleopInit(){
    
  }

  @Override
  public void teleopPeriodic() {
    limit = limitswitch.get();
    baseleftspeed = stick.getY(Hand.kLeft)*speed;
    baserightspeed = stick.getY(Hand.kRight)*speed;

    if(gearbox == 0){
      gearbox = 1;
    }
    else if(gearbox == 4){
      gearbox = 3;
    }
    else if(stick.getBumperPressed(Hand.kRight)){
      gearbox++;
    }
    else if(stick.getBumperPressed(Hand.kLeft)){
      gearbox--;
    }

    if(gearbox == 1){
      speed = 0.1;
    }
    else if(gearbox == 2){
      speed = 0.2;
    }
    else if(gearbox == 3){
      speed = 0.4;
    }

    if(gearbox2 == 3){
      gearbox2 = 2;
    }
    else if(gearbox2 == 0){
      gearbox2 = 1;
    }
    else if(stick.getStickButton(Hand.kRight)){
      gearbox2++;
    }
    else if(stick.getStickButton(Hand.kLeft)){
      gearbox2--;
    }

    if(gearbox2 == 1){
      speed2 = 0.5;
    }
    else if(gearbox2 == 2){
      speed2 = 1.0;
    }
    //
    //
    //
    //Left and Right 
    if(pov90.get()){
      standard.set(ControlMode.PercentOutput , 1.0);
    }
    else if(pov270.get()){
      standard.set(ControlMode.PercentOutput , -1.0);
    }
    else{
      standard.set(ControlMode.PercentOutput , 0.0);
    }
    //
    //
    //
    //
    //Up and Down
    if(pov0.get()){                                   //Up
      vertical1.set(ControlMode.PercentOutput , 0.2);
      vertical2.set(ControlMode.PercentOutput , 0.2);
    }
    else if(stick.getXButton() && enc.get() < 7){    //second level
      vertical1.set(ControlMode.PercentOutput , 0.8);
      vertical2.set(ControlMode.PercentOutput , 0.8);
    }
    else if(stick.getYButton() && enc.get() < 13){    //Third lavel
      vertical1.set(ControlMode.PercentOutput , 0.8);
      vertical2.set(ControlMode.PercentOutput , 0.8);
    }
    else if(pov180.get() && limit == false){           //Down
      vertical1.set(ControlMode.PercentOutput , -0.1);
      vertical2.set(ControlMode.PercentOutput , -0.1);
    }
    else if(stick.getBackButton() && limit == false){
      vertical1.set(ControlMode.PercentOutput , -0.8);
      vertical2.set(ControlMode.PercentOutput , -0.8);
    }
    else{                                             //Stop
      vertical1.set(ControlMode.PercentOutput , 0.0);
      vertical2.set(ControlMode.PercentOutput , 0.0);
    }

    //Up and Down
    //
    //
    //
    //Hatch Panel
    if(stick.getTriggerAxis(Hand.kLeft) > 0.1){
      hatchpanel.set(ControlMode.PercentOutput , 0.2);
    }
    else if(stick.getTriggerAxis(Hand.kRight) > 0.1){
      hatchpanel.set(ControlMode.PercentOutput , -0.5*speed2);
    }
    else{
      hatchpanel.set(ControlMode.PercentOutput , 0.0);
    }
    //Hatch Panel
    //
    //
    //

    if(stick.getAButton()){
      cargo.set(1.0);
    }
    else if(stick.getBButton()){
      cargo.set(-1.0);
    }
    else{
      cargo.set(0.0);
    }
      
    if(limit){
      enc.reset();   
    }

    if(stick.getStartButton()){
      enc.reset();
    }

    if(gearbox == 1) light1.set(false);
    else light1.set(true);

    if(gearbox == 2) light2.set(false);
    else light2.set(true);

    if(gearbox == 3) light3.set(false);
    else light3.set(true);

    System.out.println(enc.get());

    Update_Limelight_Tracking();
    auto = stick2.getAButton();

      if (auto)
      {
        if (m_LimelightHasValidTarget)
        {
              m_Drive.arcadeDrive(m_LimelightDriveCommand,m_LimelightSteerCommand);
        }
        else
        {
              m_Drive.arcadeDrive(0.0,0.0);
        }
      }
      else
      {
        if(Math.abs(stick.getY(Hand.kRight)) > 0.1){
          baseright.set(baserightspeed);
        }
        else{
          baseright.set(0.0);
        }
        if(Math.abs(stick.getY(Hand.kLeft)) > 0.1){
          baseleft.set(-baseleftspeed);
        }
        else{
          baseleft.set(0.0);
        }
      }
  }

  public void disableInit(){
    System.out.printf("DISABLED\n");
  }


  @Override
  public void testPeriodic() {
  }

  public void Update_Limelight_Tracking()
  {
        // These numbers must be tuned for your Robot!  Be careful!
        final double STEER_K = 0.03;                    // how hard to turn toward the target
        final double DRIVE_K = 0.26;                    // how hard to drive fwd toward the target
        final double DESIRED_TARGET_AREA = 13.0;        // Area of the target when the robot reaches the wall
        final double MAX_DRIVE = 0.7;                   // Simple speed limit so we don't drive too fast

        double tv = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tv").getDouble(0);
        double tx = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tx").getDouble(0);
        double ty = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ty").getDouble(0);
        double ta = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ta").getDouble(0);

        if (tv < 1.0)
        {
          m_LimelightHasValidTarget = false;
          m_LimelightDriveCommand = 0.0;
          m_LimelightSteerCommand = 0.0;
          return;
        }

        m_LimelightHasValidTarget = true;

        // Start with proportional steering
        double steer_cmd = tx * STEER_K;
        m_LimelightSteerCommand = steer_cmd;

        // try to drive forward until the target area reaches our desired area
        double drive_cmd = (DESIRED_TARGET_AREA - ta) * DRIVE_K;

        // don't let the robot drive too fast into the goal
        if (drive_cmd > MAX_DRIVE)
        {
          drive_cmd = MAX_DRIVE;
        }
        m_LimelightDriveCommand = drive_cmd;
  }
  
}
