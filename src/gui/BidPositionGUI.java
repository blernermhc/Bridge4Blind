package gui;

import java.awt.Graphics;
import java.awt.event.ActionEvent;

import model.Direction;
import model.Game;
import model.GameState;

/**
 * The BidGUI allows the sighted players to specify which player made the
 * winning bid.
 * 
 * @author Allison DeJordy
 * @version March 12, 2015
 * 
 **/

public class BidPositionGUI extends DirectionGUI {
	
	private Game game;
	private GameGUI gameGUI;

	// for test mode only. For now, hand number can be 1 or 2.
	private int handNum = 1;

	/**
	 * Create the panel
	 * 
	 * @param game
	 *            the game being played
	 */
	public BidPositionGUI(GameGUI gameGUI, Game game) {
		super("What position is the declarer?");
		this.game = game;
		this.gameGUI = gameGUI;

		if (Game.isTestMode()) {

			enableAndDisableButtons();
		}
	}

	/**
	 * Depending on what hand it is, it only enables the button corresponding to
	 * the appropriate bid position
	 * 
	 * @throws AssertionError
	 */
	private void enableAndDisableButtons() throws AssertionError {
		if (handNum == 1) {

			// the declarer should be south for hand 1. North is dummy. East
			// is blind.
			northButton.setEnabled(false);
			eastButton.setEnabled(false);
			westButton.setEnabled(false);

			southButton.setEnabled(true);

		} else if (handNum == 2) {

			// the declarer should be west. East is blind and dummy
			southButton.setEnabled(false);
			westButton.setEnabled(true);

		} else {

			throw new AssertionError(
					"BidPositionGUI : There are only two hands");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("North")) {
			game.initPlayingPhase(Direction.NORTH);

		} else if (e.getActionCommand().equals("East")) {
			game.initPlayingPhase(Direction.EAST);

		} else if (e.getActionCommand().equals("South")) {
			game.initPlayingPhase(Direction.SOUTH);

		} else if (e.getActionCommand().equals("West")) {
			game.initPlayingPhase(Direction.WEST);

		}

		gameGUI.changeFrame();
	}

	/**
	 * 
	 * @param handNum
	 */
	public void setHandNum(int handNum) {
		this.handNum = handNum;
		
		enableAndDisableButtons();
	}

	@Override
	public void paintComponent(Graphics g){
		
		super.paintComponent(g);
		
		gameGUI.undoButtonSetEnabled(true);
		gameGUI.backButtonSetEnabled(true);
		
		gameGUI.getGame().setGameState(GameState.FIRSTCARD);
	}
}