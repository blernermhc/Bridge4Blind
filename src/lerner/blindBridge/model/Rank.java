package lerner.blindBridge.model;

/***********************************************************************
 * Represents the rank (the number) of a card
 ***********************************************************************/
public enum Rank
{
	TWO		("2")
	, THREE	("3") 
	, FOUR	("4")
	, FIVE	("5") 
	, SIX	("6")
	, SEVEN	("7") 
	, EIGHT	("8") 
	, NINE	("9")
	, TEN	("10") 
	, JACK	("J") 
	, QUEEN	("Q") 
	, KING	("K")
	, ACE	("A")
	;
	
	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	private static final String SOUND_FOLDER = "/sounds/ranks/";
	
	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------

	/** 
	 * String name of the rank, used to construct the name of the file
	 * containing the audio of this rank
	 */
	private final String m_rankValue;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------
	
	/** the name of the file containing the audio announcement of this rank */
	private String m_sound;
	
	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------

	private Rank(String p_rank)
	{
		m_rankValue = p_rank;
		m_sound = SOUND_FOLDER + p_rank + ".WAV";
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------

	/**
	 * Given a letter find the appropriate Value.  '2' for two, etc.  'T' for ten, 
	 * 'J', 'Q', 'K' for face cards, 'A' for ace.
	 * @param p_c the Letter 
	 * @return the Rank.  Returns null if an unexpected character is passed in.
	 */
	/* not used?
	public static Rank findValue(char p_c)
	{
		switch(p_c) 
		{
		case '2': return TWO;
		case '3': return THREE;
		case '4': return FOUR;
		case '5': return FIVE;
		case '6': return SIX;
		case '7': return SEVEN;
		case '8': return EIGHT;
		case '9': return NINE;
		case 'T': return TEN;
		case 'J': return JACK;
		case 'Q': return QUEEN;
		case 'K': return KING;
		case 'A': return ACE;
		}
		assert false;
		return null;
	}
	*/

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString()
	{
		return m_rankValue;
	}

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * The name of the file that contains the sound for this rank
	 * @return file name
	 ***********************************************************************/
	public String getSound()
	{
		return m_sound;
	}
	

	
}