package lerner.blindBridge.hardware;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.log4j.Category;

import lerner.blindBridge.hardware.KeyboardController.KBD_MESSAGE;
import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.BridgeHand;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Contract;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.PlayerHand;
import lerner.blindBridge.model.Suit;

/**********************************************************************
 * Command Interpreter Controller 
 * Reads the keyboard serial port to 
 *********************************************************************/

public class CommandController implements Runnable
{
	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(CommandController.class.getName());


	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	/***********************************************************************
	 * Commands that can be entered in the command interpreter.
	 ***********************************************************************/
	public enum BridgeCommand {
		  HELP("Lists valid commands")
		, NEWHAND("Starts a new hand")
		, CONTRACT("Set contract: position numTricks suit")
		, PLAY("Play card (simulates RFID scan from sighted player next to play): position cardAbbrev (e.g., QH)")
		, DEAL("Deals a random or predefined hand and simulates scanning: [predefined hand #]")
		, B("Simulates pressing a keyboard controller button for testing: kbdPosition buttonName")
		, REOPEN("Reopens connection to keyboard controller: kbdPosition")
		, REOPENANT("Reopens connection to antenna controller: kbdPosition")
		, RESET("Sends request to reset keyboard controller: kbdPosition")
		, PRINTHAND("For testing prints a hand: player")
		, PRINTSTATE("Prints the Game Controller state")
		, CANCELRESET("Send reset finished to ensure that audio is enabled: kbdPosition")
		, SHOWKBDS("Print a list of the known keyboards with an index for use in changing the keyboard's position")
		, REINITPOS("Reinitialize keyboard and antenna positions")
		, SHOWANTS("Print a list of the known antennas with an index for use in changing the antenna's position")
		, ANTPOS("Move an antenna to a new position: idx (from SHOWANTS) newPosition)")
		, S("Simulates RFID scan of a card: position cardAbbrev (e.g., QH)")
		, QUIT("Exit program")
		;
		
		private String m_description;
		
		BridgeCommand (String p_description)
		{
			m_description = p_description;
		}
		
		public String getDescription() { return m_description; } 
	}

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	Game				m_game;
	
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------
	
	InputStream		m_input;
	PrintStream		m_output;
	Thread 			m_thread;
	
	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Configures and initializes a Keyboard Controller
	 * @param p_gameController	The gameController managing the hands
	 * @param p_direction		The player position of the player using this Keyboard Controller
	 ***********************************************************************/
	public CommandController(Game p_gameController, InputStream p_input, PrintStream p_output)
	{
		m_game = p_gameController;
		m_input = p_input;
		m_output = p_output;
	}

	//--------------------------------------------------
	// COMMUNICATION METHODS
	//--------------------------------------------------

    public void start ()
    {
	    m_thread = new Thread (this);
	    m_thread.start ();
    }

	public void run ()
	{
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(m_input));

