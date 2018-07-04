// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.stateMachine;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Contract;
import lerner.blindBridge.model.GameListener;

/***********************************************************************
 * Waits for the contract to be set.
 * When complete next state is WAIT_FOR_FIRST_PLAYER
 ***********************************************************************/
public class State_EnterContract extends ControllerState
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(State_EnterContract.class.getName());

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
			gameListener.sig_enterContract();
		}
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.gameController.ControllerState#checkState()
	 */
	public BridgeHandState checkState()
	{
		if (! m_game.getBridgeHand().testContractComplete())
			return BridgeHandState.ENTER_CONTRACT;	// continue waiting for contract

		Contract contract = m_game.getBridgeHand().getContract();
		
		for (GameListener gameListener : m_game.getGameListeners())
		{
			gameListener.sig_contractSet(contract);
		}

		// Wait to set dummy position until first player plays a card so display does not appear early

		return BridgeHandState.WAIT_FOR_FIRST_PLAYER;
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
}
