package controller;

import test.TestCards;
import model.Card;
import model.Direction;
import model.Game;

public class DummyHandAntenna extends CardIdentifier {
		
	//the direction of this hand in the game
		private Direction direction;
		
		/**
		 * Create a listener to a hand antenna
		 * @param dir the position of the player
		 * @param g the game being played
		 */
		public DummyHandAntenna (Direction dir, Game g) {
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
