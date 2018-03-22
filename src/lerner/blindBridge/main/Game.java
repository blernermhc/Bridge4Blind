package lerner.blindBridge.main;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Category;
import org.apache.logging.log4j.Level;

import gnu.io.CommPortIdentifier;
import lerner.blindBridge.hardware.AntennaController;
import lerner.blindBridge.hardware.CommandController;
import lerner.blindBridge.hardware.KeyboardController;
import lerner.blindBridge.model.BridgeHand;
import lerner.blindBridge.model.CardLibrary;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener;
import lerner.blindBridge.stateMachine.BridgeHandState;
import lerner.blindBridge.stateMachine.BridgeHandStateController;

/**********************************************************************
 * Main program for playing Blind Bridge.
 * Communicates with RFID antennas and the Blind players' Keyboard Controllers.
 * Sends events to the BridgeHand object. 
 *********************************************************************/
public class Game
{

	/**
	 * Used to collect logging output for this class
	 */
	static Category s_cat = Category.getInstance(Game.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	private Map<Direction, KeyboardController> m_keyboardControllers = new HashMap<>();
	private Map<Direction, AntennaController> m_antennaControllers = new HashMap<>();
	private CommandController m_commandController;
	

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	int m_numAntennas					= 0;
	int m_numKeyboards					= 0;
	String m_devicePattern;
	Set<CommPortIdentifier>	m_candidatePorts		= new HashSet<>();
	
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------
	
	/** the object containing the data for the current hand */
	private BridgeHand 					m_bridgeHand; 
	
	/** internal list to support moving keyboards to new positions */
	private List<KeyboardController>		m_keyboardControllerList = new ArrayList<>();

	/** internal list to support moving antennas to new positions */
	private List<AntennaController>		m_antennaControllerList = new ArrayList<>();

	/** all of the objects that may need to be notified of state changes */
	private List<GameListener>			m_gameListeners = new ArrayList<>();;

	/** the state controller engine */
	private BridgeHandStateController	m_bridgeHandStateController;
	
	/** Stack of played hands (used primarily for undo) */
	private Deque<BridgeHand>	m_playedHands = new ArrayDeque<>();
	
	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 *  Basic setup culminating in configuring according to the configuration file.
	 *  CmdLine syntax is csmvcCommandDriver configName controllerName [flags & options including
	 *     -d | -q | --logging={debug|info|warn|error}
	 *     -n |--test
	 *     --formset=<em>formsetName</em>
	 *    -- csmvc=<em>configuration parameter name</em>
	 *    --varname=<em>varValue</em> ...
	 * @throw ConfigurationException if the configuration cannot be loaded
	 ***********************************************************************/
	public void initialize (String[] p_args)
		throws Exception
	{
		Level logLevel = Level.WARN;		// use warn until we determine what command line indicates
		Logger.initialize(logLevel);

		//------------------------------
		// Create the game data object and state machine
		//------------------------------
		m_bridgeHand = new BridgeHand(this);
		m_bridgeHandStateController = new BridgeHandStateController(this);
		
		//------------------------------
		// Process command-line arguments
		//------------------------------

		// create the Options object 
		Options options = new Options();
		Option option;

		// Define simple true/false flag options
		option = new Option( "h", "help", false, "print this message" );	options.addOption(option);
		option = new Option( "d", "debug", false, "debug mode" );		options.addOption(option);
		option = new Option( "q", "quiet", false, "quiet mode" );		options.addOption(option);
		
		// Define two argument options (e.g., --logLevel debug)
		option = Option.builder("l")
			    .longOpt( "logLevel" )
			    .desc( "logging level: ERROR, WARN, INFO, DEBUG"  )
			    .hasArg()
			    .argName( "logging Level" )
			    .build();
		options.addOption(option);
		
		option = Option.builder("c")
			    .longOpt( "cardFile" )
			    .desc( "cardLibrary file to parse"  )
			    .hasArg()
			    .argName( "cardLibrary file" )
			    .build();
		options.addOption(option);
		
		option = Option.builder("D")
			    .longOpt( "devicePattern" )
			    .desc( "candidate USB Serial devices to use"  )
			    .hasArg()
			    .argName( "device pattern" )
			    .build();
		options.addOption(option);

		option = Option.builder("K")
			    .longOpt( "keyboards" )
			    .desc( "number of Keyboard Controllers to find"  )
			    .hasArg()
			    .argName( "number" )
			    .build();
		options.addOption(option);

		option = Option.builder("A")
			    .longOpt( "antennas" )
			    .desc( "number of Antenna Controllers to find"  )
			    .hasArg()
			    .argName( "number" )
			    .build();
		options.addOption(option);

		/*
		option  = Option.builder("K")
				.longOpt( "keyboard" )
				.argName( "position=device" )
				.hasArg()
                .numberOfArgs(2)
                .valueSeparator()
                .desc( "given device is at given position" )
                .build();
		options.addOption(option);
		
		option  = Option.builder("A")
				.longOpt( "antenna" )
				.argName( "position=device" )
				.hasArg()
                .numberOfArgs(2)
                .valueSeparator()
                .desc( "given device is at given position" )
                .build();
		options.addOption(option);
		*/		
		
		// parse the options
		CommandLineParser parser = new DefaultParser();
		try
	    {
	        // parse the command line arguments
			CommandLine line = parser.parse( options, p_args );
	        
			// process the options

			if ( line.hasOption( "help" ) )
			{
				// automatically generate the help statement
				HelpFormatter formatter = new HelpFormatter();
		    		formatter.printHelp( "BlindBridgeMain", options );
		    }
		    
			if ( line.hasOption( "quiet" ) ) logLevel = Level.ERROR;
			if ( line.hasOption( "debug" ) ) logLevel = Level.DEBUG;
			if ( line.hasOption( "logLevel" ) )
			{
				logLevel = Level.toLevel(line.getOptionValue("logLevel"), logLevel);
			}
			
			//------------------------------
			// Update logging level
			//------------------------------
			Logger.resetConfiguration();
			Logger.initialize(logLevel);

			//------------------------------
			// Read card libraries
			//------------------------------
			for (String cardFile : line.getOptionValues("cardFile"))
			{
				CardLibrary.readCardFile(cardFile);
			}

			//------------------------------
			// Get candidate devices
			//------------------------------
			
			if ( line.hasOption( "devicePattern" ) )
			{
				m_devicePattern = line.getOptionValue("devicePattern");
			}
			
			if ( line.hasOption( "antennas" ) )
			{
				m_numAntennas = Integer.parseInt(line.getOptionValue("antennas"));
			}
			
			if ( line.hasOption( "keyboards" ) )
			{
				m_numKeyboards = Integer.parseInt(line.getOptionValue("keyboards"));
			}
			
			findUSBCommunicationPorts(m_devicePattern);
			
		    //------------------------------
			// Add antennas (add simulated antennas, if real ones not defined)
			//------------------------------
			for (int i = 0; i < m_numKeyboards; ++i)
			{
				addKeyboardController(null);	// use card scan to determine position
			}
			
			/*
			//------------------------------
			// Add Keyboards
			//------------------------------
			if ( line.hasOption( "keyboard" ) )
			{
				Properties props = line.getOptionProperties( "keyboard" );
				String device;
				for (Direction position : Direction.values())
				{
					device = (String)props.get(position.name());
					if (device != null) 
					{
						if (s_cat.isDebugEnabled())
							s_cat.debug("initialize: adding Keyboard for postion: " + position
							            + " using device: " + device);
						addKeyboardController(position, device);
					}
				}
			}
			*/
			
		    //------------------------------
			// Add antennas (add simulated antennas, if real ones not defined)
			//------------------------------
			for (int i = 0; i < m_numAntennas; ++i)
			{
				addAntennaController(null, true);	// use card scan to determine position
			}
			// add dummy antennas in sc_testDevicesReady
			
			/*
			Properties props = line.getOptionProperties( "antenna" );
			String device;
			for (Direction position : Direction.values())
			{
				device = (props == null ? null : (String)props.get(position.name()));
				if (s_cat.isDebugEnabled()) s_cat.debug("initialize: adding Antenna for postion: " + position
				                                        + " using device: " + device);
				addAntennaController(position, device);
			}
			*/
	    }
	    catch( ParseException exp )
	    {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }

		//------------------------------
		// Create the command line input controller and start it
		//------------------------------
		m_commandController = new CommandController(this, System.in, System.out);
		m_commandController.start();
		
		//------------------------------
		// Start the game
		//------------------------------
		m_bridgeHandStateController.runStateMachine();
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Starts a new hand, pushing previous hand onto the stack of played hands.
	 * Since all other objects access BridgeHand with Game.getBridgeHand(), they
	 * will see the latest version.
	 * 
	 * This method is invoked by the state machine at the end of a hand.
	 ***********************************************************************/
	public void startNewHand()
	{
		m_playedHands.push(m_bridgeHand);
		m_bridgeHand = new BridgeHand(this);
	}

	/***********************************************************************
	 * Starts a new hand, pushing previous hand onto the stack of played hands.
	 * Since all other objects access BridgeHand with Game.getBridgeHand(), they
	 * will see the latest version.
	 * 
	 * This method is invoked by an external entity (thread), such as the command line controller.
	 ***********************************************************************/
	public void evt_startNewHand()
	{
		startNewHand();
		m_bridgeHandStateController.setForceNewState(BridgeHandState.NEW_HAND);
		m_bridgeHandStateController.notifyStateMachine();
		
	}
	
	/** set to true while we wait for keyboard to announce its position */
	private boolean m_waitingForPosition = false;

	/***********************************************************************
	 * Determines if all devices are ready to play
	 * @return true if ready, false otherwise
	 ***********************************************************************/
	public boolean sc_testDevicesReady ()
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sc_testDevicesReady: entered");
		boolean ready = true;
		
		if (ready)
		{
			for (AntennaController antController : m_antennaControllerList)
			{
				if (! antController.isDeviceReady())
				{
					ready = false;
					break;
				}
			}
		}
		
		if (ready)
		{
			for (KeyboardController kbdController : m_keyboardControllerList)
			{
				if (! kbdController.isDeviceReady())
				{
					ready = false;
					break;
				}
			}
		}

		if (s_cat.isDebugEnabled()) s_cat.debug("sc_testDevicesReady: ready: " + ready);
		
		if (! ready) return false;

		boolean reconfigRequired = false;
		
		for (KeyboardController kbdController : m_keyboardControllerList)
		{
			Direction direction = kbdController.getMyPosition(); 
			if (direction != null)
			{
				if (m_keyboardControllers.get(direction) != null)
				{
					s_cat.error("sc_testDevicesReady: direction: " + direction + " has more than one keyboard controller");
					reconfigRequired = true;
				}
				else
				{
					m_keyboardControllers.put(direction, kbdController);
				}
			}
			else
			{
				reconfigRequired = true;
			}
		}
		
		
		for (AntennaController antController : m_antennaControllerList)
		{
			Direction direction = antController.getMyPosition(); 
			if (direction != null)
			{
				if (m_antennaControllers.get(direction) != null)
				{
					s_cat.error("sc_testDevicesReady: direction: " + direction + " has more than one antenna controller");
					reconfigRequired = true;
				}
				else
				{
					m_antennaControllers.put(direction, antController);
				}
			}
			else
			{
				reconfigRequired = true;
			}
		}
		
		if (s_cat.isDebugEnabled()) s_cat.debug("sc_testDevicesReady: reconfigRequired: " + reconfigRequired);

		if (reconfigRequired)
		{
			evt_resetControllerPositions();
		}
		else
		{
			m_waitingForPosition = false;
		}
		
		if (!reconfigRequired && m_antennaControllerList.size() < BridgeHand.NUMBER_OF_PLAYERS)
		{
			// Add dummy antennas if there are fewer hardware antennas than players
			for (Direction direction : Direction.values())
			{
				if (m_antennaControllers.get(direction) == null)
				{
					AntennaController antController = addAntennaController(direction, false);
					m_antennaControllers.put(direction, antController);
				}
			}
		}

		if (s_cat.isDebugEnabled()) s_cat.debug("sc_testDevicesReady: finished reconfigRequired: " + reconfigRequired);

		return (! reconfigRequired);
	}
	//--------------------------------------------------
	// INTERNAL METHODS
	//--------------------------------------------------

	/***********************************************************************
	 * Asks each device to indicate its position
	 ***********************************************************************/
	public void evt_resetControllerPositions ()
	{
		// only call these once, until complete
		if (! m_waitingForPosition)
		{
			m_waitingForPosition = true;

			Iterator<AntennaController> antennaControllers = m_antennaControllerList.iterator();
			while (antennaControllers.hasNext())
			{
				AntennaController antController = antennaControllers.next();
				if (antController.isVirtualController())
					antennaControllers.remove();
				else
					antController.requestPosition();
			}

			for (KeyboardController kbdController : m_keyboardControllerList)
			{
				kbdController.requestPosition();
			}
		}

		m_bridgeHandStateController.setForceNewState(BridgeHandState.INITIALIZING);
		
		// clear the positional maps so we can try again on next test
		m_antennaControllers.clear();
		m_keyboardControllers.clear();
	}
	
    /***********************************************************************
     * Finds the communication ports with names that match the given pattern
     * and adds them to the set of candidate ports.
     * @param p_pattern Regexp pattern (e.g., "/dev/cu.usbmodem.*")
     ***********************************************************************/
    void findUSBCommunicationPorts ( String p_pattern )
    {
    		Pattern devicePattern = Pattern.compile(p_pattern);
    		
        @SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum.hasMoreElements() ) 
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            String portName = portIdentifier.getName();
            Matcher matcher = devicePattern.matcher(portName);
            if (matcher.matches())
            {
            		if (s_cat.isDebugEnabled()) s_cat.debug("findUSBCommunicationPorts: matched port: " + portName + " of type: " + portTypeToString(portIdentifier.getPortType()));
            		m_candidatePorts.add(portIdentifier);
            }
        }
    }
    
