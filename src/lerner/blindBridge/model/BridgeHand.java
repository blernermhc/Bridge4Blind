// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Category;

import lerner.blindBridge.hardware.AntennaController;
import lerner.blindBridge.hardware.KeyboardController;
import lerner.blindBridge.hardware.KeyboardController.KBD_MESSAGE;
import lerner.blindBridge.hardware.KeyboardController.MULTIBYTE_MESSAGE;
import lerner.blindBridge.main.Game;
import lerner.blindBridge.stateMachine.BridgeHandState;

/***********************************************************************
 * Represents the data representing the current state of the current hand
 * and manages the hand play.
 *  
 *  <P>This class supports the following events, which can trigger state
 *  changes.
 *  
 *  <br>evt_startNewHand - enters state SCXAN_BLIND_HANDS
 *  
 *  <br>evt_addScannedCard - if card completes all of the blind hands, enters
 *  state ENTER_CONTRACT.
 *  
 *  <br>evt_setContract - stores the contract, sets the first player and
 *  dummy positions.  Enters state WAIT_FOR_FIRST_PLAYER.
 *  
 *  <br>evt_playCard - plays a card
 *  
 *  <br>evt_resetKeyboard - resends the current state to a Keyboard Controller
 *  in response to a request from the Keybaord Controller to be reset.
 *  We need to wait for the request, so we know it is listening.  This
 *  can be run in any hand state and does not change the hand state.
 *  
 ***********************************************************************/
public class BridgeHand
{

	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(BridgeHand.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------
	
	/** Number of players */
	public static final int NUMBER_OF_PLAYERS	= 4;
	
	/** Number of cards to make a complete player hand */
	public static final int	CARDS_IN_HAND		= 13;

	/** fixed test hands [hand#] [direction] [cardAbbrev] */
	public static String[][][] m_testHand =
		{ // hand 0
		 {
			   { "5C", "7C", "9C", "JC", "4D", "7D", "9D", "JD", "TH", "2S", "5S", "9S", "QS" }	// north
			 , { "4C", "6C", "8C", "8D", "QD", "AD", "3H", "4H", "KH", "4S", "6S", "8S", "TS" }	// east
			 , { "3C", "QC", "KC", "3D", "6D", "TD", "KD", "7H", "8H", "JH", "AH", "7S", "AS" }	// south
			 , { "2C", "TC", "AC", "2D", "5D", "2H", "5H", "6H", "9H", "QH", "3S", "JS", "KS" }	// west
		 }
		};

	//--------------------------------------------------
	// MEMBER DATA
	//--------------------------------------------------

	/** hand data for scanned hands (blind players and dummy) */
	private Map<Direction, PlayerHand> 		m_hands;

	/** for dealing complete hands to all players for testing */
	private Map<Direction, PlayerHand> 		m_testHands;
	
	/** Tricks taken by each team (N and S have the same list, E and W have the same list) */
	private Map<Direction, List<Trick>>		m_tricksTaken;
	
	/** The contract for the hand */
	private Contract							m_contract;

	/** The position of the dummy */
	private Direction						m_dummyPosition;
	
	/** The current round */
	private Trick		 					m_currentTrick;
	
	/** The current score of the bridge game */
	private BridgeScore						m_bridgeScore = new BridgeScore();
	
	/** All of the cards that have been played (to check for attempts to play a card twice (e.g., due to an accidental scan) */
	private Set<Card>						m_cardsPlayed = new HashSet<>();

	/** reference to the top-level object (contains list of listeners, and other non-hand data) */
	private Game								m_game;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Creates the data object for a new hand
	 * @param p_game		the game controller
	 ***********************************************************************/
	public BridgeHand (Game p_game)
	{
		m_game = p_game;
		
		initializeHand();
	}
	
	//--------------------------------------------------
	// CONFIGURATION METHODS
	//--------------------------------------------------

	/***********************************************************************
	 * Initialize data for a new hand
	 ***********************************************************************/
	private void initializeHand ()
	{
		m_hands			= new HashMap<>();
		m_testHands		= new HashMap<>();
		m_contract		= null;
		m_currentTrick	= null;
		m_dummyPosition	= null;
		
		m_tricksTaken = new HashMap<>();
		
		List<Trick> list = new ArrayList<>();
		m_tricksTaken.put(Direction.NORTH, list);
		m_tricksTaken.put(Direction.SOUTH, list);	// N and S use the same list
		
		list = new ArrayList<>();
		m_tricksTaken.put(Direction.EAST, list);
		m_tricksTaken.put(Direction.WEST, list);	// E and W use the same list
		
		if (s_cat.isDebugEnabled()) s_cat.debug("initializeHand: finished");
	}

	//--------------------------------------------------
	// EVENT METHODS (PUBLIC)
	//--------------------------------------------------
	
	/***********************************************************************
	 * Handles the receipt of a card from an antenna or Keyboard Controller.
	 * Logs an error and returns false without making any changes, if the current state is not SCAN_BLIND_HANDS or SCAN_DUMMY, 
	 * @param p_direction		Position playing the card
	 * @param p_card			The card played
	 * @return true if processed event and false otherwise
	 ***********************************************************************/
	public boolean evt_addScannedCard (Direction p_direction, Card p_card)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_addScannedCard: entered for"
		                                        + " player: " + p_direction + " card: " + p_card);

