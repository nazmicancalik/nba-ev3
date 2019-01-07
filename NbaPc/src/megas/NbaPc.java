package megas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class NbaPc extends JFrame {
	
	private static final long serialVersionUID = 2280874288993963333L;
	
	// =================================================================
	// ============================ MAP INFO ===========================
	// =================================================================
	public static final int FRAME_WIDTH = 800;
	public static final int FRAME_HEIGHT = 800;
	
	public static final int CELL_WIDTH = 300;
	public static final int MEGAS_WIDTH = 100;
	
	public static final Color MEGAS_COLOR = Color.ORANGE;
	
	// =================================================================
	// ========================= POSITION INFO =========================
	// =================================================================
	static Map map;
	
	static int xPos = 3;
	static int yPos = 3;
	static int orientation = 0;
	
	
	static InputStream inputStream;
	static OutputStream outputStream;
	static DataInputStream dataInputStream;

	public NbaPc() {
		super("Map Making");
		setSize( 700, 500 );
		
		map = new Map();
		
		JPanel panel = new JPanel();

		this.getContentPane().add(panel);
		setVisible(true);
	}
	
	public static void main(String[] args) throws Exception	{
		
		NbaPc monitor = new NbaPc();
		
		monitor.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		// Enter the ip here.
		String ip = "10.0.1.1";
		
		@SuppressWarnings("resource")
		Socket socket = new Socket(ip, 1234);
		System.out.println("Connected!");

		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
		dataInputStream = new DataInputStream(inputStream);
		DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
		
		while(true){
			// Show the walls
			receivePositionInfo(dataInputStream);
			monitor.repaint();
			dataOutputStream.flush();
		}
	}
	
	public static void receivePositionInfo(DataInputStream dataInputStream) throws IOException {
		xPos = dataInputStream.readInt();
		yPos = dataInputStream.readInt();
		orientation = dataInputStream.readInt();
		int colorId = dataInputStream.readInt();
		
		boolean frontWall = dataInputStream.readBoolean();
		boolean rightWall = dataInputStream.readBoolean();
		boolean backWall = dataInputStream.readBoolean();
		boolean leftWall = dataInputStream.readBoolean();
		
		boolean[] walls = { frontWall, rightWall, backWall, leftWall };
		
		Cell cell = new Cell(colorId, walls);
		cell.isVisited = true;
		map.addCell(cell, xPos, yPos);
		
		System.out.println("***********************");
		System.out.println(xPos);
		System.out.println(yPos);
		System.out.println(orientation);
		System.out.println(colorId);
		System.out.println(frontWall);
		System.out.println(rightWall);
		System.out.println(backWall);
		System.out.println(leftWall);
		System.out.println("***********************");		
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		displayMap(map, g);
		displayMegas(xPos,yPos,g);
	}

	public void displayMap( Map map, Graphics g ){
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke( new BasicStroke( 5.0f ));

		for (int i = 0; i < map.getMap().length; i++ ){
			for (int j = 0; j < map.getMap()[0].length; j++) {
				// Draw a cell according to the cell data
				
				Cell currentCell = map.getCellAt(i, j);
				
				Color color = Color.WHITE;
				// If the cell is visited then get the color from inside.
				if (!currentCell.isVisited) {
					color = new Color(map.getCellAt(i, j).colorId);	
				}
				g2.setColor(color);
				g2.fillRect(i * CELL_WIDTH, j * CELL_WIDTH , CELL_WIDTH, CELL_WIDTH);
			}
		}
	}
	
	public void displayMegas(int xPos, int yPos, Graphics g ){
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(MEGAS_COLOR);
		// g2.setStroke( new BasicStroke( 5.0f ));
		g2.fillRect(xPos * CELL_WIDTH, yPos * CELL_WIDTH, CELL_WIDTH, CELL_WIDTH);
	}
}

