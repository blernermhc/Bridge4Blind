// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.model;

/***********************************************************************
 * Represents a card played by a player
 ***********************************************************************/
public class CardPlay
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(CardPlay.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	private Direction			m_direction;
	
	private	Card				m_card;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	public CardPlay (Direction p_direction, Card p_card)
	{
		m_direction		= p_direction;
		m_card		= p_card;
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
	public String toString()
	{
		return "CardPlay: " + m_direction + " " + m_card;
	}

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * Player playing the card
	 * @return player
	 ***********************************************************************/
	public Direction getPlayer ()
	{
		return m_direction;
	}

	/***********************************************************************
	 * Player playing the card
	 * @param p_direction player
	 ***********************************************************************/
	public void setPlayer ( Direction p_direction )
	{
		m_direction = p_direction;
	}

	/***********************************************************************
	 * Card played
	 * @return card
	 ***********************************************************************/
	public Card getCard ()
	{
		return m_card;
	}

	/***********************************************************************
	 * Card played
	 * @param p_card card
	 ***********************************************************************/
	public void setCard ( Card p_card )
	{
		m_card = p_card;
	}

}
