// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.hardware;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.log4j.Category;

import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;
import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.BridgeHand;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener;

/***********************************************************************
 * Represents a controller connected to the game via a serial USB connection
 * (i.e., an Arduino-based controller).
 ***********************************************************************/
public abstract class JSSCSerialController implements SerialPortEventListener, GameListener 
{

	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(JSSCSerialController.class.getName());

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

	/** The port we are connected to */
	protected SerialPort 	m_serialPort;

	/** The output stream to the port */
	protected OutputStream m_output;
	
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	static private Set<String>	s_portsUsed		= new HashSet<>();

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------

	/***********************************************************************
	 * Configures and initializes a Serial Controller
	 * @param p_game				The game object managing the hands
	 * @param p_direction		If non-null, the player position this controller is at.
	 * 							If null, attempts to set position based on hardware settings.
	 * @param p_deviceName		If non-empty, open the device with this name for this controller
	 * 							If null or empty, try each device in turn until you find an appropriate one
	 * @param p_hasHardware		If false, there is no hardware and the "antenna" or "keyboard"
	 * 	will be controlled from the command interpreter (for testing)
	 * @throws IOException if it cannot open a port for this controller.
	 ***********************************************************************/
	public JSSCSerialController ( Game p_game, Direction p_direction, String p_deviceName, boolean p_hasHardware )
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
			if (p_deviceName != null && !p_deviceName.isEmpty())
			{
				if (! openDevice(p_deviceName, (p_direction == null)))
					throw new IOException("Unable to open device " + p_deviceName + " for controller " + getFullName());
			}
			else
			{
				if (! findDeviceToOpen())
					throw new IOException("Unable to find a device for controller " + getFullName());
			}
		}
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------

	/***********************************************************************
	 * Attempts to open each candidate device until it finds one that responds as expected.
	 * If a device is found, removes it from the candidate list.
	 * @return true if 
	 ***********************************************************************/
	protected boolean findDeviceToOpen ()
	{
		String[] portNames = SerialPortList.getPortNames();
		
		for (String deviceName : portNames)
		{
			if (s_portsUsed.contains(deviceName)) continue;

			if (m_game.getDevicePattern() != null)
			{
		        Matcher matcher = m_game.getDevicePattern().matcher(deviceName);
		        if (! matcher.matches())
		        {
		        		if (s_cat.isDebugEnabled()) s_cat.debug("findDeviceToOpen: skipping device: " + deviceName);
		        }
			}

			if (s_cat.isDebugEnabled()) s_cat.debug("findDeviceToOpen(" + getFullName() + "): trying device: " + deviceName);
			if (openDevice(deviceName, true))
			{
				if (s_cat.isDebugEnabled()) s_cat.debug("findDeviceToOpen(" + getFullName() + "): using device: " + m_serialPort.getPortName());
				return true;
			}
		}
		s_cat.error("findDeviceToOpen(" + getFullName() + "): failed to find an appropriate device");
		return false;
	}
	
	/***********************************************************************
	 * Opens the named device, checks that it responds as expected, and sets its direction.
	 * @param p_deviceName		name of the device to open (returns false if null)
	 * @param p_direction		if true, set direction from controller message
	 * @return true if successful, false otherwise 
	 ***********************************************************************/
	protected boolean openDevice ( String p_deviceName, boolean p_setDirection )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("openDevice(" + getFullName() + "): trying device: " + p_deviceName);

		if (p_deviceName == null) return false;

		m_serialPort = new SerialPort(p_deviceName);
		try
		{
			m_serialPort.openPort();//Open serial port
			m_serialPort.setParams(SerialPort.BAUDRATE_9600, 
			                       SerialPort.DATABITS_8,
			                       SerialPort.STOPBITS_1,
			                       SerialPort.PARITY_NONE);

			String line = readFullLineWithTimeout(getPortOpenTimeout()*4);
			if (s_cat.isDebugEnabled()) s_cat.debug("openDevice(" + getFullName() + "): read full line from device:\n" + line);
			if (line.startsWith(getIdentMsg()))
			{
				if (p_setDirection)
				{
					determinePositionFromInitializationMessage(line);
				}

				// set up input listener/handler
				m_serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
				m_serialPort.addEventListener(this);

				// open the output stream
				m_output = new JSSCOutputStream(m_serialPort);

				if (s_cat.isDebugEnabled()) s_cat.debug("openDevice(" + getFullName() + "): using device: " + m_serialPort.getPortName());
				s_portsUsed.add(m_serialPort.getPortName());
				return true;
			}
			else
			{
				if (s_cat.isDebugEnabled()) s_cat.debug("openDevice(" + getFullName() + "): device is not appropriate: " + m_serialPort.getPortName() + " line: " + line);
			}
		}
		catch (Exception e)
		{
			s_cat.error("openDevice(" + getFullName() + "): Could not open serial communication port with name: " + p_deviceName, e);
		}

		try
		{
			if (m_serialPort != null) m_serialPort.closePort();
		}
		catch (Exception e)
		{
			// nothing to do
		}
		return false;
	}
	
	/***********************************************************************
	 * Attempt to reopen a device
	 * @throws SerialPortException
	 ***********************************************************************/
	public void reopen()
		throws SerialPortException
	{
		if (m_serialPort == null) return;
		
		String deviceName = m_serialPort.getPortName();
		if (m_serialPort != null)
		{
			try
			{
				m_serialPort.closePort();
			}
			catch (Exception e)
			{
				// ignore exceptions closing ports
			}
		}
		openDevice(deviceName, false);
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
				m_serialPort.closePort();
			}
			catch (Exception e)
			{
				s_cat.error("close failed: ", e);
			}
		}
	}

    /***********************************************************************
     * Reads a line, but throws an exception if the line is not read within the given time.
     * @param p_timeoutInMilliSeconds
     * @throws SerialPortException
     * @throws SerialPortTimeoutException
     ***********************************************************************/
    protected String readLineWithTimeout(String p_methodName, int p_timeoutInMilliSeconds)
    		throws SerialPortException, SerialPortTimeoutException
    {
        if(!m_serialPort.isOpened())
            throw new SerialPortException(m_serialPort.getPortName(), p_methodName, SerialPortException.TYPE_PORT_NOT_OPENED);

        long stopTime = System.currentTimeMillis() + p_timeoutInMilliSeconds;
        StringBuilder message = new StringBuilder();
        while (System.currentTimeMillis() < stopTime)
        {
        		while (m_serialPort.getInputBufferBytesCount() > 0)
        		{
            		byte[] bytes = m_serialPort.readBytes(1);
            		byte b = bytes[0];
				if ( b == '\n')
				{
					return (message.toString());
				}
				else
				{
					// ignore carriage return and other control characters
					if (b >= 32 && b < 127) message.append((char)b);
				}
        			
        		}
            try
            {
                Thread.sleep(0, 100);//Need to sleep some time to prevent high CPU loading
            }
            catch (InterruptedException e)
            {
                //Do nothing
            }
        }
        throw new SerialPortTimeoutException(m_serialPort.getPortName(), p_methodName, p_timeoutInMilliSeconds);
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
	 * 
	 * @param p_timeoutInMillis		max time to wait in milliseconds
	 * @return the second newline-terminated string read, or as much as has arrived (if any) before the timeout occurred.
	 * May return the empty string, will never return null.
	 * @throws IOException if there is an error reading the stream.
	 ***********************************************************************/
	private String readFullLineWithTimeout (int p_timeoutInMillis)
		throws SerialPortException, SerialPortTimeoutException
	{
		// ignore first line, which may be a partial line
		readLineWithTimeout("readFullLineWithTimeout", p_timeoutInMillis);

		// try up to three times to get an expected line (ignore "TIMEOUT!" lines)
		int maxAttempts = 3;
		long stopTime = System.currentTimeMillis() + p_timeoutInMillis;
		while (maxAttempts > 0 && System.currentTimeMillis() < stopTime)
		{
			String line = readLineWithTimeout("readFullLineWithTimeout", p_timeoutInMillis);
			if (line.startsWith(getName())) return line;
			if (! line.equals("TIMEOUT!")) --maxAttempts;
			if (s_cat.isDebugEnabled()) s_cat.debug("readFullLineWithTimeout: skipping line: " + line);
		}
		return "";
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


	/***********************************************************************
	 * Helper methods that lists the available port names to stdout.
	 ***********************************************************************/
	public static void listPortNames ()
	{
		String[] portNames = SerialPortList.getPortNames();
		System.out.println("PortNames:");
		for (String portName : portNames)
		{
			System.out.println(portName);
		}
	}
	
   //--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
    
	public abstract String getName();
	public abstract int getPortOpenTimeout();
	public abstract int getPortDataRate();
	public abstract String getIdentMsg();
	public abstract String getResetMsg();
	public abstract String getReadyMsg();

	/***********************************************************************
	 * Full Name of the controller.  Includes direction, if non-null.
	 * Example: "Antenna[North]"
	 * @return controller name
	 ***********************************************************************/
	public String getFullName()
	{
		return getName() + (m_myPosition == null ? "" : "[" + m_myPosition + "]");
	}

	/***********************************************************************
	 * The position of this Controller
	 * @return player
	 ***********************************************************************/
	public Direction getMyPosition ()
	{
		return m_myPosition;
	}


}
