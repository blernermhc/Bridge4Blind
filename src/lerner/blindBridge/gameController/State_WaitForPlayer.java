// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

import java.util.List;

import model.Direction;
import model.GameListener;
import model.Suit;

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
	boolean m_waitForFirstPlayer;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------
	
	BridgeHandState m_myState;

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
			m_myState = BridgeHandState.WAIT_FOR_FIRST_PLAYER;
		else
			m_myState = BridgeHandState.WAIT_FOR_NEXT_PLAYER;

		for (GameListener gameListener : m_game.getGameListeners())
		{
			gameListener.sig_setNextPlayer(m_game.getBridgeHand().getNextPlayer());
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
		List<CardPlay> currentTrick = m_game.getBridgeHand().getCurrentTrick();
		
		Direction nextPlayer = m_game.getBridgeHand().getNextPlayer();
		CardPlay cardPlay = null;
		for (CardPlay cp : currentTrick)
		{
			if (cp.getPlayer() == nextPlayer)
			{
				cardPlay = cp;
				break;
			}
		}
		
		if (cardPlay == null)
			return m_myState;	// continue waiting for scanned card
		
		//--------------------------------------------------
		// If the card we are waiting for has been played, process it
		//--------------------------------------------------
		
		// notify listeners of new card
		for (GameListener gameListener : m_game.getGameListeners())
		{
			gameListener.sig_cardPlayed(cardPlay.getPlayer(), cardPlay.getCard());
		}
		


		if (currentTrick.size() == 4)
		{
			// last card of trick played
			// TRICK_COMPLETE state will adjust nextPlayer
			return BridgeHandState.TRICK_COMPLETE;
		}
		else
		{
			if (currentTrick.size() == 1)
			{
				// first card of trick played (set current suit)
				Suit currentSuit = cardPlay.getCard().getSuit();
				m_game.getBridgeHand().setCurrentSuit(currentSuit);
				
				for (GameListener gameListener : m_game.getGameListeners())
				{
					gameListener.sig_setCurrentSuit(currentSuit);
				}
			}

			nextPlayer = cardPlay.getPlayer().getNextDirection();
			m_game.getBridgeHand().setNextPlayer(nextPlayer);
			
			if (currentTrick.size() == 1 && m_waitForFirstPlayer)
			{
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
