// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.stateMachine;

import org.apache.log4j.Category;

import lerner.blindBridge.main.Game;

/***********************************************************************
 * The State Controller for the Bridge Game.
 * Executes the onEntry() and checkState() methods of each state.
 ***********************************************************************/
public class BridgeHandStateController
{

	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(BridgeHandStateController.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------
	
	/** max time to wait before checking state again (ensures we do not miss a notify event and hang) */
	public static final long STATE_WAIT_TIME_MILLIS = 1000;

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	/** The game data */
	Game m_game;

	/** If set, state machine transitions to the indicated state upon wakeup */
	BridgeHandState		m_forceNewState		= null;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	/** The current state.  Default is the initial game state: SCAN_BLIND_HANDS */
	BridgeHandState		m_currentState		= BridgeHandState.INITIALIZING;

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Creates the state machine.  Start the state machine later, after threads have been spawned.
	 * @param p_game the game controller
	 ***********************************************************************/
	public BridgeHandStateController ( Game p_game )
	{
		m_game = p_game;
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Main processing loop.
	 * 
	 * This waits for an event (any event) and then invokes the checkState() method
	 * of the current state, to see if the system should transition to a new
	 * state.  On entry, the onEnter() method is invoked for the current state
	 * and on every state transition.  If checkState() returns null, the loop
	 * terminates.
	 * 
	 * Event generators, such as keyboard controllers and card antennas update
	 * state and issue a notify to let this method check the state.
	 ***********************************************************************/
	public synchronized void runStateMachine ()
	{
		if (m_currentState == null) return;

		m_currentState.getControllerState().onEntry(m_game);

		while (true)
		{
			BridgeHandState newState = getForceNewState();
			if (newState != null)
			{
				if (s_cat.isDebugEnabled())
					s_cat.debug("runStateMachine: FORCED transition from " + m_currentState + " to " + newState);
				m_currentState = newState;
				m_currentState.getControllerState().onEntry(m_game);
			}

			newState = m_currentState.getControllerState().checkState();

			if (newState == null)
			{
				if (s_cat.isDebugEnabled())
					s_cat.debug("runStateMachine: transition from " + m_currentState + " to exit");
				break;
			}

			if (newState != m_currentState)
			{
				if (s_cat.isDebugEnabled()) s_cat.debug("runStateMachine: transition from " + m_currentState + " to " + newState);
				m_currentState = newState;
				m_currentState.getControllerState().onEntry(m_game);
			}
			else
			{
				try
				{
					// wait for an event to be processed, but keep checking periodically, just in case
					wait(STATE_WAIT_TIME_MILLIS);
				}
				catch (InterruptedException e)
				{
					s_cat.error("runStateMachine: wait interrupted with exception: " + e);
				}
			}
		}
	}

	public synchronized void notifyStateMachine ()
	{
		notify();
	}
	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * The current state.  Default is the initial game state: SCAN_BLIND_HANDS
	 * @return state
	 ***********************************************************************/
	public BridgeHandState getCurrentState ()
	{
		return m_currentState;
	}

	/***********************************************************************
	 * If set, state machine transitions to the indicated state upon wakeup.
	 * Cleared when read.
	 * @return the new state
	 ***********************************************************************/
	public synchronized BridgeHandState getForceNewState ()
	{
		BridgeHandState state = m_forceNewState;
		m_forceNewState = null;
		return state;
	}

	/***********************************************************************
	 * If set, state machine transitions to the indicated state upon wakeup.
	 * Cleared when read.
	 * @param p_forceNewState the new state
	 ***********************************************************************/
	public synchronized void setForceNewState ( BridgeHandState p_forceNewState )
	{
		m_forceNewState = p_forceNewState;
	}

}
