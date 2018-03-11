package model;

import java.util.Comparator;

/**The Card class represents a single card in the bridge game.*/

public class Card implements Comparable<Card> {
	
	private static final String SOUND_FOLDER = "/sounds/cards/";

	/**The rank of the card.*/
	private Rank rank;
	
	/**The suit of the card.*/
	private Suit suit;
	
	/**The file path of the card's corresponding sound.*/
	private String sound;
	
	
	/**Constructor; creates a Card with the given rank and suit
	 * 
	 * @param r The rank of the card.
	 * @param s The suit of the card.
	 */
	public Card (Rank r, Suit s){
		
		rank = r;
		suit = s;
		
		//build the name of the file containing the sound
		sound = SOUND_FOLDER + rank.toString() + suit.toString() + ".WAV";
	}

	/**Checks if this card is equal to the card passed as a parameter.
	 * 
	 * @param c The card that will be checked.  
	 * @return 0 if the cards are equal; -1 if this card is lesser than c;
	 * 	1 if this card is greater than c.  Returns -1 if c is null to allow the
	 *  sorting of non-full hands.
	 */
	@Override
	public int compareTo(Card c){
		if (c == null){
			return -1;
		}

		if (this.suit != c.suit){ 
			return this.suit.compareTo(c.suit);
		}
		
		return this.rank.compareTo(c.rank);
	}
	
	/**
	 * Return a comparator that sorts by suit in the normal way, but
	 * sorts rank from high to low.
	 * @return a comparator
	 */
	public static Comparator<Card> getReverseRankComparator() {
		return new Comparator<Card>() {

			@Override
			public int compare(Card card1, Card card2) {
				if (card1 == null){
					return -1;
				}
				
				if (card2 == null) {
					return 1;
				}

				if (card1.suit != card2.suit){ 
					return card1.suit.compareTo(card2.suit);
				}
				
				return card2.rank.compareTo(card1.rank);
			}
			
		};
	}

	/**Returns the card's rank.
	 * 
	 * @return The rank of the card.
	 */
	public Rank getRank() {
		return rank;
	}
	
	/**Returns the card's suit.
	 * 
	 * @return The suit of the card.
	 */
	public Suit getSuit() {
		return suit;
	}

	/**Returns the file path to the card's sound.
	 * 
	 * @return The file path to the sound associated with this card.
	 */
	public String getSound() {
		return sound;
		
	}

	/** @return a text representation of the card */
	@Override
	public String toString(){
		return rank.toString() + suit.toString();
	}
	
	
}