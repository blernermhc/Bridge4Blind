// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

/***********************************************************************
 * Represents errors that should be made known to some or all of the players
 ***********************************************************************/
public enum ErrorCode
{
	CANNOT_PLAY_WRONG_SUIT			("Signaled if a card is played that is not the current suit, when the hand is known to have a card in the suit (i.e., dummy or blind hand)")
	, CANNOT_PLAY_NOT_IN_HAND		("Signaled if a card is played (scanned) that is known not to be in the player's hand being played")		
	, CANNOT_PLAY_ALREADY_PLAYED		("Signaled if a card is played that has already been played")
	;
	
	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(ErrorCode.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	private String m_description;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	private ErrorCode (String p_description)
	{
		m_description = p_description;
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * A description of the error. 
	 * @return error description
	 ***********************************************************************/
	public String getDescription ()
	{
		return m_description;
	}


}
