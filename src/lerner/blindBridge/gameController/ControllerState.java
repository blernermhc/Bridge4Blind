// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

/***********************************************************************
 * Interface of all state controller state objects
 ***********************************************************************/
public abstract class ControllerState
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(ControllerState.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	/** the game data */
	BridgeHand m_bridgeHand;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Code to execute when the state machine transitions to this state.
	 * Some states update game data, send messages to controllers and listeners.
	 * @param p_bridgeHand game data (save to use in checkState, if necessary)
	 ***********************************************************************/
	public abstract void onEntry( BridgeHand p_bridgeHand );
	
	/***********************************************************************
	 * Check the game state to determine if it is time to transition to a new state
	 * @return the new state, if the conditions are met, or the current state
	 * to wait for more data changes.  Returns null to break out of state machine
	 * loop.
	 ***********************************************************************/
	public abstract BridgeHandState checkState();

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * The BridgeHand managing overall game play
	 * @return the BridgeHand object
	 ***********************************************************************/
	public BridgeHand getBridgeHand ()
	{
		return m_bridgeHand;
	}

	/***********************************************************************
	 * The BridgeHand managing overall game play
	 * @param p_bridgeHand the BridgeHand object
	 ***********************************************************************/
	public void setBridgeHand ( BridgeHand p_bridgeHand )
	{
		m_bridgeHand = p_bridgeHand;
	}
	
}
