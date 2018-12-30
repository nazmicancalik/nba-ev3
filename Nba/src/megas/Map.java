package megas;

import megas.Cell;

public class Map {
	public static final int MAP_WIDTH = 7;
	
	Cell[][] map;
	
	public Map() {
		map = new Cell[MAP_WIDTH][MAP_WIDTH];
	}
}
