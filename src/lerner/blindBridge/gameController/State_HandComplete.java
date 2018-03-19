// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

import model.GameListener;

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
		m_game.startNewHand();
		return BridgeHandState.NEW_HAND;
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
}
