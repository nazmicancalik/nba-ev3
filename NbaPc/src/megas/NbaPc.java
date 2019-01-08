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
	public static final int CELL_WIDTH = 100;
	public static final int MEGAS_WIDTH = 50;
	
	public static final int FRAME_WIDTH = CELL_WIDTH * 7;
	public static final int FRAME_HEIGHT = CELL_WIDTH * 7;

	public static final Color MEGAS_COLOR = Color.ORANGE;
	public static final Color WALL_COLOR = Color.YELLOW;
	public static final Color BACKGROUND_COLOR = Color.GRAY;
	public static final Color STRIPE_COLOR = Color.DARK_GRAY;
	
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
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setBackground(BACKGROUND_COLOR);
		setResizable(true);
		setVisible(true);
		map = new Map();
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
		System.out.println(colorId);
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

		// Draw Cells
		for (int i = 0; i < map.getMap().length; i++ ){
			for (int j = 0; j < map.getMap()[0].length; j++) {
				// Draw a cell according to the cell data
				Cell currentCell = map.getCellAt(i, j);

				Color color = Color.GRAY;
				// If the cell is visited then get the color from inside.
				if (currentCell.isVisited) {
					int colorId = map.getCellAt(i, j).colorId;
					if (colorId == 6) {
						color = Color.WHITE;
					} else if (colorId == 1) {
						color = Color.GREEN;
					} else if (colorId == 2) {
						color = Color.RED;
					}
					
					System.out.println(color.toString());
				}
				
				// g2.setColor(color);
				g2.setPaint(color);
				
				// g2.drawRect(i * CELL_WIDTH, (7-j) * CELL_WIDTH , CELL_WIDTH, CELL_WIDTH);
				g2.fillRect(j * CELL_WIDTH, i * CELL_WIDTH , CELL_WIDTH, CELL_WIDTH);
				
				// Draw the walls
				g2.setStroke(new BasicStroke(10.0f));
				
				// Front Wall
				if (currentCell.frontWall) {
					g2.setColor(WALL_COLOR);
					// g2.drawLine(i*CELL_WIDTH,(7-j)*CELL_WIDTH,(i+1)*CELL_WIDTH,(7-j)*CELL_WIDTH);
					g2.draw(new Line2D.Double(j*CELL_WIDTH,i*CELL_WIDTH,(j+1)*CELL_WIDTH,i*CELL_WIDTH));	
				}
				
				// Right Wall
				if (currentCell.rightWall) {
					g2.setColor(WALL_COLOR);
					// g.drawLine((i+1)*CELL_WIDTH, (7-j)*CELL_WIDTH,(i+1)*CELL_WIDTH,  (7-j-1)*CELL_WIDTH);
					g2.draw(new Line2D.Double((j+1)*CELL_WIDTH, i*CELL_WIDTH,(j+1)*CELL_WIDTH,  (i+1)*CELL_WIDTH));	
				}
				
				// Back Wall
				if (currentCell.backWall) {
					g2.setColor(WALL_COLOR);
					// g.drawLine(i*CELL_WIDTH,(7-j) * CELL_WIDTH,(i+1)*CELL_WIDTH, (7-j-1)*CELL_WIDTH);
					g2.draw(new Line2D.Double(j*CELL_WIDTH,(i+1) * CELL_WIDTH,(j+1)*CELL_WIDTH, (i+1) *CELL_WIDTH));
				}
				
				// Left Wall
				if (currentCell.leftWall) {
					g2.setColor(WALL_COLOR);
					// g.drawLine(i*CELL_WIDTH,(7-j)*CELL_WIDTH,i*CELL_WIDTH, (7-j-1)*CELL_WIDTH);
					g2.draw(new Line2D.Double(j*CELL_WIDTH,i*CELL_WIDTH,j*CELL_WIDTH, (i+1)*CELL_WIDTH));
				}
			}
		}
		
		// Draw stripes
		g2.setColor(STRIPE_COLOR);
		g2.setStroke( new BasicStroke(0.5f));
		
		// Vertical Lines
		for(int i = 1; i<= 6; i++) {
			// g2.drawLine(i*CELL_WIDTH, 0, i*CELL_WIDTH, FRAME_HEIGHT);
			g2.draw(new Line2D.Double(i*CELL_WIDTH, 0, i*CELL_WIDTH, FRAME_HEIGHT));
		}
		
		// Horizontal Lines
		for(int i = 1; i<= 6; i++) {
			// g2.drawLine(0,i*CELL_WIDTH,FRAME_WIDTH, i*CELL_WIDTH);
			g2.draw(new Line2D.Double(0,i*CELL_WIDTH,FRAME_WIDTH, i*CELL_WIDTH));
		}
	}
	
	public void displayMegas(int xPos, int yPos, Graphics g ){
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(MEGAS_COLOR);
		// g2.setStroke( new BasicStroke( 5.0f ));
		g2.fillRect(yPos * CELL_WIDTH + ((CELL_WIDTH - MEGAS_WIDTH)/2), xPos * CELL_WIDTH + ((CELL_WIDTH - MEGAS_WIDTH)/2), MEGAS_WIDTH , MEGAS_WIDTH);
	}
}

