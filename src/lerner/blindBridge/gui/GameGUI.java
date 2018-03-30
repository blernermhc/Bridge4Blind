package lerner.blindBridge.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Category;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener_sparse;

/**
 * The Game class controls the UI for a game of bridge
 * 
 * @author Allison DeJordy
 * @version March 12, 2015
 **/

public class GameGUI extends JFrame implements GameListener_sparse
{
	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(GameGUI.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	/** Font used to display messages on the main screen */
	public static final Font		INFO_FONT				= new Font("Verdana", Font.BOLD, 30);

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	/** remembers to which screen should be displayed if GameStatusGUI invokes changeFrame() */
	private GameGUIs				m_switchFromGameStatusGUI	= GameGUIs.NONE;

	/** Holds the actual GUI components */
	private GameGUI_builder		m_gameGUI_builder;
	
	/** the Game object associated with this GUI */
	private final Game			m_game;

	/** the currently visible GUI */
	private static GameGUIs		m_currentScreen;

	/** The history of screens viewed, used for the back button */
	private Stack<GameGUIs>		m_screensViewed			= new Stack<GameGUIs>();


	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	/**
	 * Constructor; constructs a new GameGUI with the specified listener
	 * 
	 * @param m_game
	 */
	public GameGUI ( Game p_game )
	{
		// Maximize the window
		// this.setExtendedState(JFrame.MAXIMIZED_BOTH);

		// set the program to exit when the JFrame is closed
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		m_game = p_game;

		// finish configuring the JPanels associated with each GUI
		GameGUIs.initialize(this, p_game);	
		
		// create the GUI
		m_gameGUI_builder = new GameGUI_builder(this, p_game);
		
		// show the first card
		m_gameGUI_builder.show(GameGUIs.INITIALIZING_GUI);

		m_currentScreen = GameGUIs.INITIALIZING_GUI;
		m_screensViewed.push(GameGUIs.INITIALIZING_GUI);

		add(m_gameGUI_builder.getMainPanel(), BorderLayout.CENTER);
		
		// add the area for debugging messages
		// add(new JScrollPane(m_gameGUI_builder.getDebugArea()), BorderLayout.SOUTH);

		// turn off focus traversal keys so that the tab key can be used as game
		// input
		setFocusTraversalKeysEnabled(false);

		this.pack();
		this.setVisible(true);
		this.requestFocusInWindow();

		// System.out.println("current screen initially " + currentScreen);

		// needed to detect when the this gui is closed
		detectGUIClosed();

		// disable the close operation on this gui because we do not want to
		// mistakenly close this application
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	/**
	 * When the user tries to close the game, it displays a pop-up that asks the user if the user is
	 * sure about closing the window. If the user presses yes, it should quit the game and close the
	 * hardware connections.
	 */
	private void detectGUIClosed ()
	{

		// code from :
		// http://stackoverflow.com/questions/9093448/do-something-when-the-close-button-is-clicked-on-a-jframe
		this.addWindowListener(new WindowAdapter()
		{

			@Override
			public void windowClosing ( WindowEvent windowEvent )
			{
				JOptionPane.showMessageDialog ( GameGUI.this, "Press on the Quit button to close this application" );
			}

		});
	}

	
	/***********************************************************************
	 * Changes the display to a new GUI.
	 * @param p_gui the gui
	 ***********************************************************************/
	protected void changeFrame ( GameGUIs p_gui )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("changeFrame: change frame current screen " + m_currentScreen + " to " + p_gui);
		m_screensViewed.push(m_currentScreen);

		m_currentScreen = p_gui;

		m_gameGUI_builder.show(m_currentScreen);
		repaint();
		requestFocusInWindow();
		
	}
	
	public boolean disableChangeFrame = true;

	/** Advances to the next GUI in line. */
	@SuppressWarnings("boxing")
	protected void changeFrame ()
	{
		if (disableChangeFrame) return;
		if (s_cat.isDebugEnabled()) s_cat.debug("changeFrame: change frame current screen " + m_currentScreen.getName());

		// System.out.println("before change frame currentScreen " +
		// currentScreen);

		// advance to the appropriate screen
		m_screensViewed.push(m_currentScreen);

		// If the current gui is the scan dummy gui, then change frame to the
		// game status gui. Otherwise, increment value of current screen
		// by 1
		if (m_currentScreen == GameGUIs.SCAN_DUMMY_GUI)
		{

			m_currentScreen = GameGUIs.GAME_STATUS_GUI;

		}
		else if (m_currentScreen == GameGUIs.GAME_STATUS_GUI)
		{

			// System.out.println("current screen is game status gui");

			// if a new hand is started, then screen should change from
			// GameStatusGUI to NEXT_HAND_GUI. If, after the first card has
			// been played and blind person is not dummy, then screen needs to
			// switch
			// from GameStatusGUI to SCAN_DUMMY_GUI.

			if (m_switchFromGameStatusGUI == GameGUIs.SWITCH_TO_SCAN_DUMMY)
			{

				m_currentScreen = GameGUIs.SCAN_DUMMY_GUI;

			}
			else if (m_switchFromGameStatusGUI == GameGUIs.SWITCH_TO_NEXT_HAND)
			{

				if (s_cat.isDebugEnabled()) s_cat.debug("changeFrame: switching to Next Hand GUI");

				// refresh the display before switching screens

				// nextHandGUI.refreshDisplay();

				m_currentScreen = GameGUIs.NEXT_HAND_GUI;

			}

			// else, do nothing

		}
		else if (m_currentScreen == GameGUIs.NEXT_HAND_GUI)
		{

			m_currentScreen = GameGUIs.SCANNING_BLIND_GUI;

		}
		else
		{

			m_currentScreen = m_currentScreen.nextGUI();
		}

		// update text on refresh display
		if (m_currentScreen == GameGUIs.NEXT_HAND_GUI)
		{
			((NextHandGUI)GameGUIs.NEXT_HAND_GUI.getPanel()).refreshDisplay();
		}

		sig_debugMsg("Switching to screen " + m_currentScreen.getName());

		if (s_cat.isDebugEnabled()) s_cat.debug("changeFrame: Switching to screen " + m_currentScreen.getName());

		// Note : its position/order is important
		/*
		 * rick if (Game.isTestMode()) { determineIfRightGUI(); }
		 */

		m_gameGUI_builder.show(m_currentScreen);
		requestFocusInWindow();

		// System.out.println("after change frame currentScreen " +
		// currentScreen);

		// debugMsg("currentScreen " + currentScreen);
	}

	/** Returns to the last card viewed. */
	@SuppressWarnings("boxing")
	public void reverse ()
	{

		if (s_cat.isDebugEnabled()) s_cat.debug("reverse: GameGUI undo");

		// if user wants to change position of blind player
		// if(game.blindPayerHasNoCard()){

		// if the user wants to change position of blind player by goinf from
		// scanning blind cards gui to choose VI position gui
		if (m_currentScreen == GameGUIs.SCANNING_BLIND_GUI)
		{

			/**
			 * IMPORTANT : When "Back" is pressed and screen changes from SCANNING_BLIND_GUI to
			 * VI_PLAYER_GUI, the handler is still active. So do not scan cards until the position
			 * of the blind player has been chosen. Not sure what happens if you do.
			 */
			/*
			 * rick - should not need this anymore game.resetVIPlayer();
			 */

		}
		else if (m_currentScreen == GameGUIs.BID_POSITION_GUI)
		{
			// TODO: implement this
			/*
			 * rick - probably need to implement this game.reverseBidPosition();
			 */

		}

		if (!m_screensViewed.isEmpty())
		{
			m_currentScreen = m_screensViewed.pop();

			if (s_cat.isDebugEnabled()) s_cat.debug("reverse: current screen is " + m_currentScreen);

			// something extra is needed to revert to SCAN_DUMMY_GUI
			if (m_currentScreen == GameGUIs.SCAN_DUMMY_GUI)
			{

				reverseToScanDummy();

				// if(Game.isTestMode()){
				//
				// TestAntennaHandler.undo();
				// }

			}

			m_gameGUI_builder.show(m_currentScreen);

			/*
			 * rick determineIfRightGUI();
			 */

			requestFocusInWindow();
		}
	}

	/**
	 * Resets the last move
	 */
	public void undo ()
	{
		m_game.getBridgeHand().evt_undo(true);
		
		// TODO: this should be implemented with m_game.evt_undo();
		/* rick: 

		// if the user wants to change position of blind player
		if (m_currentScreen == SCANNING_BLIND_GUI && m_game.blindPayerHasNoCard())
		{

			reverse();

		}
		else if (m_currentScreen == SCANNING_BLIND_GUI)
		{

			// This is when user wants to remove the most recent cards blind
			// player' cards during dealing stage

			m_game.undoBlindPlayerCard();

			if (Game.isTestMode())
			{

				TestAntennaHandler.undo();
			}

		}
		else if (m_currentScreen == SCAN_DUMMY_GUI)
		{

			Card toRemove = m_game.undoDummyPlayerCard();

			if (toRemove == null)
			{

				// allow the user to change gui
				reverse();

			}
			else
			{

				// remove the most recent card scanned for the dummy player
				m_scanDummyGUI.undo(toRemove);

				if (Game.isTestMode())
				{

					TestAntennaHandler.undo();
				}
			}

		}
		else if (m_currentScreen == GAME_STATUS_GUI)
		{

			Direction undoTurn = m_game.getTurn();

			Direction currentTurn = m_game.undo();

			if (currentTurn != null)
			{

				m_gameStatusGUI.undoCardPlayed(currentTurn.ordinal(), undoTurn.ordinal());

				if (Game.isTestMode())
				{

					TestAntennaHandler.undo();
				}

				if (m_game.getCurrentTrick().getTrickSize() == 0)
				{

					undoButtonSetEnabled(false);

				}
				else
				{

					undoButtonSetEnabled(true);
				}

			}
			else
			{

				undoButtonSetEnabled(false);
			}

		}
		else
		{

			reverse();
		}
		 */
	}

	/*
	 * rick private void determineIfRightGUI() { // figure out if it is the right gui for listening
	 * to key press if (currentScreen == VI_PLAYER_GUI || currentScreen == HELP_GUI || currentScreen
	 * == TRUMP_SUIT_GUI || currentScreen == BID_NUMBER_GUI || currentScreen == BID_POSITION_GUI ||
	 * currentScreen == NEXT_HAND_GUI) {
	 * 
	 * ((TestAntennaHandler) game.getHandler()).setRightGUI(false);
	 * 
	 * } else {
	 * 
	 * ((TestAntennaHandler) game.getHandler()).setRightGUI(true); } }
	 */

	//--------------------------------------------------
	// Game Event Signal Handlers
	//--------------------------------------------------

	/**
	 * Adds a message to the debugging panel
	 */
	@Override
	public void sig_debugMsg ( String p_msg )
	{
		m_gameGUI_builder.getDebugArea().append(p_msg + "\n");
		m_gameGUI_builder.getDebugArea().setCaretPosition(m_gameGUI_builder.getDebugArea().getText().length() - 1);
	}

	/***********************************************************************
	 * Indicates that the system is waiting for hardware setup to complete.
	 ***********************************************************************/
	@Override
	public void sig_initializing ()
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_initializing: entered");

		m_screensViewed.clear();
		changeFrame(GameGUIs.INITIALIZING_GUI);
	}

