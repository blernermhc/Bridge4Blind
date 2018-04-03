package lerner.blindBridge.model;

import java.util.Comparator;

import org.apache.log4j.Category;

/***********************************************************************
 * Represents a playing card
 ***********************************************************************/
public class Card implements Comparable<Card>
{
	
	/**
	 * Used to collect logging output for this class
	 */
	static Category s_cat = Category.getInstance(Card.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	private static final String SOUND_FOLDER = "/sounds/cards/";

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------

	/** The rank of the card.*/
	private Rank m_rank;
	
	/** The suit of the card.*/
	private Suit m_suit;
	
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	/** the name of the file containing the audio announcement of this card */
	private String m_sound;
	
	
	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Create a card object from a suit and a number
	 * @param p_rank			the card's rank
	 * @param p_suit			the card's suit
	 ***********************************************************************/
	public Card (Rank p_rank, Suit p_suit)
	{
		m_rank = p_rank;
		m_suit = p_suit;
		
		//build the name of the file containing the sound
		m_sound = SOUND_FOLDER + m_rank.toString() + m_suit.toString() + ".WAV";
	}

	/***********************************************************************
	 * Create a card object from a number from 0-51.
	 * 0-12 is Clubs 2-Ace
	 * 13-25 is Diamonds 2-Ace
	 * 26-38 is Hearts 2-Ace
	 * 39-51 is Spaces 2-Ace
	 * @param p_cardDecimal the decimal value
	 * @throws IllegalArgumentException if the value is not between 0 and 51
	 ***********************************************************************/
	public Card (int p_cardDecimal)
		throws IllegalArgumentException
	{
		if (p_cardDecimal < 13)
		{
			m_suit = Suit.CLUBS;
			m_rank = Rank.values()[p_cardDecimal];
		}
		else if (p_cardDecimal < 26)
		{
			m_suit = Suit.DIAMONDS;
			m_rank = Rank.values()[p_cardDecimal - 13];
		}
		else if (p_cardDecimal < 39)
		{
			m_suit = Suit.HEARTS;
			m_rank = Rank.values()[p_cardDecimal - 26];
		}
		else if (p_cardDecimal < 52)
		{
			m_suit = Suit.SPADES;
			m_rank = Rank.values()[p_cardDecimal - 39];
		}
		else
		{
			throw new IllegalArgumentException("Card: number out of bounds: " + p_cardDecimal);
		}
		
		//build the name of the file containing the sound
		m_sound = SOUND_FOLDER + m_rank.toString() + m_suit.toString() + ".WAV";
	}
	
	/***********************************************************************
	 * Create a card object from an abbreviation (e.g., 9C, TD, QH, AS) 
	 * @param p_abbreviation the abbreviation to parse (converted to upper case)
	 * @throws IllegalArgumentException if the argument is not a valid abbreviation
	 ***********************************************************************/
	public Card (String p_abbreviation)
		throws IllegalArgumentException
	{
		if (p_abbreviation == null || p_abbreviation.length() < 2)
			throw new IllegalArgumentException("Card: invalid abbrev: " + p_abbreviation);

		String abbreviation = p_abbreviation.toUpperCase();
		
		char rankAbbrev;
		char suitAbbrev;
		switch (abbreviation.length())
		{
			case 2:
				rankAbbrev = abbreviation.charAt(0);
				suitAbbrev = abbreviation.charAt(1);
				break;

			case 3:
				if (! abbreviation.startsWith("10"))
					throw new IllegalArgumentException("Card: invalid abbrev (3): " + abbreviation);
				rankAbbrev = 'T';
				suitAbbrev = abbreviation.charAt(2);
				break;

			default:
				throw new IllegalArgumentException("Card: invalid abbrev length: " + abbreviation);
		}
		
		switch (rankAbbrev)
		{
			case '2':	m_rank = Rank.TWO; break; 
			case '3':	m_rank = Rank.THREE; break; 
			case '4':	m_rank = Rank.FOUR; break; 
			case '5':	m_rank = Rank.FIVE; break; 
			case '6':	m_rank = Rank.SIX; break; 
			case '7':	m_rank = Rank.SEVEN; break; 
			case '8':	m_rank = Rank.EIGHT; break; 
			case '9':	m_rank = Rank.NINE; break; 
			case 'T':	m_rank = Rank.TEN; break; 
			case 'J':	m_rank = Rank.JACK; break; 
			case 'Q':	m_rank = Rank.QUEEN; break; 
			case 'K':	m_rank = Rank.KING; break; 
			case 'A':	m_rank = Rank.ACE; break;
			default:
				throw new IllegalArgumentException("Card: invalid number: " + rankAbbrev);
		}

		switch (suitAbbrev)
		{
			case 'C':	m_suit = Suit.CLUBS; break; 
			case 'D':	m_suit = Suit.DIAMONDS; break; 
			case 'H':	m_suit = Suit.HEARTS; break; 
			case 'S':	m_suit = Suit.SPADES; break; 
			default:
				throw new IllegalArgumentException("Card: invalid suit: " + suitAbbrev);
		}
		
		//build the name of the file containing the sound
		m_sound = SOUND_FOLDER + m_rank.toString() + m_suit.toString() + ".WAV";
	}

	//--------------------------------------------------
	// COMPARISON METHODS
	//--------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals ( Object p_obj )
	{
		if (! (p_obj instanceof Card)) return false;
		Card card = (Card) p_obj;

		if (m_suit == null && card.getSuit() != null) return false;
		if (m_suit != null && card.getSuit() == null) return false;
		if (m_suit != null && card.getSuit() != null && m_suit.ordinal() != card.getSuit().ordinal()) return false;
		
		if (m_rank == null && card.getRank() != null) return false;
		if (m_rank != null && card.getRank() == null) return false;
		if (m_rank != null && card.getRank() != null && m_rank.ordinal() != card.getRank().ordinal()) return false;
		
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		int result = 17;
		result = 37 * result + (m_suit == null ? 0 : m_suit.ordinal());
		result = 37 * result + (m_rank == null ? 0 : m_rank.ordinal());

		return result;
	}

	/***********************************************************************
	 * Tests if given card is higher than this card.
	 * Orders suits from low to high: Clubs, Diamonds, Hearts, Spades.
	 * Treats Ace as higher than King.
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @param p_card the card to compare with this card (must not be null)
	 * @return -1 if p_card is lower; 0 if they are the same; 1 if p_card is higher
	 * @throws NullPointerException if p_card is null or suit or rank of this card is null
	 ***********************************************************************/
	@Override
	public int compareTo ( Card p_card )
	{
		if (m_suit != p_card.getSuit()) return m_suit.compareTo(p_card.getSuit());
		
		return m_rank.compareTo(p_card.getRank());
	}

	
	/**
	 * Return a comparator that sorts by suit in the normal way, but
	 * sorts rank from high to low.
	 * @return a comparator
	 */
	public static Comparator<Card> getReverseRankComparator()
	{
		return new Comparator<Card>() {

			@Override
			public int compare(Card card1, Card card2)
			{
				if (card1 == null) return -1;
				if (card2 == null) return 1;
				if (card1.m_suit != card2.m_suit) return card1.m_suit.compareTo(card2.m_suit);
				
				return card2.m_rank.compareTo(card1.m_rank);
			}

		};
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	public String abbreviation ()
	{
		char suits[] = { 'C', 'D', 'H', 'S' };
		char suit = (m_suit == null ? '?' : suits[m_suit.ordinal()]);

		if (m_rank == null) return "?" + suit;
		
		int num = m_rank.ordinal() + 2;
		if (num < 11)
		{
			return "" + num + suit;
		}
		else
		{
			num -= 11;
			char cards[] = { 'J', 'Q', 'K', 'A' };
			return "" + cards[num] + suit;
		}
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
	public String toString()
	{
		return m_rank.toString() + m_suit.toString();
	}


	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Card's Suit
	 * @return card's suit
	 ***********************************************************************/
	public Suit getSuit ()
	{
		return m_suit;
	}

	/***********************************************************************
	 * Card's Rank (number)
	 * @return card's rank
	 ***********************************************************************/
	public Rank getRank ()
	{
		return m_rank;
	}

	/***********************************************************************
	 * The name of the file that contains the sound for this rank
	 * @return file name
	 ***********************************************************************/
	public String getSound()
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("getSound: for card: " + this.toString() + " sound: " + m_sound);
		return m_sound;
	}
	
}