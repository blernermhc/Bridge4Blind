package gui;

import java.awt.event.ActionEvent;

import model.Direction;
import model.Game;

/**The BidGUI allows the sighted players to specify which player
* made the winning bid.
* 
* @author Allison DeJordy
* @version March 12, 2015
* 
**/

public class BidPositionGUI extends DirectionGUI {
	private Game game;
	private GameGUI gameGUI;
	
	/**
	 * Create the panel
	 * @param game the game being played
	 */
	public BidPositionGUI(GameGUI gameGUI, Game game){
		super("What position is the declarer?");
		this.game = game;		
		this.gameGUI = gameGUI;
		
		if(Game.isTestMode()){
			
			// the declarer should be south
			northButton.setEnabled(false);
			eastButton.setEnabled(false);
			westButton.setEnabled(false);
			//westButton.setEnabled(true); // if west is declarer , i.e. east is dummy
			
			southButton.setEnabled(true); // if south is declarer , i.e. north is dummy
			//southButton.setEnabled(false);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("North")){
			game.initPlayingPhase(Direction.NORTH);
			
		} else if (e.getActionCommand().equals("East")){
			game.initPlayingPhase(Direction.EAST);
			
		} else if (e.getActionCommand().equals("South")){
			game.initPlayingPhase(Direction.SOUTH);
			
		} else if (e.getActionCommand().equals("West")){
			game.initPlayingPhase(Direction.WEST);
			
		}
		
		gameGUI.changeFrame();
	}
	
}