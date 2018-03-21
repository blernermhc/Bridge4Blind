// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.hardware;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Category;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import lerner.blindBridge.main.Game;
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

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------

	/***********************************************************************
	 * Configures and initializes a Keyboard Controller
	 * @param p_game				The game object managing the hands
	 * @param p_hasHardware		If false, there is no hardware and the "antenna"
	 * 	will be controlled from the command interpreter (for testing)
	 ***********************************************************************/
	public SerialController ( Game p_game, boolean p_hasHardware )
	{
		m_game = p_game;
		if (! p_hasHardware)
		{
			m_deviceReady = true;
		}
		else
		{
			findDeviceToOpen();
		}
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
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
		Iterator<CommPortIdentifier> candidatePortIdentifiers = m_game.getCandidatePorts().iterator(); 
		while (candidatePortIdentifiers.hasNext())
		{
			CommPortIdentifier portIdentifier = candidatePortIdentifiers.next();
			if (tryOpen (portIdentifier))
			{
				candidatePortIdentifiers.remove();
				return true;
			}
			else
			{
				close();
			}
		}
		
		s_cat.error("findDeviceToOpen: Could not find serial communication port");
		return false;
	}
	
	public boolean tryOpen ( CommPortIdentifier p_portIdentifier )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("tryOpen: attempting to open device: " + p_portIdentifier.getName());
		try
		{
			// open serial port, and use class name for the appName.
			m_serialPort = (SerialPort) p_portIdentifier.open(this.getClass().getName(), getPortOpenTimeout());

			// set port parameters
			m_serialPort.setSerialPortParams(getPortDataRate(),
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			m_input = new BufferedInputStream(m_serialPort.getInputStream());
			//output = serialPort.getOutputStream();
			
			String line = readFullLineWithTimeout(m_input, getPortOpenTimeout()*2);
			if (s_cat.isDebugEnabled()) s_cat.debug("tryOpen: read second line from device: " + p_portIdentifier.getName() + "\n" + line);
			if (line.startsWith(getIdentMsg()))
			{
				// add event listeners
				m_serialPort.addEventListener(this);
				m_serialPort.notifyOnDataAvailable(true);
				m_communicationPort = p_portIdentifier;
				if (s_cat.isDebugEnabled()) s_cat.debug("tryOpen: using device: " + p_portIdentifier.getName());
				return true;
			}
			
		}
		catch (Exception e)
		{
			s_cat.error("findDeviceToOpen: initialization failed", e);
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
			m_serialPort.removeEventListener();
			m_serialPort.close();
		}
	}

	/***********************************************************************
	 * Waits for up to the given number of milliseconds for a newline-terminated 
	 * string to become available on the input stream following a newline
	 * (so we are sure to get a line from the start)
	 * @param p_input				the input stream to read
	 * @param p_timeoutInMillis		max time to wait in milliseconds
	 * @return the first newline-terminated string read, or as much as has arrived (if any) before the timeout occurred.
	 * May return the empty string, will never return null.
	 * @throws IOException if there is an error reading the stream.
	 ***********************************************************************/
	private String readFullLineWithTimeout (BufferedInputStream p_input, long p_timeoutInMillis)
		throws IOException
	{
		long maxTimeMillis = System.currentTimeMillis() + p_timeoutInMillis;

		StringBuilder line = new StringBuilder();
		boolean foundFirstNewline = false;
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
					if (foundFirstNewline)
					{
						return line.toString();
					}
					foundFirstNewline = true;
				}
				else if (foundFirstNewline)
				{
					if (ch != 13) line.append((char)ch);
				}
			}
		}
		s_cat.error("timed out");
		return line.toString();
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

}
