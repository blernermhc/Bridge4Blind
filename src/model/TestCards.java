package model;


public class TestCards {
		
	
	// total size should be 13 + 52 + 13 = 78 ; (blind + all cards + dummy)
	//	private Card[] cards = new Card[13+52+13]; 
	
	// total size = hand1 + hand 2
	// hand1 = blind + all cards + dummy
	// hand2 = blind + all cards (no dummy)
	private Card[] cards = new Card[(13+52+13) + (13 + 52)]; 
		
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


	private void createHand2() {
		// Hand 2
		
		// Bid is No Trump - 3
		// West is declarer. Blind is east. So blind is dummy.
		
		// blind hand
		cards[78] = THREE_SPADES ;
		cards[79] = TWO_SPADES ;
		cards[80] = FIVE_HEARTS ;
		cards[81] = FOUR_HEARTS ;
		cards[82] = THREE_HEARTS ;
		cards[83] = ACE_DIAMONDS ;
		cards[84] = FOUR_DIAMONDS ;
		cards[85] = THREE_DIAMONDS ;
		cards[86] = TWO_DIAMONDS ;
		cards[87] = KING_CLUBS ;
		cards[88] = QUEEN_CLUBS ;
		cards[89] = JACK_CLUBS ;
		cards[90] = SEVEN_CLUBS ;
		
		// Trick 1. North goes first since west is declarer
		cards[91] = QUEEN_SPADES ; // north
		cards[92] = TWO_SPADES ; // east
		cards[93] = FOUR_SPADES ; // south
		cards[94] = KING_SPADES ; // west
		
		// West wins.
		
		// Trick 2. West goes first
		cards[95] = FOUR_CLUBS ; // west
		cards[96] = TWO_CLUBS ; // north
		cards[97] = JACK_CLUBS ; // east
		cards[98] = ACE_CLUBS ; // south
		
		// south wins
		
		// Trick 3. South goes first
		cards[99] = FIVE_SPADES ; // south
		cards[100] = ACE_SPADES ; // west
		cards[101] = EIGHT_SPADES ; // north
		cards[102] = THREE_SPADES ; // east
		
		// west wins
		
		// Trick 4. West goes first
		cards[103] = FIVE_CLUBS ; // west
		cards[104] = TEN_CLUBS ; // north
		cards[105] = QUEEN_CLUBS ; // east
		cards[106] = THREE_CLUBS ; // south
		
		// east wins
		
		// Trick 5. East goes first.
		cards[107] = KING_CLUBS ; // east
		cards[108] = EIGHT_CLUBS ; // south
		cards[109] = SIX_CLUBS ; // west
		cards[110] = NINE_DIAMONDS ; // north
		
		// east wins.
		
		// Trick 6. East goes first
		cards[111] = THREE_HEARTS ; // east
		cards[112] = EIGHT_HEARTS ; // south
		cards[113] = QUEEN_HEARTS ; // west
		cards[114] = SIX_HEARTS ; // north
		
		// west wins
		
		// Trick 7. West goes first
		cards[115] = KING_HEARTS ; // west
		cards[116] = SEVEN_HEARTS ; // north
		cards[117] = FOUR_HEARTS ; // east
		cards[118] = NINE_HEARTS ; // south
		
		// west wins
		
		// Trick 8. West goes first
		cards[119] = ACE_HEARTS ; // west
		cards[120] = JACK_HEARTS ; // north
		cards[121] = FIVE_HEARTS ; // east
		cards[122] = TEN_HEARTS ; // south
		
		// west wins
		
		// Trick 9. West goes first
		cards[123] = TWO_HEARTS ; // west
		cards[124] = NINE_SPADES ; // north
		cards[125] = TWO_DIAMONDS ; // east
		cards[126] = SIX_SPADES ; // south
		
		// west wins
		
		// Trick 10. West goes first
		cards[127] = FIVE_DIAMONDS ; // west
		cards[128] = JACK_DIAMONDS ; // north
		cards[129] = ACE_DIAMONDS ; // east
		cards[130] = TEN_DIAMONDS ; // south
		
		// East wins
		
		// Trick 11. East goes first
		cards[131] = SEVEN_CLUBS ; // east
		cards[132] = NINE_CLUBS ; // south
		cards[133] = SIX_DIAMONDS ; // west
		cards[134] = TEN_SPADES ; // north
		
		// south wins
		
		// Trick 12. South goes first
		cards[135] = SEVEN_SPADES ; // south
		cards[136] = SEVEN_DIAMONDS ; // west
		cards[137] = JACK_SPADES ; // north
		cards[138] = THREE_DIAMONDS ; // east
		
		// north wins
		
		//Trick 13. North goes first
		cards[139] = KING_DIAMONDS ; // north
		cards[140] = FOUR_DIAMONDS ; // east
		cards[141] = QUEEN_DIAMONDS ; // south
		cards[142] = EIGHT_DIAMONDS ; // west
		
		// north wins
	}


