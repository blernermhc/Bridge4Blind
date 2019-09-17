// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.hardware;

import java.io.IOException;
import java.io.OutputStream;

import jssc.SerialPort;
import jssc.SerialPortException;

/***********************************************************************
 * Creates an output stream for a JSSC SerialPort.
 * From https://stackoverflow.com/questions/42341530/jssc-getinputstream-getoutputstream
 ***********************************************************************/
public class JSSCOutputStream extends OutputStream 
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Logger.getLogger(JSSCOutputStream.class);

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	/** The port we are connected to */
	private SerialPort 	m_serialPort;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

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
	public JSSCOutputStream ( SerialPort p_serialPort )
	{
		m_serialPort = p_serialPort;
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------

	@Override
	public void write(int data) throws IOException
	{
		if (m_serialPort != null && m_serialPort.isOpened())
		{
			try
			{
				m_serialPort.writeByte((byte)(data & 0xFF));
			}
			catch (SerialPortException e)
			{
				throw new IOException(e);
			}
		}
		else
		{
			throw new IOException("Stream not open");
		}
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
    //--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------
    
}
