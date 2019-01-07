package megas;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.NXTLightSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.Color;
import lejos.robotics.ColorAdapter;
import lejos.robotics.LightDetectorAdaptor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;
import lejos.utility.PilotProps;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;


public class Nba {
	
	// =================================================================
	// ========================== ANGLES ===============================
	// =================================================================
		
	public static final double TURN_RIGHT_ANGLE = 75.0;
	public static final double TURN_LEFT_ANGLE = 75.0;
	public static final int GRASP_ANGLE = 140;
	public static final int RELEASE_ANGLE = -180;
	
	
	// =================================================================
	// ========================== DISTANCES ============================
	// =================================================================
	public static final int HALF_BLOCK = 12;
	public static final int FULL_BLOCK = 33;

	
	// =================================================================
	// ========================== THRESHOLDS ===========================
	// =================================================================
	public static final int ANGLE_CORRECTION_THRESHOLD = 1;
	
	// =================================================================
	// ======================== PILOT PROPS ============================
	// =================================================================
	public static final int LINEAR_SPEED = 5;
	public static final int ANGULAR_SPEED = 25;

	// =================================================================
	// ========================== ENUMS ================================
	// =================================================================		
	public static final int NXT_RED_MODE = 0;
	
	// =================================================================
	// ================== MIDDLE MOTOR SPEEDS ==========================
	// =================================================================		
	public static final int MIDDLE_MOTOR_SLOW_SPEED = 100;
	public static final int MIDDLE_MOTOR_SPEED = 200;
	
	// =================================================================
	// ============= ULTRASONIC NXT MOTOR ROTATIONS ====================
	// =================================================================		
	public static final int ULTRASONIC_ROTATE_RIGHT = 90;
	public static final int ULTRASONIC_ROTATE_LEFT = -90;
	public static final int MEASUREMENT_NUMBER = 4;
	public static final float WALL_DISTANCE = 35.0f;
	
	// =================================================================
	// ==================== GYRO ROTATING ANGLEs =======================
	// =================================================================	
	public static final float RIGHT_ROTATE_GYRO_ANGLE = 90.0f;
	public static final float LEFT_ROTATE_GYRO_ANGLE = -90.0f;

	static EV3 ev3 = (EV3) BrickFinder.getDefault();
	