	private void createHand1() {
		// Hand 1
		
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
		
		// scan dummy cards. North is dummy
		cards[14] = TEN_SPADES ;
		cards[15] = SEVEN_SPADES ;
		cards[16] = FOUR_SPADES ;
		cards[17] = JACK_HEARTS ;
		cards[18] = TEN_HEARTS ;
		cards[19] = EIGHT_HEARTS ;
		cards[20] = SIX_HEARTS ;
		cards[21] = FIVE_DIAMONDS ;
		cards[22] = TWO_DIAMONDS ;
		cards[23] = ACE_CLUBS ;
		cards[24] = SIX_CLUBS ;
		cards[25] = THREE_CLUBS ;
		cards[26] = TWO_CLUBS ;
		
		// continue with trick 1
		cards[27] = FOUR_SPADES ; // north
		cards[28] = TWO_SPADES ; // east
		cards[29] =  JACK_SPADES ; // south
		
		//  WEST WINS
		
		// TRICK 2. West plays first.
		
		cards[30] =  ACE_SPADES ; // west
		cards[31] = SEVEN_SPADES ; // north
		cards[32] = THREE_SPADES ; // east
		cards[33] =  FIVE_HEARTS ; // south
		
		// SOUTH WINS
		
		// TRICK 3. SOUTH plays first
		cards[34] = KING_HEARTS ; // south
		cards[35] = TWO_HEARTS ; // west
		cards[36] = SIX_HEARTS ; // north
		cards[37] = FOUR_HEARTS ; // east
		
		// SOUTH WINS
		
		// TRICK 4. SOUTH plays first
		cards[38] = ACE_HEARTS ; // south
		cards[39] = THREE_HEARTS ;// west
		cards[40] = EIGHT_HEARTS ; // north
		cards[41] = SEVEN_HEARTS ; // east
		
		// SOUTH WINS
		
		// TRICK 5. SOUTH plays first
		cards[42] = KING_DIAMONDS ; // south
		cards[43] = SEVEN_DIAMONDS ; // west
		cards[44] = TWO_DIAMONDS ; // north
		cards[45] = THREE_DIAMONDS ; // east
		
		// South wins
		
		// TRICK 6. South plays first
		cards[46] = ACE_DIAMONDS ; // south
		cards[47] = JACK_DIAMONDS ; // west
		cards[48] = FIVE_DIAMONDS ; // north
		cards[49] = EIGHT_DIAMONDS ; // east
		
		// South wins
		
		// Trick 7. South plays first
		cards[50] = FOUR_DIAMONDS ; // south
		cards[51] = QUEEN_DIAMONDS ; // west
		cards[52] = TEN_HEARTS ; // north
		cards[53] = NINE_DIAMONDS ; // east
		// North wins
		
		// Trick 8. North plays first
		cards[54] = TEN_SPADES ; // north
		cards[55] = FIVE_SPADES ; // east
		cards[56] = NINE_HEARTS ; // south
		cards[57] = SIX_SPADES ; // west
		
		// South wins
		
		// Trick 9. South plays first
		cards[58] = SIX_DIAMONDS ; // south
		cards[59] = NINE_SPADES ; // west
		cards[60] = JACK_HEARTS ; // north
		cards[61] = TEN_DIAMONDS ; //east
		
		// north wins
		
		// Trick 10. North goes first
		cards[62] = ACE_CLUBS ; // north
		cards[63] = FIVE_CLUBS ; // east 
		cards[64] = FOUR_CLUBS ; // south
		cards[65] = EIGHT_CLUBS ; // west
				
		// north wins
		
		// Trick 11. North goes first
		cards[66] = SIX_CLUBS ; // north
		cards[67] = KING_CLUBS ; // east
		cards[68] = NINE_CLUBS ; // south
		cards[69] = TEN_CLUBS ; // west
		
		// east wins
		
		// Trick 12. East goes first
		cards[70] = JACK_CLUBS ; // east
		cards[71] = SEVEN_CLUBS ; // south
		cards[72] = QUEEN_CLUBS ; // west
		cards[73] = THREE_CLUBS ; // north
		
		// west wins
		
		// Trick 13. West goes first
		cards[74] = QUEEN_SPADES ; // west
		cards[75] = TWO_CLUBS ; // north
		cards[76] = EIGHT_SPADES ; // east
		cards[77] = QUEEN_HEARTS ; // south
		
		// south wins
	}
	

	public Card getNextCard() {
		
		index++ ;
		
		return cards[index] ;

	}
}
