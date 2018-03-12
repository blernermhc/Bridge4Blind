package lerner.blindBridge.gameController;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

import org.apache.log4j.Category;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**********************************************************************
 * Communicates with a Blind players' Keyboard Controller 
 *********************************************************************/

public class KeyboardController implements SerialPortEventListener
{
	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(KeyboardController.class.getName());

	public enum Button {
		PLAY
		, H1, H2, H3, H4, HC
		, D1, D2, D3, D4, DC
		, UP, DOWN
		, REPEAT, STATE
		, UNDO, MASTERUNDO
	};
	
	/**
	 *********************************************************************
	 * Parameter-less Messages to send to Keyboard Controller.
	 * The first parameter is the ID of the event to send in the message.
	 * The second parameter is the time in milliseconds we expect the
	 * audio associated with the event will take to play.
	 * The system ensures that once a message is sent, the next message
	 * will be sent no sooner than allowed by the reserve. 
	 **********************************************************************
	 */
	public enum KBD_MESSAGE {
		NOOP					(0, 0)
		, SCAN_HAND			(1, 1500)
		, SCAN_DUMMY			(2, 1500)
		, CLEAR_DUMMY		(3, 0)
		, REMIND_PLAY		(4, 1000)
		, REMIND_DUMMY		(5, 1000)
		, HAND_COMPLETE		(6, 1000)
		, ALREADY_PLAYED		(7, 2000)
		, PRESS_TO_CONFIRM	(8, 2000)
		, NEW_GAME			(9, 0)
		, NEW_HAND			(10, 0)
		, START_RELOAD		(11, 1000)
		, FINISH_RELOAD		(12, 1000)
		, ENTER_CONTRACT		(13, 1500)
		, CANNOT_PLAY		(14, 1500)
		;
		
		private int m_msgId;
		private int m_reserveInMillis;
		
		KBD_MESSAGE (int p_msgId, int p_reserveInMillis)
		{
			m_msgId				= p_msgId;
			m_reserveInMillis	= p_reserveInMillis;
		}
		
		public int getMsgId() { return m_msgId; }
		public int getReserveInMillis() { return m_reserveInMillis; }
	};

	/**
	 *********************************************************************
	 * Multi-byte Messages to send to Keyboard Controller.
	 * The first parameter is the ID of the event to send in the message.
	 * The second parameter is the time in milliseconds we expect the
	 * audio associated with the event will take to play.
	 * The system ensures that once a message is sent, the next message
	 * will be sent no sooner than allowed by the reserve. 
	 **********************************************************************
	 */
	public enum MULTIBYTE_MESSAGE {
		NOOP					(0, 0)
		, SET_PLAYER			(1, 2000)
		, SET_DUMMY			(2, 2000)
		, SET_NEXT_PLAYER	(3, 0)
		, SET_CONTRACT		(4, 3000)
		, ADD_CARD_TO_HAND	(5, 1500)
		, PLAY_CARD			(6, 3000)
		, UNPLAY_CARD		(7, 3000)
		, TRICK_TAKEN		(8, 1500)
		, CANNOT_PLAY		(9, 4000)
		;
		
		private int m_msgId;
		private int m_reserveInMillis;
		
		MULTIBYTE_MESSAGE (int p_msgId, int p_reserveInMillis)
		{
			m_msgId				= p_msgId;
			m_reserveInMillis	= p_reserveInMillis;
		}
		
		public int getMsgId() { return m_msgId; }
		public int getReserveInMillis() { return m_reserveInMillis; }
	};

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	/** Final message sent by the Keyboard Controller during boot-up */
	static final String READY_MSG = "Ready!";

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	BlindBridgeMain	m_gameController;
	
	/** the device this controller uses */
	String m_device;

	/** 
	 * If true, read ASCII newline-terminated messages from
	 * the Keyboard Controller until the final setup message is received.
	 * Then switch back to byte-message mode.
	 */
	boolean m_readLineEventMode = false;
	
	/**
	 * If true, read one ASCII newline-terminated message from
	 * the Keyboard Controller and then switch back to byte message
	 * mode.
	 */
	boolean m_readOneLineEventMode = false;
	
	/**
	 * The position of this Keyboard Controller
	 */
	Direction m_myPosition;
	
	/**
	 * The position of the partner of this controller
	 */
	Direction m_myPartnersPosition;
	
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------
	

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
	private OutputStream output;
	
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;

	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
	
	
	/** Maximum delay to insert to ensure reserve time between messages (in milliseconds) */
	private static final long MAX_RESERVE_DELAY_MILLIS = 5000;  

