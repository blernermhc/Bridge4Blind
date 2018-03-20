// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.stateMachine;

import lerner.blindBridge.hardware.AntennaController;
import lerner.blindBridge.hardware.KeyboardController;
import lerner.blindBridge.main.Game;

/***********************************************************************
 * Initial state on startup.  Waits for devices to initialize.
 * When hardware is ready, enters NEW_HAND state.
 ***********************************************************************/
public class State_Initializing extends ControllerState
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(State_Initializing.class.getName());

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
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.gameController.ControllerState#checkState()
	 */
	public BridgeHandState checkState()
	{
		boolean ready = true;
		
		if (ready)
		{
			for (AntennaController antController : m_game.getAntennaControllers().values())
			{
				if (! antController.isDeviceReady())
				{
					ready = false;
					break;
				}
			}
		}
		
		if (ready)
		{
			for (KeyboardController kbdController : m_game.getKeyboardControllers().values())
			{
				if (! kbdController.isDeviceReady())
				{
					ready = false;
					break;
				}
			}
		}
		
		if (ready)
			return BridgeHandState.NEW_HAND;
		else
			return BridgeHandState.INITIALIZING;
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
}
