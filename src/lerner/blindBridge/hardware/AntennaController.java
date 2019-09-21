package lerner.blindBridge.hardware;
import java.io.IOException;

import org.apache.log4j.Category;

import jssc.SerialPortEvent;
import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.BridgeScore;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.CardLibrary;
import lerner.blindBridge.model.Contract;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.ErrorCode;
import lerner.blindBridge.model.Suit;
import lerner.blindBridge.model.Trick;

/**********************************************************************
 * Handles communication with an RFID card reader device connected
 * using a USB Serial port. Note that the Blind player's keyboard device
 * also includes an RFID card reader.  This class handles the embedded
 * card reader. An instance of KeyboardController handles the keyboard itself.
 *********************************************************************/
public class AntennaController extends JSSCSerialController
{
	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(AntennaController.class.getName());


	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	/** A name to describe this class */
	public static final String CONTROLLER_NAME = "Antenna";
	
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;

	/** Default bits per second for COM port. */
	//private static final int DATA_RATE = 115200;
	private static final int DATA_RATE = 9600;
	
	/** Start of messages sent by the Antenna Hardware during boot-up or firmware reset */
	private static final String IDENT_MSG = "Antenna(";

	/** End of message sent by the Antenna Hardware at start of boot-up or firmware reset */
	private static final String RESET_MSG = ": Resetting";

	/** End of final message sent by the Antenna Hardware during boot-up or firmware reset */
	private static final String READY_MSG = ": Reset Complete";

	/** Card present messages begin with this string */
	private static String s_cardPresentPrefix = "CARD: ";

	/** Card removed messages begin with this string */
	private static String s_cardRemovedPrefix = "CARD REMOVED";
	
	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------
	
	/** register our identity message */
	static
	{
		addIdentMsg(IDENT_MSG);
	}
	

	enum AntennaControllerState {
		  DETERMINE_POSITION
		, CAPTURE_CARD
		, SCAN_CARDS
		, PLAY_CARDS
	};

	/** Processing state of the controller */
	private AntennaControllerState m_controllerState = AntennaControllerState.CAPTURE_CARD;
	
	/** Last seen card */
	private Card m_currentCard = null;

	/** Buffer used to hold bytes read from serial port until a newline is read. */
	StringBuilder m_message = new StringBuilder();

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Configures and initializes an Antenna Controller
	 * @param p_game				The game object managing the hands
	 * @param p_direction		If non-null, the player position this controller is at.
	 * 							If null, attempts to set position based on hardware settings.
	 * @param p_deviceName		If non-empty, open the device with this name for this controller
	 * 							If null or empty, try each device in turn until you find an appropriate one
	 * @param p_hasHardware		If false, there is no hardware and the "antenna"
	 * 	will be controlled from the command interpreter (for testing)
	 * @throws IOException if it cannot open a port for this controller.
	 ***********************************************************************/
	public AntennaController(Game p_game, Direction p_direction, String p_deviceName, boolean p_hasHardware)
		throws IOException
	{
		super(p_game, p_direction, p_deviceName, p_hasHardware);
		
		if (getMyPosition() != null)
		{
			send_setPosition(getMyPosition());
			m_controllerState = AntennaControllerState.CAPTURE_CARD;
		}
	}

	//--------------------------------------------------
	// CONFIGURATION METHODS (used by openDevice)
	//--------------------------------------------------

	/* (non-Javadoc)
	 * @see lerner.blindBridge.hardware.SerialController#getName()
	 */
	public String getName() { return CONTROLLER_NAME; }
	
	/* (non-Javadoc)
	 * @see lerner.blindBridge.hardware.SerialController#getPortOpenTimeout()
	 */
	public int getPortOpenTimeout() { return TIME_OUT; }

	/* (non-Javadoc)
	 * @see lerner.blindBridge.hardware.SerialController#getPortDataRate()
	 */
	public int getPortDataRate() { return DATA_RATE; }

	/* (non-Javadoc)
	 * @see lerner.blindBridge.hardware.SerialController#getIdentMsg()
	 */
	public String getIdentMsg() { return IDENT_MSG; }
	
	/* (non-Javadoc)
	 * @see lerner.blindBridge.hardware.SerialController#getReadyMsg()
	 */
	public String getReadyMsg() { return READY_MSG; }
	
