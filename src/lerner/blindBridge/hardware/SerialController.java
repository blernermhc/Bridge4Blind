// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.hardware;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.log4j.Category;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import lerner.blindBridge.main.Game;
import lerner.blindBridge.main.Game.CandidatePort;
import lerner.blindBridge.model.BridgeHand;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener;

/***********************************************************************
 * Represents a controller connected to the game via a serial USB connection
 * (i.e., an Arduino-based controller).
 ***********************************************************************/
public abstract class SerialController implements SerialPortEventListener, GameListener 
{

	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(SerialController.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	/** The game data */
	protected Game			m_game;
	
	/** Indicates if the device has completed initialization or reset */
	protected boolean 		m_deviceReady = false;
	
	/** Indicates if the controller has actual hardware, or is virtualized in software */
	protected boolean		m_virtualController = false;

	/** The position of this Keyboard Controller */
	protected Direction		m_myPosition;

	/** The communication port identifier used to open the serial port */
	protected CommPortIdentifier	m_communicationPort;
	
	/** The port we are connected to */
	protected SerialPort 	m_serialPort;

	/**
	* A BufferedInputStream which will be fed by a InputStream 
	* Does not convert to chars, in case controller is sending binary data.
	*/
	protected BufferedInputStream m_input;

	/** The output stream to the port */
	protected OutputStream m_output;
	
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------

	/***********************************************************************
	 * Configures and initializes a Keyboard Controller
	 * @param p_game				The game object managing the hands
	 * @param p_direction		If non-null, the player position this controller is at.
	 * 							If null, attempts to set position based on hardware settings.
	 * @param p_deviceName		If non-empty, open the device with this name for this controller
	 * 							If null or empty, try each device in turn until you find an appropriate one
	 * @param p_hasHardware		If false, there is no hardware and the "antenna"
	 * 	will be controlled from the command interpreter (for testing)
	 * @throws IOException if it cannot open a port for this controller.
	 ***********************************************************************/
	public SerialController ( Game p_game, Direction p_direction, String p_deviceName, boolean p_hasHardware )
		throws IOException
	{
		m_game = p_game;
		m_myPosition = p_direction;
		
		if (! p_hasHardware)
		{
			m_virtualController = true;
			m_deviceReady = true;
		}
		else
		{
			m_virtualController = false;
			if (p_deviceName == null || p_deviceName.isEmpty())
			{
				if (! findDeviceToOpen())
					throw new IOException("Unable to find a device for this controller");
			}
			else
			{
				if (! openDevice(p_deviceName))
					throw new IOException("Unable to open device " + p_deviceName + " for this controller");
			}
		}
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------

	public abstract String getName();
	public abstract int getPortOpenTimeout();
	public abstract int getPortDataRate();
	public abstract String getIdentMsg();
	public abstract String getResetMsg();
	public abstract String getReadyMsg();

	/***********************************************************************
	 * Attempts to open each candidate device until it finds one that responds as expected.
	 * If a device is found, removes it from the candidate list.
	 * @return true if 
	 ***********************************************************************/
	protected boolean findDeviceToOpen ()
	{
		// use Iterator hasNext / next, because we also use remove
		Iterator<Game.CandidatePort> candidatePorts = m_game.getCandidatePorts().iterator(); 
		while (candidatePorts.hasNext())
		{
			CandidatePort candidatePort = candidatePorts.next();
			CommPortIdentifier portIdentifier = candidatePort.m_portIdentifier;
			
			if (candidatePort.m_skipTypes.contains(getName()))
			{
				if (s_cat.isDebugEnabled())
				{
					s_cat.debug("findDeviceToOpen: already determined that this port is not: " + getName()
								+ " port: " + portIdentifier.getName());
				}
				continue;
			}
			
			if (tryOpen (portIdentifier))
			{
				candidatePorts.remove();
				return true;
			}
			else
			{
				candidatePort.m_skipTypes.add(getName());
				close();
			}
		}
		
		s_cat.error("findDeviceToOpen: Could not find serial communication port");
		return false;
	}
	
	/***********************************************************************
	 * Attempts to open each candidate device until it finds one that responds as expected.
	 * If a device is found, removes it from the candidate list.
	 * @return true if 
	 ***********************************************************************/
	protected boolean openDevice ( String p_deviceName )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("openDevice(" + getName() + "): trying device: " + p_deviceName);

		if (p_deviceName == null) return false;
		
		// use Iterator hasNext / next, because we also use remove
		Iterator<Game.CandidatePort> candidatePorts = m_game.getCandidatePorts().iterator(); 
		while (candidatePorts.hasNext())
		{
			CandidatePort candidatePort = candidatePorts.next();
			CommPortIdentifier portIdentifier = candidatePort.m_portIdentifier;
			
			if (s_cat.isDebugEnabled()) s_cat.debug("openDevice(" + getName() + "):considering port: " + portIdentifier.getName()); 

			if (p_deviceName.equals(candidatePort.m_portIdentifier.getName()))
			{
				if (tryOpen (portIdentifier))
				{
					candidatePorts.remove();
					return true;
				}
				else
				{
					candidatePort.m_skipTypes.add(getName());
					close();
				}
			}
		}
		
		s_cat.error("openDevice: Could not open serial communication port with name: " + p_deviceName);
		return false;
	}
	
	/***********************************************************************
	 * Attempts to open the given port and then sees if it is connected
	 * to an instance of the expected hardware, by inspecting the lines
	 * of text arriving on the serial line.
	 * <p>
	 * Arduinos reset when a connection is made, so we look for the lines
	 * output during setup.  Each line should begin with the IdentMsg string
	 * (e.g., "Antenna(" or "Keyboard(") followed by the last know position of
	 * the device (an ordinal of a Direction enum value), a close paren and
	 * arbitrary text.
	 * <p>
	 * This method scans the text arriving on the serial line and extracts the
	 * text between two newline characters (since it may connect in the middle
	 * of a line).
	 * <p>
	 * Invalid positions are OK.  The state controller will check these
	 * as part of the INITIALIZATION state checks.
	 * 
	 * @param p_portIdentifier	the port to open
	 * @return true if successful, false otherwise
	 ***********************************************************************/
	public boolean tryOpen ( CommPortIdentifier p_portIdentifier )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("tryOpen(" + getName() + "): attempting to open device: " + p_portIdentifier.getName());
		try
		{
			// open serial port, and use class name for the appName.
			m_serialPort = (SerialPort) p_portIdentifier.open(this.getClass().getName(), getPortOpenTimeout());

			// set port parameters
			m_serialPort.setSerialPortParams(getPortDataRate(),
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			/* did not seem to help with missing characters from antenna controller
			m_serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN
			                                | SerialPort.FLOWCONTROL_RTSCTS_OUT);
			m_serialPort.setRTS(true);
			*/
			
			// open the streams
			m_input = new BufferedInputStream(m_serialPort.getInputStream());
			m_output = m_serialPort.getOutputStream();
			
			String line = readFullLineWithTimeout(m_input, getPortOpenTimeout()*4);
			if (s_cat.isDebugEnabled()) s_cat.debug("tryOpen(" + getName() + "): read full line from device:\n" + line);
			if (line.startsWith(getIdentMsg()))
			{
				if (m_myPosition == null)
				{
					determinePositionFromInitializationMessage(line);
				}

				// add event listeners
				m_serialPort.addEventListener(this);
				m_serialPort.notifyOnDataAvailable(true);
				m_communicationPort = p_portIdentifier;
				if (s_cat.isDebugEnabled()) s_cat.debug("tryOpen(" + getName() + "): using device: " + p_portIdentifier.getName());
				return true;
			}
			
		}
		catch (Exception e)
		{
			s_cat.error("findDeviceToOpen(" + getName() + "): initialization failed", e);
		}
		return false;
	}
	
	/***********************************************************************
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 ***********************************************************************/
	public synchronized void close()
	{
		if (m_serialPort != null)
		{
			try
			{
				m_serialPort.removeEventListener();
				m_serialPort.close();
			}
			catch (Exception e)
			{
				s_cat.error("close failed: ", e);
			}
		}
	}

	/***********************************************************************
	 * Waits for up to the given number of milliseconds for a newline-terminated 
	 * string to become available on the input stream following at least one newline
	 * (so we are sure to get a line from the start).
	 * <p>
	 * We still see some noise on the lines which interferes with identifying the
	 * device on the serial line.  So we make multiple attempts.
	 * If the line does not start with the CONTROLLER_NAME of an AntennaController
	 * or KeyboardController (i.e., "Antenna" or "Keyboard"), keep trying up to
	 * three more times.  If still not found, just return the last line we read.
	 * <p>
	 * Also ignores "TIMEOUT!" which is sometimes generated by the RFID antenna library.
	 * @param p_input				the input stream to read
	 * @param p_timeoutInMillis		max time to wait in milliseconds
	 * @return the second newline-terminated string read, or as much as has arrived (if any) before the timeout occurred.
	 * May return the empty string, will never return null.
	 * @throws IOException if there is an error reading the stream.
	 ***********************************************************************/
	private String readFullLineWithTimeout (BufferedInputStream p_input, long p_timeoutInMillis)
		throws IOException
	{
		long maxTimeMillis = System.currentTimeMillis() + p_timeoutInMillis;

		StringBuilder line = new StringBuilder();
		boolean foundNewline = false;
		int maxLinesToTest = 4;
		int buflen = 256;
		byte[] buffer = new byte[buflen];
		byte ch;
		while (System.currentTimeMillis() < maxTimeMillis)
		{
			int bytesRead = p_input.read(buffer, 0, buflen);
			for (int i = 0; i < bytesRead; ++i)
			{
				ch = buffer[i];
				if (ch == '\n')
				{
					if (foundNewline)
					{
						if (line.toString().equals("TIMEOUT!"))
						{	// skip this from Adafruit_PN532 library
							line = new StringBuilder();
							continue;
						}
						if ( line.toString().startsWith(AntennaController.CONTROLLER_NAME)
							 || line.toString().startsWith(KeyboardController.CONTROLLER_NAME) )
						{	// found a candidate
							return line.toString();
						}
						// skip and try again
						if (--maxLinesToTest <= 0) return line.toString();
						if (s_cat.isDebugEnabled()) s_cat.debug("readFullLineWithTimeout: skipping line: " + line.toString());
						line = new StringBuilder();
					}
					foundNewline = true;
				}
				else if (foundNewline)
				{
					// ignore carriage return and other control characters
					if (ch >= 32 && ch < 127) line.append((char)ch);
					//if (ch != 13) line.append((char)ch);
				}
			}
		}
		s_cat.error("timed out");
		return line.toString();
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
	private void determinePositionFromInitializationMessage ( String p_line )
	{
		String line = p_line.substring(getIdentMsg().length());	// remove prefix
		int endPos = line.indexOf(')');
		if (endPos <= 0)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("determinePositionFromInitializationMessage: did not find closing paren");
			return;	// did not find position
		}
		line = line.substring(0, endPos);
		int position = Integer.parseInt(line);
		if (position >= 0 && position < BridgeHand.NUMBER_OF_PLAYERS)
		{
			m_myPosition = Direction.values()[position];
			if (s_cat.isDebugEnabled()) s_cat.debug("determinePositionFromInitializationMessage: found position: " + m_myPosition);
		}
		else
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("determinePositionFromInitializationMessage: ignoring illegal position: " + position);
		}
	}

	/***********************************************************************
	 * Indicates if the controller has actual hardware, or is virtualized in software
	 * @return true if simulated in software, false if talks to actual hardware
	 ***********************************************************************/
	public boolean isVirtualController ()
	{
		return m_virtualController;
	}

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

}
