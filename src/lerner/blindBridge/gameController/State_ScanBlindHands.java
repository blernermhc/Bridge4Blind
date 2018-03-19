// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

import model.GameListener;

/***********************************************************************
 * Waits for all of the blind players to scan their hands.
 * When complete next state is Enter_Contract
 ***********************************************************************/
public class State_ScanBlindHands extends ControllerState
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(State_Scan_Blind_Hands.class.getName());

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
		
		// notify all listeners we have entered this state
		for (GameListener gameListener : m_game.getGameListeners())
		{
			gameListener.sig_scanBlindHands();
		}
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.gameController.ControllerState#checkState()
	 */
	public BridgeHandState checkState()
	{
		if (! m_game.getBridgeHand().testBlindHandsComplete())
			return BridgeHandState.SCAN_BLIND_HANDS; // continue waiting for scanned cards
		
		// notify all listeners we have completed this state
		for (GameListener gameListener : m_game.getGameListeners())
		{
			gameListener.sig_blindHandsScanned();
		}

		return BridgeHandState.ENTER_CONTRACT;
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
}