	/**
	 * Time last message was sent to the Keyboard Controller. 
	 * This is used to ensure that we space messages to the Keyboard
	 * Controller sufficiently to allow the blind player time to hear the messages.
	 */
	private long m_timeOfLastMessage = System.currentTimeMillis();
	
	/**
	 * The amount of time to reserve to ensure that the next message can be heard
	 */
	private long m_messageReserveMillis = 0;
	
	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Configures and initializes a Keyboard Controller
	 * @param p_gameController	The gameController managing the hands
	 * @param p_direction			The player position of the player using this Keyboard Controller
	 ***********************************************************************/
	public KeyboardController(BlindBridgeMain p_gameController, Direction p_direction, String p_device)
	{
		m_gameController = p_gameController;
		m_device = p_device;
		initialize();
		setPlayer(p_direction);
	}

	/***********************************************************************
	 * Sends a message to the Keyboard Controller to indicate the playerId of the keyboard controller.
	 * (i.e., which position is the blind player playing)
	 * Logs an error if the message fails.
	 * @param p_direction	the position
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean setPlayer(Direction p_direction)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("setPlayer: entered" + " player: " + p_direction);

		m_myPosition = p_direction;
		m_myPartnersPosition = p_direction.getPartner();
		
		boolean status = send_multiByteMessage( MULTIBYTE_MESSAGE.SET_PLAYER
		                                        , 0
		                                        , p_direction.ordinal()
		                                        , 0
                								  );

		if (s_cat.isDebugEnabled()) s_cat.debug("setPlayer: finished");
		return status;
	}
	
	/***********************************************************************
	 * Resends a message to the Keyboard Controller to indicate the playerId of the keyboard controller.
	 * (i.e., which position is the blind player playing)
	 * Logs an error if the message fails.
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean setPlayer()
	{
		if (m_myPosition == null)
		{
			s_cat.error("setPlayer: myPosition is not set.");
			return false;
		}

		if (s_cat.isDebugEnabled()) s_cat.debug("setPlayer: setting player to: " + m_myPosition);
		return setPlayer(m_myPosition);
	}
	
	
	//--------------------------------------------------
	// METHODS TO SEND EVENTS TO Keyboard Controller
	//--------------------------------------------------
	
	/***********************************************************************
	 * Sends a parameter-less message to the Keyboard Controller
	 * Logs an error if the message fails.
	 * @param p_msg	the message to send
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_simpleMessage(KBD_MESSAGE p_msg)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_simpleMessage: entered"
		                                        + " msg: " + p_msg);
		boolean status	= true;
		int msg			= p_msg.getMsgId();
		int reserve		= p_msg.getReserveInMillis();
		try
		{
			ensureMessageReserve(reserve);
			output.write((byte)msg);			
		}
		catch (IOException e)
		{
			s_cat.error("send_simpleMessage: failed:", e);
			status = false;
		}
		if (s_cat.isDebugEnabled()) s_cat.debug("send_simpleMessage: finished");
		return status;
	}
	
	/***********************************************************************
	 * Sends a generic multi-byte message to the Keyboard Controller.
	 * Logs an error if the message fails.
	 * @param p_msg		the message to send
	 * @param p_direction	the player position to send in the message
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_multiByteMessage(MULTIBYTE_MESSAGE p_msg, Direction p_direction)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_multiByteMessage(p): entered"
		                                        + " msg: " + p_msg
		                                        + " player: " + p_direction
		                                        );
		boolean status = send_multiByteMessage(p_msg, 0, p_direction.ordinal(), 0);
		
		if (s_cat.isDebugEnabled()) s_cat.debug("send_multiByteMessage(p): finished");
		return status;
	}
	
	/***********************************************************************
	 * Sends a generic multi-byte message to the Keyboard Controller.
	 * Logs an error if the message fails.
	 * @param p_msg		the message to send
	 * @param p_direction	the player position to send in the message
	 * @param p_card		a card to include in the message
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_multiByteMessage(MULTIBYTE_MESSAGE p_msg, Direction p_direction, Card p_card)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_multiByteMessage(pc): entered"
		                                        + " msg: " + p_msg
		                                        + " player: " + p_direction
		                                        + " card: " + p_card
		                                        );
		boolean status = send_multiByteMessage( p_msg
		                                        , p_card.getSuit().ordinal()
		                                        , p_direction.ordinal()
		                                        , p_card.getRank().ordinal()
		                                        );
		
		if (s_cat.isDebugEnabled()) s_cat.debug("send_multiByteMessage(pc): finished");
		return status;
	}
	
	/***********************************************************************
	 * Sends a message to the Keyboard Controller indicating the current contract.
	 * Logs an error if the message fails.
	 * @param p_contract		the contract
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_contract (Contract p_contract)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sendContract: entered" + " contract: " + p_contract);

		boolean status = send_multiByteMessage( MULTIBYTE_MESSAGE.SET_CONTRACT
		                                        , p_contract.getTrump().ordinal()
		                                        , p_contract.getBidWinner().ordinal()
		                                        , p_contract.getContractNum()
                								  );

		if (s_cat.isDebugEnabled()) s_cat.debug("sendContract: finished");
		return status;
	}
			
	/***********************************************************************
	 * Sends a message to the Keyboard Controller indicating that the requested
	 * card cannot be played.  The suit does not match the current suit and the
	 * hand contains a card with the necessary suit.
	 * Logs an error if the message fails.
	 * @param p_card			card played
	 * @param p_currentSuit	current suit
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_cannotPlay (Card p_card, Suit p_currentSuit)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_cannotPlay: entered"
												+ " card: " + p_card
												+ " curSuit: " + p_currentSuit
												);

		boolean status = send_multiByteMessage( MULTIBYTE_MESSAGE.CANNOT_PLAY
		                                        , p_currentSuit.ordinal()
		                                        , 0
		                                        , 0
                								  );

		if (s_cat.isDebugEnabled()) s_cat.debug("send_cannotPlay: finished");
		return status;
	}
		
	/***********************************************************************
	 * Sends a generic multi-byte message to the Keyboard Controller.
	 * Logs an error if the message fails.
	 * @param p_msg	the message to send
	 * @param p_suit		the suit value (3 bits)
	 * @param p_player	the player position value (4 bits)
	 * @param p_cardNumber	the number of the card (4 bits - 0:Two, 1:Three, ..., 12:Ace)
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_multiByteMessage(MULTIBYTE_MESSAGE p_msg, int p_suit, int p_player, int p_cardNumber)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_multiByteMessage: entered"
		                                        + " msg: " + p_msg
		                                        + " suit: " + p_suit
		                                        + " player: " + p_player
		                                        + " cardNum: " + p_cardNumber
		                                        );
		boolean status	= true;
		int opId			= p_msg.getMsgId();
		int reserve		= p_msg.getReserveInMillis();
		try
		{
			byte msg[] = {
			              (byte) ((0b10000000) | ((opId & 0b1111) << 3) | (p_suit & 0b111))
			              , (byte) (((p_player & 0b1111) << 4) | (p_cardNumber & 0b1111))
						};
			ensureMessageReserve(reserve);
			output.write(msg, 0, 2);
		}
		catch (IOException e)
		{
			s_cat.error("send_multiByteMessage: failed:", e);
			status = false;
		}
		if (s_cat.isDebugEnabled()) s_cat.debug("send_multiByteMessage: finished");
		return status;
	}
	
	/***********************************************************************
	 * Simulates a key press on the Keyboard Controller.
	 * (for testing the Keyboard Controller software).
	 * @param p_buttonName the name of the button to be pressed
	 ***********************************************************************/
	public boolean send_pressButton(String p_buttonName)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_multiByteMessage: entered: button: " + p_buttonName);