			commandLine(in, m_output);
		}
		catch (Exception e)
		{
			s_cat.error("run: problem reading command input stream", e);
		}
		finally
		{
			try
			{
				if (m_input != null) m_input.close();
			}
			catch (Exception e)
			{
				s_cat.error("run: failed to close stream", e);
			}
		}
	}

	/***********************************************************************
	 * Main loop for commands from the game controller.
	 * Input from Keyboard Controllers are handled via interrupt handlers.
	 ***********************************************************************/
	public void commandLine ( BufferedReader p_in, PrintStream p_out )
	{
		while (true)
		{
			p_out.println("Enter command: ");
			String line = "";
			try
			{
				line = p_in.readLine();
			}
			catch (Exception e)
			{
				s_cat.error("commandLine: read failed", e);
			}
			
			if (line.trim().equals("")) continue;	// skip empty input
			
			if (line.trim().equals("?")) line = "help";

			CommandController.BridgeCommand cmd = null;
			try
			{
				// special case for b (add space after b, if missing)
				if (line.matches("^b[nNeEsSwW] .*"))
				{
					line = "b " + line.substring(1);
				}

				// special case for s (add space after s, if missing)
				if (line.matches("^s[nNeEsSwW] .*"))
				{
					line = "s " + line.substring(1);
				}

				// special case for p (add space after s, if missing)
				if (line.matches("^ph [nNeEsSwW]"))
				{
					line = "printhand " + line.substring(3);
				}

				// special case for p (add space after s, if missing)
				if (line.equals("ps"))
				{
					line = "printstate";
				}

				String[] args = line.split(" ");
				if (args.length <= 0) continue;
				
				cmd = CommandController.BridgeCommand.valueOf(args[0].toUpperCase());
				
				switch (cmd)
				{
					case SHOWKBDS:
					{
						int idx = 0;
						p_out.println("Keyboards:");
						for (KeyboardController kbdController : m_game.getKeyboardControllers().values())
						{
							p_out.println("  " + idx + ": " + kbdController.getMyPosition() + " (" + kbdController.m_communicationPort.getName() + ")");
							++idx;
						}
					}
					break;
					
					case REINITPOS:
					{ // change the position of a keyboard
						if (args.length != 1)
							throw new IllegalArgumentException("Wrong number of arguments");
						m_game.evt_resetControllerPositions();
					}
					break;
						
					case QUIT:
					{ // change the position of a keyboard
						if (args.length != 1)
							throw new IllegalArgumentException("Wrong number of arguments");
						m_game.evt_exit();
					}
					break;
						
					case SHOWANTS:
					{
						int idx = 0;
						p_out.println("Antennas:");
						for (AntennaController antennaController : m_game.getAntennaControllers().values())
						{
							p_out.println("  " + idx + ": " + antennaController.getMyPosition() + " (" + antennaController.m_serialPort.getName() + ")");
							++idx;
						}
					}
					break;
					
					/*
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
					*/
						
					case NEWHAND:
					{
						m_game.evt_startNewHand();
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
						m_game.getBridgeHand().evt_setContract(contract);
					}
					break;

					case PLAY:
					{
						if (args.length != 3)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						Card card = new Card(args[++idx]);
						m_game.getBridgeHand().evt_playCard(direction, card);
					}
					break;
						
					case REOPEN:
					{
						if (args.length != 2)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						KeyboardController kbdController = m_game.getKeyboardControllers().get(direction); 
						if (kbdController != null)
						{
							kbdController.close();
							kbdController.tryOpen(kbdController.m_communicationPort);
						}
					}
					break;
						
					case REOPENANT:
					{
						if (args.length != 2)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						AntennaController antController = m_game.getAntennaControllers().get(direction); 
						if (antController != null)
						{
							antController.close();
							antController.tryOpen(antController.m_communicationPort);
						}
					}
					break;
						
					case RESET:
					{
						if (args.length != 2)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						KeyboardController kbdController = m_game.getKeyboardControllers().get(direction); 
						if (kbdController != null) kbdController.send_simpleMessage(KBD_MESSAGE.START_RELOAD);
					}
					break;
						
					case CANCELRESET:
					{	// should not be necessary; ensures audio is enabled
						if (args.length != 2)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						KeyboardController kbdController = m_game.getKeyboardControllers().get(direction); 
						if (kbdController != null) kbdController.send_reloadFinished();
					}
					break;
						
					case DEAL:
					{
						if (args.length != 1 && args.length != 2)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						int testHand = -1;
						if (args.length == 2)
						{
							testHand = Integer.parseInt(args[++idx]);
							if (testHand < 0 || testHand >=  BridgeHand.m_testHand.length)
							throw new IllegalArgumentException("Invalid testHand: " + testHand);
						}
						m_game.getBridgeHand().evt_dealHands(testHand);
					}
					break;
						
					case B:
					{
						if (args.length != 3)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						KeyboardController kbdController = m_game.getKeyboardControllers().get(direction); 
						if (kbdController != null) kbdController.send_pressButton(args[++idx]);
					}
					break;
						
					case S:
					{
						if (args.length != 3)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						Card card = new Card(args[++idx]);
						AntennaController antController = m_game.getAntennaControllers().get(direction); 
						if (antController != null) p_out.println("Ant[" + direction + "] " + antController.processCardPresentEvent(card));
					}
					break;
						
					case PRINTHAND:
					{
						if (args.length != 2)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						printHand(p_out, direction);
					}
					break;
					
					case PRINTSTATE:
					{
						if (args.length != 1)
							throw new IllegalArgumentException("Wrong number of arguments");
						p_out.println(m_game.getBridgeHand().toString());
					}
					break;

					case HELP:
					{
						printHelp(p_out);
					}
					break;
					
					default:
						break;
				}
			}
			catch (Exception e)
			{
				p_out.print("Error: ");
				p_out.println(e.getMessage());
				e.printStackTrace(p_out);
				if (cmd != null) p_out.println(cmd.getDescription());
				else printHelp(p_out);
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
	private void printHelp (PrintStream p_out)
	{
		p_out.println("Available commands:");
		for (BridgeCommand cmd : BridgeCommand.values())
		{
			p_out.println(cmd + ": " + cmd.getDescription());
		}
		p_out.println();
	}	
	
	/***********************************************************************
	 * Prints a hand, if known, for testing
	 * @param p_direction	the play to print
	 ***********************************************************************/
	private void printHand (PrintStream p_out, Direction p_direction)
	{
		PlayerHand hand = m_game.getBridgeHand().getHands().get(p_direction);
		if (hand == null) hand = m_game.getBridgeHand().getTestHands().get(p_direction);
		if (hand == null)
		{
			p_out.println("no hand for player: " + p_direction);
			return;
		}
		
		p_out.print(p_direction);
		for (Card card : hand.getCards())
		{
			p_out.print(" " + card.abbreviation());
		}
		p_out.println();
	}	
	
	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

}
