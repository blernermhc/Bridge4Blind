package gui_test;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;



public class MyPanel extends JPanel {

	
	public void paint(Graphics g){
		
		super.paint(g);
		
		//setOpaque(true);
		setBackground(Color.RED);
		
	}
	public static void main(String[] args) {

		JFrame f = new JFrame() ;
		
		f.setSize(900, 600);
		
		MyPanel panel = new MyPanel() ;

		
		
		f.add(panel) ;
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		f.setVisible(true);

	}

}
