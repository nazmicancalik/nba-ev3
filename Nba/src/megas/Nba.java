package megas;
import java.io.DataOutputStream;
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
	
	// =================================================================
	// ========================== THRESHOLDS ===========================
	// =================================================================			
	public static final double BLUE_BALL_THRESHOLD = 0.37;
	
	
	
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
	
	
	static EV3 ev3 = (EV3) BrickFinder.getDefault();
	
	// =================================================================
	// ========================== SENSORS ==============================
	// =================================================================
	static EV3UltrasonicSensor ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S1);
	static NXTLightSensor nxtLightSensor = new NXTLightSensor(SensorPort.S2);
	static EV3ColorSensor ev3ColorSensor = new EV3ColorSensor(SensorPort.S4);
	
	static ColorAdapter ev3ColorAdapter = new ColorAdapter(ev3ColorSensor);
	static LightDetectorAdaptor nxtLightDetectorAdaptor = new LightDetectorAdaptor((SampleProvider)nxtLightSensor);
; 

	static MovePilot pilot;
	
	/**
	 * Gets the ultrasonic value
	 * @return ultrasonic_reading
	 */
	static float getUltrasonicSensorValue() {
		SampleProvider sampleProvider = ultrasonicSensor.getDistanceMode();
		if(sampleProvider.sampleSize() > 0) {
			float [] samples = new float[sampleProvider.sampleSize()];
			sampleProvider.fetchSample(samples, 0);
			return samples[0];
		}
		return -1;
	}
	
	public static void main(String[] args) throws Exception {		
		EV3 ev3 = (EV3) BrickFinder.getDefault();
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
		
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
    	EV3MediumRegulatedMotor middleMotor = new EV3MediumRegulatedMotor(MotorPort.C);
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
		
    	// =================================================================
    	// ====================== SENSOR SETTINGS ==========================
    	// =================================================================
    	nxtLightSensor.setCurrentMode("Red");
    	nxtLightSensor.setFloodlight(NXT_RED_MODE);
    	nxtLightSensor.setFloodlight(true);
    	nxtLightDetectorAdaptor.setReflected(true);

		// ServerSocket serverSocket = new ServerSocket(1234);
		
		
    	
		graphicsLCD.clear();
		graphicsLCD.drawString("Nba", graphicsLCD.getWidth()/2, 0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString("Waiting", graphicsLCD.getWidth()/2, 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.refresh();
		
		// Socket client = serverSocket.accept();
		
		graphicsLCD.clear();
		graphicsLCD.drawString("Nba", graphicsLCD.getWidth()/2, 0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.drawString("Connected", graphicsLCD.getWidth()/2, 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
		graphicsLCD.refresh();
        
		// OutputStream outputStream = client.getOutputStream();
		// DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
		sendCellData(ultrasonicSensorMotor);
		
		middleMotor.setSpeed(MIDDLE_MOTOR_SPEED);
		middleMotor.rotate(RELEASE_ANGLE);
		middleMotor.stop();
		middleMotor.setSpeed(MIDDLE_MOTOR_SLOW_SPEED);
		pilot.travel(HALF_BLOCK);
		middleMotor.rotate(GRASP_ANGLE);
		middleMotor.stop();
		determineBallColor(graphicsLCD);
		
		// Turn back
		pilot.travel(-HALF_BLOCK);
		
		// dataOutputStream.close();
		// serverSocket.close();
	}
	
	public static void sendCellData(EV3LargeRegulatedMotor ultrasonicSensorMotor) {
		float front_distance = getUltrasonicSensorValue();
		ultrasonicSensorMotor.rotate(ULTRASONIC_ROTATE_RIGHT);
		float right_distance = getUltrasonicSensorValue();
		ultrasonicSensorMotor.rotate(ULTRASONIC_ROTATE_RIGHT);
		float back_distance = getUltrasonicSensorValue();
		ultrasonicSensorMotor.rotate(3*ULTRASONIC_ROTATE_LEFT);
		float left_distance = getUltrasonicSensorValue();
		ultrasonicSensorMotor.rotate(ULTRASONIC_ROTATE_RIGHT);
		Color color = ev3ColorAdapter.getColor();

	}
	
	public static void determineBallColor(GraphicsLCD graphicsLCD) {
		
		double reading = nxtLightDetectorAdaptor.getLightValue();
		if (reading < BLUE_BALL_THRESHOLD) {
			graphicsLCD.clear();
			graphicsLCD.drawString("BLUE BALL", graphicsLCD.getWidth()/2, 0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.refresh();
		} else {
			graphicsLCD.clear();
			graphicsLCD.drawString("RED BALL", graphicsLCD.getWidth()/2, 0, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
			graphicsLCD.refresh();
		}
	}
	
	
	public static void turnRight() {
		pilot.rotate(TURN_RIGHT_ANGLE);
	}
	
	public static void turnLeft() {
		pilot.rotate(TURN_LEFT_ANGLE);
	}
}
