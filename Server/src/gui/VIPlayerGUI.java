package gui;

import java.awt.event.ActionEvent;

import controller.AntennaHandler;
import controller.DummyHandler;
import model.Direction;
import model.Game;

/**The VIPlayerGUI allows the sighted players to specify what position 
 * the visually impaired player is.
 * 
 * @author Allison DeJordy
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
	}

	/**
	 * Sets the antenna the blind player is on.
	 * Starts the antenna handler listening to the blind person's antenna.
	 * Moves to the next step of the GUI
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		

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
}