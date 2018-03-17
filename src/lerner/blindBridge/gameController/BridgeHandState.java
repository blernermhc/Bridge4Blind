// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

/***********************************************************************
 * Represents the state of the current hand.
 * The hand can be in one of the following states:
 * 
 * <br>SCAN_BLIND_HANDS - waits for all of the Blind player's Keyboard
 * Controllers to report all 13 cards in their hand.  Once all are complete,
 * the state moves on to:
 * 
 *  <br>ENTER_CONTRACT - waits for the players to complete the bidding process
 *  and come up with a contract.  Once someone enters the Contract, the state
 *  moves on to:
 *  
 *  <br>WAIT_FOR_FIRST_PLAYER - waits for the first player to play a card.
 *  This differs from waiting for the general state of waiting for the next
 *  player to play because special actions occur following this state. Once
 *  the first player has played a card, the state moves on to:
 *  
 *  <br>SCAN_DUMMY - waits for someone to scan in all of the dummy's cards.
 *  Once complete, the state moves on to:
 *  
 *  <br>WAIT_FOR_NEXT_PLAYER - waits for the next player (m_nextPlayerId) to
 *  play a card.  This could come from an antenna, or a blind player's
 *  Keyboard Controller.
 *  
 *  <br>TRICK_COMPLETE - entered when the last card has been played in a hand
 *  After a delay, changes to HAND_COMPLETE or SCAN_BLIND_HANDS.
 *  
 *  <br>HAND_COMPLETE - entered when the last trick has been played (after TRICK_COMPLETE)
 *  After a delay, changes to the first state of the next hand: SCAN_BLIND_HANDS.
 ***********************************************************************/
public enum BridgeHandState
{
	SCAN_BLIND_HANDS			(new State_ScanBlindHands())
	, ENTER_CONTRACT			(new State_EnterContract())
	, WAIT_FOR_FIRST_PLAYER	(new State_WaitForPlayer(true))
	, SCAN_DUMMY				(new State_ScanDummy())
	, WAIT_FOR_NEXT_PLAYER	(new State_WaitForPlayer(false))
	, TRICK_COMPLETE			(new State_TrickComplete())
	, HAND_COMPLETE			(new State_HandComplete())
	;

	/**
	 * Used to collect logging output for this class
	 */
	//private static Category s_cat = Category.getInstance(BridgeHandState.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	ControllerState m_controllerState;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	private BridgeHandState ()
	{
		m_controllerState = null;
	}

	private BridgeHandState ( ControllerState p_controllerState )
	{
		m_controllerState = p_controllerState;
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Returns the ControllerState object that defines the implementation
	 * of this state.
	 * @return the controller state object (may be null)
	 ***********************************************************************/
	public ControllerState getControllerState()
	{
		return m_controllerState;
	}

}
