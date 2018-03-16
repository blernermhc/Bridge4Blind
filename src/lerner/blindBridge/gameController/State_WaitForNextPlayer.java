// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

import java.util.List;

import model.Direction;
import model.GameListener;

/***********************************************************************
 * Waits for the next player of a hand to play a card
 * When complete next state is either {@link BridgeHandState#WAIT_FOR_NEXT_PLAYER}
 * or {@link BridgeHandState#TRICK_COMPLETE}.
 ***********************************************************************/
public class State_WaitForNextPlayer extends ControllerState
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(State_WaitForNextPlayer.class.getName());

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
		List<CardPlay> currentTrick = m_bridgeHand.getCurrentTrick();
		Direction nextPlayer = m_bridgeHand.getNextPlayer();
		CardPlay expectedCardPlay = null;
		for (CardPlay cardPlay : currentTrick)
		{
			if (cardPlay.getPlayer() == nextPlayer)
			{
				expectedCardPlay = cardPlay;
				break;
			}
		}
		
		if (expectedCardPlay == null)
			return BridgeHandState.WAIT_FOR_NEXT_PLAYER;	// continue waiting for scanned card

		if (currentTrick.size() == 4)
		{
			// TRICK_COMPLETE state will adjust nextPlayer
			return BridgeHandState.TRICK_COMPLETE;
		}
		else
		{
			nextPlayer = nextPlayer.getNextDirection();
			m_bridgeHand.setNextPlayer(nextPlayer);
			
			for (GameListener gameListener : m_bridgeHand.getGameListeners())
			{
				gameListener.setNextPlayer(m_bridgeHand.getNextPlayer());
			}

			return BridgeHandState.WAIT_FOR_NEXT_PLAYER;
		}
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
}
