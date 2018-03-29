package gui;

import javax.swing.JFrame;

import controller.AntennaHandler;
import lerner.blindBridge.gui.GameGUI;
import model.CardDatabase;
import model.Game;

public class GUITest extends JFrame{
	
	public GUITest () {
		
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
		GameGUI gui = new GameGUI(new Game(new AntennaHandler(new CardDatabase()), false));
		gui.setVisible(true);
		
	}
	
	public static void main(String[] args){
		
		new GUITest();
		
	}
	
}