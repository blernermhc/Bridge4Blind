// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Category;

import lerner.blindBridge.gameController.KeyboardController.KBD_MESSAGE;
import lerner.blindBridge.gameController.KeyboardController.MULTIBYTE_MESSAGE;

/***********************************************************************
 * Represents the state of the current hand and manages the hand play
 * The hand can be in one of the following states (HandState m_handState):
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
 *  Keyboard Controller.  This is the final state until the hand is complete.
 *  At which time the state moves back to SCAN_BLIND_HANDS.
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
 ***********************************************************************/
public class BridgeHand
{

	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(BridgeHand.class.getName());

	public enum HandState {
		  SCAN_BLIND_HANDS
		  , ENTER_CONTRACT
		  , WAIT_FOR_FIRST_PLAYER
		  , SCAN_DUMMY
		  , WAIT_FOR_NEXT_PLAYER
		  , TRICK_COMPLETE
		  , HAND_COMPLETE
	};
	
	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	private HandState			m_handState		= HandState.SCAN_BLIND_HANDS;
	
	// hand data
	private Map<Direction, PlayerHand> m_hands;

	/** for dealing complete hands for testing */
	private Map<Direction, PlayerHand> m_testHands;
	
	private Map<Direction, List<CardPlay>>	m_tricksTaken;
	
	private Suit					m_currentTrump;
	
	private Contract				m_contract;

	private Direction				m_dummyPosition;
	
	// trick data
	
	private List<CardPlay> 		m_currentTrick;
	
	private Suit					m_currentSuit;

	private Direction				m_nextPlayer;
	
	private Map<Direction, KeyboardController>		m_keyboardControllers;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	public BridgeHand (Map<Direction, KeyboardController> p_keyboardControllers)
	{
		m_keyboardControllers = p_keyboardControllers;
		resetHand();
	}
	
	private void resetTrick ()
	{
		m_currentTrick	= new ArrayList<>();
		m_currentSuit	= null;
		m_handState = HandState.WAIT_FOR_FIRST_PLAYER;
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
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_startNewHand: entered.  State: " + m_handState);

		resetHand();
		for (KeyboardController kbdController : m_keyboardControllers.values())
		{
			kbdController.send_simpleMessage(KBD_MESSAGE.NEW_HAND);
		}
		for (KeyboardController kbdController : m_keyboardControllers.values())
		{
			kbdController.send_simpleMessage(KBD_MESSAGE.SCAN_HAND);
		}
		m_handState = HandState.SCAN_BLIND_HANDS;

		if (s_cat.isDebugEnabled()) s_cat.debug("evt_startNewHand: finished.  State: " + m_handState);
			
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
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_addScannedCard: entered.  State: " + m_handState
		                                        + " player: " + p_direction + " card: " + p_card);

		if (m_handState != HandState.SCAN_BLIND_HANDS && m_handState != HandState.SCAN_DUMMY)
		{
			s_cat.error("evt_addScannedCard: ignoring event since state is not SCAN_BLIND_HANDS or SCAN_DUMMY.  State: " + m_handState);
			return false;
		}
		
		PlayerHand hand = m_hands.get(p_direction);
		if (hand == null)
		{
			hand = new PlayerHand(p_direction);
			m_hands.put(p_direction, hand);
		}
		hand.addCard(p_card);
		
		boolean handComplete = (hand.m_cards.size() == 13);
		
