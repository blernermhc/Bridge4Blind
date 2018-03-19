package model;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Keeps track of the cards that are played into a trick and the suit that was led.
 * 
 * @author Barbara Lerner
 * @version May 18, 2012
 *
 */
public class Trick {
	// the suit of the current trick
	private Suit ledSuit;
	
	// the cards in the current trick
	private Card[] cards = new Card[Game.NUM_PLAYERS];
	
	private int numCards = 0 ;

	public Trick() {}
	/**
	 * Checks if the trick is over.
	 * 
	 * @return true if every player has played a card into the trick.
	 */
	public boolean isOver() {

		// walk over the current trick array
		for (int i = 0; i < cards.length; i++) {

			// if any of the spots is empty, return false
			if (cards[i] == null) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @return true if no cards have been played into the trick yet
	 */
	public boolean isEmpty(){

		for (int i = 0; i < cards.length; i++){
			if (cards[i] != null){
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Add a card to the trick.  The player should not have played a card yet.
	 * @param c the card to play
	 * @param position the position of the player
	 */
	public void add(Card c, int position) {
		
		System.out.println("Trick add()");
		
		assert (cards[position] == null) || (cards[position].equals(c));
		cards[position] = c;
		numCards++ ;
		
		System.out.println("numCards " + numCards);
		
		assert numCards <= Game.NUM_PLAYERS ;
	}

	/**
	 * Return the position of the player who played the high card.  
	 * @param suitCards the cards of the suit we are looking for.  All of the cards 
	 * 	must be of the same suit.  suitCards should not be empty.
	 * @return the high card
	 */
	public int getHighCardPlayer(ArrayList<Integer> suitCards) {
		assert !suitCards.isEmpty();
		Iterator<Integer> posIter = suitCards.iterator();
		
		int winner = posIter.next();
		Card highCard = cards[winner];
		Card currentCard;

		while(posIter.hasNext()) {
			int nextPos = posIter.next();
			currentCard = cards[nextPos];
			if (currentCard.compareTo(highCard) > 0) {
				highCard = cards[nextPos];
				winner = nextPos;
			}
		}
		return winner;
	}

	/**
	 * Return all the cards in the trick that match the given suit
	 * @param s the suit to match
	 * @return all the cards in the trick that match the given suit
	 */
	public ArrayList<Integer> getCardsOfSuit(Suit s) {

		ArrayList<Integer> suitCards = new ArrayList<Integer>();
		for (int i = 0; i < cards.length; i++) {
			if (cards[i].getSuit() == s) {
				suitCards.add(i);
			}
		}
		return suitCards;
	}

	/**
	 * Determine the position of the player who won the trick
	 * @param trump the trump suit
	 * @return the position of the player who won the trick
	 */
	public int determineWinner(Suit trump) {
		if (trump != Suit.NOTRUMP) {
			ArrayList<Integer> trumpCards = getCardsOfSuit(trump);

			// if any trump were played, the player who played the highest one wins
			if (trumpCards.size() > 0) {
				return getHighCardPlayer(trumpCards);
			}
		}

		// No trump were played.  The winner is the player of the highest card in 
		// the suit that was led.
		ArrayList<Integer> suitCards = getCardsOfSuit(ledSuit);
		return getHighCardPlayer(suitCards);
	}

	/**
	 * @return the suit of the first card played in the trick
	 */
	public Suit getLedSuit() {
		return ledSuit;
	}

	public void setLedSuit(Suit suit) {
		ledSuit = suit;
	}

	/**
	 * Get the card played by the player at a particular position
	 * @param position the player position
	 * @return the card played.  Returns null if the player has not yet played a card.
	 */
	public Card getCard(int position) {
		return cards[position];
	}

	/**
	 * Removes a card from the trick
	 * @param pos the position of the player whose card is removed.
	 */
	public void clearCard(int pos) {
		
		System.out.println("Trick clearCard()");
		
		cards[pos] = null;
		
		numCards-- ;
		
		System.out.println("numCards " + numCards);
		
		assert numCards >= 0 ;
	}

	/**
	 * @return a string representation of the trick
	 */
	@Override
	public String toString() {
		String s = "";

		for (int i = 0; i < cards.length; i++){
			if(cards[i] != null){
				if (i > 0) {
					s += ", ";
				}
				
				s += cards[i].toString();
			}
		}
		return s;
	}
	
	public int getTrickSize(){
		
		return numCards ;
	}
}