		boolean status	= true;
		try
		{
			Button button = Button.valueOf(p_buttonName.toUpperCase());
			
			int msg = (0b01000000 | button.ordinal());
			ensureMessageReserve(0);		// leave time for system messages, but do not reserve time for button audio
			output.write((byte)msg);
		}
		catch (Exception e)
		{
			s_cat.error("pressButton: failed to press: " + p_buttonName, e);
			status = false;
		}
		if (s_cat.isDebugEnabled()) s_cat.debug("pressButton: finished");
		return status;
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	/***********************************************************************
	 * Ensures that the previous message reserve is met, sleeping if necessary.
	 * @param p_newReserve	time to reserve for next message, in milliseconds.
	 * @return true if a delay was introduced and false otherwise
	 ***********************************************************************/
	private boolean ensureMessageReserve (int p_newReserve)
	{
		long curTime = System.currentTimeMillis();
		long delayTime = ((m_timeOfLastMessage + m_messageReserveMillis) - curTime);
		boolean delayed = false;
		if (delayTime < 100) delayTime = 100;	// don't flood serial line?
		if (delayTime > 0)
		{
			if (delayTime > MAX_RESERVE_DELAY_MILLIS) delayTime = MAX_RESERVE_DELAY_MILLIS;
			try
			{
	            Thread.sleep(delayTime);
	        }
			catch (InterruptedException e)
			{
	            e.printStackTrace();
	        }
			delayed = true;
		}
		m_timeOfLastMessage = System.currentTimeMillis();
		m_messageReserveMillis = p_newReserve;
		return delayed;
	}
	

