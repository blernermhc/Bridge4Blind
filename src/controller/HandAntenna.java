package controller;

import model.Card;
import model.Direction;
import model.Game;

/**
 * Listens to an antenna for a hand
 * 
 * @author Barbara Lerner
 * @version Jul 28, 2012
 *
 */
public class HandAntenna extends CardIdentifier {
	//the direction of this hand in the game
	private Direction direction;
	
	/**
	 * Create a listener to a hand antenna
	 * @param dir the position of the player
	 * @param g the game being played
	 */
	public HandAntenna (Direction dir, Game g) {
		super (g);
		this.direction = dir;
	}

	/**
	 * Tells the game that a card was found on a hand antenna
	 * @param c the card found
	 */
	@Override
	protected void tellGameAboutCard(Card c) {
		game.cardFound(direction, c);
	}
	
}
