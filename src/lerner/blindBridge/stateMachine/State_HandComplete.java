// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.stateMachine;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.GameListener;

/***********************************************************************
 * Entered when the last trick of a hand has been played.
 * When complete next state is {@link BridgeHandState#NEW_HAND}.
 ***********************************************************************/
public class State_HandComplete extends ControllerState
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(State_HandComplete.class.getName());

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
	public void onEntry ( Game p_game )
	{
		m_game = p_game;
		
		m_game.getBridgeHand().scoreContract();

		for (GameListener gameListener : m_game.getGameListeners())
		{
			gameListener.sig_handComplete(m_game.getBridgeHand().getBridgeScore());
		}
	}
	
	/* (non-Javadoc)
	 * @see lerner.blindBridge.gameController.ControllerState#checkState()
	 */
	public BridgeHandState checkState()
	{
		// Wait for a controller to start a new hand,
		// so we can leave the current hand state visible.
		// The Game#evt_startNewGame method sets the
		// state to NEW_HAND.
		/*
		m_game.startNewHand();
		return BridgeHandState.NEW_HAND;
		*/
		return BridgeHandState.HAND_COMPLETE;
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
}