	//--------------------------------------------------
	// COMMUNICATION METHODS
	//--------------------------------------------------

	/***********************************************************************
	 * Set up communication with the Keyboard Controller
	 * @return false if initialization fails
	 ***********************************************************************/
	public boolean initialize()
	{
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
			output = serialPort.getOutputStream();

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

	private String binary8 (int p_num)
	{
		return String.format("%8s", Integer.toBinaryString(p_num & 0xff)).replace(" ", "0");
	}
	
	private String binaryMsgToString (int p_num)
	{
		String b = binary8(p_num);
		String a = (p_num >= 32 && p_num <  127) ? " (" + (char) p_num + ")" : "";
		return String.format("%s: %3d%4s", b, p_num, a);
	}
	

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
					if (m_readLineEventMode || m_readOneLineEventMode)
					{
						String line = readLine(input);
						System.out.println("Keyboard(" + m_myPosition + "): " + line);
						if (READY_MSG.equals(line))
						{
							m_readLineEventMode = false;
							System.out.println("Found " + READY_MSG);
						}
						m_readOneLineEventMode = false;
					}
					else
					{
						// read an 8-bit byte, but operate on it as an int, so it is, in effect, unsigned
						int msg = input.read();
						if (msg != 192)
							System.out.println("From Keyboard: " + binaryMsgToString(msg));
						String messageDescription = m_gameController.processIncomingMessage(this, msg);
						if (messageDescription != null)
							System.out.println("    Operation: " + messageDescription);
					}
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
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * If true, read ASCII newline-terminated messages from
	 * the Keyboard Controller until the final setup message is received.
	 * Then switch back to byte-message mode.
	 * Default is false (byte-message mode).
	 * @return boolean 
	 ***********************************************************************/
	public boolean isReadLineEventMode ()
	{
		return m_readLineEventMode;
	}

	/***********************************************************************
	 * If true, read ASCII newline-terminated messages from
	 * the Keyboard Controller until the final setup message is received.
	 * Then switch back to byte-message mode.
	 * Default is false (byte-message mode).
	 * @param p_readLineEventMode boolean
	 ***********************************************************************/
	public void setReadLineEventMode ( boolean p_readLineEventMode )
	{
		m_readLineEventMode = p_readLineEventMode;
	}

	/***********************************************************************
	 * If true, read one ASCII newline-terminated message from
	 * the Keyboard Controller and then switch back to byte message
	 * mode.
	 * Default is false (byte-message mode).
	 * @return boolean
	 ***********************************************************************/
	public boolean isReadOneLineEventMode ()
	{
		return m_readOneLineEventMode;
	}

	/***********************************************************************
	 * If true, read one ASCII newline-terminated message from
	 * the Keyboard Controller and then switch back to byte message
	 * mode.
	 * Default is false (byte-message mode).
	 * @param p_readOneLineEventMode boolean
	 ***********************************************************************/
	public void setReadOneLineEventMode ( boolean p_readOneLineEventMode )
	{
		m_readOneLineEventMode = p_readOneLineEventMode;
	}

	/***********************************************************************
	 * The position of this Keyboard Controller
	 * @return player
	 ***********************************************************************/
	public Direction getMyPosition ()
	{
		return m_myPosition;
	}

	/***********************************************************************
	 * The position of this Keyboard Controller's partner
	 * @return player
	 ***********************************************************************/
	public Direction getMyPartnersPosition ()
	{
		return m_myPartnersPosition;
	}

	/***********************************************************************
	 * The amount of time to reserve to ensure that the next message can be heard.
	 * Set this to zero during reset, to avoid unnecessary delays.
	 * @return time in milliseconds
	 ***********************************************************************/
	public long getMessageReserveMillis ()
	{
		return m_messageReserveMillis;
	}

	/***********************************************************************
	 * The amount of time to reserve to ensure that the next message can be heard
	 * Set this to zero during reset, to avoid unnecessary delays.
	 * @param p_messageReserveMillis time in milliseconds
	 ***********************************************************************/
	public void setMessageReserveMillis ( long p_messageReserveMillis )
	{
		m_messageReserveMillis = p_messageReserveMillis;
	}

}
