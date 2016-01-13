package Snake_Ladder;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game implements Runnable,MouseListener{
	private static final int width = 1024;
	private static final int height = 1024;
	
	
	private boolean running = false;
	private Thread thread;
	private Painter painter;
	private BufferedImage image,One,Tom,dice,TomTurn,JerryTurn,TomBoard,JerryBoard;
	private Random random = new Random();
	
	BufferedImage Jerry,Two,Three,Four,Five,Six;
	
	private boolean jerry = true;
	private boolean yourTurn = false;
	private boolean isTomWon = false;
	private boolean isJerryWon = false;
	private boolean accepted = false;
	private boolean unableToCommunicate = false;
	private boolean isTomReadyToGo = false;
	private boolean isJerryReadyToGo = false;
	
	@SuppressWarnings("unused")
	private Font font = new Font("Verdana",Font.BOLD,32);
	private Font largerfont = new Font("Verdana",Font.BOLD,50);
	@SuppressWarnings("unused")
	private Font smallerfont = new Font("Verdana",Font.BOLD,20);
	
	private String waitingString = "Waiting for the Opponent";
	private String unableToCommunicateString = "Unable To Communicate";
	@SuppressWarnings("unused")
	private String WinString = "You Won";
	@SuppressWarnings("unused")
	private String EnemyWinString = "Enemy Won";
	
	private ServerSocket serverSocket;
	private Socket socket;
	private String ip;
	private int port;
	private int errors = 0;
	private Scanner scanner = new Scanner(System.in);
	private DataInputStream dis;
	private DataOutputStream dos;
	private int TomScore;
	private int JerryScore;
	private int TomIndex;
	private int JerryIndex;
	private DataInputStream dis1;
	private DataOutputStream dos1;
	
	
	Map<Integer,Integer> ladder = new LinkedHashMap<Integer,Integer>();
	
	private String Space[] = new String[100] ;
	
	
	public Game(){
		
		ladder.put(1, 38);
		ladder.put(4, 14);
		ladder.put(9, 31);
		ladder.put(21, 42);
		ladder.put(28, 84);
		ladder.put(51, 67);
		ladder.put(80, 100);
		ladder.put(71, 91);
		
		
		ladder.put(98, 79);
		ladder.put(95, 75);
		ladder.put(93, 73);
		ladder.put(87, 24);
		ladder.put(62, 19);
		ladder.put(64, 60);
		ladder.put(54, 34);
		ladder.put(17,  7);
		
				
		Space[20] = "L";
		Space[28] = "L";
		Space[77] = "L";
		Space[49] = "L";
		Space[70] = "L";
		Space[90] = "L";
		Space[93] = "L";
		Space[98] = "L";
		
		
		System.out.println("Please enter the ip:");
		ip = scanner.nextLine();
		System.out.println("Please enter port:");
		port = scanner.nextInt();
		if( port<1 || port>65535 ){
			System.out.println("The port you entered was incorrect, please provide the correct port:" );
			port = scanner.nextInt();
		}
		
		if(!connect()) initializeNewServer();
		
		
		
		
		painter = new Painter();
		
		
		JFrame frame = new JFrame("Snake And Ladder");
		frame.setContentPane(painter);
		frame.pack();
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.addMouseListener(this);
		loadImage();
		dice = One;
		thread = new Thread(this,"Display");
	}

	
	private void loadImage() {
		try{
		 image = ImageIO.read(getClass().getResource("/Snakes.jpg"));
		 One = ImageIO.read(getClass().getResource("/One.png"));
		 Tom = ImageIO.read(getClass().getResource("/Tom.jpg"));
		 Jerry = ImageIO.read(getClass().getResource("/jerry.jpg"));
		 Two = ImageIO.read(getClass().getResource("/Two.png"));
		 Three = ImageIO.read(getClass().getResource("/Three.png"));
		 Four = ImageIO.read(getClass().getResource("/Four.png"));
		 Five = ImageIO.read(getClass().getResource("/Five.png"));
		 Six = ImageIO.read(getClass().getResource("/Six.png"));
		 TomTurn = ImageIO.read(getClass().getResource("/TomTurn.jpg"));
		 JerryTurn = ImageIO.read(getClass().getResource("/jerryTurn.jpg"));
		 TomBoard = ImageIO.read(getClass().getResource("/TomBoard.jpg"));
		 JerryBoard = ImageIO.read(getClass().getResource("/JerryBoard.jpg"));
		}catch(IOException e){
			System.out.println("Couldn't load the images");
		}
		
	}
	
	
	private boolean connect(){
		try{
			socket = new Socket(ip,port);
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			dis1 = new DataInputStream(socket.getInputStream());
			dos1 = new DataOutputStream(socket.getOutputStream());
			accepted = true;
			
		}catch(Exception e){
			System.out.println("Failed to connect to "+ip+":"+port+"| Initializing server");
	
			return false;
		}
		System.out.println("Successfully connected to server");
		return true;
		
	}
	
	
	private void initializeNewServer(){
		try{
			serverSocket = new ServerSocket(port , 8 , InetAddress.getByName(ip));
		}catch(Exception e){
			e.printStackTrace();
		}
		jerry = false;
		yourTurn = true;
		
	}
	
	private void ListenForServerRequest(){
		socket = null;
		try{
			socket = serverSocket.accept();
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			dis1 = new DataInputStream(socket.getInputStream());
			dos1 = new DataOutputStream(socket.getOutputStream());
			accepted = true;
			System.out.println("CLIENT HAS REQUESTED AND WE HAVE ACCEPTED");
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	public void run() {
		while(running){
			update();
			painter.repaint();
			
			if(!jerry && !accepted) {
				
				ListenForServerRequest();
			}
		}
		
	}
	private int calculateIndex(int score){
		if(score>=1 && score<=10){
			return (1+10-score);
		}else if(score>=21 && score<=30){
			return (21+30-score);
		}else if(score>=41 && score<=50){
			return (41+50-score);
		}else if(score>=61 && score<=70){
			return (61+70-score);
		}else if(score>=81 && score<=90){
			return (81+90-score);
		}else{
			return score;
		}
	}
	
	private void update(){
		if(errors>=10)   unableToCommunicate = true;
		
		
		if(!yourTurn && !unableToCommunicate){
			try{
				int num = dis.readInt();
				
				int score = dis1.readInt();
				if(jerry)
					TomScore = score;
				else
					JerryScore = score;

				//System.out.println("TomScore:"+TomScore+","+"JerryScore:"+JerryScore);
				if(TomScore>=100)
					TomScore = 100;
				if(JerryScore>=100)
					JerryScore = 100;
				if(TomScore == 1 || TomScore == 4 || TomScore == 9 || TomScore == 21 || TomScore == 28 || TomScore == 51 || TomScore == 80 || TomScore == 71 || TomScore == 98 || TomScore == 95 || TomScore == 93 || TomScore == 87 || TomScore == 64 || TomScore == 62 || TomScore == 54 || TomScore == 17 ){
					TomScore = TomScore +( ladder.get(TomScore)-TomScore);
				}
				if(JerryScore == 1 || JerryScore == 4 || JerryScore == 9 || JerryScore == 21 || JerryScore == 28 || JerryScore == 51 || JerryScore == 80 || JerryScore == 71 || JerryScore == 98 || JerryScore == 95 || JerryScore == 87 || JerryScore  == 64 || JerryScore == 62 || JerryScore == 54 || JerryScore == 17){
					JerryScore = JerryScore + (ladder.get(JerryScore)-JerryScore);
				}
				
				TomIndex = calculateIndex(TomScore);
				JerryIndex = calculateIndex(JerryScore);
				
				
				switch(num){
				case 1:
					dice = One;
					break;
				case 2:
					dice = Two;
					break;
				case 3:
					dice = Three;
					break;
				case 4:
					dice = Four;
					break;
				case 5:
					dice = Five;
					break;
				case 6:
					dice = Six;
					break;
				}
				checkForTomWin();
				checkForJerryWin();
				yourTurn = true;
			}catch(Exception e){
				e.printStackTrace();
				errors++;
			}
		}
		
	}
	
	private void checkForJerryWin() {
		if(JerryScore == 100)
			isJerryWon = true;
	}


	private void checkForTomWin() {
		if(TomScore == 100)
			isTomWon = true;
		
	}


	private void render(Graphics g){
		g.drawImage(image, 0, 0, null);
		
		
		if(unableToCommunicate){
			g.setColor(Color.RED);
			g.setFont(largerfont);
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth(unableToCommunicateString);
			g.drawString(unableToCommunicateString, width/2 - stringWidth/2, height/2);
			return;
		
		}
		
		if(accepted){
			g.drawImage(dice, 790, 216, null);
			if(!jerry){
				if(yourTurn){
					g.drawImage(TomTurn, 790, 10, null);
					g.drawImage(Jerry, 790, 430, null);
				}
				else{
					g.drawImage(Tom, 790, 10, null);
					g.drawImage(Jerry, 790, 430, null);
				}
			}
			else{
				if(yourTurn){
					g.drawImage(Tom, 790, 10, null);
					g.drawImage(JerryTurn, 790, 430, null);
				}
				else{
					g.drawImage(Tom, 790, 10, null);
					g.drawImage(Jerry, 790, 430, null);
				}
			}
			g.drawImage(TomBoard, 10+((100-TomIndex)%10)*70+((100-TomIndex)%10)*4,8+((int)(100-TomIndex)/10)*72+((int)(100-TomIndex)/10)*4 , null);
			
			g.drawImage(JerryBoard, 10+((100-JerryIndex)%10)*70+((100-JerryIndex)%10)*4,8+((int)(100-JerryIndex)/10)*72+((int)(100-JerryIndex)/10)*4 , null);
			
			
			if(isTomWon || isJerryWon){
				if(isTomWon){
					g.setColor(Color.RED);
					g.setFont(largerfont);
					Graphics2D g2 = (Graphics2D)g;
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					int stringWidth = g2.getFontMetrics().stringWidth("Tom Won!!!!");
					g.drawString("Tom Won!!!!", width/2 - stringWidth/2, height/2);
				}
				else{
					g.setColor(Color.RED);
					g.setFont(largerfont);
					Graphics2D g2 = (Graphics2D)g;
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					int stringWidth = g2.getFontMetrics().stringWidth("Jerry Won!!!!");
					g.drawString("Jerry Won!!!!", width/2 - stringWidth/2, height/2);	
				}
			}
		}else{
			g.setColor(Color.RED);
			g.setFont(largerfont);
			g.drawImage(One, 790, 216, null);
			g.drawImage(Tom, 790, 10, null);
			g.drawImage(Jerry, 790, 430, null);
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int stringWidth = g2.getFontMetrics().stringWidth(waitingString);
			g.drawString(waitingString, width/2 - stringWidth/2, height/2);
		}
	}
	
	
	
	
	
	private class Painter extends JPanel{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		Painter(){
			requestFocus();
		}
		
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			render(g);
		}
	}
	
	
	public synchronized void start(){
		running = true;
		thread.start();
	}
	
	public synchronized void stop(){
		running = false;
		try{
			thread.join();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		new Game().start();
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		int number = 0;
		if(accepted){
			
			if(yourTurn && !unableToCommunicate && !isTomWon && !isJerryWon){
			
				number = random.nextInt(6)+1;
				

				if(jerry && !isJerryReadyToGo && number == 1)
					isJerryReadyToGo = true;
				else if(!jerry && !isTomReadyToGo && number==1)
					isTomReadyToGo = true;
				
				
				if(jerry && isJerryReadyToGo)
					JerryScore += number;
				else if(!jerry && isTomReadyToGo)
					TomScore += number;
				switch(number){
				case 1:
					dice = One;
					break;
				case 2:
					dice = Two;
					break;
				case 3:
					dice = Three;
					break;
				case 4:
					dice = Four;
					break;
				case 5:
					dice = Five;
					break;
				case 6:
					dice = Six;
					break;
				}
				if(TomScore>=100)
					TomScore = 100;
				if(JerryScore>=100)
					JerryScore = 100;
				if(TomScore == 1 || TomScore == 4 || TomScore == 9 || TomScore == 21 || TomScore == 28 || TomScore == 51 || TomScore == 80 || TomScore == 71 || TomScore == 98 || TomScore == 95 || TomScore == 93 || TomScore == 87 || TomScore == 64 || TomScore == 62 || TomScore == 54 || TomScore == 17 ){
					TomScore = TomScore +( ladder.get(TomScore)-TomScore);
				}
				if(JerryScore == 1 || JerryScore == 4 || JerryScore == 9 || JerryScore == 21 || JerryScore == 28 || JerryScore == 51 || JerryScore == 80 || JerryScore == 71 || JerryScore == 98 || JerryScore == 95 || JerryScore == 87 || JerryScore  == 64 || JerryScore == 62 || JerryScore == 54 || JerryScore == 17){
					JerryScore = JerryScore + (ladder.get(JerryScore)-JerryScore);
				}
				TomIndex = calculateIndex(TomScore);
				JerryIndex = calculateIndex(JerryScore);
				//System.out.println("TomScore:"+TomScore+","+"JerryScore:"+JerryScore);
				
				checkForTomWin();
				checkForJerryWin();
				yourTurn = false;
				painter.repaint();
				Toolkit.getDefaultToolkit().sync();
		
		try{
			dos.writeInt(number);
			dos.flush();
			if(jerry)
				dos1.writeInt(JerryScore);
			else
				dos1.writeInt(TomScore);
			dos.flush();
			
		}catch(Exception e1){
			e1.printStackTrace();
			errors++;
		}}}
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}




