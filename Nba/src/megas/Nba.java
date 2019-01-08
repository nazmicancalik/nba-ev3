package megas;
import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;
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
	public static final double TURN_LEFT_ANGLE = -75.0;
	public static final int GRASP_ANGLE = 140;
	public static final int RELEASE_ANGLE = -180;
	
	
	// =================================================================
	// ========================== DISTANCES ============================
	// =================================================================
	public static final int HALF_BLOCK = 12;
	public static final int FULL_BLOCK = 31;

	
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
	public static final int MEASUREMENT_NUMBER = 5;
	public static final float WALL_DISTANCE = 30.0f;
	
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
	
	static int current_mod = 0; // mapping
	private static final String filepath="./map_file";

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
		Button.waitForAnyPress();
		//turnRight();
		//turnRight();
		//turnLeft();
		//goForward(-FULL_BLOCK);
		Map map = new Map();
		System.out.println(map.toString());
		map = dfs(ultrasonicSensorMotor, dataOutputStream);
		current_mod = 1;
		dataOutputStream.writeInt(current_mod);
		map.writeObjectToFile(filepath);
		System.out.println(map.toString());
		map.ReadObjectFromFile(filepath);
		System.out.println(map.toString());

		dataOutputStream.close();
		serverSocket.close();
	}
	
	
	
	public static Cell explore(EV3LargeRegulatedMotor ultrasonicSensorMotor, DataOutputStream dataOutputStream) throws IOException{
		boolean[] walls = new boolean[4];
		int colorId = ev3ColorAdapter.getColorID();
		if (colorId!=7) {
			// Take the measurements
			// Front
			int wall_readings = 0;
			for(int i=0 ; i < MEASUREMENT_NUMBER ; i++) {
				float measurement = getUltrasonicSensorValue();
				if (measurement < WALL_DISTANCE) {
					wall_readings++;
				}
			}
			if (wall_readings > 2) {
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
			if (wall_readings > 2) {
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
			if (wall_readings > 2) {
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
			if (wall_readings > 2) {
				walls[(3 + orientation)%4] = true;
			}
			else {
				walls[(3 + orientation)%4] = false;
			}
			wall_readings = 0;
			ultrasonicSensorMotor.rotate(ULTRASONIC_ROTATE_RIGHT);			
		}

		Cell cell = new Cell(colorId, walls);
		
		// Send the cell data to draw the map
		sendPositionData(dataOutputStream, cell);
		return cell;
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
		dataOutputStream.writeInt(current_mod);
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
			return samples[0]*100;
		}
		return -1;
	}
	
	public static void turnRight() {
		orientation = (orientation +1) % 4;
		gyroSensor.reset();
		pilot.rotate(TURN_RIGHT_ANGLE);
		float gyro_value = getGyroSensorValue();
		if(gyro_value > RIGHT_ROTATE_GYRO_ANGLE + 1) {
			pilot.rotate(RIGHT_ROTATE_GYRO_ANGLE - gyro_value);
		}else if (gyro_value < RIGHT_ROTATE_GYRO_ANGLE - 1) {
			pilot.rotate(RIGHT_ROTATE_GYRO_ANGLE - gyro_value);
		}
		
		gyroSensor.reset();

	}
	
	public static void turnLeft() {
		if(orientation == 0) {
			orientation = 3;
		}else {
			orientation = (orientation -1) % 4;	
		}
		gyroSensor.reset();
		pilot.rotate(TURN_LEFT_ANGLE);
		float gyro_value = getGyroSensorValue();
		
		if(gyro_value > LEFT_ROTATE_GYRO_ANGLE + 1) {
			pilot.rotate(LEFT_ROTATE_GYRO_ANGLE - gyro_value);
		}else if (gyro_value < LEFT_ROTATE_GYRO_ANGLE - 1) {
			pilot.rotate(LEFT_ROTATE_GYRO_ANGLE - gyro_value);
		}
		
		gyroSensor.reset();

	}
	
	public static void goForward(int distance) {
		gyroSensor.reset();
		pilot.travel(distance);
		float gyro_value = getGyroSensorValue();
		if (Math.abs(gyro_value)>ANGLE_CORRECTION_THRESHOLD) {
			pilot.rotate(-gyro_value);
		}
		gyroSensor.reset();
	}
	
	public static float getGyroSensorValue() {
	    SampleProvider sampleProvider = gyroSensor.getAngleAndRateMode();
	    //while(sampleProvider.sampleSize() == 0);
		float [] sample = new float[sampleProvider.sampleSize()];
    	sampleProvider.fetchSample(sample, 0);
    	float angle = sample[0];
    	return -1*angle;
	}
	
	public static Map dfs(EV3LargeRegulatedMotor ultrasonicSensorMotor, DataOutputStream dataOutputStream) throws IOException {
		Map map = new Map();
		Point orijin = new Point(xPos, yPos);
		Stack<Point> stack = new Stack<Point>();
		stack.push(orijin);
		ArrayList<Point> traversed = new ArrayList<Point>();
		traversed.add(orijin);
		Point current_coordinates = orijin;
		
		Stack<Integer> traversed_directions = new Stack<Integer>();

		
		while(!stack.isEmpty()) {
			Point new_coordinates = stack.pop();
			int manhattan_distance = Math.abs(new_coordinates.x - current_coordinates.x) + Math.abs(new_coordinates.y - current_coordinates.y);
		
			System.out.println("===========================");
			System.out.println("Current x: " + current_coordinates.x + "Current y: " + current_coordinates.y);
			System.out.println("new x: " + new_coordinates.x + "new y: " + new_coordinates.y);
			System.out.println("Manhattan Distance: " + manhattan_distance);
			System.out.println("===========================");
			if (manhattan_distance > 1) {
				while(manhattan_distance > 1 || checkIfThereIsWallBetween(map, current_coordinates,new_coordinates)) {	// Burasý kesin büyüktür 1 olmalý.
					
					int direction = traversed_directions.pop();
					System.out.println("#########################");
					System.out.println("Manhattan Distance: " + manhattan_distance);
					System.out.println("Direction: " + direction);
					System.out.println("Pre Orientation: " + orientation);
					System.out.println("#########################");
					
					if (direction == 0) {
						changeOrientationAndGoUp();
						System.out.println("-----------------");
						System.out.println("GO UP");
						current_coordinates.x = current_coordinates.x - 1;
					} else if (direction == 1) {
						changeOrientationAndGoRight();
						System.out.println("-----------------");
						System.out.println("GO RIGHT");
						current_coordinates.y = current_coordinates.y + 1;
					} else if (direction == 2) {
						System.out.println("-----------------");
						System.out.println("GO DOWN");
						changeOrientationAndGoDown();
						current_coordinates.x = current_coordinates.x + 1;
					} else if (direction == 3) {
						System.out.println("-----------------");
						System.out.println("GO LEFT");
						changeOrientationAndGoLeft();
						current_coordinates.y = current_coordinates.y - 1;
					}
					System.out.println("-----------------");
					System.out.println("After turning Orientation: " + orientation);
					manhattan_distance = Math.abs(new_coordinates.x - current_coordinates.x) + Math.abs(new_coordinates.y - current_coordinates.y);
				}
			}
			
			if(new_coordinates.x < current_coordinates.x){
				// Go up
				traversed_directions.push(2);
				changeOrientationAndGoUp();
			}
			else if(new_coordinates.x > current_coordinates.x) {
				// Go down
				traversed_directions.push(0);
				changeOrientationAndGoDown();
			}
			else if(new_coordinates.y > current_coordinates.y) {
				// Go right
				traversed_directions.push(3);
				changeOrientationAndGoRight();
			}
			else if(new_coordinates.y < current_coordinates.y) {
				// Go left
				traversed_directions.push(1);
				changeOrientationAndGoLeft();
			}
		
			Point prev_coordinates = current_coordinates;
			current_coordinates = new_coordinates;
			xPos = current_coordinates.x;
			yPos = current_coordinates.y;
			traversed.add(current_coordinates);
			
			Cell current_cell = explore(ultrasonicSensorMotor, dataOutputStream);
			map.addCell(current_cell, current_coordinates.x, current_coordinates.y);
			if (current_cell.colorId != 7) {
				if(!current_cell.frontWall) {
					Point forward_coordinates = new Point(current_coordinates.x -1 , current_coordinates.y);
					if(!traversed.contains(forward_coordinates)) {
						stack.push(forward_coordinates);
					}
				}
				if(!current_cell.rightWall) {
					Point right_coordinates = new Point(current_coordinates.x , current_coordinates.y+1);
					if(!traversed.contains(right_coordinates)) {
						stack.push(right_coordinates);
					}			
				}
				if(!current_cell.backWall) {
					Point back_coordinates = new Point(current_coordinates.x +1 , current_coordinates.y);
					if(!traversed.contains(back_coordinates)) {
						stack.push(back_coordinates);
					}
				}
				if(!current_cell.leftWall) {
					Point left_coordinates = new Point(current_coordinates.x , current_coordinates.y-1);
					if(!traversed.contains(left_coordinates)) {
						stack.push(left_coordinates);
					}
				}
			}else {
				goForward(-FULL_BLOCK);
				traversed_directions.pop();
				current_coordinates = prev_coordinates;
				
			}


		}		
		return map;
	}
	
	public static boolean checkIfThereIsWallBetween(Map map, Point current, Point next) {
		Cell currentCell = map.getCellAt(current.x, current.y);
		Cell nextCell = map.getCellAt(next.x, next.y);
		
		boolean ret = false;
		if (current.x > next.x) {
			ret = currentCell.frontWall;
		} else if (current.x < next.x) {
			ret = currentCell.backWall;
		} else if (current.y > next.y) {
			ret = currentCell.leftWall;
		} else if (current.y < next.y) {
			ret = currentCell.rightWall;
		}
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("Current x: " + current.x + "Current y: " + current.y);
		System.out.println("next x: " + next.x + "next y: " + next.y);
		System.out.println(ret);
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		return ret;
	}
	
	public static void changeOrientationAndGoUp() {
		if(orientation==1) {
			turnLeft();
		}
		else if(orientation==2) {
			turnRight();
			turnRight();
		}
		else if(orientation==3) {
			turnRight();
		}
		
		goForward(FULL_BLOCK);
		orientation = 0;
	}
	
	public static void changeOrientationAndGoDown() {
		if(orientation==3) {
			turnLeft();
		}
		else if(orientation==0) {
			turnRight();
			turnRight();
		}
		else if(orientation==1) {
			turnRight();
		}
		
		goForward(FULL_BLOCK);
		orientation = 2;

	}
	
	public static void changeOrientationAndGoRight() {
		if(orientation==2) {
			turnLeft();
		}
		else if(orientation==3) {
			turnRight();
			turnRight();
		}
		else if(orientation==0) {
			turnRight();
		}

		goForward(FULL_BLOCK);
		orientation = 1;

	}
	
	public static void changeOrientationAndGoLeft() {
		if(orientation==0) {
			turnLeft();
		}
		else if(orientation==1) {
			turnRight();
			turnRight();
		}
		else if(orientation==2) {
			turnRight();
		}
		
		goForward(FULL_BLOCK);
		orientation = 3;
	}
	
	
	
}