		for (KeyboardController kbdController : m_keyboardControllers.values())
		{
			if (p_direction == m_dummyPosition || p_direction == kbdController.getMyPosition())
			{
				kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.ADD_CARD_TO_HAND, p_direction, p_card);
				if (handComplete)
				{
					kbdController.send_simpleMessage(KBD_MESSAGE.HAND_COMPLETE);
				}
			}
		}
		
		if (m_handState == HandState.SCAN_BLIND_HANDS && handComplete)
		{
			boolean complete = true;
			for (KeyboardController kbdController : m_keyboardControllers.values())
			{
				PlayerHand blindHand = m_hands.get(kbdController.getMyPosition());
				if (blindHand == null || blindHand.m_cards.size() != 13)
				{
					complete = false;
					break;
				}
			}
			if (complete) sc_finishedBlindScan();
		}

		if (m_handState == HandState.SCAN_DUMMY && handComplete && p_direction == m_dummyPosition)
		{
			sc_finishedDummyScan();
		}
		
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_addScannedCard: finished.  State: " + m_handState);

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
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_setContract: entered.  State: " + m_handState
		                                        + " contract: " + p_contract);

		if (m_handState != HandState.ENTER_CONTRACT)
		{
			s_cat.error("evt_setContract: ignoring event since state is not ENTER_CONTRACT.  State: " + m_handState);
			return false;
		}
		
		m_contract = p_contract;
		m_currentTrump = p_contract.getTrump();
		for (KeyboardController kbdController : m_keyboardControllers.values())
		{
			kbdController.send_contract(p_contract);
		}

		// set first player
		m_nextPlayer = p_contract.getBidWinner().getNextDirection();
		m_dummyPosition = m_nextPlayer.getNextDirection();
		for (KeyboardController kbdController : m_keyboardControllers.values())
		{
			kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.SET_NEXT_PLAYER, m_nextPlayer);
		}
		for (KeyboardController kbdController : m_keyboardControllers.values())
		{
			kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.SET_DUMMY, m_dummyPosition);
		}

		// update state
		m_handState = HandState.WAIT_FOR_FIRST_PLAYER;

		if (s_cat.isDebugEnabled()) s_cat.debug("evt_setContract: finished.  State: " + m_handState);

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
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_playCard: entered.  State: " + m_handState
		                                        + " player: " + p_direction + " card: " + p_card);

		if (m_handState != HandState.WAIT_FOR_FIRST_PLAYER && m_handState != HandState.WAIT_FOR_NEXT_PLAYER)
		{
			s_cat.error("evt_addScannedCard: ignoring event since state is not WAIT_FOR_FIRST_PLAYER or WAIT_FOR_NEXT_PLAYER.  State: " + m_handState);
			return false;
		}

		if (p_direction != m_nextPlayer)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("playCard: ignoring attempt to play card for wrong player.  player: " + p_direction + " nextPlayer: " + m_nextPlayer);
			return false;
		}
		
		PlayerHand hand = m_hands.get(p_direction);
		if (hand == null) hand = m_testHands.get(p_direction);	// if we have test hands, adjust those for printhand

		if (hand != null)
		{
			// managed hand: check for valid play
			if (! hand.testPlay(p_card, m_currentSuit))
			{
				// announce illegal play
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
				announceError("Cannot play card, suit is " + m_currentSuit);
				return true;
			}

			if (! hand.useCard(p_card))
			{
				s_cat.error("evt_playCard: did not play card, not in hand: " + p_card);
				return true;
			}
			
		}

		CardPlay cardPlay = new CardPlay(p_direction, p_card);
		m_currentTrick.add(cardPlay);
		
		for (KeyboardController kbdController : m_keyboardControllers.values())
		{
			kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.PLAY_CARD, p_direction, p_card);
		}
		
		if (m_currentTrick.size() == 4)
		{
			sc_finishTrick();
			// sets next player to winner of trick
		}
		else if (m_currentTrick.size() == 1)
		{
			sc_firstCardPlayed(cardPlay);
			m_nextPlayer = m_nextPlayer.getNextDirection();
		}
		else
		{
			m_handState = HandState.WAIT_FOR_NEXT_PLAYER;
			m_nextPlayer = m_nextPlayer.getNextDirection();
		}
			
		for (KeyboardController kbdController : m_keyboardControllers.values())
		{
			kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.SET_NEXT_PLAYER, m_nextPlayer);
		}
		
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_playCard: finished.  State: " + m_handState);

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
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_resetKeyboard: entered.  State: " + m_handState
		                                        + " position: " + p_kbdController.getMyPosition());


		if (s_cat.isDebugEnabled()) s_cat.debug("evt_resetKeyboard: sending newGame");
		
		p_kbdController.send_simpleMessage(KBD_MESSAGE.NEW_GAME);
		p_kbdController.setMessageReserveMillis(0);

		p_kbdController.setPlayer();
		p_kbdController.setMessageReserveMillis(0);

		// send hand, if we have it
		Direction direction = p_kbdController.getMyPosition();
		PlayerHand hand = m_hands.get(direction);
		if (hand != null) resendHand(p_kbdController, direction, hand);

		// send contract
		p_kbdController.send_simpleMessage(KBD_MESSAGE.ENTER_CONTRACT);
		p_kbdController.setMessageReserveMillis(0);

		if (m_contract != null)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("evt_resetKeyboard: sending contract");
			p_kbdController.send_contract(m_contract);
			p_kbdController.setMessageReserveMillis(0);
		}

		if (m_dummyPosition != null)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("evt_resetKeyboard: sending setDummy");
			p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.SET_DUMMY, m_dummyPosition);
			p_kbdController.setMessageReserveMillis(0);
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
				p_kbdController.setMessageReserveMillis(0);
			}
		}

		tricksTaken = m_tricksTaken.get(Direction.EAST);
		if (tricksTaken != null)
		{
			for (CardPlay cardPlay : tricksTaken)
			{
				p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.TRICK_TAKEN, cardPlay.getPlayer());
				p_kbdController.setMessageReserveMillis(0);
			}
		}

		// send current play
		if (m_currentTrick.size() > 0)
		{
			for (CardPlay cardPlay : m_currentTrick)
			{
				// set the next player before each play so first card sets current suit
				p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.SET_NEXT_PLAYER, cardPlay.getPlayer());
				p_kbdController.setMessageReserveMillis(0);

				p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.PLAY_CARD, cardPlay.getPlayer(), cardPlay.getCard());
				p_kbdController.setMessageReserveMillis(0);
			}
		}

		if (m_nextPlayer != null)
		{
			p_kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.SET_NEXT_PLAYER, m_nextPlayer);
			p_kbdController.setMessageReserveMillis(0);
		}

		if (s_cat.isDebugEnabled()) s_cat.debug("evt_resetKeyboard: sending reloadFinished");
		p_kbdController.setMessageReserveMillis(1000); // ensure at least one second between reset start and finish audio
		p_kbdController.send_simpleMessage(KBD_MESSAGE.FINISH_RELOAD);
		
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_resetKeyboard: finished.  State: " + m_handState);
	}
	
	
	/***********************************************************************
	 * Simulates the scanning of a hand by a blind player or by the dummy.
	 * Generates addScannedCard events (which sends the scanned cards to the Keyboard Controller(s)).
	 * @param p_kbdController	the position to scan
	 ***********************************************************************/
	public void evt_scanHandTest (Direction p_direction)
	{
		dealHands();		// noop if hands already dealt

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
	// STATE CHANGE HELPER METHODS (PRIVATE)
	//--------------------------------------------------
	
	/***********************************************************************
	 * Actions to take once all blind keyboard controllers have completely scanned their cards.
	 * Changes state to ENTER_CONTRACT
	 ***********************************************************************/
	private void sc_finishedBlindScan ()
	{
		m_handState = HandState.ENTER_CONTRACT;
		
		for (KeyboardController kbdController : m_keyboardControllers.values())
		{
			kbdController.send_simpleMessage(KBD_MESSAGE.ENTER_CONTRACT);
		}
		if (s_cat.isDebugEnabled()) s_cat.debug("sc_finishedBlindScan: finished.  State: " + m_handState);
	}

	/***********************************************************************
	 * Actions to take once the dummy has completely scanned their cards
	 * Changes state to WAIT_FOR_NEXT_PLAYER
	 ***********************************************************************/
	private void sc_finishedDummyScan ()
	{
		m_handState = HandState.WAIT_FOR_NEXT_PLAYER;
		if (s_cat.isDebugEnabled()) s_cat.debug("sc_finishedDummyScan: finished.  State: " + m_handState);
	}

	/***********************************************************************
	 * Special processing run after the first card of a trick has been played.
	 * Sets the current suit for the trick.
	 * Changes the state to either SCAN_DUMMY or WAIT_FOR_NEXT_PLAYER.
	 * @param p_cardPlay	card being played
	 ***********************************************************************/
	private void sc_firstCardPlayed (CardPlay p_cardPlay)
	{
		m_currentSuit = p_cardPlay.getCard().getSuit();
		
		if (m_hands.get(m_dummyPosition) == null)
		{
			PlayerHand hand = new PlayerHand(m_dummyPosition);
			m_hands.put(m_dummyPosition, hand);
			m_handState = HandState.SCAN_DUMMY;
			
			// run Keyboard Controller hooks
			for (KeyboardController kbdController : m_keyboardControllers.values())
			{
				kbdController.send_simpleMessage(KBD_MESSAGE.SCAN_DUMMY);
			}
		}
		else
		{
			m_handState = HandState.WAIT_FOR_NEXT_PLAYER;
		}
		if (s_cat.isDebugEnabled()) s_cat.debug("sc_firstCardPlayed: finished.  State: " + m_handState);
	}
	
	/***********************************************************************
	 * Activities that happen at the end of a trick.
	 * Sets state to WAIT_FOR_FIRST_PLAYER
	 ***********************************************************************/
	private void sc_finishTrick ()
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

		for (KeyboardController kbdController : m_keyboardControllers.values())
		{
			kbdController.send_multiByteMessage(MULTIBYTE_MESSAGE.TRICK_TAKEN, best.getPlayer());
		}
		
		if (m_tricksTaken.get(Direction.NORTH).size() + m_tricksTaken.get(Direction.EAST).size() >= 13)
		{
			sc_finishHand();
		}
		
		m_handState = HandState.WAIT_FOR_FIRST_PLAYER;
		if (s_cat.isDebugEnabled()) s_cat.debug("sc_finishTrick: finished.  State: " + m_handState);
	}
	
	/***********************************************************************
	 * Actions taken when hand is finished (all thirteen tricks taken).
	 ***********************************************************************/
	private void sc_finishHand()
	{
		scoreContract();
		evt_startNewHand();
		if (s_cat.isDebugEnabled()) s_cat.debug("sc_finishHand: finished.  State: " + m_handState);
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
	private void scoreContract ()
	{
		//TODO: scoring not implemented yet
	}
	
	/***********************************************************************
	 * Deals a hand for testing
	 ***********************************************************************/
	private void dealHands()
	{
		if (m_testHands != null && m_testHands.size() > 0) return;	// already dealt
		
		List<Card> deck = new ArrayList<>(52);
		
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
			p_kbdController.setMessageReserveMillis(0);
		}
		
		boolean handComplete = (p_hand.m_cards.size() == 13);
		if (handComplete)
		{
			p_kbdController.send_simpleMessage(KBD_MESSAGE.HAND_COMPLETE);
			p_kbdController.setMessageReserveMillis(0);
		}
	}


	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		
		out.append("BridgeHand:");
		out.append("\n  Hand State: " + m_handState);
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

		return out.toString();
	}

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * The current state of play 
	 * @return current state
	 ***********************************************************************/
	public HandState getHandState ()
	{
		return m_handState;
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
	 * Position of the dummy.
	 * May be null
	 * @return dummy position
	 ***********************************************************************/
	public Direction getDummyPosition ()
	{
		return m_dummyPosition;
	}

}
