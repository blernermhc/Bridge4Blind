// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

/***********************************************************************
 * Represents the suit component of a playing card
 ***********************************************************************/
public enum Suit
{
	CLUBS		("C") 
	, DIAMONDS	("D")  
	, HEARTS		("H")
	, SPADES		("S") 
	, NOTRUMP	("N")
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

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	/** the name of the file containing the audio announcement of this suit */
	private String m_sound;
		
	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------

	private Suit(String p_suit)
	{
		m_suitString = p_suit;
		m_sound = SOUND_FOLDER + m_suitString + ".WAV";
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