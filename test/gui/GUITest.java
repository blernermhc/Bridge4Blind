package gui;

import javax.swing.JFrame;

public class GUITest extends JFrame{
	
	public GUITest () {
		
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
		GameGUI gui = new GameGUI(null);
		this.add(gui);
		this.setVisible(true);
		
	}
	
	public static void main(String[] args){
		
		new GUITest();
		
	}
	
}