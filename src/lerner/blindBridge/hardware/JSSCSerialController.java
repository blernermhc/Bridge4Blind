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
	
	/** Indicates if the device has completed initialization or reset and is ready to accept commands */
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

	/** Ports already assigned to other controllers */
	static private Set<String>	s_portsUsed		= new HashSet<>();
	
	/** Set of identMsgs to eliminate devices that identify as alternate serial controllers */
	static private Set<String>	s_identMsgs		= new HashSet<>();
	static void addIdentMsg (String p_identMsg) { s_identMsgs.add(p_identMsg); }

	/**
	 * The name of the event receiver thread.
	 * Set to null, using setReceiverThreadName(null), to recompute when next event is received.
	 * Always use the setters and getters to access this, since multiple threads read and write it.
	 * Note, only the serialEvent method can actually change the name.  That method runs within
	 * that thread so it has access to the name.  The SerialPort provides no other access to the
	 * thread, so we cannot change it from the parent thread. 
	 */
	private String m_receiverThreadName	= null;

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------

	/***********************************************************************
	 * Configures and initializes a Serial Controller
	 * <p>(assumes both direction and deviceName are provided, or neither, if hasHardware is true)
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
				if (! openDevice(p_deviceName, false))
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
	 * @param p_readPosition		if true, set controller position from information in device ready message
	 * @return true if successful, false otherwise 
	 ***********************************************************************/
	protected boolean openDevice ( String p_deviceName, boolean p_readPosition )
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

			String line = identifyAndWaitForDevice(getPortOpenTimeout()*4);
			if (line != null)
			{
				if (p_readPosition)
				{
					determinePositionFromInitializationMessage(line);
				}

				if (s_cat.isDebugEnabled()) s_cat.debug("openDevice(" + getFullName() + "): using device: " + m_serialPort.getPortName());
				s_portsUsed.add(m_serialPort.getPortName());

				// open the output stream
				m_output = new JSSCOutputStream(m_serialPort);

				m_deviceReady = true;	// device should be ready to accept commands now

				// set up input listener/handler (this must be last, since it enables event triggers)
				m_serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
				m_serialPort.addEventListener(this);

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
		
		m_deviceReady = false;	// openDevice sets this to true, if successful
		
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
	 * Helper that reads bytes from the controller's port, but never returns null.
	 * If no bytes are available, returns a zero length array.
	 * @return bytes read from port
	 * @throws SerialPortException
	 ***********************************************************************/
	protected byte[] readBytesFromPort()
		throws SerialPortException
	{
		if (m_serialPort == null) return new byte[0];
		byte bytes[] = m_serialPort.readBytes();
		if (bytes == null) return new byte[0];
		return bytes;
	}

    /***********************************************************************
     * Reads a line, but throws an exception if the line is not read within the given time.
     * @param p_timeoutInMilliSeconds		time after which we should stop reading
     * @throws SerialPortException			if port cannot be read
     * @throws SerialPortTimeoutException	if timeout reached before newline read
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
        			// read one character at a time so we do not read past newline
            		byte[] bytes = m_serialPort.readBytes(1);
            		if (bytes == null) break;		// don't think this can happen unless some other program is reading
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
     * Attempts to identify that the device is one we are expecting, and if so,
     * waits for the device to indicate that it is ready to receive commands.
     * <p>
     * Assumes that the caller has just opened the port, triggering a device
     * reset.  Also assumes that the device sends enough identifying lines
     * before the ready line so that we do not miss all of them.  If there
     * is too much time between opening the port and reading the lines, we
     * may miss all of them and fail to identify the device.
     * <p>
     * Skips the first line, since we may have missed some initial bytes.
     * <br>Checks up to three subsequent lines for a line that begins with
     * the text we expect for a device of the type we are opening (the 
     * getIdentMsg() string).
     * <p>
     * If a proper identification message is read, reads lines until the ready message
     * is found (getReadyMsg()).
     * <p>
     * This method returns the ready message line, if found.
     * <p>
     * The method returns null if the timeout expires before identifying and
     * reading the ready message, or if some error occurs.
	 * 
	 * @param p_timeoutInMillis		max time to wait in milliseconds
	 * @return the ready line if found before the timeout occurred, null otherwise.
	 ***********************************************************************/
	private String identifyAndWaitForDevice (int p_timeoutInMillis)
	{
		long stopTime = System.currentTimeMillis() + p_timeoutInMillis;

		try
		{
			// ignore first line, which may be a partial line
			readLineWithTimeout("identifyAndResetDevice(skip)", p_timeoutInMillis);
	
			// try up to three times to get an expected line (ignore "TIMEOUT!" lines)
			int maxAttempts = 3;
			boolean waitForReady = false;
			String line = "";
			while (maxAttempts > 0 && System.currentTimeMillis() < stopTime)
			{
				line = readLineWithTimeout("identifyAndResetDevice(id)", p_timeoutInMillis);
				if (line.startsWith(getIdentMsg()))
				{
					if (s_cat.isDebugEnabled()) s_cat.debug("identifyAndResetDevice(id): RECOGNIZED line: " + line);
					waitForReady = true;
					break;
				}
				
				// stop here if it identifies as something other than what we are looking for
				for (String identMsg : s_identMsgs)
				{
					if (line.startsWith(identMsg))
					{
						if (s_cat.isDebugEnabled()) s_cat.debug("identifyAndResetDevice(id): rejecting line: " + line);
						return null;
					}
				}
				
				if (! line.equals("TIMEOUT!")) --maxAttempts;
				if (s_cat.isDebugEnabled()) s_cat.debug("identifyAndResetDevice(id): skipping line: " + line);
			}
			
			if (! waitForReady)
			{
				if (s_cat.isDebugEnabled()) s_cat.debug("identifyAndResetDevice: device " + m_serialPort.getPortName() + " did not identify as " + getName());
				return null;		// device was not identified as expected
			}
			
			// if identified, wait for ready line (consider the line read above first)
			while (System.currentTimeMillis() < stopTime)
			{
				if (line.endsWith(getReadyMsg()))
				{
					if (s_cat.isDebugEnabled()) s_cat.debug("identifyAndResetDevice(wait): RECOGNIZING line: " + line);
					return line;
				}
				if (s_cat.isDebugEnabled()) s_cat.debug("identifyAndResetDevice(wait): skipping line: " + line);
				line = readLineWithTimeout("identifyAndResetDevice(wait)", p_timeoutInMillis);
			}
			
			String message = "identifyAndResetDevice: device " + m_serialPort.getPortName() + " did send ready message: " + getReadyMsg();
			if (s_cat.isDebugEnabled()) s_cat.debug(message);
			return null;
		}
		catch (Exception e)
		{
			String message = "identifyAndResetDevice: error while identifying device " + m_serialPort.getPortName();
			if (s_cat.isDebugEnabled()) s_cat.debug(message, e);
			return null;
		}
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Inspects the ready message from the device to determine the position
	 * the device has remembered.
	 * @param p_line		the ready message line (or any initialization line)
	 ***********************************************************************/
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
    
	/***********************************************************************
	 * The controller's name, used in debugging messages
	 * (e.g., "Antenna" or "Keyboard").
	 * Each sub-class must implement this method to return the appropriate value.
	 * @return the controller name
	 ***********************************************************************/
	public abstract String getName();
	
	public abstract int getPortOpenTimeout();
	public abstract int getPortDataRate();
	
	/***********************************************************************
	 * The prefix to expect on each line read during setup after the device
	 * port is opened (e.g., "Antenna(" or "Keyboard(").
	 * Each sub-class must implement this method to return the appropriate value.
	 * @return the prefix used to identify the device.
	 ***********************************************************************/
	public abstract String getIdentMsg();
		
	/***********************************************************************
	 * Text that indicates that the device is ready to receive commands.
	 * During setup, the controller looks for a line that ends with this
	 * text.
	 * Each sub-class must implement this method to return the appropriate value.
	 * @return text to look for
	 ***********************************************************************/
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

	/***********************************************************************
	 * Indicates if the device has completed initialization or reset and
	 * is ready to receive commands.
	 * @return true if ready, false otherwise
	 ***********************************************************************/
	public boolean isDeviceReady ()
	{
		return m_deviceReady;
	}

	/***********************************************************************
	 * Updates the name of the thread that receives events from the
	 * keyboard device.  Invoked when the name has not yet been sent and
	 * an event arrives or when an event changes the position.  Changing
	 * the position from another thread signals that the name needs to be
	 * updated when the next event arrives.
	 ***********************************************************************/
	protected void updateReceiverThreadName(String m_devicePrefix)
	{
		String name = m_devicePrefix + " Receiver " + (m_myPosition == null ? Thread.currentThread().getId() : m_myPosition);
	    Thread.currentThread().setName(name);
	    setReceiverThreadName(name);
	}
	
	/***********************************************************************
	 * Method to indicate that the event receiver thread name needs to
	 * be updated (null) or has been updated (non-null string).
	 * @param p_name the new name
	 ***********************************************************************/
	public void setReceiverThreadName (String p_name)
	{
		m_receiverThreadName = p_name;
	}

	/***********************************************************************
	 * Method to indicate that the event receiver thread name needs to
	 * be updated (null) or has been updated (non-null string).
	 * @param p_name the new name
	 ***********************************************************************/
	public String getReceiverThreadName ()
	{
		return m_receiverThreadName;
	}

}