    /***********************************************************************
     * Returns the name of a port type
     * @param p_portType	the portType
     * @return the name
     ***********************************************************************/
    private String portTypeToString ( int p_portType )
    {
        switch ( p_portType )
        {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }

    //--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * Adds a keyboard controller to the configuration
	 * @param p_direction		the position of the controller, if known
	 * @return the keybaord object
	 ***********************************************************************/
	public KeyboardController addKeyboardController (Direction p_direction)
		throws IOException
	{
		KeyboardController kbdController = new KeyboardController(this, p_direction);
		m_keyboardControllerList.add(kbdController);
		m_gameListeners.add(kbdController);
		return kbdController;
	}
	
	/***********************************************************************
	 * Adds an antenna controller to the configuration
	 * NOTE: antenna will call back with antennaPositionDetermined once the
	 * position has been determined (i.e., by scanning a card with the appropriate suit).
	 * @param p_direction		Antenna position (null if using scan to determine position)
	 * @param p_hasHardware		If false, there is no hardware and the "antenna"
	 * 	will be controlled from the command interpreter (for testing)
	 * @return the new antenna object
	 ***********************************************************************/
	public AntennaController addAntennaController (Direction p_direction, boolean p_hasHardware)
	{
		AntennaController antController = new AntennaController(this, p_direction, p_hasHardware);
		m_antennaControllerList.add(antController);
		m_gameListeners.add(antController);
		return antController;
	}
	
	/***********************************************************************
	 * Record antenna's position once determined.
	 * @param p_antController the antenna
	 ***********************************************************************/
	public void antennaPositionDeterminedXX (AntennaController p_antController)
	{
		m_antennaControllers.put(p_antController.getMyPosition(), p_antController);
	}
	
	/***********************************************************************
	 * Record keyboard's position once determined.
	 * @param p_kbdController the keyboard
	 ***********************************************************************/
	public void keyboardPositionDeterminedXX (KeyboardController p_kbdController)
	{
		m_keyboardControllers.put(p_kbdController.getMyPosition(), p_kbdController);
	}
	
	/***********************************************************************
	 * The object containing the data for the current hand.
	 * @return game hand data
	 ***********************************************************************/
	public BridgeHand getBridgeHand ()
	{
		return m_bridgeHand;
	}

	/***********************************************************************
	 * The state machine.
	 * @return the state machine
	 ***********************************************************************/
	public BridgeHandStateController getStateController ()
	{
		return m_bridgeHandStateController;
	}

	/***********************************************************************
	 * Get the Keyboard Controller map (Direction, KeyboardController).
	 * @return keyboard controllers
	 ***********************************************************************/
	public Map<Direction, KeyboardController> getKeyboardControllers ()
	{
		return m_keyboardControllers;
	}

	/***********************************************************************
	 * Get the Antenna Controller map (Direction, AntennaController).
	 * @return antenna controllers
	 ***********************************************************************/
	public Map<Direction, AntennaController> getAntennaControllers ()
	{
		return m_antennaControllers;
	}

	/***********************************************************************
	 * The objects to be notified of various events.
	 * @return list of listeners
	 ***********************************************************************/
	public List<GameListener> getGameListeners ()
	{
		return m_gameListeners;
	}

	//--------------------------------------------------
	// MAIN routing
	//--------------------------------------------------

	/***********************************************************************
	 * Main routine.  Initializes system and starts it running.
	 * @param p_args
	 * @throws Exception
	 ***********************************************************************/
	public static void main(String[] p_args) throws Exception
	{
		Game main = new Game();
		main.initialize(p_args);
	}

	/***********************************************************************
	 * The set of devices that may be connected to antenna or keyboard controllers.
	 * @return set of device names
	 ***********************************************************************/
	public Set<CommPortIdentifier> getCandidatePorts ()
	{
		return m_candidatePorts;
	}

 }
