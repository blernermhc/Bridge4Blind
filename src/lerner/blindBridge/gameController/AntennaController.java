package lerner.blindBridge.gameController;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Category;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import model.BridgeScore;
import model.Card;
import model.Contract;
import model.Direction;
import model.GameListener;
import model.Suit;

/**********************************************************************
 * Communicates with an RFID Antenna Controller 
 *********************************************************************/

public class AntennaController implements SerialPortEventListener, GameListener
{
	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(AntennaController.class.getName());


	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	private static String s_cardPresentPrefix = "CARD: ";
	private static String s_cardRemovedPrefix = "CARD REMOVED";
	
	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	/** The game data */
	Game		m_game;
	
	/** the device this controller uses */
	String m_device;


	/** The position of this Keyboard Controller */
	Direction m_myPosition;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	enum AntennaControllerState {
		CAPTURE_CARD
		, SCAN_CARDS
		, PLAY_CARDS
	};

	/** Last seen card */
	private Card m_currentCard = null;

	/** Processing state of the controller */
	private AntennaControllerState m_controllerState = AntennaControllerState.CAPTURE_CARD;
	

	SerialPort serialPort;

	/** The port we're normally going to use. */
	private static final String PORT_NAMES[] = { 
	                                            // "/dev/tty.usbserial-A9007UX1", // Mac OS X
	                                            "/dev/cu.usbmodem14641", // Mac OS X
	                                            "/dev/cu.usbmodem146331", // Mac OS X
	                                            "/dev/ttyACM0", // Raspberry Pi
	                                            "/dev/ttyUSB0", // Linux
	                                            "COM3", // Windows
											};

	/**
	* A BufferedReader which will be fed by a InputStreamReader 
	* converting the bytes into characters 
	* making the displayed results codepage independent
	*/
	private BufferedInputStream input;

	/** The output stream to the port */
	//private OutputStream output;
	
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;

	/** Default bits per second for COM port. */
	//private static final int DATA_RATE = 9600;
	private static final int DATA_RATE = 115200;
	
	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Configures and initializes a Keyboard Controller
	 * @param p_game		The game object managing the hands
	 * @param p_direction		The player position of the player using this Keyboard Controller
	 * @param p_device			The USB serial device of the antenna this controller is listening to 
	 ***********************************************************************/
	public AntennaController(Game p_game, Direction p_direction, String p_device)
	{
		m_game = p_game;
		m_myPosition = p_direction;
		m_device = p_device;
		initialize();
	}

	//--------------------------------------------------
	// COMMUNICATION METHODS
	//--------------------------------------------------

	/***********************************************************************
	 * Set up communication with the Keyboard Controller
	 * @returns false if initialization fails
	 ***********************************************************************/
	public boolean initialize()
	{
		if (m_device == null) return true;	// simulated controller, no hardware to connect to
		
		// the next line is for Raspberry Pi and 
		// gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
		//System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
		while (portEnum.hasMoreElements())
		{
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			System.out.println("CommPortId: " + currPortId.getName());
		}

		portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements())
		{
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			if (m_device != null && currPortId.getName().equals(m_device))
			{
				portId = currPortId;
				break;
			}
			else
			{
				for (String portName : PORT_NAMES)
				{
					if (currPortId.getName().equals(portName))
					{
						portId = currPortId;
						break;
					}
				}
			}
		}
		if (portId == null)
		{
			s_cat.error("Could not find COM port");
			return false;
		}

		
		try
		{
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedInputStream(serialPort.getInputStream());
			// input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			// output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		}
		catch (Exception e)
		{
			s_cat.error("initialize: failed", e);
			return false;
		}
		
		return true;
	}

	/***********************************************************************
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 ***********************************************************************/
	public synchronized void close()
	{
		if (serialPort != null)
		{
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	private String readLine (BufferedInputStream p_input)
		throws IOException
	{
		StringBuilder line = new StringBuilder();
		int ch;
		while ((ch = p_input.read()) != '\n')
		{
			// do not include carriage return or newline
			if (ch != 13) line.append((char)ch);
		}
		return line.toString();
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

	private String processCardRemovedEvent()
	{
		if (m_currentCard != null)
		{
			m_currentCard = null;
			return "Removed current card";
		}
		return "Ignore card-remove event, as there is no current card";
	}
	
	public String processCardPresentEvent ( Card p_card )
	{
		String description;
		switch (m_controllerState)
		{
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
	public synchronized void serialEvent(SerialPortEvent oEvent)
	{
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE)
		{
			try
			{
				while (input.available() > 0)
				{
					String line = readLine(input);
					String description = processLine(line);
					System.out.println("From " + m_myPosition + " Antenna: " + description);
				}
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
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
	 * @see model.GameListener#gameReset()
	 */
	@Override
	public void sig_gameReset ()
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
	public void sig_trickWon ( Direction p_winner )
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
		
		return out.toString();
	}
	
	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * The position of this Keyboard Controller
	 * @return player
	 ***********************************************************************/
	public Direction getMyPosition ()
	{
		return m_myPosition;
	}

	/***********************************************************************
	 * The most recently seen (and not removed) card.
	 * @return the card
	 ***********************************************************************/
	public synchronized Card getCurrentCard ()
	{
		return m_currentCard;
	}

}
