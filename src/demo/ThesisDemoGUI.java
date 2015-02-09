package demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;


public class ThesisDemoGUI {
	
	private static ThesisDemo demo;
	
	public static void main (String[] args){
		
		try {
			createWindow();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void createWindow() throws IOException {
		
			demo = new ThesisDemo();
		 
	       //Create and set up the window. 
	       JFrame frame = new JFrame("Demo GUI");
	       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
	       
	       final JButton startButton = new JButton("Start");
	       startButton.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				try {
					demo.start();
				} catch (IOException e) {
					System.out.println("Antenna handler is not running.  Exiting.");
					System.exit(0);
				} catch (InterruptedException e) {
					System.out.println("Cycling timer was interrupted!");
					e.printStackTrace();
				}
				startButton.setEnabled(false);
				
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
	    	   
	    	   
	       });
	       
	       frame.getContentPane().add(startButton, BorderLayout.CENTER); 
	 
	       //Display the window. 
	       frame.setLocationRelativeTo(null); 
	       frame.pack();
	       frame.setVisible(true); 
	       
	       demo.getHandler().run();
	    }
	
	
}