		BridgeHandState currentState = m_game.getStateController().getCurrentState();
		if (currentState != BridgeHandState.SCAN_BLIND_HANDS && currentState != BridgeHandState.SCAN_DUMMY)
		{
			s_cat.error("evt_addScannedCard: ignoring event since state is not SCAN_BLIND_HANDS or SCAN_DUMMY.  State: " + currentState);
			return false;
		}
		
		PlayerHand hand = m_hands.get(p_direction);
		if (hand == null)
		{
			hand = new PlayerHand(p_direction);
			m_hands.put(p_direction, hand);
		}
		hand.addCard(p_card);
		
		boolean handComplete = hand.isComplete();
		
		// notify listeners of new card
		for (GameListener gameListener : m_game.getGameListeners())
		{
			gameListener.sig_cardScanned(p_direction, p_card, handComplete);
		}
		
		m_game.getStateController().notifyStateMachine();

		if (s_cat.isDebugEnabled()) s_cat.debug("evt_addScannedCard: finished.");
		
		return true;
	}
		
	/***********************************************************************
	 * Enters the current contract, if state is ENTER_CONTRACT
	 * Sets m_nextPlayerId and m_dummyPosition
	 * Changes state to WAIT_FOR_FIRST_PLAYER.
	 * Logs an error and makes no changes, if the current state is not ENTER_CONTRACT, 
	 * Logs an error and returns false without making any changes, if the current state is not ENTER_CONTRACT, 
	 * @return true if processed event and false otherwise
	 * @param p_contract current contract
	 ***********************************************************************/
	public boolean evt_setContract ( Contract p_contract )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_setContract: entered for "
		                                        + " contract: " + p_contract);

		BridgeHandState currentState = m_game.getStateController().getCurrentState();
		if (currentState != BridgeHandState.ENTER_CONTRACT)
		{
			s_cat.error("evt_setContract: ignoring event since state is not ENTER_CONTRACT.  State: " + currentState);
			return false;
		}
		
		m_contract = p_contract;
		
		m_game.getStateController().notifyStateMachine();
		
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_setContract: finished.");

		return true;
	}

	/***********************************************************************
	 * Play a card
	 * Logs an error and returns false without making any changes, if the current state is not WAIT_FOR_FIRST_PLAYER or WAIT_FOR_NEXT_PLAYER, 
	 * @param p_direction	player playing the card
	 * @param p_card		card played
	 * @return true if processed event and false otherwise
	 ***********************************************************************/
	public boolean evt_playCard (Direction p_direction, Card p_card)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_playCard: entered for"
		                                        + " player: " + p_direction + " card: " + p_card);

		BridgeHandState currentState = m_game.getStateController().getCurrentState();
		if (currentState != BridgeHandState.WAIT_FOR_FIRST_PLAYER && currentState != BridgeHandState.WAIT_FOR_NEXT_PLAYER)
		{
			s_cat.error("evt_addScannedCard: ignoring event since state is not WAIT_FOR_FIRST_PLAYER or WAIT_FOR_NEXT_PLAYER.  State: " + currentState);
			return false;
		}

		if (p_direction != m_currentTrick.getNextPlayer())
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("playCard: ignoring attempt to play card for wrong player.  player: " + p_direction + " nextPlayer: " + m_currentTrick.getNextPlayer());
			return false;
		}
		
		if (m_cardsPlayed.contains(p_card))
		{
			for (GameListener listener : m_game.getGameListeners())
			{
				listener.sig_error(ErrorCode.CANNOT_PLAY_ALREADY_PLAYED, p_direction, p_card, null, 0);
			}
		}
		
		PlayerHand hand = m_hands.get(p_direction);
		if (hand == null) hand = m_testHands.get(p_direction);	// if we have test hands, adjust those for printhand

		if (hand != null)
		{
			// managed hand: check for valid play
			if (! hand.testPlay(p_card, m_currentTrick.getCurrentSuit()))
			{
				// announce illegal play
				for (GameListener listener : m_game.getGameListeners())
				{
					listener.sig_error(ErrorCode.CANNOT_PLAY_WRONG_SUIT, p_direction, p_card, m_currentTrick.getCurrentSuit(), 0);
				}

				// TODO: move the following code to keyboard controller
				/*
				for (KeyboardController kbdController : m_keyboardControllers.values())
				{
					if ((p_direction != m_dummyPosition && p_direction == kbdController.getMyPosition())
						||
						(p_direction == m_dummyPosition && p_direction == kbdController.getMyPartnersPosition()))
					{
						kbdController.send_cannotPlay(p_card, m_currentTrick.getCurrentSuit());
						return true;
					}
				}
				*/
				return true;
			}

			if (! hand.useCard(p_card))
			{
				// announce illegal play
				for (GameListener listener : m_game.getGameListeners())
				{
					listener.sig_error(ErrorCode.CANNOT_PLAY_NOT_IN_HAND, p_direction, p_card, null, 0);
				}
				return true;
			}
			
		}

		m_currentTrick.playCard(p_direction, p_card);
		
		m_game.getStateController().notifyStateMachine();

		if (s_cat.isDebugEnabled()) s_cat.debug("evt_playCard: finished.");

		return true;
	}
	
	/***********************************************************************
	 * Resets a Keyboard Controller and sends the current state.
	 * This runs in response to the message from the Keyboard Controller
	 * indicating that it is ready to listen.
	 * @param p_kbdController	the controller ready to reset
	 ***********************************************************************/
	public void evt_resetKeyboard (KeyboardController p_kbdController)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_resetKeyboard: entered for"
		                                        + " position: " + p_kbdController.getMyPosition());


		if (s_cat.isDebugEnabled()) s_cat.debug("evt_resetKeyboard: sending newGame");
		
		p_kbdController.send_reserveAudioPlaybackTime(false);
		p_kbdController.send_simpleMessage(KBD_MESSAGE.NEW_GAME);

		p_kbdController.setPlayer();

		// send hand, if we have it
		Direction direction = p_kbdController.getMyPosition();
		PlayerHand hand = m_hands.get(direction);
		if (hand != null) resendHand(p_kbdController, direction, hand);

		// send contract
		p_kbdController.send_simpleMessage(KBD_MESSAGE.ENTER_CONTRACT);

		if (m_contract != null && m_contract.isComplete())
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("evt_resetKeyboard: sending contract");
			p_kbdController.send_contract(m_contract);
		}

		if (m_dummyPosition != null)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("evt_resetKeyboard: sending setDummy");
			p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.SET_DUMMY, m_dummyPosition);
		}
		
		// send dummy hand, if we have it
		direction = m_dummyPosition;
		hand = m_hands.get(direction);
		if (hand != null) resendHand(p_kbdController, direction, hand);
		
		//send tricks taken
		List<Trick> tricksTaken = m_tricksTaken.get(Direction.NORTH);
		if (tricksTaken != null)
		{
			for (Trick trick : tricksTaken)
			{
				p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.TRICK_TAKEN, trick.getWinner());
			}
		}

		tricksTaken = m_tricksTaken.get(Direction.EAST);
		if (tricksTaken != null)
		{
			for (Trick trick : tricksTaken)
			{
				p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.TRICK_TAKEN, trick.getWinner());
			}
		}

		// send current play
		if (m_currentTrick != null)
		{
			for (CardPlay cardPlay : m_currentTrick.getCardsPlayed())
			{
				// set the next player before each play so first card sets current suit
				p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.SET_NEXT_PLAYER, cardPlay.getPlayer());
	
				p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.PLAY_CARD, cardPlay.getPlayer(), cardPlay.getCard());
			}

			if (m_currentTrick.getNextPlayer() != null)
			{
				p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.SET_NEXT_PLAYER, m_currentTrick.getNextPlayer());
			}
		}
		
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_resetKeyboard: sending reloadFinished");
		p_kbdController.send_reserveAudioPlaybackTime(true);
		p_kbdController.send_reloadFinished();
		
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_resetKeyboard: finished.");
	}
	
	
	/***********************************************************************
	 * Simulates the scanning of a hand by a blind player or by the dummy.
	 * Generates addScannedCard events (which sends the scanned cards to the Keyboard Controller(s)).
	 * @param p_kbdController	the position to scan
	 * @param p_testHand 		if non-negative, index of a predefined test hand.
	 ***********************************************************************/
	public void evt_scanHandTest (Direction p_direction, int p_testHand)
	{
		dealHands(p_testHand);		// noop if hands already dealt

		PlayerHand hand = m_testHands.get(p_direction);
		if (hand == null)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("scanHand: no hand available");
			return;
		}
		
		try
		{
			for (Card card : hand.getCards())
			{
				evt_addScannedCard(p_direction, card);
			}
		}
		catch (Exception e)
		{
			System.out.println ("scanHandTest: exception: " + e);
			e.printStackTrace(new PrintStream(System.out));
		}
	}

	//--------------------------------------------------
	// DATA ACCESS HELPER METHODS (used by ControllerState objects)
	//--------------------------------------------------
	
	/***********************************************************************
	 * Returns true if the hands for all of the Blind Keyboard Controllers
	 * have been completely scanned.
	 * @return true if all are complete, false if waiting for more cards
	 ***********************************************************************/
	public boolean testBlindHandsComplete()
	{
		// test hands for each direction with a Blind Keyboard Controller
		for (Direction direction : m_game.getKeyboardControllers().keySet())
		{
			PlayerHand hand = m_hands.get(direction); 
			if (hand == null || ! hand.isComplete()) return false;
		}
		return true;
	}
	
	/***********************************************************************
	 * Determines if the indicated player is a blind player.
	 * Returns true if there is a Keyboard Controller for that direction.
	 * @param p_direction	the direction to test
	 * @return true if blind
	 ***********************************************************************/
	public boolean isBlindPlayer ( Direction p_direction )
	{
		for (Direction direction : m_game.getKeyboardControllers().keySet())
		{
			if (direction == p_direction) return true;
		}
		return false;
	}
	
	/***********************************************************************
	 * Returns true if contract is complete (GUI enters contract one component at a time).
	 * @return true if complete, false, otw.
	 ***********************************************************************/
	public boolean testContractComplete ()
	{
		if (m_contract == null) return false;
		return m_contract.isComplete();
	}
	
	/***********************************************************************
	 * Returns true if the dummy hand has been completely scanned.
	 * @return true if hand is complete, false otherwise.
	 ***********************************************************************/
	public boolean testDummyComplete ()
	{
		PlayerHand hand = m_hands.get(m_dummyPosition); 
		if (hand == null || ! hand.isComplete()) return false;
		return true;
	}
	
	/***********************************************************************
	 * Returns true if all tricks have been completed.
	 * @return true if hand is complete, false otherwise
	 ***********************************************************************/
	public boolean testHandComplete ()
	{
		if (m_tricksTaken.get(Direction.NORTH).size() + m_tricksTaken.get(Direction.EAST).size() >= 13)
			return true;
		else
			return false;
	}
	
	/***********************************************************************
	 * Activities that happen at the start of a trick.
	 * Sets first player based on contract.
	 * Once a hand has started, sc_finishTrick starts the next trick, based on the trick winner.
	 * @return the position of the first player to play a card
	 ***********************************************************************/
	public Direction sc_startFirstTrick ()
	{
		Direction firstPlayer = m_contract.getBidWinner().getNextDirection();
		m_currentTrick = new Trick(firstPlayer);
		return firstPlayer;
	}
	
	/***********************************************************************
	 * Activities that happen at the end of a trick.
	 * @return The position of the winner of the trick 
	 ***********************************************************************/
	public Direction sc_finishTrick ()
	{
		Direction winner = m_currentTrick.completeTrick(m_contract.getTrump());
		m_tricksTaken.get(winner).add(m_currentTrick);
		
		m_currentTrick = new Trick(winner);
		
		return winner;
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Somehow makes an error known.
	 * At some point, this might generate Audio or write to the display
	 * @param p_message the error message
	 ***********************************************************************/
	public void announceError (String p_message)
	{
		System.out.println(p_message);
	}

	/***********************************************************************
	 * Determines who gets what points
	 ***********************************************************************/
	public void scoreContract ()
	{
		//TODO: scoring not implemented yet
	}
	
	/***********************************************************************
	 * Deals a hand for testing
	 * @param p_testHand if non-negative, index of a predefined test hand.
	 ***********************************************************************/
	private void dealHands(int p_testHand)
	{
		if (m_testHands != null && m_testHands.size() > 0) return;	// already dealt
		
		List<Card> deck = new ArrayList<>(52);
		
		if (p_testHand >= 0)
		{	// deal a predefined hand
			for (Direction direction : Direction.values())
			{
				PlayerHand hand = new PlayerHand(direction);
				m_testHands.put(direction, hand);
				
				for (int idx = 0; idx < 13; ++idx)
				{
					Card card = new Card(m_testHand[p_testHand][direction.ordinal()][idx]);
					hand.addCard(card);
				}
				if (s_cat.isDebugEnabled()) s_cat.debug("dealHand: " + hand.toString());
			}
			
		}
		
		else
			
		{	// deal a random hand
			for (Suit suit : Suit.values())
			{
				if (suit != Suit.NOTRUMP)
				{
					for (Rank cardNum : Rank.values())
						deck.add(new Card(cardNum, suit));
				}
			}
			
			Random r = new Random();
	
			for (Direction direction : Direction.values())
			{
				PlayerHand hand = new PlayerHand(direction);
				m_testHands.put(direction, hand);
				
				for (int idx = 0; idx < 13; ++idx)
				{
					int rndIndex = r.nextInt(deck.size());
					hand.addCard(deck.remove(rndIndex));
				}
				if (s_cat.isDebugEnabled()) s_cat.debug("dealHand: " + hand.toString());
			}
		}
	}
	
	
	/***********************************************************************
	 * Sends the scanned cards to the Keyboard Controller
	 * @param p_kbdController	the keyboard controller to update
	 * @param p_direction			player position whose hand is being resent (player or dummy)
	 * @param p_hand				the hand to send
	 ***********************************************************************/
	public void resendHand (KeyboardController p_kbdController, Direction p_direction, PlayerHand p_hand)
	{
		if (p_hand == null)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("resendHand: no hand available");
			return;
		}
		
		for (Card card : p_hand.getCards())
		{
			p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.ADD_CARD_TO_HAND, p_direction, card);
		}
		
		if (p_hand.isComplete())
		{
			p_kbdController.send_simpleMessage(KBD_MESSAGE.HAND_COMPLETE);
		}
	}


	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		
		out.append("BridgeHand:");
		out.append("\n  Hand State: " + m_game.getStateController().getCurrentState());
		out.append("\n  Contract: " + m_contract);
		out.append("\n  Dummy Position: " + m_dummyPosition);

		out.append(m_currentTrick == null ? "\n No Current Trick" : m_currentTrick.toString());

		List<Trick> tricksTaken = m_tricksTaken.get(Direction.NORTH);
		int numTricks = (tricksTaken == null ? 0 : tricksTaken.size());
		out.append("\n  Tricks Taken (NS): " + numTricks);

		tricksTaken = m_tricksTaken.get(Direction.EAST);
		numTricks = (tricksTaken == null ? 0 : tricksTaken.size());
		out.append("\n  Tricks Taken (EW): " + numTricks);
		
		out.append("\n\nAntennas");
		for (AntennaController antController : m_game.getAntennaControllers().values())
		{
			out.append("\n  " + antController);
		}

		out.append("\n\nKeyboards");
		for (KeyboardController kbdController : m_game.getKeyboardControllers().values())
		{
			out.append("\n  " + kbdController);
		}

		return out.toString();
	}

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * Hands as known so far
	 * @return hands known so far
	 ***********************************************************************/
	public Map<Direction, PlayerHand> getHands ()
	{
		return m_hands;
	}

	/***********************************************************************
	 * Test Hands as known so far
	 * @return hands known so far
	 ***********************************************************************/
	public Map<Direction, PlayerHand> getTestHands ()
	{
		return m_testHands;
	}

	/***********************************************************************
	 * The cards currently played and by who
	 * @return the current trick
	 ***********************************************************************/
	public Trick getCurrentTrick ()
	{
		return m_currentTrick;
	}

	/***********************************************************************
	 * The current contract.
	 * May be null.
	 * @return current contract
	 ***********************************************************************/
	public Contract getContract ()
	{
		return m_contract;
	}

	/***********************************************************************
	 * Next player to play a card.
	 * May be null.
	 * @return next player
	 ***********************************************************************/
	public Direction getNextPlayer ()
	{
		return m_currentTrick.getNextPlayer();
	}

	/***********************************************************************
	 * Position of the dummy.
	 * May be null
	 * @return dummy position
	 ***********************************************************************/
	public Direction getDummyPosition ()
	{
		return m_dummyPosition;
	}

	/***********************************************************************
	 * Position of the dummy.
	 * May be null
	 * @param p_dummyPosition dummy position
	 ***********************************************************************/
	public void setDummyPosition ( Direction p_dummyPosition )
	{
		m_dummyPosition = p_dummyPosition;
	}

	/***********************************************************************
	 * The current score of the bridge game
	 * @return score
	 ***********************************************************************/
	public BridgeScore getBridgeScore ()
	{
		return m_bridgeScore;
	}

}
