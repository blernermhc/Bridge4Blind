package gui;

import javax.swing.JFrame;

import model.Game;

public class GUITest extends JFrame{
	
	public GUITest () {
		
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
		GameGUI gui = new GameGUI(new Game());
		gui.setVisible(true);
		
	}
	
	public static void main(String[] args){
		
		new GUITest();
		
	}
	
}