// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Category;

import lerner.blindBridge.gameController.KeyboardController.KBD_MESSAGE;
import lerner.blindBridge.gameController.KeyboardController.MULTIBYTE_MESSAGE;
import model.BridgeScore;
import model.Card;
import model.Contract;
import model.Direction;
import model.GameListener;
import model.Rank;
import model.Suit;

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
	
	/** Number of cards to make a complete player hand */
	public static final int	CARDS_IN_HAND	= 13;

	//--------------------------------------------------
	// MEMBER DATA
	//--------------------------------------------------

	// hand data
	private Map<Direction, PlayerHand> 		m_hands;

	/** for dealing complete hands for testing */
	private Map<Direction, PlayerHand> 		m_testHands;
	
	private Map<Direction, List<CardPlay>>	m_tricksTaken;
	
	private Suit								m_currentTrump;
	
	private Contract							m_contract;

	private Direction						m_dummyPosition;
	
	// trick data
	
	private List<CardPlay> 					m_currentTrick;
	
	private Suit								m_currentSuit;

	private Direction						m_nextPlayer;
	
	private Map<Direction, KeyboardController>		m_keyboardControllers = new HashMap<>();

	private Map<Direction, AntennaController>		m_antennaControllers = new HashMap<>();

	/** all of the objects that may need to be notified of state changes */
	private List<GameListener>				m_gameListeners = new ArrayList<>();;
	
	/** the state controller engine */
	private BridgeHandStateController		m_bridgeHandStateController;
	
	/** The current score of the bridge game */
	private BridgeScore						m_bridgeScore = new BridgeScore();
	
	private Set<Card>						m_cardsPlayed = new HashSet<>();

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	public BridgeHand ()
	{
		m_bridgeHandStateController = new BridgeHandStateController(this);
		
		resetHand();
	}
	
	//--------------------------------------------------
	// CONFIGURATION METHODS
	//--------------------------------------------------
	private void resetTrick ()
	{
		m_currentTrick	= new ArrayList<>();
		m_currentSuit	= null;
		
		if (s_cat.isDebugEnabled()) s_cat.debug("resetTrick: finished");
	}
	
	private void resetHand ()
	{
		resetTrick();

		m_hands			= new HashMap<>();
		m_testHands			= new HashMap<>();
		m_currentTrump	= null;
		m_contract		= null;
		m_nextPlayer		= null;	
		m_dummyPosition	= null;
		
		m_tricksTaken = new HashMap<>();
		
		List<CardPlay> list = new ArrayList<>();
		m_tricksTaken.put(Direction.NORTH, list);
		m_tricksTaken.put(Direction.SOUTH, list);	// N and S use the same list
		
		list = new ArrayList<>();
		m_tricksTaken.put(Direction.EAST, list);
		m_tricksTaken.put(Direction.WEST, list);	// E and W use the same list
		
		m_bridgeHandStateController.setForceNewState(BridgeHandState.NEW_HAND);
		
		if (s_cat.isDebugEnabled()) s_cat.debug("resetHand: finished");
	}

	/***********************************************************************
	 * Starts the state machine
	 ***********************************************************************/
	public void startGame()
	{
		m_bridgeHandStateController.runStateMachine();
	}
	
	//--------------------------------------------------
	// EVENT METHODS (PUBLIC)
	//--------------------------------------------------
	
	/***********************************************************************
	 * Indicates that we are to start a new hand.
	 * @return true if processed event and false otherwise (always returns true)
	 ***********************************************************************/
	public boolean evt_startNewHand ()
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_startNewHand: entered.");

		resetHand();

		return true;
	}
	
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

		BridgeHandState currentState = m_bridgeHandStateController.getCurrentState();
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
		
		boolean handComplete = (hand.m_cards.size() == CARDS_IN_HAND);
		
		// notify listeners of new card
		for (GameListener gameListener : m_gameListeners)
		{
			gameListener.cardScanned(p_direction, p_card, handComplete);
		}
		
		m_bridgeHandStateController.notifyStateMachine();

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

		BridgeHandState currentState = m_bridgeHandStateController.getCurrentState();
		if (currentState != BridgeHandState.ENTER_CONTRACT)
		{
			s_cat.error("evt_setContract: ignoring event since state is not ENTER_CONTRACT.  State: " + currentState);
			return false;
		}
		
		m_contract = p_contract;
		m_currentTrump = p_contract.getTrump();
		
		m_bridgeHandStateController.notifyStateMachine();
		
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

		BridgeHandState currentState = m_bridgeHandStateController.getCurrentState();
		if (currentState != BridgeHandState.WAIT_FOR_FIRST_PLAYER && currentState != BridgeHandState.WAIT_FOR_NEXT_PLAYER)
		{
			s_cat.error("evt_addScannedCard: ignoring event since state is not WAIT_FOR_FIRST_PLAYER or WAIT_FOR_NEXT_PLAYER.  State: " + currentState);
			return false;
		}

		if (p_direction != m_nextPlayer)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("playCard: ignoring attempt to play card for wrong player.  player: " + p_direction + " nextPlayer: " + m_nextPlayer);
			return false;
		}
		
		if (m_cardsPlayed.contains(p_card))
		{
			for (GameListener listener : m_gameListeners)
			{
				listener.announceError(ErrorCode.CANNOT_PLAY_ALREADY_PLAYED, p_direction, p_card, null, 0);
			}
		}
		
		PlayerHand hand = m_hands.get(p_direction);
		if (hand == null) hand = m_testHands.get(p_direction);	// if we have test hands, adjust those for printhand

		if (hand != null)
		{
			// managed hand: check for valid play
			if (! hand.testPlay(p_card, m_currentSuit))
			{
				// announce illegal play
				for (GameListener listener : m_gameListeners)
				{
					listener.announceError(ErrorCode.CANNOT_PLAY_WRONG_SUIT, p_direction, p_card, m_currentSuit, 0);
				}

				// TODO: move the following code to keyboard controller
				/*
				for (KeyboardController kbdController : m_keyboardControllers.values())
				{
					if ((p_direction != m_dummyPosition && p_direction == kbdController.getMyPosition())
						||
						(p_direction == m_dummyPosition && p_direction == kbdController.getMyPartnersPosition()))
					{
						kbdController.send_cannotPlay(p_card, m_currentSuit);
						return true;
					}
				}
				*/
				return true;
			}

			if (! hand.useCard(p_card))
			{
				// announce illegal play
				for (GameListener listener : m_gameListeners)
				{
					listener.announceError(ErrorCode.CANNOT_PLAY_NOT_IN_HAND, p_direction, p_card, null, 0);
				}
				return true;
			}
			
		}

		CardPlay cardPlay = new CardPlay(p_direction, p_card);
		m_currentTrick.add(cardPlay);
		
		m_bridgeHandStateController.notifyStateMachine();

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

		if (m_contract != null)
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
		List<CardPlay> tricksTaken = m_tricksTaken.get(Direction.NORTH);
		if (tricksTaken != null)
		{
			for (CardPlay cardPlay : tricksTaken)
			{
				p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.TRICK_TAKEN, cardPlay.getPlayer());
			}
		}

		tricksTaken = m_tricksTaken.get(Direction.EAST);
		if (tricksTaken != null)
		{
			for (CardPlay cardPlay : tricksTaken)
			{
				p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.TRICK_TAKEN, cardPlay.getPlayer());
			}
		}

		// send current play
		if (m_currentTrick.size() > 0)
		{
			for (CardPlay cardPlay : m_currentTrick)
			{
				// set the next player before each play so first card sets current suit
				p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.SET_NEXT_PLAYER, cardPlay.getPlayer());

				p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.PLAY_CARD, cardPlay.getPlayer(), cardPlay.getCard());
			}
		}

		if (m_nextPlayer != null)
		{
			p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.SET_NEXT_PLAYER, m_nextPlayer);
		}

		if (s_cat.isDebugEnabled()) s_cat.debug("evt_resetKeyboard: sending reloadFinished");
		p_kbdController.send_reserveAudioPlaybackTime(true);
		p_kbdController.send_simpleMessage(KBD_MESSAGE.FINISH_RELOAD);
		
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
			for (Card card : hand.m_cards)
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
		for (Direction direction : m_keyboardControllers.keySet())
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
		for (Direction direction : m_keyboardControllers.keySet())
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
	 * Activities that happen at the end of a trick.
	 * @return The position of the winner of the trick 
	 ***********************************************************************/
	public Direction sc_finishTrick ()
	{
		// determine winner
		CardPlay best = null;
		
		for (CardPlay cardPlay : m_currentTrick)
		{
			if (best == null || (compareCards(best.getCard(), cardPlay.getCard(), m_currentTrump) > 0))
			{
				best = cardPlay;
			}
		}
		
		m_tricksTaken.get(best.getPlayer()).add(best);
		
		resetTrick();
		m_nextPlayer = best.getPlayer();
		
		return best.getPlayer();
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
	 * Compares a card played against the current best in the trick.
	 * @param p_curBest		Current best card (assumes first card played is first curBest)
	 * @param p_testCard		Subsequent card played
	 * @param p_trumpSuit	Current trump
	 * @return 1 if test card is better, 0 otherwise
	 ***********************************************************************/
	private int compareCards (Card p_curBest, Card p_testCard, Suit p_trumpSuit)
	{
		if (p_trumpSuit != null && p_trumpSuit != Suit.NOTRUMP)
		{	// check trump
			if (p_curBest.getSuit() == p_trumpSuit)
			{
				if (p_testCard.getSuit() != p_trumpSuit) return -1;	// trump better than non-trump 
				if (p_testCard.getRank().ordinal() > p_curBest.getRank().ordinal())
					return 1;	// higher trump is better
				else
					return -1;
			}
			else if (p_testCard.getSuit() == p_trumpSuit) return 1;	// trump better than non-trump 
		}
		
		// neither card is trump (assume current suit is the suit of cur best
		if (p_curBest.getSuit() != p_testCard.getSuit()) return -1;	// test is not the right suit
		
		// suits match
		if (p_testCard.getRank().ordinal() > p_curBest.getRank().ordinal())
			return 1;	// higher is better when suits match
		else
			return -1;
	}
	
	/***********************************************************************
	 * Determines who gets what points
	 ***********************************************************************/
	public void scoreContract ()
	{
		//TODO: scoring not implemented yet
	}
	
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
	                                       {
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
		
		for (Card card : p_hand.m_cards)
		{
			p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.ADD_CARD_TO_HAND, p_direction, card);
		}
		
		boolean handComplete = (p_hand.m_cards.size() == 13);
		if (handComplete)
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
		out.append("\n  Hand State: " + m_bridgeHandStateController.getCurrentState());
		out.append("\n  Contract: " + m_contract);
		out.append("\n  Current Suit: " + m_currentSuit);
		out.append("\n  Current Trump: " + m_currentTrump);
		out.append("\n  Dummy Position: " + m_dummyPosition);
		out.append("\n  Next Player: " + m_nextPlayer);

		out.append("\n  Current Trick: ");
		for (CardPlay cardPlay : m_currentTrick)
		{
			out.append("\n    " + cardPlay);
		}

		List<CardPlay> tricksTaken = m_tricksTaken.get(Direction.NORTH);
		int numTricks = (tricksTaken == null ? 0 : tricksTaken.size());
		out.append("\n  Tricks Taken (NS): " + numTricks);

		tricksTaken = m_tricksTaken.get(Direction.EAST);
		numTricks = (tricksTaken == null ? 0 : tricksTaken.size());
		out.append("\n  Tricks Taken (EW): " + numTricks);
		
		out.append("\n\nAntennas");
		for (AntennaController antController : m_antennaControllers.values())
		{
			out.append("\n  " + antController);
		}

		out.append("\n\nKeyboards");
		for (KeyboardController kbdController : m_keyboardControllers.values())
		{
			out.append("\n  " + kbdController);
		}

		return out.toString();
	}

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * Adds a keyboard controller to the configuration
	 * @param p_direction		the position of the controller
	 * @param p_device		the device path to use (attempts to find one if null)
	 ***********************************************************************/
	public void addKeyboardController (Direction p_direction, KeyboardController p_kbdController)
	{
		m_keyboardControllers.put(p_direction, p_kbdController);
		m_gameListeners.add(p_kbdController);
	}
	
	/***********************************************************************
	 * Adds an antenna controller to the configuration
	 * @param p_direction		the position of the controller
	 * @param p_device		the device path to use (attempts to find one if null)
	 ***********************************************************************/
	public void addAntennaController (Direction p_direction, AntennaController p_antController)
	{
		m_antennaControllers.put(p_direction, p_antController);
		m_gameListeners.add(p_antController);
	}
	

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
	 * @return cards played
	 ***********************************************************************/
	public List<CardPlay> getCurrentTrick ()
	{
		return m_currentTrick;
	}

	/***********************************************************************
	 * The current suit (suit of first card played)
	 * May be null.
	 * @return current suit
	 ***********************************************************************/
	public Suit getCurrentSuit ()
	{
		return m_currentSuit;
	}

	/***********************************************************************
	 * The current suit (suit of first card played)
	 * May be null.
	 * @param p_currentSuit current suit
	 ***********************************************************************/
	public void setCurrentSuit ( Suit p_currentSuit )
	{
		m_currentSuit = p_currentSuit;
	}

	/***********************************************************************
	 * The current trump suit.
	 * May be null.
	 * @return trump suit
	 ***********************************************************************/
	public Suit getCurrentTrump ()
	{
		return m_currentTrump;
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
		return m_nextPlayer;
	}

	/***********************************************************************
	 * Next player to play a card.
	 * May be null.
	 * @param p_nextPlayer next player
	 ***********************************************************************/
	public void setNextPlayer ( Direction p_nextPlayer )
	{
		m_nextPlayer = p_nextPlayer;
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
	 * The objects to be notified of various events.
	 * @return list of listeners
	 ***********************************************************************/
	public List<GameListener> getGameListeners ()
	{
		return m_gameListeners;
	}

	/***********************************************************************
	 * The current score of the bridge game
	 * @return score
	 ***********************************************************************/
	public BridgeScore getBridgeScore ()
	{
		return m_bridgeScore;
	}

	/***********************************************************************
	 * Get the Keyboard Controller map (Direction, KeyboardController).
	 * @return keyboard controllers
	 ***********************************************************************/
	public Map<Direction, KeyboardController> getKeyboardControllers ()
	{
		return m_keyboardControllers;
	}

	/***********************************************************************
	 * Get the Antenna Controller map (Direction, AntennaController).
	 * @return antenna controllers
	 ***********************************************************************/
	public Map<Direction, AntennaController> getAntennaControllers ()
	{
		return m_antennaControllers;
	}

}
