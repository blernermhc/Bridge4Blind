// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package model;

import java.util.HashMap;
import java.util.Map;

/***********************************************************************
 * Represents the score of a Bridge Game
 ***********************************************************************/
public class BridgeScore
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(BridgeScore.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	private Map<Direction, Integer[]> m_score	= new HashMap<>();

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Creats a new score object with zero points
	 ***********************************************************************/
	public BridgeScore ()
	{
		Integer northScore[] = { new Integer(0), new Integer(0) };
		m_score.put(Direction.NORTH, northScore);

		Integer eastScore[] = { new Integer(0), new Integer(0) };
		m_score.put(Direction.EAST, eastScore);
}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	public void scoreHand (Contract p_contract, int p_NSTricksTaken, int p_EWTricksTaken)
	{
		// TODO: implement scoring
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * Return the current bridge score map.  The keys are NORTH, for the North/South team,
	 * and EAST, for the East/West team.  The values are an array of Integers representing
	 * points above the line (index 0) and below the line (index 1).
	 * @return score map
	 ***********************************************************************/
	public Map<Direction, Integer[]> getScore ()
	{
		return m_score;
	}

}
