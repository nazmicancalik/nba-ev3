package megas;

public class Cell {
	
	public static final float WALL_DISTANCE_THRESHOLD = 25.0f;
	
	int colorId;
	
	float frontDistance;
	float rightDistance;
	float backDistance;
	float leftDistance;
	
	boolean frontWall = false;
	boolean rightWall = false;
	boolean backWall = false;
	boolean leftWall = false;
	
	
	public Cell(int colorId, float[] ultrasonicReadings) {
		
		this.colorId = colorId;
		
		this.frontDistance = ultrasonicReadings[0];
		this.rightDistance = ultrasonicReadings[1];
		this.backDistance = ultrasonicReadings[2];
		this.leftDistance = ultrasonicReadings[3];

		if (frontDistance < WALL_DISTANCE_THRESHOLD) {
			frontWall = true;
		} else if (rightDistance < WALL_DISTANCE_THRESHOLD) {
			rightWall = true;
		} else if(backDistance < WALL_DISTANCE_THRESHOLD) {
			backWall = true;
		} else if(leftDistance < WALL_DISTANCE_THRESHOLD) {
			leftWall = true;
		}
	}
}
