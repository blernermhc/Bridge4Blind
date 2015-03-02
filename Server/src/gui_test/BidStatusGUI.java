package gui_test;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;



//public class BidStatusGUI extends JComponent{
	
public class BidStatusGUI extends JPanel{

	public BidStatusGUI(){
		
		super() ;
		
		
		System.out.println("Bid Status GUI created") ;
	}
	
	public void paint(Graphics g){
		
		g.setColor(Color.PINK);
		g.fillRect(0, 0, getWidth(), getHeight());
	}
	

	



}
