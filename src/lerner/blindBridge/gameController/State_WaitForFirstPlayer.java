// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

import model.Direction;
import model.GameListener;
import model.Suit;

/***********************************************************************
 * Waits for the first player of a hand to play a card
 * When complete next state is SCAN_DUMMY.
 ***********************************************************************/
public class State_WaitForFirstPlayer extends ControllerState
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(State_WaitForFirstPlayer.class.getName());

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
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.gameController.ControllerState#checkState()
	 */
	public BridgeHandState checkState()
	{
		if (m_bridgeHand.getCurrentTrick().size() == 0)
			return BridgeHandState.WAIT_FOR_FIRST_PLAYER;	// continue waiting for contract

		CardPlay cardPlay = m_bridgeHand.getCurrentTrick().get(0);	// get first trick
		Suit currentSuit = cardPlay.getCard().getSuit();
		Direction nextPlayer = cardPlay.getPlayer().getNextDirection();

		m_bridgeHand.setCurrentSuit(currentSuit );
		m_bridgeHand.setNextPlayer(nextPlayer);
		
		for (GameListener gameListener : m_bridgeHand.getGameListeners())
		{
			gameListener.setCurrentSuit(m_bridgeHand.getCurrentSuit());
		}

		for (GameListener gameListener : m_bridgeHand.getGameListeners())
		{
			gameListener.setNextPlayer(m_bridgeHand.getNextPlayer());
		}
		
		return BridgeHandState.SCAN_DUMMY;
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
}
