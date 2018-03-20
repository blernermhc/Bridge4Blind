package lerner.blindBridge.main;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Category;
import org.apache.logging.log4j.Level;

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
			// Read card libraries
			//------------------------------
			for (String cardFile : line.getOptionValues("cardFile"))
			{
				CardLibrary.readCardFile(cardFile);
			}

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
			
		    //------------------------------
			// Add antennas (add simulated antennas, if real ones not defined)
			//------------------------------
			Properties props = line.getOptionProperties( "antenna" );
			String device;
			for (Direction position : Direction.values())
			{
				device = (props == null ? null : (String)props.get(position.name()));
				if (s_cat.isDebugEnabled()) s_cat.debug("initialize: adding Antenna for postion: " + position
				                                        + " using device: " + device);
				addAntennaController(position, device);
			}
	    }
	    catch( ParseException exp )
	    {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }

	    //------------------------------
		// Update logging level
		//------------------------------
		Logger.resetConfiguration();
		Logger.initialize(logLevel);
		
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
	
	//--------------------------------------------------
	// INTERNAL METHODS
	//--------------------------------------------------
	
	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * Adds a keyboard controller to the configuration
	 * @param p_direction		the position of the controller
	 * @param p_device		the device path to use (attempts to find one if null)
	 ***********************************************************************/
	public void addKeyboardController (Direction p_direction, String p_device)
	{
		KeyboardController kbdController = new KeyboardController(this, p_direction, p_device);
		m_keyboardControllers.put(p_direction, kbdController);
		m_keyboardControllerList.add(kbdController);
		m_gameListeners.add(kbdController);
	}
	
	/***********************************************************************
	 * Adds a keyboard controller to the configuration
	 * @param p_direction		the position of the controller
	 * @param p_device		the device path to use (attempts to find one if null)
	 ***********************************************************************/
	public void addAntennaController (Direction p_direction, String p_device)
	{
		AntennaController antController = new AntennaController(this, p_direction, p_device);
		m_antennaControllers.put(p_direction, antController);
		m_antennaControllerList.add(antController);
		m_gameListeners.add(antController);
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
}
