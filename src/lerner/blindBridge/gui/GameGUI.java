package lerner.blindBridge.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Category;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener_sparse;
import lerner.blindBridge.model.Trick;

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

	/** Holds the actual GUI components */
	private GameGUI_builder		m_gameGUI_builder;
	
	/** the Game object associated with this GUI */
	private final Game			m_game;

	/** the currently visible GUI */
	private static GameGUIs		m_currentScreen;

	/** Maximum delay to insert to ensure requested minimum GUI display time (in milliseconds) */
	private static final long MAX_DISPLAY_DELAY_MILLIS = 5000;  

	/**
	 * Time of the previous GUI change.
	 * This is used to enforce requested minimum GUI display times.
	 */
	private long m_timeOfLastDisplayChange = System.currentTimeMillis();
	
	/**
	 * The minimum display time requested for the currently displayed GUI.
	 */
	private long m_minimumDisplayMillis = 0;
	

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
	 * When changing GUIs, ensures that the requested minimum display time for
	 * the current GUI has been met
	 * @param p_gui the new GUI to display
	 ***********************************************************************/
	protected void changeFrame ( GameGUIs p_gui )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("changeFrame: change frame current screen " + m_currentScreen + " to " + p_gui);

		ensureMinimumDisplayTime(p_gui.getMinDisplayTimeMillis(), m_currentScreen == p_gui);
		
		m_currentScreen = p_gui;

		m_gameGUI_builder.show(m_currentScreen);
		repaint();
		requestFocusInWindow();
		
	}
	
	/***********************************************************************
	 * Ensures that the minimum display time requested for the current GUI is
	 * met before changing to a new, different, GUI.
	 * @param p_newReserve	time to reserve for next display, in milliseconds.
	 * @param p_sameGUI		if true, reset the timer, but do not introduce a delay.
	 * 						The current GUI is just being updated (e.g., a new
	 * 						card play is shown).
	 * @return true if a delay was introduced and false otherwise
	 ***********************************************************************/
	private boolean ensureMinimumDisplayTime (int p_newReserve, boolean p_sameGUI)
	{
		boolean delayed = false;
		
		if (! p_sameGUI)
		{
			long curTime = System.currentTimeMillis();
			long delayTime = ((m_timeOfLastDisplayChange + m_minimumDisplayMillis) - curTime);
			if (delayTime > 0)
			{
				if (delayTime > MAX_DISPLAY_DELAY_MILLIS) delayTime = MAX_DISPLAY_DELAY_MILLIS;
				try
				{
					if (s_cat.isDebugEnabled()) s_cat.debug("ensureMinimumDisplayTime: about to delay: " + delayTime);
		            Thread.sleep(delayTime);
					if (s_cat.isDebugEnabled()) s_cat.debug("ensureMinimumDisplayTime: back from delay");
		        }
				catch (InterruptedException e)
				{
		            e.printStackTrace();
		        }
				delayed = true;
			}
		}
		
		m_timeOfLastDisplayChange = System.currentTimeMillis();
		m_minimumDisplayMillis = p_newReserve;
		return delayed;
	}

	/***********************************************************************
	 * Resets the last display time.
	 * Invoked when changing the content of displays without changing GUIs.
	 * This ensures that the latest information will be shown for at least
	 * the minimum amount of time requested by the current GUI.
	 ***********************************************************************/
	public void resetTimeOfLastDisplayChange()
	{
		m_timeOfLastDisplayChange = System.currentTimeMillis();
	}

	/**
	 * Resets the last move
	 */
	public void undo ()
	{
		m_game.getBridgeHand().evt_undo(true);
	}

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

		changeFrame(GameGUIs.INITIALIZING_GUI);
	}

	/***********************************************************************
	 * Resets the game to start over.
	 ***********************************************************************/
	@Override
	public void sig_gameReset ()
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_gameReset: Game reset");

		changeFrame(GameGUIs.SCANNING_BLIND_GUI);

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
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_setNextPlayer: entered. Direction: " + p_direction);

		changeFrame(GameGUIs.GAME_STATUS_GUI);
	}

	/***********************************************************************
	 * Indicates that the hand is complete.
	 ***********************************************************************/
	@Override
	public void sig_trickWon ( Trick p_direction )
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

}