	/***********************************************************************
	 * Changes state to DETERMINE_POSITION to use card scans to specify
	 * the antenna's position.
	 ***********************************************************************/
	public void requestPosition()
	{
		m_myPosition = null;
		setReceiverThreadName(null);
		m_controllerState = AntennaControllerState.DETERMINE_POSITION;
	}
	
	//--------------------------------------------------
	// COMMUNICATION METHODS
	//--------------------------------------------------
	
	public void send_setPosition ( Direction p_direction )
	{
		if (isVirtualController()) return;
		
		if (! isDeviceReady())
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("send_setPosition(" + getFullName() + "): device is not ready yet.  Try again later");
			return;
		}

		if (m_output == null)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("send_setPosition(" + getFullName() + "): skipping non-open device. direction: " + p_direction);
			return;
		}
		
		int msg = (0b01000000 | p_direction.ordinal());
		try
		{
			m_output.write((byte)msg);
		}
		catch (Exception e)
		{
			s_cat.error("sendQueuedMessages: failed to send message: " + msg, e);
		}
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	/***********************************************************************
	 * Determines the antenna's position based on the suit of the first card scanned.
	 * Clubs is North, Diamonds is East, Hearts is South and Spades is West
	 * @param p_card		the scanned card
	 * @return the direction (null if the card is null or did not have one of these four suits.
	 ***********************************************************************/
	private Direction determinePositionFromCardScan ( Card p_card )
	{
		if (p_card == null) return null;
		
		switch ( p_card.getSuit() )
		{
			case CLUBS:		m_myPosition = Direction.NORTH; break;
			case DIAMONDS:	m_myPosition = Direction.EAST; break;
			case HEARTS:		m_myPosition = Direction.SOUTH; break;
			case SPADES:		m_myPosition = Direction.WEST; break;
			default:			return null;
		}
		
		setReceiverThreadName(null);
		return m_myPosition;
	}

	/***********************************************************************
	 * Parses the output of the antenna controller, looking for cards present
	 * and card removed events.  If any are found, processes them.
	 * Lines using the following format are processed.  Other lines are simply returned.
	 *   Card present events:
	 *     4 byte hex:		"CARD: 0xAAbbCCdd"
	 *     7 byte hex:		"CARD: 0xAAbbCCddEEffGG"
	 *     
	 *   Card removed events:
	 *                      "CARD REMOVED"
	 *                      
	 * @param p_line the line to parse
	 * @return a message to print describing the message from the antenna
	 ***********************************************************************/
	private String processLine (String p_line)
	{
		if (p_line.endsWith(RESET_MSG))
		{
			m_deviceReady = false;
			return p_line;
		}
		
		if (p_line.endsWith(READY_MSG))
		{
			m_deviceReady = true;
			return p_line;
		}
		
		if (p_line.startsWith(s_cardRemovedPrefix))
		{
			return processCardRemovedEvent();
		}
		
		if (! p_line.startsWith(s_cardPresentPrefix))
		{
			return p_line;
		}
		
		String cardId = p_line.substring(s_cardPresentPrefix.length());
		if (! cardId.startsWith("0x"))
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("processLine: illegal hex value (no 0x): " + cardId);
			return p_line;
		}
		
		cardId = cardId.substring(2).trim();	// remove leading "0x:"

		if (cardId.contains(" "))
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("processLine: unexpected space in hex string): " + cardId);
			return p_line;
		}
			
		Card card = CardLibrary.findCard(cardId.toString());
		return processCardPresentEvent(card);
	}

	/***********************************************************************
	 * Actions to take when a card has been removed from the antenna
	 * @return a description of the actions taken
	 ***********************************************************************/
	public String processCardRemovedEvent()
	{
		if (m_currentCard != null)
		{
			m_currentCard = null;
			return "Removed current card";
		}
		return "Ignore card-remove event, as there is no current card";
	}
	
	/***********************************************************************
	 * Actions to take when a card has appeared at the antenna.
	 * @param p_card		the card present
	 * @return a description of the actions taken
	 ***********************************************************************/
	public String processCardPresentEvent ( Card p_card )
	{
		String description;
		switch (m_controllerState)
		{
			case DETERMINE_POSITION:
			{
				if (determinePositionFromCardScan (p_card) != null)
				{
					description = "Card scan set my postion to: " + m_myPosition;
					m_controllerState = AntennaControllerState.CAPTURE_CARD;
					send_setPosition(m_myPosition);
				}
				else
				{
					description = "Could not set my position from card scan: " + p_card;
				}
			}
			break;
			
			case CAPTURE_CARD:
			{
				m_currentCard = p_card;
				description = "Captured card: " + p_card;
			}
			break;
			
			case SCAN_CARDS:
			{
				m_game.getBridgeHand().evt_addScannedCard(m_myPosition, p_card);
				m_currentCard = null;
				description = "Scanned card: " + p_card;
			}
			break;
			
			case PLAY_CARDS:
			{
				boolean accepted = m_game.getBridgeHand().evt_playCard(m_myPosition, p_card);
				if (!accepted) m_currentCard = p_card;
				else m_currentCard = null;
				description = "Played card: " + p_card;
			}
			break;
			
			default:
			{
				if (s_cat.isDebugEnabled()) s_cat.debug("processCardPresentEvent: unknown state: " + m_controllerState);
				description = "unknown state: " + m_controllerState;
			}
		}
		return description;
	}
	
	/* *******************************************************************
	 * (non-Javadoc)
	 * @see gnu.io.SerialPortEventListener#serialEvent(gnu.io.SerialPortEvent)
	 * Handle an event on the serial port. Read the data and print it.
	 * *******************************************************************/
	public synchronized void serialEvent(SerialPortEvent p_event)
	{
		if (getReceiverThreadName() == null) updateReceiverThreadName("Ant");

		if(p_event.isRXCHAR() && p_event.getEventValue() > 0)
		{
	        try
			{
	            byte buffer[] = readBytesFromPort();
	            for (byte b: buffer)
				{
					if ( b == '\n')
					{
						if (m_message.length() > 0)
						{
							String toProcess = m_message.toString();
							String description = processLine(toProcess);
							System.out.println("From " + getFullName() + ": " + description);
							m_message.setLength(0);
						}
					}
					else
					{
						// do not include carriage return
						if (b != 13) m_message.append((char)b);
					}
	            }                
	        }
	        catch (Exception e)
			{
	        		//TODO: NOTE: if this is triggered, bytes read from the device may be lost.
	        		// However, process method should never throw an exception, so this should not happen
				s_cat.error("serialEvent: FAILURE: e.toString()",e);
	        }
	    }
		// Ignore all the other eventTypes, but you should consider the other ones.
	}

	//--------------------------------------------------
	// GAME LISTENER METHODS
	//--------------------------------------------------

	/* (non-Javadoc)
	 * @see model.GameListener#debugMsg(java.lang.String)
	 */
	@Override
	public void sig_debugMsg ( String p_string )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see model.GameListener#sig_initializing()
	 */
	public void sig_initializing ()
	{
		// nothing to do
	}
	
	/* (non-Javadoc)
	 * @see model.GameListener#gameReset()
	 */
	@Override
	public void sig_gameReset ()
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_gameReset_undo(boolean, boolean)
	 */
	@Override
	public void sig_gameReset_undo ( boolean p_redoFlag, boolean p_confirmed )
	{
		// nothing to do
	}
	
	/* (non-Javadoc)
	 * @see model.GameListener#scanBlindHands()
	 */
	@Override
	public void sig_scanBlindHands ()
	{
		if (m_game.getBridgeHand().isBlindPlayer(m_myPosition))
		{
			m_controllerState = AntennaControllerState.SCAN_CARDS;
			if (m_currentCard != null)
			{
				m_game.getBridgeHand().evt_addScannedCard(m_myPosition, m_currentCard);
				m_currentCard = null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see model.GameListener#scanDummyHand()
	 */
	@Override
	public void sig_scanDummyHand ()
	{
		if (m_myPosition == m_game.getBridgeHand().getDummyPosition())
		{
			m_controllerState = AntennaControllerState.SCAN_CARDS;
			if (m_currentCard != null)
			{
				m_game.getBridgeHand().evt_addScannedCard(m_myPosition, m_currentCard);
				m_currentCard = null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see model.GameListener#cardScanned(model.Direction, model.Card, boolean)
	 */
	@Override
	public void sig_cardScanned ( Direction p_direction, Card p_card, boolean p_handComplete )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_cardScanned_undo(boolean, lerner.blindBridge.model.Direction, lerner.blindBridge.model.Card, boolean, boolean)
	 */
	@Override
	public void sig_cardScanned_undo ( boolean p_redoFlag, boolean p_confirmed, Direction p_direction, Card p_card, boolean p_handComplete )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_cardPlayed_undo(boolean, boolean, lerner.blindBridge.model.Direction, lerner.blindBridge.model.Card)
	 */
	@Override
	public void sig_cardPlayed_undo ( boolean p_redoFlag, boolean p_confirmed, Direction p_direction, Card p_card )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see model.GameListener#blindHandsScanned()
	 */
	@Override
	public void sig_blindHandsScanned ()
	{
		if (m_game.getBridgeHand().isBlindPlayer(m_myPosition))
		{
			m_controllerState = AntennaControllerState.CAPTURE_CARD;
		}
	}

	/* (non-Javadoc)
	 * @see model.GameListener#dummyHandScanned()
	 */
	@Override
	public void sig_dummyHandScanned ()
	{
		if (m_myPosition == m_game.getBridgeHand().getDummyPosition())
		{
			m_controllerState = AntennaControllerState.CAPTURE_CARD;
		}
	}

	/* (non-Javadoc)
	 * @see model.GameListener#enterContract()
	 */
	@Override
	public void sig_enterContract ()
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see model.GameListener#contractSet(model.Contract)
	 */
	@Override
	public void sig_contractSet ( Contract p_contract )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_contractSet_undo(boolean, boolean, lerner.blindBridge.model.Contract)
	 */
	@Override
	public void sig_contractSet_undo ( boolean p_redoFlag, boolean p_confirmed, Contract p_contract )
	{
		// nothing to do
	}
	
	/* (non-Javadoc)
	 * @see model.GameListener#setDummyPosition(model.Direction)
	 */
	@Override
	public void sig_setDummyPosition ( Direction p_direction )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see model.GameListener#setNextPlayer(model.Direction)
	 */
	@Override
	public void sig_setNextPlayer ( Direction p_direction )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("setNextPlayer: [" + m_myPosition + "] next: "+ p_direction);
		if (m_myPosition == p_direction)
		{
			m_controllerState = AntennaControllerState.PLAY_CARDS;
			if (m_currentCard != null)
			{
				boolean cardPlayed = m_game.getBridgeHand().evt_playCard(m_myPosition, m_currentCard);
				if (cardPlayed) m_currentCard = null;
			}
		}
		else
		{
			m_controllerState = AntennaControllerState.CAPTURE_CARD;
		}
		if (s_cat.isDebugEnabled()) s_cat.debug("setNextPlayer(out): " + this.toString());
	}

	/* (non-Javadoc)
	 * @see model.GameListener#setCurrentSuit(model.Suit)
	 */
	@Override
	public void sig_setCurrentSuit ( Suit p_suit )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see model.GameListener#cardPlayed(model.Direction, model.Card)
	 */
	@Override
	public void sig_cardPlayed ( Direction p_direction, Card p_card )
	{
		if (m_myPosition == p_direction)
		{
			m_controllerState = AntennaControllerState.CAPTURE_CARD;
		}
	}

	/* (non-Javadoc)
	 * @see model.GameListener#trickWon(model.Direction)
	 */
	@Override
	public void sig_trickWon ( Trick p_winner )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see model.GameListener#handComplete(model.BridgeScore)
	 */
	@Override
	public void sig_handComplete ( BridgeScore p_score )
	{
		// nothing to do
	}
	
	/* (non-Javadoc)
	 * @see model.GameListener#announceError(lerner.blindBridge.gameController.ErrorCode, model.Direction, model.Card, model.Suit, int)
	 */
	@Override
	public void sig_error (	ErrorCode p_errorCode,
								Direction p_direction,
								Card p_card,
								Suit p_suit,
								int p_num )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		
		out.append("Antenna[" + m_myPosition + "]");
		out.append(" state: " + m_controllerState);
		out.append(" curCard: " + m_currentCard);
		out.append(" device: " + (m_virtualController ? "virtual" : m_serialPort.getPortName()));
		
		return out.toString();
	}
	
	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * The most recently seen (and not removed) card.
	 * @return the card
	 ***********************************************************************/
	public synchronized Card getCurrentCard ()
	{
		return m_currentCard;
	}

}