	// =================================================================
	// ========================== SENSORS ==============================
	// =================================================================
	static EV3UltrasonicSensor ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S1);
	// static NXTLightSensor nxtLightSensor = new NXTLightSensor(SensorPort.S2);
	static EV3ColorSensor ev3ColorSensor = new EV3ColorSensor(SensorPort.S4);
	static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S3);

	static ColorAdapter ev3ColorAdapter = new ColorAdapter(ev3ColorSensor);
	// static LightDetectorAdaptor nxtLightDetectorAdaptor = new LightDetectorAdaptor((SampleProvider)nxtLightSensor);
	
	
	// ====================================================================
	// ======================= POSITION INFO ==============================
	// ====================================================================
	static int orientation = 0;
	static int xPos = 3;
	static int yPos = 3;
	
	static MovePilot pilot;
	static GraphicsLCD graphicsLCD;

	public static void main(String[] args) throws Exception {		
		EV3 ev3 = (EV3) BrickFinder.getDefault();
		graphicsLCD = ev3.getGraphicsLCD();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Nba", graphicsLCD.getWidth()/2, 0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		
		PilotProps pilotProps = new PilotProps();
		pilotProps.setProperty(PilotProps.KEY_WHEELDIAMETER, "4.96");
		pilotProps.setProperty(PilotProps.KEY_TRACKWIDTH, "11.94");
		pilotProps.setProperty(PilotProps.KEY_LEFTMOTOR, "B");
		pilotProps.setProperty(PilotProps.KEY_RIGHTMOTOR, "D");
		pilotProps.setProperty(PilotProps.KEY_REVERSE, "false");
		pilotProps.storePersistentValues();
		pilotProps.loadPersistentValues();
    	
    	EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);
    	EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.D);
    	EV3LargeRegulatedMotor ultrasonicSensorMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    	
    	float leftWheelDiameter = Float.parseFloat(pilotProps.getProperty(PilotProps.KEY_WHEELDIAMETER, "5.72"));
    	float rightWheelDiameter = Float.parseFloat(pilotProps.getProperty(PilotProps.KEY_WHEELDIAMETER, "5.28"));
    	
    	float trackWidth = Float.parseFloat(pilotProps.getProperty(PilotProps.KEY_TRACKWIDTH, "11.95"));
    	boolean reverse = Boolean.parseBoolean(pilotProps.getProperty(PilotProps.KEY_REVERSE, "false"));
    	
    	Chassis chassis = new WheeledChassis(new Wheel[]{WheeledChassis.modelWheel(leftMotor,leftWheelDiameter).offset(-trackWidth/2).invert(reverse),WheeledChassis.modelWheel(rightMotor,rightWheelDiameter).offset(trackWidth/2).invert(reverse)}, WheeledChassis.TYPE_DIFFERENTIAL);
    	
    	pilot = new MovePilot(chassis);
    	pilot.setLinearSpeed(LINEAR_SPEED);
    	pilot.setAngularSpeed(ANGULAR_SPEED);
    	pilot.stop();
		
		ServerSocket serverSocket = new ServerSocket(1234);
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Nba", graphicsLCD.getWidth()/2, 0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString("Waiting", graphicsLCD.getWidth()/2, 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.refresh();
		
		Socket client = serverSocket.accept();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Nba", graphicsLCD.getWidth()/2, 0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString("Connected", graphicsLCD.getWidth()/2, 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.refresh();
        
		InputStream inputStream = client.getInputStream();
		DataInputStream dataInputStream = new DataInputStream(inputStream);
		
		OutputStream outputStream = client.getOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
		
		
		explore(ultrasonicSensorMotor, dataOutputStream);
		
		
		dataOutputStream.close();
		serverSocket.close();
	}
	
	
	
	public static void explore(EV3LargeRegulatedMotor ultrasonicSensorMotor, DataOutputStream dataOutputStream) throws IOException{
		boolean[] walls = new boolean[4];
		
		// Take the measurements
		// Front
		int wall_readings = 0;
		for(int i=0 ; i < MEASUREMENT_NUMBER ; i++) {
			float measurement = getUltrasonicSensorValue();
			if (measurement < WALL_DISTANCE) {
				wall_readings++;
			}
		}
		if (wall_readings > 1) {
			walls[(0 + orientation)%4] = true;
		}
		else {
			walls[(0 + orientation)%4] = false;
		}
		wall_readings = 0;
		ultrasonicSensorMotor.rotate(ULTRASONIC_ROTATE_RIGHT);
		// Right
		for(int i=0 ; i < MEASUREMENT_NUMBER ; i++) {
			float measurement = getUltrasonicSensorValue();
			if (measurement < WALL_DISTANCE) {
				wall_readings++;
			}
		}
		if (wall_readings > 1) {
			walls[(1 + orientation)%4] = true;
		}
		else {
			walls[(1 + orientation)%4] = false;
		}
		wall_readings = 0;
		ultrasonicSensorMotor.rotate(ULTRASONIC_ROTATE_RIGHT);
		// Back
		for(int i=0 ; i < MEASUREMENT_NUMBER ; i++) {
			float measurement = getUltrasonicSensorValue();
			if (measurement < WALL_DISTANCE) {
				wall_readings++;
			}
		}
		if (wall_readings > 1) {
			walls[(2 + orientation)%4] = true;
		}
		else {
			walls[(2 + orientation)%4] = false;
		}
		wall_readings = 0;
		ultrasonicSensorMotor.rotate(3 * ULTRASONIC_ROTATE_LEFT);
		// Left
		for(int i=0 ; i < MEASUREMENT_NUMBER ; i++) {
			float measurement = getUltrasonicSensorValue();
			if (measurement < WALL_DISTANCE) {
				wall_readings++;
			}
		}
		if (wall_readings > 1) {
			walls[(3 + orientation)%4] = true;
		}
		else {
			walls[(3 + orientation)%4] = false;
		}
		wall_readings = 0;
		ultrasonicSensorMotor.rotate(ULTRASONIC_ROTATE_RIGHT);
		
		int colorId = ev3ColorAdapter.getColorID();
		
		Cell cell = new Cell(colorId, walls);
		
		// Send the cell data to draw the map
		sendPositionData(dataOutputStream, cell);
	}
	
	public static void grabTheBall(GraphicsLCD graphicsLCD) {
		EV3MediumRegulatedMotor middleMotor = new EV3MediumRegulatedMotor(MotorPort.C);
		Utils utils = new Utils();
		
		middleMotor.setSpeed(MIDDLE_MOTOR_SPEED);
		middleMotor.rotate(RELEASE_ANGLE);
		middleMotor.stop();
		middleMotor.setSpeed(MIDDLE_MOTOR_SLOW_SPEED);
		pilot.travel(HALF_BLOCK);
		middleMotor.rotate(GRASP_ANGLE);
		middleMotor.stop();
		
		utils.determineBallColor(graphicsLCD);
	}
	
	public static void sendPositionData(DataOutputStream dataOutputStream, Cell currentCell) throws IOException {
		dataOutputStream.writeInt(xPos);
		dataOutputStream.writeInt(yPos);
		
		dataOutputStream.writeInt(orientation);
		dataOutputStream.writeInt(currentCell.colorId);
		
		dataOutputStream.writeBoolean(currentCell.frontWall);
		dataOutputStream.writeBoolean(currentCell.rightWall);
		dataOutputStream.writeBoolean(currentCell.backWall);
		dataOutputStream.writeBoolean(currentCell.leftWall);
	}
	
	public static float getUltrasonicSensorValue() {
		SampleProvider sampleProvider = ultrasonicSensor.getDistanceMode();
		if(sampleProvider.sampleSize() > 0) {
			float [] samples = new float[sampleProvider.sampleSize()];
			sampleProvider.fetchSample(samples, 0);
			return samples[0];
		}
		return -1;
	}
	
	public static void turnRight() {
		gyroSensor.reset();
		pilot.rotate(TURN_RIGHT_ANGLE);
		float gyro_value = getGyroSensorValue();
		if(gyro_value > RIGHT_ROTATE_GYRO_ANGLE + 1) {
			pilot.rotate(RIGHT_ROTATE_GYRO_ANGLE - gyro_value);
		}else if (gyro_value < RIGHT_ROTATE_GYRO_ANGLE - 1) {
			pilot.rotate(RIGHT_ROTATE_GYRO_ANGLE - gyro_value);
		}
	}
	
	public static void turnLeft() {
		gyroSensor.reset();
		pilot.rotate(TURN_LEFT_ANGLE);
		float gyro_value = getGyroSensorValue();
		if(gyro_value > LEFT_ROTATE_GYRO_ANGLE + 1) {
			pilot.rotate(LEFT_ROTATE_GYRO_ANGLE - gyro_value);
		}else if (gyro_value < LEFT_ROTATE_GYRO_ANGLE - 1) {
			pilot.rotate(LEFT_ROTATE_GYRO_ANGLE - gyro_value);
		}
	}
	
	public static void goForward(int distance) {
		gyroSensor.reset();
		pilot.travel(distance);
		float gyro_value = getGyroSensorValue();
		if (Math.abs(gyro_value)>ANGLE_CORRECTION_THRESHOLD) {
			pilot.rotate(-gyro_value);
		}
	}
	
	public static float getGyroSensorValue() {
	    SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
	    while(sampleProvider.sampleSize() == 0);
		float [] sample = new float[sampleProvider.sampleSize()];
    	sampleProvider.fetchSample(sample, 0);
    	float angle = sample[0];
    	return angle;
	}
	
}
