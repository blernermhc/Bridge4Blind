package model;

/**
 * The cards for the first two hands are hardcoded. This class provides the DummyHandler with the next card.
 * @author Humaira Orchee
 * @version March 12, 2015
 *
 */
public class TestCards {
		
	
	// total size = hand1 + hand 2
	// hand1 = blind + all cards + dummy + wrong cards
	// hand2 = blind + all cards (no dummy) + wrong cards
	private Card[] cards = new Card[(13 + 52 + 13 + 8) + (13 + 52 + 3)]; 
		
	private int index = -1 ;

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
		
		createHand1();
		
		createHand2();
				

	}


	/**
	 * Creates Hand 2. Bid is 3 No Trump. West is declarer. East is dummy. East is also blind. 
	 */
	private void createHand2() {
		// Hand 2
		
		// Bid is No Trump - 3
		// West is declarer. Blind is east. So blind is dummy.
		
		// blind hand
		cards[86] = THREE_SPADES ;
		cards[87] = TWO_SPADES ;
		cards[88] = FIVE_HEARTS ;
		cards[89] = FOUR_HEARTS ;
		cards[90] = THREE_HEARTS ;
		cards[91] = ACE_DIAMONDS ;
		cards[92] = FOUR_DIAMONDS ;
		cards[93] = THREE_DIAMONDS ;
		cards[94] = TWO_DIAMONDS ;
		cards[95] = TWO_DIAMONDS ; // test - blind hand is scanned same card as a previous one
		cards[96] = FOUR_HEARTS ; // test - blind hand is scanned same card as a previous one
		cards[97] = KING_CLUBS ;
		cards[98] = QUEEN_CLUBS ;
		cards[99] = JACK_CLUBS ;
		cards[100] = SEVEN_CLUBS ;
		
		// Trick 1. North goes first since west is declarer
		cards[101] = QUEEN_SPADES ; // north
		cards[102] = TWO_SPADES ; // east
		cards[103] = FOUR_SPADES ; // south
		cards[104] = KING_SPADES ; // west
		
		// West wins.
		
		// Trick 2. West goes first
		cards[105] = FOUR_CLUBS ; // west
		cards[106] = TWO_CLUBS ; // north
		cards[107] = ACE_CLUBS ; // east. test - to check that blind player cannot play a card it does not have
		cards[108] = JACK_CLUBS ; // east
		cards[109] = ACE_CLUBS ; // south
		
		// south wins
		
		// Trick 3. South goes first
		cards[110] = FIVE_SPADES ; // south
		cards[111] = ACE_SPADES ; // west
		cards[112] = EIGHT_SPADES ; // north
		cards[113] = THREE_SPADES ; // east
		
		// west wins
		
		// Trick 4. West goes first
		cards[114] = FIVE_CLUBS ; // west
		cards[115] = TEN_CLUBS ; // north
		cards[116] = QUEEN_CLUBS ; // east
		cards[117] = THREE_CLUBS ; // south
		
		// east wins
		
		// Trick 5. East goes first.
		cards[118] = KING_CLUBS ; // east
		cards[119] = EIGHT_CLUBS ; // south
		cards[120] = SIX_CLUBS ; // west
		cards[121] = NINE_DIAMONDS ; // north
		
		// east wins.
		
		// Trick 6. East goes first
		cards[122] = THREE_HEARTS ; // east
		cards[123] = EIGHT_HEARTS ; // south
		cards[124] = QUEEN_HEARTS ; // west
		cards[125] = SIX_HEARTS ; // north
		
		// west wins
		
		// Trick 7. West goes first
		cards[126] = KING_HEARTS ; // west
		cards[127] = SEVEN_HEARTS ; // north
		cards[128] = FOUR_HEARTS ; // east
		cards[129] = NINE_HEARTS ; // south
		
		// west wins
		
		// Trick 8. West goes first
		cards[130] = ACE_HEARTS ; // west
		cards[131] = JACK_HEARTS ; // north
		cards[132] = FIVE_HEARTS ; // east
		cards[133] = TEN_HEARTS ; // south
		
		// west wins
		
		// Trick 9. West goes first
		cards[134] = TWO_HEARTS ; // west
		cards[135] = NINE_SPADES ; // north
		cards[136] = TWO_DIAMONDS ; // east
		cards[137] = SIX_SPADES ; // south
		
		// west wins
		
		// Trick 10. West goes first
		cards[138] = FIVE_DIAMONDS ; // west
		cards[139] = JACK_DIAMONDS ; // north
		cards[140] = ACE_DIAMONDS ; // east
		cards[141] = TEN_DIAMONDS ; // south
		
		// East wins
		
		// Trick 11. East goes first
		cards[142] = SEVEN_CLUBS ; // east
		cards[143] = NINE_CLUBS ; // south
		cards[144] = SIX_DIAMONDS ; // west
		cards[145] = TEN_SPADES ; // north
		
		// south wins
		
		// Trick 12. South goes first
		cards[146] = SEVEN_SPADES ; // south
		cards[147] = SEVEN_DIAMONDS ; // west
		cards[148] = JACK_SPADES ; // north
		cards[149] = THREE_DIAMONDS ; // east
		
		// north wins
		
		//Trick 13. North goes first
		cards[150] = KING_DIAMONDS ; // north
		cards[151] = FOUR_DIAMONDS ; // east
		cards[152] = QUEEN_DIAMONDS ; // south
		cards[153] = EIGHT_DIAMONDS ; // west
		
		// north wins
	}


	/**
	 * Creates Hand 1. Bid is 4 Hearts. Blind is east. South is declarer. North is dummy.
	 */
	private void createHand1() {
		// Hand 1
		
		// Bid is 4 Hearts.
		
		// first 13 are blind person's cards. Remember to make blind East
		cards[0] = EIGHT_SPADES ;
		cards[1] = FIVE_SPADES ;
		cards[2] = FIVE_SPADES ; // test - blind hand is scanned same card as previous
		cards[3] = THREE_SPADES ;
		cards[4] = TWO_SPADES ;
		cards[5] = JACK_CLUBS ;
		cards[6] = FIVE_CLUBS ;
		cards[7] = KING_CLUBS ;
		cards[8] = TEN_DIAMONDS ;
		cards[9] = NINE_DIAMONDS ;
		cards[10] = THREE_DIAMONDS ;
		cards[11] = FIVE_CLUBS ; // test - blind hand is scanned same card as a previous one
		cards[12] = EIGHT_DIAMONDS ;
		cards[13] = FOUR_HEARTS ;
		cards[14] = SEVEN_HEARTS ;
		
		//South is declarer. North is dummy. 
		
		// TRICK 1. West plays first.
		cards[15] = KING_SPADES ; // west
		
		// scan dummy cards. North is dummy
		cards[16] = KING_SPADES ; // test - dummy player is scanned the first card to be played
		cards[17] = TEN_SPADES ;
		cards[18] = SEVEN_SPADES ;
		cards[19] = FOUR_SPADES ;
		cards[20] = JACK_HEARTS ;
		cards[21] = JACK_HEARTS ; // test - dummy hand is scanned same card as previous
		cards[22] = TEN_HEARTS ;
		cards[23] = EIGHT_HEARTS ;
		cards[24] = SIX_HEARTS ;
		cards[25] = FIVE_DIAMONDS ;
		cards[26] = TWO_DIAMONDS ;
		cards[27] = ACE_CLUBS ;
		cards[28] = SIX_CLUBS ;
		cards[29] = THREE_CLUBS ;
		cards[30] = TEN_SPADES ; // test - dummy hand is scanned same card as a previous one
		cards[31] = FIVE_CLUBS ; // test - one of the blind player's card is scanned to the dummy hand
		cards[32] = TWO_CLUBS ;
	
		// continue with trick 1
		cards[33] = FOUR_SPADES ; // north
		cards[34] = TWO_SPADES ; // east
		cards[35] =  JACK_SPADES ; // south
	
		//  WEST WINS 
		
		// TRICK 2. West plays first.
		
		cards[36] =  ACE_SPADES ; // west
		cards[37] = SEVEN_SPADES ; // north
		cards[38] = SIX_SPADES ; // east. test - To check that the blind player cannot play a card it does not have
		cards[39] = THREE_SPADES ; // east
		cards[40] =  FIVE_HEARTS ; // south
		
		// SOUTH WINS
		
		// TRICK 3. SOUTH plays first
		cards[41] = KING_HEARTS ; // south
		cards[42] = TWO_HEARTS ; // west
		cards[43] = ACE_HEARTS ; // north. test - To check that the dummy player cannot play a card it does not have
		cards[44] = SIX_HEARTS ; // north
		cards[45] = FOUR_HEARTS ; // east
	
		// SOUTH WINS
		
		// TRICK 4. SOUTH plays first
		cards[46] = ACE_HEARTS ; // south
		cards[47] = THREE_HEARTS ;// west
		cards[48] = EIGHT_HEARTS ; // north
		cards[49] = SEVEN_HEARTS ; // east
		
		// SOUTH WINS
		
		// TRICK 5. SOUTH plays first
		cards[50] = KING_DIAMONDS ; // south
		cards[51] = SEVEN_DIAMONDS ; // west
		cards[52] = TWO_DIAMONDS ; // north
		cards[53] = THREE_DIAMONDS ; // east
		
		// South wins
		
		// TRICK 6. South plays first
		cards[54] = ACE_DIAMONDS ; // south
		cards[55] = JACK_DIAMONDS ; // west
		cards[56] = FIVE_DIAMONDS ; // north
		cards[57] = EIGHT_DIAMONDS ; // east
		
		// South wins
		
		// Trick 7. South plays first
		cards[58] = FOUR_DIAMONDS ; // south
		cards[59] = QUEEN_DIAMONDS ; // west
		cards[60] = TEN_HEARTS ; // north
		cards[61] = NINE_DIAMONDS ; // east
		// North wins
		
		// Trick 8. North plays first
		cards[62] = TEN_SPADES ; // north
		cards[63] = FIVE_SPADES ; // east
		cards[64] = NINE_HEARTS ; // south
		cards[65] = SIX_SPADES ; // west
		
		// South wins
		
		// Trick 9. South plays first
		cards[66] = SIX_DIAMONDS ; // south
		cards[67] = NINE_SPADES ; // west
		cards[68] = JACK_HEARTS ; // north
		cards[69] = TEN_DIAMONDS ; //east
	
		// north wins
		
		// Trick 10. North goes first
		cards[70] = ACE_CLUBS ; // north
		cards[71] = FIVE_CLUBS ; // east 
		cards[72] = FOUR_CLUBS ; // south
		cards[73] = EIGHT_CLUBS ; // west
			
		// north wins
		
		// Trick 11. North goes first
		cards[74] = SIX_CLUBS ; // north
		cards[75] = KING_CLUBS ; // east
		cards[76] = NINE_CLUBS ; // south
		cards[77] = TEN_CLUBS ; // west
	
		// east wins
		
		// Trick 12. East goes first
		cards[78] = JACK_CLUBS ; // east
		cards[79] = SEVEN_CLUBS ; // south
		cards[80] = QUEEN_CLUBS ; // west
		cards[81] = THREE_CLUBS ; // north
	
		// west wins
		
		// Trick 13. West goes first
		cards[82] = QUEEN_SPADES ; // west
		cards[83] = TWO_CLUBS ; // north
		cards[84] = EIGHT_SPADES ; // east
		cards[85] = QUEEN_HEARTS ; // south
		
		// south wins
	}
	

	public Card getNextCard() {
		
		index++ ;
		
		return cards[index] ;

	}
	
	public void undo(){
		
		index-- ;
		
		System.out.println("TestCards undo(). index is " + index);
		
		assert index >= -1 ;
	}
}
