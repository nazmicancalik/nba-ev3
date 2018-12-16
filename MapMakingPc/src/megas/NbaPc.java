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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class NbaPc extends JFrame {
	
	
	// ========================= MAP DATA ==============================
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
	
	// ========================= MAP DATA ==============================
	
	public static ArrayList<Point> environment = new ArrayList<>();
	
	private static final long serialVersionUID = 2280874288993963333L;

	static InputStream inputStream;
	static OutputStream outputStream;
	static DataInputStream dataInputStream;
	
	static int side = 0;
	static int position = 0;
	static float distance = 0;

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
		
		while( true ){
			side = dataInputStream.readInt();
			position = dataInputStream.readInt();
			distance = dataInputStream.readFloat()*100;
			
			System.out.println("Side     : " + side);
			System.out.println("Position : " + position);
			System.out.println("Distance : " + distance);
			System.out.println("=====================================");
			monitor.repaint();
			dataOutputStream.flush();
		}
	}
	
	public void paint(Graphics g) {
		// super.repaint();
		super.paint(g);
		displayTrack(g);
		displayMegas(g);
		displayEnvironment(g);
	}
	
	// Finds the position of Megas and Draw the position.
	public void displayMegas(Graphics g) {
		Graphics2D g2 = ( Graphics2D ) g;
		g2.setPaint( Color.BLUE );
		g2.setStroke( new BasicStroke( 7.0f ));
		
		int megas_X = 0;
		int megas_Y = 0;
		
		int shownPosition = position * SCALE_FACTOR;
		
		// Calculate the position of Megas.
		if(side == 0) {
			megas_Y = SQUARE_Y + SQUARE_HEIGHT;
			megas_X = SQUARE_X + SQUARE_WIDTH - shownPosition;
		} else if (side == 1) {
			megas_Y = SQUARE_Y + SQUARE_HEIGHT - shownPosition;
			megas_X = SQUARE_X;
		} else if (side == 2) {
			megas_Y = SQUARE_Y;
			megas_X = SQUARE_X + shownPosition;
		} else {
			megas_Y = SQUARE_Y + shownPosition;
			megas_X = SQUARE_X + SQUARE_WIDTH;
		}
		
		
		// Draw Megas
		g2.draw(new Rectangle(megas_X, megas_Y,2,2));
	}
	
	public void displayEnvironment(Graphics g) {
		Graphics2D g2 = ( Graphics2D ) g;
		g2.setPaint( Color.GREEN);
		g2.setStroke( new BasicStroke( 5.0f ));
		
		int obstacle_X = 0;
		int obstacle_Y = 0;
		
		int shownPosition = position * SCALE_FACTOR;
		
		// Calculate the position of Megas.
		if(side == 0) {
			obstacle_Y = (int) (SQUARE_Y + SQUARE_HEIGHT + (distance / FACTOR));
			obstacle_X = SQUARE_X + SQUARE_WIDTH - shownPosition;
		} else if (side == 1) {
			obstacle_Y = SQUARE_Y + SQUARE_HEIGHT - shownPosition;
			obstacle_X = (int) (SQUARE_X - (distance / FACTOR));
		} else if (side == 2) {
			obstacle_Y = (int) (SQUARE_Y - (distance / FACTOR));
			obstacle_X = SQUARE_X + shownPosition;
		} else {
			obstacle_Y = SQUARE_Y + shownPosition;
			obstacle_X = (int) (SQUARE_X + SQUARE_WIDTH + (distance / FACTOR));
		}
		
		environment.add(new Point(obstacle_X,obstacle_Y));
		
		for (int i=0;  i<environment.size(); i++) {
			g2.draw(new Rectangle(environment.get(i).x,environment.get(i).y, 4, 4));	
		}
	}
	
	// Prints the square path.
	public void displayTrack(Graphics g) {
		Graphics2D g2 = ( Graphics2D ) g;
		g2.setPaint( Color.ORANGE );
		g2.setStroke( new BasicStroke( 5.0f ));
		
		g2.draw(new Rectangle(SQUARE_X,SQUARE_Y, SQUARE_WIDTH, SQUARE_HEIGHT));
	}
}