	/***********************************************************************
	 * Resets the game to start over.
	 ***********************************************************************/
	@Override
	public void sig_gameReset ()
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_gameReset: Game reset");

		m_screensViewed.clear();
		changeFrame(GameGUIs.SCANNING_BLIND_GUI);

		m_switchFromGameStatusGUI = GameGUIs.NONE;
		
		// Rick: you cannot create a new listener within a listener signal handler
		// since you cannot change the list of listeners while iterating over them.
		// If necessary, add a "reset" method to GameStatusGUI, so we do not have to
		// register a new listener.
		//m_gameStatusGUI = new GameStatusGUI(this, m_game);
	}

	/***********************************************************************
	 * Indicates that the system is waiting for the blind players to scan their hands.
	 ***********************************************************************/
	@Override
	public void sig_scanBlindHands ()
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_scanBlindHands: entered");

		changeFrame(GameGUIs.SCANNING_BLIND_GUI);
	}

	/***********************************************************************
	 * Indicates that the system is waiting for the dummy to scan their hand.
	 ***********************************************************************/
	@Override
	public void sig_scanDummyHand ()
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_scanDummyHand: entered");

		changeFrame(GameGUIs.SCAN_DUMMY_GUI);
	}

	/***********************************************************************
	 * Indicates that the system is waiting for the contract information
	 ***********************************************************************/
	@Override
	public void sig_enterContract ()
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_enterContract: entered");

		changeFrame(GameGUIs.BID_POSITION_GUI);
	}

	/***********************************************************************
	 * Indicates that the system is waiting for a card from a player
	 * @param p_direction the player to play.
	 ***********************************************************************/
	@Override
	public void sig_setNextPlayer ( Direction p_direction )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_setNextPlayer: entered");

		changeFrame(GameGUIs.GAME_STATUS_GUI);
	}

	/***********************************************************************
	 * Indicates that the hand is complete.
	 ***********************************************************************/
	@Override
	public void sig_trickWon ( Direction p_direction )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_trickWon: entered");

		changeFrame(GameGUIs.NEXT_HAND_GUI);
	}

	/**
	 * Display a help message
	 */
	@SuppressWarnings("boxing")
	public void showHelp ()
	{
		changeFrame(GameGUIs.HELP_GUI);
	}

	public void setSwitchFromGameStatusGUI ( GameGUIs p_switchFromGameStatusGUI )
	{
		m_switchFromGameStatusGUI = p_switchFromGameStatusGUI;

		if (s_cat.isDebugEnabled()) s_cat.debug("switchFromGameStatusGUI: " + m_switchFromGameStatusGUI);
	}

	public void undoButtonSetEnabled ( boolean enabled )
	{

		// TODO: is this needed? m_undoButton.setEnabled(enabled);
		repaint();
	}

	public void redoButtonSetEnabled ( boolean enabled )
	{

		// TODO: is this needed? m_redoButton.setEnabled(enabled);
		repaint();
	}

	public void reverseToScanBlind ()
	{

		// TODO: implement this in BridgeHand
		// game.setGameState(GameState.DEALING);

	}

	public void reverseToScanDummy ()
	{
		// TODO: implement this in BridgeHand
		// game.setGameState(GameState.SCANNING_DUMMY);

	}

}
