package model;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Suit;

/**The Hand class represents a hand in the bridge game.
*
* @author Allison DeJordy
**/

public class Hand {
	
	//the maximum size of a hand
	private static final int MAX_CARDS = 13;
	
	/** the cards that are in this hand */
	protected Card[] cards = new Card[MAX_CARDS];
	
	//the number of cards currently in this hand
	private int numCards = 0;
	
	// the stack of cards in the hand to preserve the order in which they were dealt
	private Stack<Card> cardStack = new Stack<Card>() ;
	
	/**
	 * Adds a card to this hand.  Does nothing if the card is already in the hand.
	 * The hand should not be full.
	 * 
	 * @param c The card to be added.
	 */
	public void addCard(Card c){
		
		//if the hand does not already contain the given card
		if (!containsCard(c)){
			assert numCards < MAX_CARDS;
			
			cards[numCards] = c;
			numCards++;
			Arrays.sort(cards, 0, numCards, Card.getReverseRankComparator());
			cardStack.push(c) ;
			
		}
		
	}
	
	/**Retrieves the card at the given index.
	 * 
	 * @param i The index at which to find the card.
	 * @return The card at index i.
	 */
	public Card getCardAt(int i){
		
		if (i > 0 && i < cards.length){
			return cards[i];
		}
		assert false;
		return null;
		
	}
	
	/**Removes a card from this hand.
	 * 
	 * @param c The card to be removed.  Does nothing if the card is not in the hand.
	 */
	public void removeCard(Card c){
		
		if(c == null){
			
			return ;
		}
		
		for (int i = 0; i < numCards; i++){
			
			//remove the card when it is found
			if (cards[i] != null && cards[i].equals(c)){
				numCards--;
				cards[i] = cards[numCards];
				Arrays.sort(cards, 0, numCards, Card.getReverseRankComparator());
				return;
				
			}
		}
	}
	
	/**Clears the hand of all cards.*/
	public void clear() {
		
		for (int i = 0; i < cards.length; i++){
			cards[i] = null;
		}

		numCards = 0;

	}
	
	/**Checks whether a certain card is contained in this hand.
	 * 
	 * @param c The card to be checked for.
	 * @return Whether this card is contained in this hand.
	 */
	public boolean containsCard(Card c){
		
		//walk over all the cards
		for (int i = 0; i < numCards; i++){
			
			//return true if the card is found
			if(cards[i] != null && cards[i].equals(c)){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns the number of cards in a particular suit
	 * @param s the suit
	 * @return the number of cards in that suit
	 */
	public int getNumOfSuit(Suit s){
		
		int numOfSuit = 0;
		
		for (int i = 0; i < numCards; i++){
			if (cards[i].getSuit().equals(s)){
				numOfSuit++;
			}
		}
		
		return numOfSuit;
		
	}

	public int getNumCards() {
		return numCards;
	}
	
	/**
	 * @return true if the hand contains 13 cards
	 */
	public boolean isFull() {
		return numCards == MAX_CARDS;
	}

	public Iterator<Card> cards() {
		return new Iterator<Card>() {
			private int next = 0;

			@Override
			public boolean hasNext() {
				return next < numCards;
			}

			@Override
			public Card next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				next++;
				return cards[next-1];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}

	public Iterator<Card> cards(final Suit s) {
		return new Iterator<Card>() {
			private int next = 0;
			private int numReturned = 0;
			private int max = getNumOfSuit(s);

			@Override
			public boolean hasNext() {
				return numReturned < max;
			}

			@Override
			public Card next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				
				while (cards[next].getSuit() != s) {
					next++;
				}
				next++;
				numReturned++;
				return cards[next-1];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
	/**
	 * Returns true if this hand has no cards. Otherwise returns false.
	 * @return True if this hand has no cards. Otherwise returns false.
	 */
	public boolean isEmpty(){
		
		return (numCards == 0) ;
	}
	
	
	/**
	 * Removes and returns the card most recently added to the hand
	 * @return The card most recently added to the hand
	 */
	public Card removeRecentCard(){
		
		if(cardStack.isEmpty()){
			
			return null ;
			
		}
		
		return cardStack.pop() ;
	}
	
}