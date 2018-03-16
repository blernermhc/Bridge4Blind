// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

import model.GameListener;

/***********************************************************************
 * Waits for the dummy to scan their hands.
 * When complete next state is WAIT_FOR_NEXT_PLAYER
 ***********************************************************************/
public class State_ScanDummy extends ControllerState
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(State_ScanDummy.class.getName());

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
		
		// notify all listeners we have entered this state
		for (GameListener gameListener : m_bridgeHand.getGameListeners())
		{
			gameListener.scanDummyHand();
		}
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.gameController.ControllerState#checkState()
	 */
	public BridgeHandState checkState()
	{
		if (! m_bridgeHand.testDummyComplete())
			return BridgeHandState.SCAN_DUMMY; // continue waiting for scanned cards

		// notify all listeners we have completed this state
		for (GameListener gameListener : m_bridgeHand.getGameListeners())
		{
			gameListener.dummyHandScanned();
		}

		return BridgeHandState.WAIT_FOR_NEXT_PLAYER;
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
}
