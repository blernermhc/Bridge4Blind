// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***********************************************************************
 * Contains the completed tricks of a hand along with inspections methods.
 ***********************************************************************/
public class TrickSet
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(TrickSet.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------
	
	/** The contract used to determine the hand winner */
	private Contract							m_contract;
	
	/** The team that won the hand */
	private Direction						m_winner;
	
	/** The raw sequence of tricks completed during the hand */
	private Deque<Trick>						m_trickSequence			= new ArrayDeque<>();
	
	/** A stack of "undone" tricks that can be "redone" */
	private Deque<Trick>						m_trickSequence_redo		= new ArrayDeque<>();

	/** The tricks taken by each team (NORTH and SOUTH have the same list, EAST and WEST have the same list) */
	private Map<Direction, List<Trick>>		m_tricksTaken = new HashMap<>();

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	public TrickSet ( Contract p_contract )
	{
		m_contract = p_contract;
		resetTricksTaken();
	}
	
	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Recomputes the cache of tricks taken by each team.
	 ***********************************************************************/
	private void resetTricksTaken ()
	{
		m_winner = null;		// recompute when needed
		m_tricksTaken = new HashMap<>();
		List<Trick> nsTrickList = new ArrayList<>();
		List<Trick> ewTrickList = new ArrayList<>();
		
		m_tricksTaken.put(Direction.NORTH, nsTrickList);
		m_tricksTaken.put(Direction.SOUTH, nsTrickList);

		m_tricksTaken.put(Direction.EAST, ewTrickList);
		m_tricksTaken.put(Direction.WEST, ewTrickList);

		for (Trick trick : m_trickSequence)
		{
			Direction winner = trick.getWinner();
			if (winner == null) continue;
			
			if (winner == Direction.NORTH || winner == Direction.SOUTH)
			{
				nsTrickList.add(trick);
			}
			
			if (winner == Direction.EAST || winner == Direction.WEST)
			{
				ewTrickList.add(trick);
			}
		}
	}
	
	/***********************************************************************
	 * Inserts a completed trick into the sequence of tricks completed in the hand.
	 * @param p_trick	the trick
	 ***********************************************************************/
	public void addCompletedTrick ( Trick p_trick )
	{
		if (p_trick == null) return;

		Direction winner = p_trick.getWinner();

		if (winner != null)
		{
			m_trickSequence_redo.clear();
			m_trickSequence.push(p_trick);
			m_tricksTaken.get(winner).add(p_trick);
		}
	}
	
	/***********************************************************************
	 * Determines if the hand is complete (based on the number of completed tricks).
	 * @return true if complete, false otherwise
	 ***********************************************************************/
	public boolean isComplete ()
	{
		return (m_trickSequence.size() == BridgeHand.CARDS_IN_HAND);
	}
	
	/***********************************************************************
	 * Pops the last completed trick off the stack and returns it.
	 * Also puts it on the redo stack, in case the pop is undone.
	 * Returns null, if there are no completed tricks on the stack.
	 * @return the trick or null, if none
	 ***********************************************************************/
	public Trick undoTrick ()
	{
		Trick lastTrick = m_trickSequence.poll();
		if (lastTrick != null) m_trickSequence_redo.push(lastTrick);
		resetTricksTaken();
		return lastTrick;
	}

	/***********************************************************************
	 * Reverses a previous undoTrick.
	 * Returns null, if there are no tricks on the redo stack.
	 * @return the restored trick or null, if none
	 ***********************************************************************/
	public Trick redoTrick ()
	{
		Trick lastTrick = m_trickSequence_redo.poll();
		if (lastTrick != null) m_trickSequence.push(lastTrick);
		resetTricksTaken();
		return lastTrick;
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	/***********************************************************************
	 * Determines the team that won the hand.
	 * Returns null, if the hand is not complete.
	 * @return	either NORTH or EAST, if complete
	 ***********************************************************************/
	public Direction computeWinner ()
	{
		if (! isComplete()) return null;
		
		Direction contractTeam = m_contract.getBidWinner().getTeam();
		
		if (m_tricksTaken.get(contractTeam).size() >= m_contract.getContractNum() + 6)
		{
			return contractTeam;
		}
		else
		{
			return contractTeam.getNextDirection().getTeam();
		}
	}
	
	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Returns the team that won the hand.
	 * Returns null, if the hand is not complete.
	 * @return	either NORTH or EAST, if complete
	 ***********************************************************************/
	public Direction getWinner ()
	{
		if (m_winner == null) m_winner = computeWinner();
		return m_winner;
	}

	/***********************************************************************
	 * Returns the number of tricks won by the given player's team.
	 * @param p_direction	the player
	 * @return the number of tricks won
	 ***********************************************************************/
	public int getNumTricksWon ( Direction p_direction )
	{
		return m_tricksTaken.get(p_direction).size();
	}

	/***********************************************************************
	 * The raw sequence of tricks completed during the hand.
	 * @return trick sequence (may be empty, but never null)
	 ***********************************************************************/
	public Deque<Trick> getTrickSequence ()
	{
		return m_trickSequence;
	}

}
