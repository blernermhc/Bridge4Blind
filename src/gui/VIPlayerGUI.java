package gui;

import java.awt.Graphics;
import java.awt.event.ActionEvent;

import controller.Handler;
import controller.TestAntennaHandler;
import lerner.blindBridge.model.Direction;
import model.Game;

/**The VIPlayerGUI allows the sighted players to specify what position 
 * the visually impaired player is.
 * 
 * @author Allison DeJordy
 * 
 * @version March 12, 2015
 */

public class VIPlayerGUI extends DirectionGUI{
	private Game game;
	private GameGUI gameGUI;

	
	/**Constructor; constructs a new VIPlayerGUI.
	 * @param gameGUI the frame the gui is inside of
	 * @param game the game being played
	 */
	public VIPlayerGUI(GameGUI gameGUI, Game game) {
		//call the superclass constructor with the appropriate title
		super ("What position is the visually impaired player?");
		this.game = game;
		this.gameGUI = gameGUI;
		
		if(Game.isTestMode()){
			
			// the blind player can only be east in the test case
			northButton.setEnabled(false);
			southButton.setEnabled(false);
			westButton.setEnabled(false);
			
			eastButton.setEnabled(true);
			

			((TestAntennaHandler) game.getHandler()).setRightGUI(false);
		}
	}

	/**
	 * Sets the antenna the blind player is on.
	 * Starts the antenna handler listening to the blind person's antenna.
	 * Moves to the next step of the GUI
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// gui.debugMsg("Not listening to the antenna");
		Handler handler = game.getHandler();
		if (handler != null) {
			// Yikes!  This used to just be handler.start.  Was that causing
			// our timing problems???
			new Thread(handler, "Antenna handler").start() ;
			
		}
		else {
			System.out.println("Server is not running!");
		}

		if (e.getActionCommand().equals("North")){
			
			game.setBlindPosition(Direction.NORTH);
			
		} else if (e.getActionCommand().equals("East")){
			
			game.setBlindPosition(Direction.EAST);
			
		} else if (e.getActionCommand().equals("South")){
			
			game.setBlindPosition(Direction.SOUTH);
			
		} else if (e.getActionCommand().equals("West")){
			
			game.setBlindPosition(Direction.WEST);
			
		}
		
		
		gameGUI.changeFrame();
	}
	
	@Override
	public void paintComponent(Graphics g){
		
		super.paintComponent(g);
		
		gameGUI.undoButtonSetEnabled(false);
		gameGUI.backButtonSetEnabled(false);
	}

	
	
}