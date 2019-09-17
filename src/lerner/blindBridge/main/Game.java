package lerner.blindBridge.main;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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

import lerner.blindBridge.audio.AudibleGameListener;
import lerner.blindBridge.gui.GameGUI;
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

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	int m_numAntennas					= 0;
	int m_numKeyboards					= 0;
	Pattern m_devicePattern;
	
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------
	
	/** the object containing the data for the current hand */
	private BridgeHand 					m_bridgeHand; 
	
	/** the keyboard controller assigned to each position.  Null for positions of sighted players. */
	private Map<Direction, KeyboardController> m_keyboardControllers = new HashMap<>();
	
	/** the antenna controller assigned to each position.  All positions have antenna controllers, unless using command line for some positions. */
	private Map<Direction, AntennaController> m_antennaControllers = new HashMap<>();

	/** The command line controller */
	private CommandController m_commandController;
	
	/** The GUI controller */
	private 	GameGUI m_gameGUI;

	/** Handles audio announcements for everyone to hear (Keyboard controller manages audio for blind players) */
	private 	AudibleGameListener m_audibleGameListener;

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
	
	/** Stack of hands that were popped off of the played Hands (used for redo) */
	private Deque<BridgeHand>	m_undoneHands = new ArrayDeque<>();
	
	/** set to true while we wait for keyboard to announce its position */
	private boolean m_waitingForPosition = false;

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 *  Configure and run program.
	 * @throw Exception if there are problems parsing the command line,
	 * starting the hardware, or initializing the system.
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
		option = new Option( "p", "listPortNames", false, "lists available port names" );		options.addOption(option);
		
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

		option  = Option.builder("k")
				.longOpt( "keyboard" )
				.argName( "position=device" )
				.hasArg()
                .numberOfArgs(2)
                .valueSeparator()
                .desc( "given device is at given position" )
                .build();
		options.addOption(option);
		
		option  = Option.builder("a")
				.longOpt( "antenna" )
				.argName( "position=device" )
				.hasArg()
                .numberOfArgs(2)
                .valueSeparator()
                .desc( "given device is at given position" )
                .build();
		options.addOption(option);
		
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
		    		return;
		    }

			if ( line.hasOption( "listPortNames" ) )
			{
				AntennaController.listPortNames();
				return;
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
			if (s_cat.isDebugEnabled())
			{
				s_cat.debug("args: ");
				for (String arg : p_args)
				{
					s_cat.debug("    " + arg);
				}
			}

			//------------------------------
			// Create the command line input controller and start it
			//------------------------------
			m_commandController = new CommandController(this, System.in, System.out);
			m_commandController.start();
			
			//------------------------------
			// Create the Audio generator and add it as an event listener
			//------------------------------
			m_audibleGameListener = new AudibleGameListener(this);
			addGameListener(m_audibleGameListener);
			
			//------------------------------
			// Create the GUI and start it
			//------------------------------
			m_gameGUI = new GameGUI(this);
			addGameListener(m_gameGUI);
		
			//------------------------------
			// Read card libraries
			//------------------------------
			String[] cardFiles = line.getOptionValues("cardFile");
			if (cardFiles != null)
			{
				for (String cardFile : cardFiles)
				{
					CardLibrary.readCardFile(cardFile);
				}
			}

			//------------------------------
			// Get candidate devices
			//------------------------------
			
			if ( line.hasOption( "devicePattern" ) )
			{
				m_devicePattern = Pattern.compile(line.getOptionValue("devicePattern"));
			}
			
			if ( line.hasOption( "antennas" ) )
			{
				m_numAntennas = Integer.parseInt(line.getOptionValue("antennas"));
			}
			
			if ( line.hasOption( "keyboards" ) )
			{
				m_numKeyboards = Integer.parseInt(line.getOptionValue("keyboards"));
			}
			
		    //------------------------------
			// Add antennas
			//------------------------------
			if ( line.hasOption( "antenna" ) )
			{
			    //------------------------------
				// Add antennas bound to known device names
				// (add simulated antennas later, if real ones not defined)
				//------------------------------
				Properties props = line.getOptionProperties( "antenna" );
				for (Direction position : Direction.values())
				{
					String device = (String)props.get(position.name());
					if (device != null)
					{
						if (s_cat.isDebugEnabled()) s_cat.debug("initialize: adding Antenna for position: " + position
						                                        + " using device: " + device);
						addAntennaController(position, device, true);
					}
				}
			}
			else
			{
			    //------------------------------
				// Add antennas with hardware but unknown device name
				// (add simulated antennas later, if real ones not defined)
				//------------------------------
				for (int i = 0; i < m_numAntennas; ++i)
				{
					addAntennaController(null, null, true);	// if not remembered, use card scan to determine position
				}
				// add dummy antennas in sc_testDevicesReady
			}
			
			//------------------------------
			// Add Keyboards
			//------------------------------
			if ( line.hasOption( "keyboard" ) )
			{
				//------------------------------
				// Add Keyboards bound to known device names
				//------------------------------
				Properties props = line.getOptionProperties( "keyboard" );
				String device;
				for (Direction position : Direction.values())
				{
					device = (String)props.get(position.name());
					if (device != null) 
					{
						if (s_cat.isDebugEnabled())
							s_cat.debug("initialize: adding Keyboard for position: " + position
							            + " using device: " + device);
						addKeyboardController(position, device);
					}
				}
			}
			else
			{
				//------------------------------
				// Add Keyboards with hardware but unknown device name
				//------------------------------
				for (int i = 0; i < m_numKeyboards; ++i)
				{
					addKeyboardController(null, null);			// if not remembered, ask to determine position
				}
			}
	    }
	    catch( ParseException exp )
	    {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }

		//------------------------------
		// Start the game
		//------------------------------
		m_bridgeHandStateController.runStateMachine();
	}

	/***********************************************************************
	 * Attempt to reopen all ports (e.g., after accidentally unplugging USB cable)
	 ***********************************************************************/
	public void reopenAllPorts()
	{
		for (AntennaController antController : m_antennaControllerList)
		{
			try
			{
				antController.reopen();
			}
			catch (Exception e)
			{
				s_cat.error("reopenAllPorts: exception for controller: " + antController, e);
			}
		}

		for (KeyboardController kbdController : m_keyboardControllerList)
		{
			try
			{
				kbdController.reopen();
			}
			catch (Exception e)
			{
				s_cat.error("reopenAllPorts: exception for controller: " + kbdController, e);
			}
		}
	}

	/***********************************************************************
	 * Cleanup at the end of the program
	 ***********************************************************************/
	public void closeAllPorts()
	{
		for (AntennaController antController : m_antennaControllerList)
		{
			try
			{
				antController.close();
			}
			catch (Exception e)
			{
				s_cat.error("closeAllPorts: exception", e);
			}
		}

		for (KeyboardController kbdController : m_keyboardControllerList)
		{
			try
			{
				kbdController.close();
			}
			catch (Exception e)
			{
				s_cat.error("closeAllPorts: exception", e);
			}
		}
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
    /***********************************************************************
     * Returns true if running in test mode.
     * Not implemented yet.
     * @return true if in test mode
     ***********************************************************************/
    public static boolean isTestMode()
    {
    		return false;	// Rick did not implement this
    }

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
		m_undoneHands.clear();	// Once you start a new hand, you can no longer redo a hand undo
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
	
	/***********************************************************************
	 * Actions required to undo/redo a startNewHand event.
	 * @param p_redoFlag		If true, redo.  Otherwise, undo.
	 * @param p_undoEvent	the undo event created by the startNewHand event.
	 * @param p_confirmed	if false this is the initial request (announce only),
	 *  If true, the request has been confirmed and the undo actions should be processed.
	 ***********************************************************************/
	public void evt_startNewHand_undo ( boolean p_redoFlag, boolean p_confirmed )
	{
		if (p_confirmed)
		{
			if (p_redoFlag)
			{
				BridgeHand nextHand = m_undoneHands.poll();
				if (nextHand == null)
				{
					s_cat.error("evt_startNewHand_undo: no next hand, ignoring redo");
					return;
				}
				
				m_playedHands.push(m_bridgeHand);
				m_bridgeHand = nextHand;
			}
			else
			{
				BridgeHand prevHand = m_playedHands.poll();
				if (prevHand == null)
				{
					m_bridgeHand = new BridgeHand(this);
				}
				else
				{
					m_undoneHands.push(m_bridgeHand);
					m_bridgeHand = prevHand;
				}
			}
		}
		
		// notify listeners of event
		for (GameListener gameListener : getGameListeners())
		{
			gameListener.sig_gameReset_undo(p_redoFlag, p_confirmed);
		}
		
		if (p_confirmed)
			m_bridgeHandStateController.setForceNewState(BridgeHandState.NEW_HAND);
	}

	

	/***********************************************************************
	 * Quit program gracefully
	 ***********************************************************************/
	public void evt_exit()
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("evt_exit: exiting");
		closeAllPorts();
		System.exit(0);
	}
	
	//--------------------------------------------------
	// ACTIONS TAKEN BY STATE MACHINE States
	//--------------------------------------------------
	
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
					if (s_cat.isDebugEnabled()) s_cat.debug("sc_testDevicesReady: Antenna " + antController.getMyPosition() + " is not ready yet.");
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
					if (s_cat.isDebugEnabled()) s_cat.debug("sc_testDevicesReady: Keyboard " + kbdController.getMyPosition() + " is not ready yet.");
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
				if (s_cat.isDebugEnabled()) s_cat.debug("sc_testDevicesReady: Waiting for a Keyboard to identify its position.");
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
				if (s_cat.isDebugEnabled()) s_cat.debug("sc_testDevicesReady: Waiting for an Antenna to identify its position.");
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
					AntennaController antController = null;
					try
					{
						antController = addAntennaController(direction, null, false);
					}
					catch (Exception e)
					{	// this should never happen for Virtual controllers
						s_cat.error("sc_testDevicesReady: failed to create virtual antenna controllers", e);
					}
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
					antennaControllers.remove();	// sc_testDevicesReady will add necessary virtual antennaControllers
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
	
    //--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * Adds a keyboard controller to the configuration
	 * @param p_direction		the position of the controller, if known
	 * @param p_deviceName		the name of the device this controller is using, if known
	 * @return the keyboard object
	 * @throws IOException if it cannot open a port for this controller.
	 ***********************************************************************/
	public KeyboardController addKeyboardController (Direction p_direction, String p_deviceName)
		throws IOException
	{
		KeyboardController kbdController = new KeyboardController(this, p_direction, p_deviceName);
		m_keyboardControllerList.add(kbdController);
		addGameListener(kbdController);
		return kbdController;
	}
	
	/***********************************************************************
	 * Adds an antenna controller to the configuration
	 * NOTE: antenna will call back with antennaPositionDetermined once the
	 * position has been determined (i.e., by scanning a card with the appropriate suit).
	 * @param p_direction		Antenna position (null if using scan to determine position)
	 * @param p_deviceName		the name of the device this controller is using, if known
	 * @param p_hasHardware		If false, there is no hardware and the "antenna"
	 * 	will be controlled from the command interpreter (for testing)
	 * @return the new antenna object
	 * @throws IOException if it cannot open a port for this controller.
	 ***********************************************************************/
	public AntennaController addAntennaController (Direction p_direction, String p_deviceName, boolean p_hasHardware)
		throws IOException
	{
		AntennaController antController = new AntennaController(this, p_direction, p_deviceName, p_hasHardware);
		m_antennaControllerList.add(antController);
		addGameListener(antController);
		return antController;
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

	/***********************************************************************
	 * The objects to be notified of various events.
	 * @param p_listener the listener to add
	 ***********************************************************************/
	public void addGameListener (GameListener p_listener)
	{
		m_gameListeners.add(p_listener);
	}

	/***********************************************************************
	 * A regular expression to limit the ports considered for game hardware.
	 * Default is null (consider all ports).
	 * @return the regular expression string from the command line
	 ***********************************************************************/
	public Pattern getDevicePattern ()
	{
		return m_devicePattern;
	}

	
	//--------------------------------------------------
	// MAIN routine
	//--------------------------------------------------

	/***********************************************************************
	 * Main routine.  Initializes system and starts it running.
	 * @param p_args
	 * @throws Exception
	 ***********************************************************************/
	public static void main(String[] p_args) throws Exception
	{
		int status = 0;
		Game main = null;
		try
		{
			main = new Game();
			main.initialize(p_args);
		}
		catch (Exception e)
		{
			s_cat.error("main: uncaught exception", e);
			status = 1;
		}
		finally
		{
			System.out.println("Exiting");
			if (main != null) main.closeAllPorts();
			System.exit(status);
		}
	}

 }
