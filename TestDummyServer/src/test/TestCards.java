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
		// Refer to http://www.rpbridge.net/1a00.htm
		
		// Bid is 4 Hearts.
		
		// first 13 are blind person's cards. Remember to make blind East
		cards[0] = EIGHT_SPADES ;
		cards[1] = FIVE_SPADES ;
		cards[2] = THREE_SPADES ;
		cards[3] = TWO_SPADES ;
		cards[4] = JACK_CLUBS ;
		cards[5] = FIVE_CLUBS ;
		cards[6] = KING_CLUBS ;
		cards[7] = TEN_DIAMONDS ;
		cards[8] = NINE_DIAMONDS ;
		cards[9] = THREE_DIAMONDS ;
		cards[10] = EIGHT_DIAMONDS ;
		cards[11] = FOUR_HEARTS ;
		cards[12] = SEVEN_HEARTS ;
		
		//South is declarer. North is dummy. 
		
		// TRICK 1. West plays first.
		
		cards[13] = KING_SPADES ; // west
		cards[14] = FOUR_SPADES ; // north
		cards[15] = TWO_SPADES ; // east
		cards[16] =  JACK_SPADES ; // south
		
		//  WEST WINS
		
		// TRICK 2. West plays first.
		
		cards[17] =  ACE_SPADES ; // west
		cards[18] = SEVEN_SPADES ; // north
		cards[19] = THREE_SPADES ; // east
		cards[20] =  FIVE_HEARTS ; // south
		
		// SOUTH WINS
		
		// TRICK 3. SOUTH plays first
		cards[21] = KING_HEARTS ; // south
		cards[22] = TWO_HEARTS ; // west
		cards[23] = SIX_HEARTS ; // north
		cards[24] = FOUR_HEARTS ; // east
		
		// SOUTH WINS
		
		// TRICK 4. SOUTH plays first
		cards[25] = ACE_HEARTS ; // south
		cards[26] = THREE_HEARTS ;// west
		cards[27] = EIGHT_HEARTS ; // north
		cards[28] = SEVEN_HEARTS ; // east
		
		// SOUTH WINS
		
		// TRICK 5. SOUTH plays first
		cards[29] = KING_DIAMONDS ; // south
		cards[30] = SEVEN_DIAMONDS ; // west
		cards[31] = TWO_DIAMONDS ; // north
		cards[32] = THREE_DIAMONDS ; // east
		
		// South wins
		
		// TRICK 6. South plays first
		cards[33] = ACE_DIAMONDS ; // south
		cards[34] = JACK_DIAMONDS ; // west
		cards[35] = FIVE_DIAMONDS ; // north
		cards[36] = EIGHT_DIAMONDS ; // east
		
		// South wins
		
		// Trick 7. South plays first
		cards[37] = FOUR_DIAMONDS ; // south
		cards[38] = QUEEN_DIAMONDS ; // west
		cards[39] = TEN_HEARTS ; // north
		cards[40] = NINE_HEARTS ; // east
		// North wins
		
		// Trick 8. North plays first
		cards[41] = TEN_SPADES ; // north
		cards[42] = FIVE_SPADES ; // east
		cards[43] = NINE_HEARTS ; // south
		cards[44] = SIX_HEARTS ; // west
		
		// South wins
		
		// Trick 9. South plays first
		cards[45] = SIX_DIAMONDS ; // south
		cards[46] = NINE_SPADES ; // west
		cards[47] = JACK_HEARTS ; // north
		cards[48] = TEN_HEARTS ; //east
		
		// north wins
		
		// Trick 10. North goes first
		cards[49] = ACE_CLUBS ; // north
		cards[50] = FIVE_CLUBS ; // east 
		cards[51] = FOUR_CLUBS ; // south
		cards[52] = EIGHT_CLUBS ; // west
				
		// north wins
		
		// Trick 11. North goes first
	}
	

	public Card getNextCard() {
		
		/*index++ ;
		
		return cards[index] ;*/
		
		Card card = ACE_SPADES ;
		
		System.out.println("Returning " + card.toString());
		
		return card;
	}
}
