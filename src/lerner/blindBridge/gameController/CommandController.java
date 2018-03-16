package lerner.blindBridge.gameController;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.log4j.Category;

import lerner.blindBridge.gameController.KeyboardController.KBD_MESSAGE;
import model.Card;
import model.Contract;
import model.Direction;
import model.Suit;

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
	}

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	BlindBridgeMain	m_gameController;
	BridgeHand		m_bridgeHand;
	
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
	public CommandController(BlindBridgeMain p_gameController, BridgeHand p_bridgeHand, InputStream p_input, PrintStream p_output)
	{
		m_gameController = p_gameController;
		m_bridgeHand = p_bridgeHand;
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

			CommandController.BridgeCommand cmd = null;
			try
			{
				// special case for b (add space after b, if missing)
				if (line.matches("^b[nNeEsSwW] .*"))
				{
					line = "b " + line.substring(1);
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
						for (KeyboardController kbdController : m_bridgeHand.getKeyboardControllers().values())
						{
							p_out.println("  " + idx + ": " + kbdController.getMyPosition() + " (" + kbdController.m_device + ")");
							++idx;
						}
					}
					break;
					
					/*
					case KBDPOS:
					{ // change the position of a keyboard
						if (args.length != 3)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						int kbdIndex = Integer.parseInt(args[++idx]);
						if (kbdIndex < 0 || kbdIndex >= m_bridgeHand.getKeyboardControllers().values().size())
							throw new IllegalArgumentException("Invalid kbdIndex: " + kbdIndex);
						Direction direction = Direction.fromString(args[++idx]);
						KeyboardController kbdController = m_keyboardControllerList.get(kbdIndex);
						m_keyboardControllers.remove(kbdController.getMyPosition());
						kbdController.setPlayer(direction);
						m_keyboardControllers.put(direction, kbdController);
					}
					break;
					*/
						
					case SHOWANTS:
					{
						int idx = 0;
						p_out.println("Antennas:");
						for (AntennaController antennaController : m_bridgeHand.getAntennaControllers().values())
						{
							p_out.println("  " + idx + ": " + antennaController.getMyPosition() + " (" + antennaController.m_device + ")");
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
						KeyboardController kbdController = m_bridgeHand.getKeyboardControllers().get(direction); 
						if (kbdController != null) kbdController.initialize();
					}
					break;
						
					case RESET:
					{
						if (args.length != 2)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						KeyboardController kbdController = m_bridgeHand.getKeyboardControllers().get(direction); 
						if (kbdController != null) kbdController.send_simpleMessage(KBD_MESSAGE.START_RELOAD);
					}
					break;
						
					case CANCELRESET:
					{	// should not be necessary; ensures audio is enabled
						if (args.length != 2)
							throw new IllegalArgumentException("Wrong number of arguments");
						int idx = 0;
						Direction direction = Direction.fromString(args[++idx]);
						KeyboardController kbdController = m_bridgeHand.getKeyboardControllers().get(direction); 
						if (kbdController != null) kbdController.send_simpleMessage(KBD_MESSAGE.FINISH_RELOAD);
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
						KeyboardController kbdController = m_bridgeHand.getKeyboardControllers().get(direction); 
						if (kbdController != null) kbdController.send_pressButton(args[++idx]);
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
						p_out.println(m_bridgeHand.toString());
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
	private void printHand (PrintStream p_out, Direction p_direction)
	{
		PlayerHand hand = m_bridgeHand.getHands().get(p_direction);
		if (hand == null) hand = m_bridgeHand.getTestHands().get(p_direction);
		if (hand == null)
		{
			p_out.println("no hand for player: " + p_direction);
			return;
		}
		
		p_out.print(p_direction);
		for (Card card : hand.m_cards)
		{
			p_out.print(" " + card.abbreviation());
		}
		p_out.println();
	}	
	
	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

}
