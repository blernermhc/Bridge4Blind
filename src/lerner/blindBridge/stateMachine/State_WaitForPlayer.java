// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.stateMachine;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.CardPlay;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener;
import lerner.blindBridge.model.Suit;
import lerner.blindBridge.model.Trick;

/***********************************************************************
 * Waits for the next player of a hand to play a card.
 * This class is used for both the {@link BridgeHandState#WAIT_FOR_NEXT_PLAYER} and
 * {@link BridgeHandState#WAIT_FOR_NEXT_PLAYER} states (as indicated by the
 * p_waitForFirstPlayer boolean.
 * 
 * When complete next state is either {@link BridgeHandState#WAIT_FOR_NEXT_PLAYER}
 * {@link BridgeHandState#SCAN_DUMMY} or {@link BridgeHandState#TRICK_COMPLETE}.
 ***********************************************************************/
public class State_WaitForPlayer extends ControllerState
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(State_WaitForPlayer.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	/**
	 * If true, this is implementing WAIT_FOR_FIRST_PLAYER.
	 * If false, this is implementing WAIT_FOR_NEXT_PLAYER.
	 * Set by an argument to the constructor.
	 */
	private boolean m_waitForFirstPlayer;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------
	
	/** 
	 * State to return if checkState does not find what it is looking for.
	 * Set to either WAIT_FOR_FIRST_PLAYER or WAIT_FOR_NEXT_PLAYER.
	 */
	private BridgeHandState m_myState;
	
	/** The player we are waiting for */
	private Direction m_playerToWaitFor;

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * This class is used for both the {@link BridgeHandState#WAIT_FOR_NEXT_PLAYER} and
	 * {@link BridgeHandState#WAIT_FOR_NEXT_PLAYER} states, as indicated by the
	 * p_waitForFirstPlayer boolean.
	 * @param p_waitForFirstPlayer	if true, process as WAIT_FOR_FIRST_PLAYER.
	 * Otherwise, process as WAIT_FOR_NEXT_PLAYER.
	 ***********************************************************************/
	public State_WaitForPlayer (boolean p_waitForFirstPlayer)
	{
		m_waitForFirstPlayer = p_waitForFirstPlayer;
		
		//-----------------------------------------------------------
		// IMPORTANT NOTE: we cannot set m_myState in the constructor
		// because the enum has not finished initialization at that
		// point and the enum constants are null.
		// So, set this in the entry method.
		//-----------------------------------------------------------
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/* (non-Javadoc)
	 * @see lerner.blindBridge.gameController.ControllerState#onEntry(lerner.blindBridge.gameController.BridgeHand)
	 */
	public void onEntry ( Game p_game )
	{
		m_game = p_game;

		//-----------------------------------------------------------
		// IMPORTANT NOTE: we cannot set m_myState in the constructor
		// because the enum has not finished initialization at that
		// point and the enum constants are null.
		//-----------------------------------------------------------
		if (m_waitForFirstPlayer)
		{
			m_myState = BridgeHandState.WAIT_FOR_FIRST_PLAYER;
			m_playerToWaitFor = m_game.getBridgeHand().sc_startFirstTrick();
		}
		else
		{
			m_myState = BridgeHandState.WAIT_FOR_NEXT_PLAYER;
			m_playerToWaitFor = m_game.getBridgeHand().getCurrentTrick().getNextPlayer();
		}
		
		for (GameListener gameListener : m_game.getGameListeners())
		{
			gameListener.sig_setNextPlayer(m_playerToWaitFor);
		}
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.gameController.ControllerState#checkState()
	 */
	public BridgeHandState checkState()
	{
		//--------------------------------------------------
		// First, see if the trick contains a card played by the next player.
		// The evt_playCard methods adds cards to the current trick.
		//--------------------------------------------------
		Trick currentTrick = m_game.getBridgeHand().getCurrentTrick();
		if (currentTrick == null) return m_myState;	// continue waiting for scanned card
		
		CardPlay cardPlay = currentTrick.hasPlayed(m_playerToWaitFor); 
		if (cardPlay == null) return m_myState;	// continue waiting for scanned card
		
		//--------------------------------------------------
		// If the card we are waiting for has been played, process it
		//--------------------------------------------------
		
		// notify listeners of new card
		for (GameListener gameListener : m_game.getGameListeners())
		{
			gameListener.sig_cardPlayed(cardPlay.getPlayer(), cardPlay.getCard());
		}
		
		if (currentTrick.isComplete())
		{
			// last card of trick played
			// TRICK_COMPLETE state will adjust nextPlayer
			return BridgeHandState.TRICK_COMPLETE;
		}
		else
		{
			if (currentTrick.getCardsPlayed().size() == 1)
			{
				// first card of trick played (send current suit)
				Suit currentSuit = m_game.getBridgeHand().getCurrentTrick().getCurrentSuit();
				
				for (GameListener gameListener : m_game.getGameListeners())
				{
					gameListener.sig_setCurrentSuit(currentSuit);
				}
			}

			if (m_waitForFirstPlayer)
			{
				Direction dummyPosition = m_game.getBridgeHand().getContract().getBidWinner().getPartner();

				m_game.getBridgeHand().setDummyPosition(dummyPosition);
				
				for (GameListener gameListener : m_game.getGameListeners())
				{
					gameListener.sig_setDummyPosition(dummyPosition);
				}
				
				return BridgeHandState.SCAN_DUMMY;
			}
			else
			{
				// use SWITCH_TO_NEXT_PLAYER so state machine will call onEntry for new state
				// rather than thinking we are in the same state.
				return BridgeHandState.SWITCH_TO_NEXT_PLAYER;
			}
		}
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
	
}
