package controller;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;

public class KeypadTester extends JFrame {
	
	public KeypadTester(){
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
		this.setVisible(true);
		this.addKeyListener(new KeyAdapter(){
			
			public void keyPressed(KeyEvent e){
				
				System.out.println(e.getKeyCode());
				
			}
			
		});
		setFocusTraversalKeysEnabled(false);
		
	}
	


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new KeypadTester();

	}

}
