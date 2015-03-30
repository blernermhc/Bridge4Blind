package model;

import java.util.Iterator;

public class Player {
	private Hand hand = new Hand();
	private int tricksWon;
	private boolean blind = false;
	private boolean dummy = false;

	public Hand getHand() {
		return hand;
	}

	public void newHand() {
		hand.clear();
		tricksWon = 0;
	}

	public void wonTrick() {
		tricksWon++;
	}

	public void removeCard(Card card) {
		hand.removeCard(card);
	}

	public void addCard(Card card) {
		hand.addCard(card);
	}

	public boolean hasFullHand() {
		return hand.isFull();
	}

	public int getTricksWon() {
		return tricksWon;
	}

	public Iterator<Card> cards() {
		return hand.cards();
	}

	public Iterator<Card> cards(Suit s) {
		return hand.cards(s);
	}

	public int getNumOfSuit(Suit s) {
		return hand.getNumOfSuit(s);
	}

	public void setDummy(boolean value) {
		dummy = value;
	}

	public boolean isDummy() {
		return dummy;
	}

	public void setBlind(boolean value) {
		blind = value;
	}

	public boolean isBlind() {
		return blind;
	}

	/**
	 * Checks that the card matches the suit led. If it does not, it makes sure
	 * that the player has no cards of the led suit.
	 * 
	 * @param card
	 *            the card being played
	 * @param ledSuit
	 *            the suit led in this trick
	 * @return true iff it is legal for the player to play this card
	 */
	public boolean isLegal(Card card, Suit ledSuit) {

		// See if the card is actually in the players' hand if the player
		// is the dummy or blind because these are the only players that the
		// application knows about

		if (dummy || blind) {

			// if the blind or dummy player is not supposed to have the card it
			// played, then the card is not legal.
			if (!hand.containsCard(card)) {

				return false;

			}
		}

		if (card.getSuit() == ledSuit) {
			return true;
		}

		if (getNumOfSuit(ledSuit) == 0) {
			return true;
		}

		// Player has cards of the led suit in hand, so playing
		// this card is not legal.
		return false;
	}

}
