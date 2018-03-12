package lerner.blindBridge.gameController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
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

import lerner.blindBridge.gameController.KeyboardController.KBD_MESSAGE;

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

	public enum BridgeCommand {
		  NEWHAND("Starts a new hand")
		, CONTRACT("Set contract: position numTricks suit")
		, PLAY("Play card (simulates RFID scan from sighted player next to play): cardAbbrev (e.g., QH)")
		, SCANHAND("Simulates scanning keyboard hand for testing: kbdPosition")
		, SCANDUMMY("Simulates scanning the dummy's hand for teasting")
		, B("Simulates pressing a keyboard controller button for testing: kbdPosition buttonName")
		, REOPEN("Reopens connection to keyboard controller: kbdPosition")
		, RESET("Sends request to reset keyboard controller: kbdPosition")
		, PRINTHAND("For testing prints a hand: player")
		, PRINTSTATE("Prints the Game Controller state")
		, CANCELRESET("Send reset finished to ensure that audio is enabled: kbdPosition")
		, SHOWKBDS("Print a list of the known keyboards with an index for use in changing the keyboard's position")
		, KBDPOS("Move a keyboard to a new position: idx (from SHOWKBDS) newPosition)")
		, SHOWANTS("Print a list of the known antennas with an index for use in changing the antenna's position")
		, ANTPOS("Move an antenna to a new position: idx (from SHOWANTS) newPosition)")
		;
		
		private String m_description;
		
		BridgeCommand (String p_description)
		{
			m_description = p_description;
		}
		
		public String getDescription() { return m_description; } 
	};

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------

	private Map<Direction, KeyboardController> m_keyboardControllers = new HashMap<>();
	private Map<Direction, AntennaController> m_antennaControllers = new HashMap<>();
	

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
		

	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Initializes the application
	 * @throws IOException
	 ***********************************************************************/
	public void startNewGame ()
		throws IOException
	{
		m_bridgeHand = new BridgeHand(m_keyboardControllers);
		m_bridgeHand.evt_startNewHand();
	}
	
	/***********************************************************************
	 * Main loop for commands from the game controller.
	 * Input from Keyboard Controllers are handled via interrupt handlers.
	 ***********************************************************************/
	public void commandLine()
	{
		while (true)
		{
			System.out.println("Enter command: ");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line = "";
			try
			{
				line = in.readLine();
			}
			catch (Exception e)
			{
				System.out.println ("commandLine: read failed: " + e);
				e.printStackTrace(new PrintStream(System.out));
			}

			BridgeCommand cmd = null;
			try
			{
				// special case for b (add space after b, if missing)
				if (line.matches("^b[nNeEsSwW] .*"))
				{
					line = "b " + line.substring(1);
				}

				String[] args = line.split(" ");
				if (args.length <= 0) continue;
				
				cmd = BridgeCommand.valueOf(args[0].toUpperCase());
				
				switch (cmd)
				{
					case SHOWKBDS:
					{
						int idx = 0;
						System.out.println("Keyboards:");
						for (KeyboardController kbdController : m_keyboardControllerList)
						{
							System.out.println("  " + idx + ": " + kbdController.getMyPosition() + " (" + kbdController.m_device + ")");
							++idx;
						}
						break;
					}
					
					case KBDPOS:
					{ // change the position of a keyboard
						if (args.length != 3)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						int kbdIndex = Integer.parseInt(args[++idx]);
						if (kbdIndex < 0 || kbdIndex >= m_keyboardControllerList.size())
							throw new IllegalArgumentException("Invalid kbdIndex: " + kbdIndex);
						Direction direction = Direction.fromString(args[++idx]);
						KeyboardController kbdController = m_keyboardControllerList.get(kbdIndex);
						m_keyboardControllers.remove(kbdController.getMyPosition());
						kbdController.setPlayer(direction);
						m_keyboardControllers.put(direction, kbdController);
					}
					break;
						
					case SHOWANTS:
					{
						int idx = 0;
						System.out.println("Antennas:");
						for (AntennaController antennaController : m_antennaControllerList)
						{
							System.out.println("  " + idx + ": " + antennaController.getMyPosition() + " (" + antennaController.m_device + ")");
							++idx;
						}
						break;
					}
					
					case ANTPOS:
					{ // change the position of an antenna
						if (args.length != 3)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						int antIndex = Integer.parseInt(args[++idx]);
						if (antIndex < 0 || antIndex >= m_antennaControllerList.size())
							throw new IllegalArgumentException("Invalid antIndex: " + antIndex);
						Direction direction = Direction.fromString(args[++idx]);
						AntennaController antennaController = m_antennaControllerList.get(antIndex);
						m_antennaControllers.remove(antennaController.getMyPosition());
						antennaController.setPlayer(direction);
						m_antennaControllers.put(direction, antennaController);
					}
					break;
						
					case NEWHAND:
					{
						m_bridgeHand.evt_startNewHand();
					}
					break;
						
					case CONTRACT:
					{
						if (args.length != 4)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						int numTricks = Integer.parseInt(args[++idx]);
						if (numTricks < 0 || numTricks > 7)
							throw new IllegalArgumentException("Invalid numTricks: " + numTricks);
						Suit suit = Suit.valueOf(args[++idx].toUpperCase());
						Contract contract = new Contract(direction, suit, numTricks);
						m_bridgeHand.evt_setContract(contract);
					}
					break;

					case PLAY:
					{
						if (args.length != 2)
							throw new IllegalArgumentException("Wrong number of arguments");
						if (m_bridgeHand.getNextPlayer() == null)
							throw new IllegalArgumentException("Cannot play, no next player");
						int idx = 0;
						Card card = new Card(args[++idx]);
						m_bridgeHand.evt_playCard(m_bridgeHand.getNextPlayer(), card);
					}
					break;
						
					case REOPEN:
					{
						if (args.length != 2)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						m_keyboardControllers.get(direction).initialize();
					}
					break;
						
					case RESET:
					{
						if (args.length != 2)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						m_keyboardControllers.get(direction).send_simpleMessage(KBD_MESSAGE.START_RELOAD);
					}
					break;
						
					case CANCELRESET:
					{	// should not be necessary; ensures audio is enabled
						if (args.length != 2)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						m_keyboardControllers.get(direction).send_simpleMessage(KBD_MESSAGE.FINISH_RELOAD);
					}
					break;
						
					case SCANHAND:
					{
						if (args.length != 2)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						m_bridgeHand.evt_scanHandTest(direction);
					}
					break;
						
					case SCANDUMMY:
					{
						if (args.length != 1)
							throw new IllegalArgumentException("Wrong number of arguments");
						m_bridgeHand.evt_scanHandTest(m_bridgeHand.getDummyPosition());
					}
					break;
						
					case B:
					{
						if (args.length != 3)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						m_keyboardControllers.get(direction).send_pressButton(args[++idx]);					
					}
					break;
						
					case PRINTHAND:
					{
						if (args.length != 2)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						printHand(direction);
					}
					break;
					
					case PRINTSTATE:
					{
						if (args.length != 1)
							throw new IllegalArgumentException("Wrong number of arguments");
						System.out.println(m_bridgeHand.toString());
					}
					break;

					default:
						break;
				}
			}
			catch (Exception e)
			{
				System.out.print("Error: ");
				System.out.println(e.getMessage());
				e.printStackTrace(new PrintStream(System.out));
				if (cmd != null) System.out.println(cmd.getDescription());
			}
		}
	}

	//--------------------------------------------------
	// INTERNAL METHODS
	//--------------------------------------------------

	/***********************************************************************
	 * Prints a hand, if known, for testing
	 * @param p_direction	the play to print
	 ***********************************************************************/
	private void printHand (Direction p_direction)
	{
		PlayerHand hand = m_bridgeHand.getHands().get(p_direction);
		if (hand == null) hand = m_bridgeHand.getTestHands().get(p_direction);
		if (hand == null)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("printHand: no hand for player: " + p_direction);
			return;
		}
		
		System.out.print(p_direction);
		for (Card card : hand.m_cards)
		{
			System.out.print(" " + card.abbreviation());
		}
		System.out.println();
	}	
	
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
		/*
		Thread t = new Thread()
		{
			public void run()
			{
				//the following line will keep this app alive for 1000 seconds,
				//waiting for events to occur and responding to them (printing incoming messages to console).
				try {Thread.sleep(1000000);} catch (InterruptedException ie) {}
			}
		};
		t.start();
		*/
		System.out.println("Started");
		main.startNewGame();
		main.commandLine();
	}
}
