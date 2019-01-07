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
	
	public static final int SQUARE_WIDTH = 300;
	public static final int SQUARE_HEIGHT= SQUARE_WIDTH;
	public static final int SQUARE_X = (FRAME_WIDTH / 2) - (SQUARE_WIDTH / 2);
	public static final int SQUARE_Y = SQUARE_X;
	
	public static final int ENVIRONMENT_RECT_WIDTH = 10;
	public static final int ENVIRONMENT_RECT_HEIGHT = 10;
	
	public static final int FACTOR = 1;
	
	public static final int SCALE_FACTOR = SQUARE_WIDTH / 100;

	
	// =================================================================
	// ========================= POSITION INFO =========================
	// =================================================================
	static int xPos = 3;
	static int yPos = 3;
	static int orientation = 0;
	
	
	static InputStream inputStream;
	static OutputStream outputStream;
	static DataInputStream dataInputStream;

	public NbaPc() {
		super("Map Making");
		setSize( 700, 500 );
		
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
		
		boolean[] walls = new boolean[4];
		
		xPos = dataInputStream.readInt();
		yPos = dataInputStream.readInt();
		orientation = dataInputStream.readInt();
		int colorId = dataInputStream.readInt();
		
		boolean frontWall = dataInputStream.readBoolean();
		boolean rightWall = dataInputStream.readBoolean();
		boolean backWall = dataInputStream.readBoolean();
		boolean leftWall = dataInputStream.readBoolean();
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
}

