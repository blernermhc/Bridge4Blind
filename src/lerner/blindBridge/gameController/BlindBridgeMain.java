package lerner.blindBridge.gameController;
import java.io.IOException;
import java.util.ArrayList;
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

import model.Card;
import model.Direction;

/**********************************************************************
 * Main program for playing Blind Bridge.
 * Communicates with RFID antennas and the Blind players' Keyboard Controllers.
 * Sends events to the BridgeHand object. 
 *********************************************************************/
public class BlindBridgeMain
{

	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(BlindBridgeMain.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	private Map<Direction, KeyboardController> m_keyboardControllers = new HashMap<>();
	private Map<Direction, AntennaController> m_antennaControllers = new HashMap<>();
	private CommandController m_commandController;
	

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------
	
	private BridgeHand m_bridgeHand; 
	
	/** internal list to support moving keyboards to new positions */
	private List<KeyboardController> m_keyboardControllerList = new ArrayList<>();

	/** internal list to support moving antennas to new positions */
	private List<AntennaController> m_antennaControllerList = new ArrayList<>();

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
		// Create the game data object
		//------------------------------
		m_bridgeHand = new BridgeHand();
		
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
			// Add antennas
			//------------------------------
			if ( line.hasOption( "antenna" ) )
			{
				Properties props = line.getOptionProperties( "antenna" );
				String device;
				for (Direction position : Direction.values())
				{
					device = (String)props.get(position.name());
					if (device != null) 
					{
						if (s_cat.isDebugEnabled())
							s_cat.debug("initialize: adding Antenna for postion: " + position
							            + " using device: " + device);
						addAntennaController(position, device);
					}
				}
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
		m_commandController = new CommandController(this, m_bridgeHand, System.in, System.out);
		m_commandController.start();
		
		//------------------------------
		// Start the game
		//------------------------------
		m_bridgeHand.startGame();
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	//--------------------------------------------------
	// INTERNAL METHODS
	//--------------------------------------------------

	/***********************************************************************
	 * Main handler for messages from the Keyboard Controllers.
	 * @param p_controller	the keyboard controller sending the message
	 * @param p_msg			the message (only the low 8 bits are considered)
	 * 						using an int rather than byte to avoid sign issues
	 * @return a description of the message
	 * @throws IOException if there are communication problems
	 ***********************************************************************/
	public String processIncomingCard (AntennaController p_controller, int p_msg)
		throws IOException
	{
		// TODO
		System.out.println("From Antenna: " + p_msg);
		return "Not Impl Yet.";
	}
	
	/***********************************************************************
	 * Main handler for messages from the Keyboard Controllers.
	 * @param p_controller	the keyboard controller sending the message
	 * @param p_msg			the message (only the low 8 bits are considered)
	 * 						using an int rather than byte to avoid sign issues
	 * @return a description of the message
	 * @throws IOException if there are communication problems
	 ***********************************************************************/
	public String processIncomingMessage (KeyboardController p_controller, int p_msg)
		throws IOException
	{
		int opId = (p_msg >> 6);
		int cardId = (p_msg & 0b00111111);
		Card card = (cardId < 52 ? new Card(cardId) : null);
		
		String cardAbbrev = (card == null ? "" : card.abbreviation());
		
		if (opId == 0)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("processIncomingMessage: play card from hand: " + cardId);
			m_bridgeHand.evt_playCard(p_controller.getMyPosition(), card);
			return "Play card (" + cardId + "): " + cardAbbrev;
		}
		else if (opId == 1)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("processIncomingMessage: play card from parner (dummy): " + cardId);
			m_bridgeHand.evt_playCard(p_controller.getMyPartnersPosition(), card);
			return "Play card (" + cardId + "): " + cardAbbrev;
		}
		else if (opId == 2)
		{
			if (cardId == 0)
			{
				if (s_cat.isDebugEnabled()) s_cat.debug("processIncomingMessage: undo play card");
				//TODO: m_bridgeHand.evt_undoPlayCard(p_controller.getMyPosition());
				return "Undo Play card (" + cardId + "): " + cardAbbrev;
			}
			else
			{
				if (s_cat.isDebugEnabled()) s_cat.debug("processIncomingMessage: undo play card");
				//TODO: m_bridgeHand.evt_masterUndo();
				return "Master Undo";
			}
		}
		else if (opId == 3)
		{
			if ((cardId & 0b00100000) != 0)
			{
				int id = (cardId & 0b1111);
				return "Incomplete message: opId: " + Integer.toBinaryString(id) + " (" + id + ")";
			}
			else
			{
				if (cardId == 0)
				{
					p_controller.setReadOneLineEventMode(true);
					// return "Read line:";
					return null;
				}
				else if (cardId == 1)
				{
					p_controller.setReadLineEventMode(true);
					return "Restarting";
				}
				else if (cardId == 2)
				{
					System.out.println("    about to initiate reset: " + (m_bridgeHand == null ? "null" : "notnull"));
					m_bridgeHand.evt_resetKeyboard(p_controller);
					return "Initiate reset";
				}
				else
				{
					return "unknown code: " + cardId;
				}
			}
		}
		return "no code";
	}
	
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
		m_bridgeHand.addKeyboardController(p_direction, kbdController);
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
		m_bridgeHand.addAntennaController(p_direction, antController);
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
		BlindBridgeMain main = new BlindBridgeMain();
		main.initialize(p_args);
	}
}
