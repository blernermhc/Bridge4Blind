package gui_test;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;



public class PlayerStatusGUI extends JPanel {
	
	public final Direction dir ;
	
	private Color color = Color.BLACK ;
	
	private JButton button ;
	

	public PlayerStatusGUI(Direction dir) {
		
		
		//setLayout(new FlowLayout()) ;
		
		//setLayout(new CardLayout());
		
		//setLayout(new BorderLayout()) ;
		
		//setLayout(new GridLayout(1,1));
		
		this.dir = dir ;

		System.out.println("Player status GUI created at " + dir) ;
		
		Border border = BorderFactory.createLineBorder(Color.BLACK, 3) ;
		
		this.setBorder(border);
		
		// both these lines are necessary
		setPreferredSize(new Dimension(300,200));
		setMinimumSize(new Dimension(300,200));
		
		String s1 = "" + dir ;
		String s2 = s1 + "\n" + dir ;
		


						
	}

	
	
	public void paintComponent(Graphics g) {
		
			
		System.out.println("Player Status GUI paint "  + dir);
		
		Graphics2D g2d = (Graphics2D) g;
		
		
		if(dir == Direction.NORTH){
			
			
			color = (Color.RED) ;
			
			
		}else if(dir == Direction.SOUTH){
			
			//g.setColor(Color.GREEN) ;
			
			color = (Color.GREEN) ;
			
		}else if(dir == Direction.EAST){
			
			//g.setColor(Color.BLUE) ;
			
			color =  (Color.BLUE) ;
			
		}else if(dir == Direction.WEST){
		
			//g.setColor(Color.YELLOW) ;
			color = (Color.YELLOW) ;
		}
		
		
		//setBackground(color);
		g2d.setColor(color);
		//g2d.fillRect(0, 0, getWidth(), getHeight());
		
		//g2d.fillRect(0, 0, 175, 175);
		
		System.out.println(getWidth() + " , " + getHeight());
		
		//g.setColor(Color.BLACK);
		//g.drawRect(0, 0, getWidth(), getHeight());
		
		g2d.drawString("HELLO WORLD", 50, 50);
		
	}

	
	

}

/*public class PlayerStatusGUI extends JTextArea{
	
	

	private Direction dir ;
	
	private double rotation;

	public PlayerStatusGUI(Direction dir) {
		
		super() ;
		//setLayout(new BorderLayout()) ;
		this.dir = dir ;
		
		rotation = (dir.ordinal() + 2) * .5 * Math.PI;
		
		
		System.out.println("Player status GUI created at " + dir) ;
		
		this.setText("Player status GUI created at " + dir + "\n" + dir + "\n" + dir + "\n"+ dir + "\n"+ dir + "\n"+ dir + "\n"+ dir + "\n"+ dir + "\n"+ dir + "\n"+ dir + "\n"+ dir + "\n"+ dir + "\n"+ dir + "\n");
		
		//System.out.println(this.getSize().getWidth() + " , " + this.getSize().getHeight()) ;
		
		
		ImageIcon image = new ImageIcon("TrumpCard1.jpg") ;
		//this.setIcon(image);
		
		this.setBackground(Color.RED);
		
		//this.setSize(300, 200);
		
	//	System.out.println(this.getSize());

		
	}
	
	public void paintComponent(Graphics g) {
		

		System.out.println("x " + this.getX() );
		System.out.println("y " + this.getY() );
		System.out.println("width " + this.getWidth());
		System.out.println("height " + this.getHeight());
		
        Graphics2D twoDee = (Graphics2D) g;
        twoDee.rotate(Math.PI *2);
        super.paintComponent(twoDee);
   }

}*/
