// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

/**
 * Manages information about a contract:  the suit and number of tricks to win.
 * @author Barbara Lerner
 * @version May 18, 2012
 *
 */
public class Contract
{
	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	public static final int MAX_BID = 7;
	
	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------

	/** the suit of the contract */
	private Suit m_trumpSuit;
	
	/** the number of tricks in the contract */
	private int m_contractNum;
	
	/** The player who won the bid */
	private Direction m_bidWinner;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Default constructor used by GUI
	 ***********************************************************************/
	public Contract () { }
	
	/***********************************************************************
	 * Construct a contract with complete information
	 * @param p_bidWinner	the bid winner
	 * @param p_trumpSuit	the trump suit
	 * @param p_contractNum	the contract number
	 ***********************************************************************/
	public Contract (Direction p_bidWinner, Suit p_trumpSuit, int p_contractNum)
	{
		m_bidWinner = p_bidWinner;
		m_trumpSuit = p_trumpSuit;
		m_contractNum = p_contractNum;
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	public String toString()
	{
		return "" + m_contractNum + " " + m_trumpSuit.toString() + " " + m_bidWinner;
	}

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * The contract suit
	 * @return suit
	 ***********************************************************************/
	public Suit getTrump()
	{
		return m_trumpSuit;
	}

	/***********************************************************************
	 * The contract suit
	 * @param p_suit suit
	 ***********************************************************************/
	public void setTrump (Suit p_suit)
	{
		m_trumpSuit = p_suit;
	}

	/***********************************************************************
	 * The number of tricks in the contract
	 * @return number of tricks
	 ***********************************************************************/
	public int getContractNum()
	{
		return m_contractNum;
	}
	
	/***********************************************************************
	 * The number of tricks in the contract
	 * @param p_contractNum number of tricks
	 ***********************************************************************/
	public void setContractNum(int p_contractNum)
	{
		assert p_contractNum >= 1 && p_contractNum <= MAX_BID;
		this.m_contractNum = p_contractNum;
	}

	/***********************************************************************
	 * The player making the contract
	 * @return player
	 ***********************************************************************/
	public Direction getBidWinner()
	{
		return m_bidWinner;
	}

	/***********************************************************************
	 * The player making the contract
	 * @param p_bidWinner player
	 ***********************************************************************/
	public void setBidWinner (Direction p_bidWinner)
	{
		this.m_bidWinner = p_bidWinner;
	}
	
}
