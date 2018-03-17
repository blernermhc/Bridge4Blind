// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

import model.Direction;
import model.GameListener;

/***********************************************************************
 * Entered when the last card of a trick has been played.
 * When complete next state is either {@link BridgeHandState#HAND_COMPLETE}
 * or {@link BridgeHandState#WAIT_FOR_NEXT_PLAYER}.
 ***********************************************************************/
public class State_TrickComplete extends ControllerState
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(State_TrickComplete.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/* (non-Javadoc)
	 * @see lerner.blindBridge.gameController.ControllerState#onEntry(lerner.blindBridge.gameController.BridgeHand)
	 */
	public void onEntry ( BridgeHand p_bridgeHand )
	{
		m_bridgeHand = p_bridgeHand;
		
		Direction winner = m_bridgeHand.sc_finishTrick();

		for (GameListener gameListener : m_bridgeHand.getGameListeners())
		{
			gameListener.trickWon(winner);
		}

		for (GameListener gameListener : m_bridgeHand.getGameListeners())
		{
			gameListener.setNextPlayer(m_bridgeHand.getNextPlayer());
		}
	}
	
	/* (non-Javadoc)
	 * @see lerner.blindBridge.gameController.ControllerState#checkState()
	 */
	public BridgeHandState checkState()
	{
		if (! m_bridgeHand.testHandComplete())
			return BridgeHandState.WAIT_FOR_NEXT_PLAYER;
		else
			return BridgeHandState.HAND_COMPLETE;
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
}
