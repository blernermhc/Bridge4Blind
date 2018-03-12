package lerner.blindBridge.gameController;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Category;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**********************************************************************
 * Communicates with an RFID Antenna Controller 
 *********************************************************************/

public class AntennaController implements SerialPortEventListener
{
	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(AntennaController.class.getName());


	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	BlindBridgeMain	m_gameController;
	
	/** the device this controller uses */
	String m_device;


	/**
	 * The position of this Keyboard Controller
	 */
	Direction m_myPosition;
		
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------
	
	private Card m_currentCard = null;
	

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
	public AntennaController(BlindBridgeMain p_gameController, Direction p_direction, String p_device)
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
	 ***********************************************************************/
	public void setPlayer(Direction p_direction)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("setPlayer: entered" + " player: " + p_direction);

		m_myPosition = p_direction;
		
		if (s_cat.isDebugEnabled()) s_cat.debug("setPlayer: finished");
	}
	

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------


	//--------------------------------------------------
	// COMMUNICATION METHODS
	//--------------------------------------------------

	/***********************************************************************
	 * Set up communication with the Keyboard Controller
	 * @returns false if initialization fails
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

	private static String s_cardPrefix = "UID Value:  ";
	
	/***********************************************************************
	 * Parses the output of the antenna controller, looking for cards to play.
	 * Card lines look like this:
	 * UID Value:  0x4 0x5A 0x8B 0x7A 0x83 0x1E 0x80
	 * @param p_line the line to parse
	 * @return the card, if found, or null otherwise
	 ***********************************************************************/
	private Card parseLine (String p_line)
	{
		//TODO change the Antenna Arduino Sketch to return the card Id as a simple hex string.
		
		if (! p_line.startsWith(s_cardPrefix)) return null;
		
		// parse space-separated hex values like "0x4" and "0x7A"
		String[] hexStrings = p_line.substring(s_cardPrefix.length()).split(" ");
		
		if (hexStrings.length != 7)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("parseLine: incorrect number of hex values (should be 7), found: " + hexStrings.length);
			return null;
		}
		
		StringBuilder cardId = new StringBuilder();
		for (String hexStr : hexStrings)
		{
			if (! hexStr.startsWith("0x"))
			{
				if (s_cat.isDebugEnabled()) s_cat.debug("parseLine: illegal hex value (no 0x): " + hexStr);
				return null;
			}
			hexStr = hexStr.substring(2);
			if (hexStr.length() == 1) hexStr = "0" + hexStr;
			if (hexStr.length() != 2)
			{
				if (s_cat.isDebugEnabled()) s_cat.debug("parseLine: illegal hex value (bad length): " + hexStr);
				return null;
			}
			cardId.append(hexStr);
		}
		
		return CardLibrary.findCard(cardId.toString());
	}
	
	private synchronized void setCurrentCard (Card p_card)
	{
		m_currentCard = p_card;
	}
	
	public synchronized Card getCurrentCard ()
	{
		Card card = m_currentCard;
		m_currentCard = null;
		return card;
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
					System.out.println("From " + m_myPosition + " Antenna: " + line);
					Card card = parseLine(line);
					if (card != null)
					{
						System.out.println("From " + m_myPosition + " Antenna read card: " + card);
						setCurrentCard(card);
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
	 * The position of this Keyboard Controller
	 * @return player
	 ***********************************************************************/
	public Direction getMyPosition ()
	{
		return m_myPosition;
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
