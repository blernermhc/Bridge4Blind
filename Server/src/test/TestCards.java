package test;

import model.Card;
import model.Rank;
import model.Suit;

public class TestCards {
		
	
	// total size should be 13 + 52
	private Card[] cards = new Card[13+52];
	
		
	private int index = 0 ;

	// spades
	public static final Card ACE_SPADES = new Card (Rank.ACE, Suit.SPADES);
	public static final Card TWO_SPADES = new Card (Rank.DEUCE, Suit.SPADES);
	public static final Card THREE_SPADES = new Card (Rank.THREE, Suit.SPADES);
	public static final Card FOUR_SPADES = new Card (Rank.FOUR, Suit.SPADES);
	public static final Card FIVE_SPADES = new Card (Rank.FIVE, Suit.SPADES);
	public static final Card SIX_SPADES = new Card (Rank.SIX, Suit.SPADES);
	public static final Card SEVEN_SPADES = new Card (Rank.SEVEN, Suit.SPADES);
	public static final Card EIGHT_SPADES = new Card (Rank.EIGHT, Suit.SPADES);
	public static final Card NINE_SPADES = new Card (Rank.NINE, Suit.SPADES);
	public static final Card TEN_SPADES = new Card (Rank.TEN, Suit.SPADES);
	public static final Card JACK_SPADES = new Card (Rank.JACK, Suit.SPADES);
	public static final Card QUEEN_SPADES = new Card (Rank.QUEEN, Suit.SPADES);
	public static final Card KING_SPADES = new Card (Rank.KING, Suit.SPADES);

	// clubs
	public static final Card ACE_CLUBS = new Card (Rank.ACE, Suit.CLUBS);
	public static final Card TWO_CLUBS = new Card (Rank.DEUCE, Suit.CLUBS);
	public static final Card THREE_CLUBS = new Card (Rank.THREE, Suit.CLUBS);
	public static final Card FOUR_CLUBS = new Card (Rank.FOUR, Suit.CLUBS);
	public static final Card FIVE_CLUBS = new Card (Rank.FIVE, Suit.CLUBS);
	public static final Card SIX_CLUBS = new Card (Rank.SIX, Suit.CLUBS);
	public static final Card SEVEN_CLUBS = new Card (Rank.SEVEN, Suit.CLUBS);
	public static final Card EIGHT_CLUBS = new Card (Rank.EIGHT, Suit.CLUBS);
	public static final Card NINE_CLUBS = new Card (Rank.NINE, Suit.CLUBS);
	public static final Card TEN_CLUBS = new Card (Rank.TEN, Suit.CLUBS);
	public static final Card JACK_CLUBS = new Card (Rank.JACK, Suit.CLUBS);
	public static final Card QUEEN_CLUBS = new Card (Rank.QUEEN, Suit.CLUBS);
	public static final Card KING_CLUBS = new Card (Rank.KING, Suit.CLUBS);
	
	// hearts
	public static final Card ACE_HEARTS = new Card (Rank.ACE, Suit.HEARTS);
	public static final Card TWO_HEARTS = new Card (Rank.DEUCE, Suit.HEARTS);
	public static final Card THREE_HEARTS = new Card (Rank.THREE, Suit.HEARTS);
	public static final Card FOUR_HEARTS = new Card (Rank.FOUR, Suit.HEARTS);
	public static final Card FIVE_HEARTS = new Card (Rank.FIVE, Suit.HEARTS);
	public static final Card SIX_HEARTS = new Card (Rank.SIX, Suit.HEARTS);
	public static final Card SEVEN_HEARTS = new Card (Rank.SEVEN, Suit.HEARTS);
	public static final Card EIGHT_HEARTS = new Card (Rank.EIGHT, Suit.HEARTS);
	public static final Card NINE_HEARTS = new Card (Rank.NINE, Suit.HEARTS);
	public static final Card TEN_HEARTS = new Card (Rank.TEN, Suit.HEARTS);
	public static final Card JACK_HEARTS = new Card (Rank.JACK, Suit.HEARTS);
	public static final Card QUEEN_HEARTS = new Card (Rank.QUEEN, Suit.HEARTS);
	public static final Card KING_HEARTS = new Card (Rank.KING, Suit.HEARTS);
	
	
	// diamonds
	public static final Card ACE_DIAMONDS = new Card (Rank.ACE, Suit.DIAMONDS);
	public static final Card TWO_DIAMONDS = new Card (Rank.DEUCE, Suit.DIAMONDS);
	public static final Card THREE_DIAMONDS = new Card (Rank.THREE, Suit.DIAMONDS);
	public static final Card FOUR_DIAMONDS = new Card (Rank.FOUR, Suit.DIAMONDS);
	public static final Card FIVE_DIAMONDS = new Card (Rank.FIVE, Suit.DIAMONDS);
	public static final Card SIX_DIAMONDS = new Card (Rank.SIX, Suit.DIAMONDS);
	public static final Card SEVEN_DIAMONDS = new Card (Rank.SEVEN, Suit.DIAMONDS);
	public static final Card EIGHT_DIAMONDS = new Card (Rank.EIGHT, Suit.DIAMONDS);
	public static final Card NINE_DIAMONDS = new Card (Rank.NINE, Suit.DIAMONDS);
	public static final Card TEN_DIAMONDS = new Card (Rank.TEN, Suit.DIAMONDS);
	public static final Card JACK_DIAMONDS = new Card (Rank.JACK, Suit.DIAMONDS);
	public static final Card QUEEN_DIAMONDS = new Card (Rank.QUEEN, Suit.DIAMONDS);
	public static final Card KING_DIAMONDS = new Card (Rank.KING, Suit.DIAMONDS);
	
	/**
	 * 
	 */
	public TestCards(){
		
		// first 13 are blind pperson's cards
		cards[0] = ACE_SPADES ;
		cards[1] = TWO_SPADES ;
		cards[2] = THREE_SPADES ;
		cards[3] = FOUR_CLUBS ;
		cards[4] = FIVE_CLUBS ;
		cards[5] = SIX_CLUBS ;
		cards[6] = SEVEN_DIAMONDS ;
		cards[7] = EIGHT_DIAMONDS ;
		cards[8] = NINE_DIAMONDS ;
		cards[9] = TEN_HEARTS ;
		cards[10] = JACK_HEARTS ;
		cards[11] = QUEEN_HEARTS ;
		cards[12] = KING_HEARTS ;
		
		// blind player is not dummy or first player.
		// trick 1
		cards[13] = ACE_CLUBS ; 
				
	}
	

	public Card getNextCard() {
		
		/*index++ ;
		
		return cards[index] ;*/
		
		Card card = ACE_SPADES ;
		
		System.out.println("Returning " + card.toString());
		
		return card;
	}
}
