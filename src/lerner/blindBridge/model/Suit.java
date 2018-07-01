package lerner.blindBridge.model;

/***********************************************************************
 * Represents the suit component of a playing card
 ***********************************************************************/
public enum Suit
{
	CLUBS		("C", "\u2663", "Clubs") 
	, DIAMONDS	("D", "\u2666", "Diamonds")  
	, HEARTS		("H", "\u2665", "Hearts")
	, SPADES		("S", "\u2660", "Spades") 
	, NOTRUMP	("N", "NT", "No Trump")
	;
	

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------
	
	private static final String SOUND_FOLDER = "/sounds/suits/";

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------

	/** 
	 * string name of the suit, used to construct the name of the file
	 * containing the audio of this suit
	 */
	private String m_suitString;
	
	/**
	 * Full name of the suit
	 */
	private String m_suitName;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	/** the name of the file containing the audio announcement of this suit */
	private String m_sound;
	
	
	private String m_symbol;
		
	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------

	private Suit(String p_suit, String p_symbol, String p_name)
	{
		m_suitString = p_suit;
		m_sound = SOUND_FOLDER + m_suitString + ".WAV";
		m_symbol = p_symbol;
		m_suitName = p_name;
	}
	
	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/**
	 * Finds the appropriate Suit given a letter.  H = Hearts, C = Clubs, S = Spades
	 * D = Diamonds
	 * @param p_c the letter
	 * @return the Suit.  Returns null if an unexpected code is found.
	 */
	public static Suit findSuit(char p_c)
	{
		switch(p_c)
		{
		case 'H': return HEARTS;
		case 'D': return DIAMONDS;
		case 'S': return SPADES;
		case 'C': return CLUBS;
		}
		
		assert false;
		return null;
	}
	
	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString()
	{
		return m_suitString;
	}
	
	/**
	 * 
	 * @return the graphical symbol for the suit.
	 */
	public String getSymbol()
	{
		return m_symbol;
	}


	/**
	 * 
	 * @return the full name for the suit.
	 */
	public String getName()
	{
		return m_suitName;
	}


	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * The name of the file that contains the sound for this suit
	 * @return file name
	 ***********************************************************************/
	public String getSound()
	{
		return m_sound;
	}

}