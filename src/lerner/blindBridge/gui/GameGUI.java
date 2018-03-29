package lerner.blindBridge.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Contract;
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

	// number of GUIs in the relevant to the play. Exclude the HelpGUI
	private static final int	NUM_GUIS					= 9;

	private static final int	HELP_GUI					= 8;

	private static final int	GAME_STATUS_GUI			= 5;

	private static final int	SCAN_DUMMY_GUI			= 6;

	private static final int	NEXT_HAND_GUI			= 7;

	private static final int	TRUMP_SUIT_GUI			= 4;

	private static final int	BID_NUMBER_GUI			= 3;

	private static final int	BID_POSITION_GUI			= 2;

	private static final int	SCANNING_BLIND_GUI		= 1;

	private static final int	VI_PLAYER_GUI			= 0;

	// GameStatusGUI should switch to the following three screens
	protected static final int	NONE						= 11;

	protected static final int	SWITCH_TO_SCAN_DUMMY		= 12;

	protected static final int	SWITCH_TO_NEXT_HAND		= 13;

	// protected static final int SWITCH_TO_SCANNING_BLIND = 13;

	// remembers to which screen should be displayed if GameStatusGUI invokes
	// changeFrame()
	private int					m_switchFromGameStatusGUI	= NONE;

	// the CardLayout controlling the display of GUIS
	private CardLayout			m_layout;

	// the Game object associated with this GUI
	private final Game			m_game;

	// the currently visible GUI
	private static int			m_currentScreen;

	// The history of screens viewed, used for the back button
	private Stack<Integer>		m_screensViewed			= new Stack<Integer>();

	// game status giu
	private GameStatusGUI		m_gameStatusGUI;

	// bid position gui
	private BidPositionGUI		m_bidPositionGUI;

	// bid number gui
	private BidNumberGUI			m_bidNumberGUI;

	// trump suit gui
	private BidSuitGUI			m_bidSuitGUI;

	// next hand gui
	private NextHandGUI			m_nextHandGUI;

	// VIPlayerGUI
	//private VIPlayerGUI			m_viPlayerGUI;

	// Scan blind cards gui
	private ScanningBlindGUI		m_scanningBlindGUI;

	// Scan dummy cards gui
	private ScanDummyGUI			m_scanDummyGUI;

	// buttons
	private JButton				m_resumeButton;

	private JButton				m_undoButton;

	private JButton				m_helpButton;

	private JButton				m_quitButton;

	private JButton				m_backButton;

	/** Font used to display messages on the main screen */
	public static final Font	INFO_FONT				= new Font("Verdana", Font.BOLD, 30);

	private String[]				m_cardNames;

	// Where debugging information shows up
	private JTextArea			m_debugArea				= new JTextArea(10, 80);

	private JPanel				m_cardPanel;

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

		/*
		 * rick if (Game.isTestMode()) {
		 * 
		 * addKeyListener(game.getHandler()); setFocusable(true); }
		 */

		// initialize some of the guis before createCards()

		// game status giu
		m_gameStatusGUI = new GameStatusGUI(this, m_game);

		// bid position gui
		m_bidPositionGUI = new BidPositionGUI(this, m_game);

		// bid number gui
		m_bidNumberGUI = new BidNumberGUI(this, m_game);

		// trump suit gui
		m_bidSuitGUI = new BidSuitGUI(this, m_game);

		// next hand gui
		m_nextHandGUI = new NextHandGUI(this, m_game);

		// VIPlayerGUI
		// rick: no longer relevant: viPlayerGUI = new VIPlayerGUI(this, game);

		// Scan blind cards gui
		m_scanningBlindGUI = new ScanningBlindGUI(this, m_game);

		// Scan dummy cards gui
		m_scanDummyGUI = new ScanDummyGUI(this, m_game);

		createCards();

		// create the main panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		// mainPanel.add(createInfoPanel(), BorderLayout.NORTH);
		mainPanel.add(m_cardPanel, BorderLayout.CENTER);
		mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

		add(mainPanel, BorderLayout.CENTER);

		// add the area for debugging messages
		// add(new JScrollPane(debugArea), BorderLayout.SOUTH);

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
	 * SkyeTekReader window.
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

				JOptionPane
						.showMessageDialog(	GameGUI.this,
											"Press on the Quit button to close this application");
			}

		});
	}

	private JPanel createButtonPanel ()
	{
		JPanel southPanel = new JPanel(new GridLayout(1, 0));
		southPanel.add(createResumeButtonPanel());
		southPanel.add(createBackButtonPanel());
		southPanel.add(createUndoButtonPanel());
		southPanel.add(createHelpButtonPanel());
		southPanel.add(createQuitPanel());
		return southPanel;
	}

	@SuppressWarnings("boxing")
	private void createCards ()
	{
		m_cardPanel = new JPanel();
		m_layout = new CardLayout();
		m_cardPanel.setLayout(m_layout);

		// create the GUI name array
		m_cardNames = new String[NUM_GUIS];
		m_cardNames[VI_PLAYER_GUI] = "viGUI";
		m_cardNames[SCANNING_BLIND_GUI] = "scanningGUI";
		m_cardNames[BID_POSITION_GUI] = "bidPosGUI";
		m_cardNames[BID_NUMBER_GUI] = "bidNumGUI";
		m_cardNames[TRUMP_SUIT_GUI] = "trumpSuitGUI";
		// cardNames[FIRST_CARD_GUI] = "firstCardGUI";
		m_cardNames[GAME_STATUS_GUI] = "gameStatusGUI";
		m_cardNames[SCAN_DUMMY_GUI] = "scanDummyGUI";
		// cardNames[7] = "resetGUI";
		// cardNames[GAME_STATUS_GUI] = "gameStatusGUI";
		m_cardNames[NEXT_HAND_GUI] = "nextHandGUI";
		m_cardNames[HELP_GUI] = "help";

		// add all the GUIs to the card panel

		// cardPanel.add(viPlayerGUI, cardNames[VI_PLAYER_GUI]);

		m_cardPanel.add(m_scanningBlindGUI, m_cardNames[SCANNING_BLIND_GUI]);

		m_cardPanel.add(m_bidPositionGUI, m_cardNames[BID_POSITION_GUI]);
		m_cardPanel.add(m_bidNumberGUI, m_cardNames[BID_NUMBER_GUI]);

		m_cardPanel.add(m_bidSuitGUI, m_cardNames[TRUMP_SUIT_GUI]);
		// cardPanel.add(new FirstCardGUI(this, game),
		// cardNames[FIRST_CARD_GUI]);

		m_cardPanel.add(m_gameStatusGUI, m_cardNames[GAME_STATUS_GUI]);

		m_cardPanel.add(m_scanDummyGUI, m_cardNames[SCAN_DUMMY_GUI]);

		// cardPanel.add(new ResetGUI(game), cardNames[7]);
		// cardPanel.add(new GameStatusGUI(game), cardNames[GAME_STATUS_GUI]);

		m_cardPanel.add(m_nextHandGUI, m_cardNames[NEXT_HAND_GUI]);

		m_cardPanel.add(new HelpGUI(this), m_cardNames[HELP_GUI]);

		// show the first card
		m_layout.show(m_cardPanel, m_cardNames[VI_PLAYER_GUI]);
		m_currentScreen = VI_PLAYER_GUI;
		m_screensViewed.push(VI_PLAYER_GUI);
	}

	/**
	 * Creates a JPanel which displays debugging information.
	 * 
	 * @return a JPanel displaying the bid, current antenna and trick
	 */
	protected JPanel createInfoPanel ()
	{
		JPanel infoPanel = new JPanel(new GridLayout(1, 0));
		infoPanel.add(createBidPanel());
		infoPanel.add(createAntennaPanel());
		infoPanel.add(createTrickPanel());
		return infoPanel;
	}

	/**
	 * Creates the panel with the quit button
	 * 
	 * @return the panel created
	 */
	protected JPanel createQuitPanel ()
	{

		m_quitButton = GUIUtilities.createButton("Quit");
		m_quitButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent e )
			{

				// a pop-up that asks the user if the user is sure about closing
				// the window. If the user presses yes, it should quit the game
				// and close the SkyeTekReader window.

				// code from :
				// http://stackoverflow.com/questions/9093448/do-something-when-the-close-button-is-clicked-on-a-jframe

				if (JOptionPane
						.showConfirmDialog(	GameGUI.this,
											"Are you sure you want to close this window?",
											"Confirm Exit", JOptionPane.YES_NO_OPTION,
											JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
				{

					m_game.evt_exit();
				}

			}

		});

		JPanel quitPanel = GUIUtilities.packageButton(m_quitButton, FlowLayout.CENTER);
		return quitPanel;

	}

	/**
	 * Creates a JPanel which displays the current bid.
	 * 
	 * @return a JPanel with a JLabel that displays the bid
	 */
	protected JPanel createBidPanel ()
	{

		JPanel bidPanel = new JPanel(new FlowLayout());
		// JLabel bid = new JLabel("Bid: ");
		// bid.setFont(INFO_FONT);
		// bidPanel.add(bid);
		// bidLabel = new JLabel("N/A");
		// bidLabel.setFont(INFO_FONT);
		// bidPanel.add(bidLabel);
		return bidPanel;

	}

	/**
	 * Creates a JPanel displaying the current antenna.
	 * 
	 * @return a JPanel with a JLabel displaying the current antenna
	 */
	protected JPanel createAntennaPanel ()
	{

		JPanel antennaPanel = new JPanel(new FlowLayout());
		// JLabel antenna = new JLabel("Next player: ");
		// antenna.setFont(INFO_FONT);
		// antennaPanel.add(antenna);
		// antennaLabel = new JLabel("N/A");
		// antennaLabel.setFont(INFO_FONT);
		// antennaPanel.add(antennaLabel);
		return antennaPanel;

	}

	/**
	 * Creates a JPanel which displays the current trick.
	 * 
	 * @return a JPanel with a JLabel displaying the current trick
	 */
	protected JPanel createTrickPanel ()
	{

		JPanel trickPanel = new JPanel(new FlowLayout());
		// JLabel trick = new JLabel("Trick: ");
		// trick.setFont(INFO_FONT);
		// trickPanel.add(trick);
		// trickLabel = new JLabel("N/A");
		// trickLabel.setFont(INFO_FONT);
		// trickPanel.add(trickLabel);
		return trickPanel;

	}

	/**
	 * Creates the "Undo" button panel at the bottom of the screen.
	 * 
	 * @return A JPanel containing a single, right-oriented "Undo" button.
	 */
	protected JPanel createUndoButtonPanel ()
	{

		// create a new JPanel with a FlowLayout
		m_undoButton = GUIUtilities.createButton("Undo");
		m_undoButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent evt )
			{

				undo();
			}

		});

		return GUIUtilities.packageButton(m_undoButton, FlowLayout.RIGHT);
		// panel.add(GUIUtilities.packageButton(resetButton, FlowLayout.RIGHT));
		// return panel;

	}

	/**
	 * Creates the "Back" button panel at the bottom of the screen.
	 * 
	 * @return A JPanel containing a single, right-oriented "Back" button.
	 */
	protected JPanel createBackButtonPanel ()
	{

		// create a new JPanel with a FlowLayout
		m_backButton = GUIUtilities.createButton("Back");
		m_backButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent evt )
			{
				// showResetGUI();

				reverse();
			}

		});

		return GUIUtilities.packageButton(m_backButton, FlowLayout.RIGHT);
		// panel.add(GUIUtilities.packageButton(resetButton, FlowLayout.RIGHT));
		// return panel;

	}

	/**
	 * Creates the "help" button panel at the bottom of the screen.
	 * 
	 * @return A JPanel containing a single, center-oriented "help" button.
	 */
	protected JPanel createHelpButtonPanel ()
	{

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		m_helpButton = GUIUtilities.createButton("Help");

		m_helpButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent arg0 )
			{
				showHelp();
			}

		});

		panel.add(GUIUtilities.packageButton(m_helpButton, FlowLayout.CENTER));
		return panel;

	}

	/**
	 * Creates the "resume" button panel at the bottom of the screen.
	 * 
	 * @return A JPanel containing a single, left-oriented "Resume" button.
	 */
	protected JPanel createResumeButtonPanel ()
	{

		// create a new JPanel with a FlowLayout
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		m_resumeButton = GUIUtilities.createButton("Resume");
		m_resumeButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent arg0 )
			{
				// reverse();
				/*
				 * rick if (game != null) { System.out.println("Stopping server"); try {
				 * game.closeHandler(); //BridgeActualGame.closeServerWindow();
				 * 
				 * } catch (IOException e) { // TODO Auto-generated catch block
				 * System.out.println("Unable to close connection"); e.printStackTrace(); } }
				 */

				// game.resumeGame();
				/* rick: start GUI from game
				sig_debugMsg("Starting server");
				BridgeActualGame.startServer();
				*/

				// start the game 15 seconds after starting the C# Server
				/*
				 * rick TimerTask timerTask = new TimerTask() {
				 * 
				 * @Override public void run() {
				 * 
				 * try { sig_debugMsg("Activating antennas"); game.activateAntennas(); Handler
				 * handler = game.getHandler(); if (handler != null) { new Thread(handler,
				 * "Antenna handler").start() ; }
				 * 
				 * 
				 * } catch (UnknownHostException e) {
				 * 
				 * // TODO Auto-generated catch block sig_debugMsg(e.getMessage());
				 * e.printStackTrace();
				 * 
				 * } catch (IOException e) {
				 * 
				 * // TODO Auto-generated catch block sig_debugMsg(e.getMessage());
				 * e.printStackTrace(); } sig_debugMsg("Resuming game"); game.resumeGame();
				 * sig_debugMsg("Game resumed"); }
				 * 
				 * };
				 * 
				 * // wait 4 seconds after starting the server to start the game Timer timer = new
				 * Timer(true); timer.schedule(timerTask, 4000);
				 */

			}

		});

		if (Game.isTestMode())
		{

			m_resumeButton.setEnabled(false);
		}

		panel.add(GUIUtilities.packageButton(m_resumeButton, FlowLayout.LEFT));
		return panel;
	}

	private void setAntennaLabel ( String text )
	{
		// antennaLabel.setText(text);
	}

	private void setTrickLabel ( String text )
	{
		// trickLabel.setText(text);
	}

	/** Advances to the next GUI in line. */
	@SuppressWarnings("boxing")
	protected void changeFrame ()
	{

		System.out.println("change frame current screen " + m_cardNames[m_currentScreen]);

		// System.out.println("before change frame currentScreen " +
		// currentScreen);

		// advance to the appropriate screen
		m_screensViewed.push(m_currentScreen);

		// If the current gui is the scan dummy gui, then change frame to the
		// game status gui. Otherwise, increment value of current screen
		// by 1
		if (m_currentScreen == SCAN_DUMMY_GUI)
		{

			m_currentScreen = GAME_STATUS_GUI;

		}
		else if (m_currentScreen == GAME_STATUS_GUI)
		{

			// System.out.println("current screen is game status gui");

			// if a new hand is started, then screen should change from
			// GameStatusGUI to NEXT_HAND_GUI. If, after the first card has
			// been played and blind person is not dummy, then screen needs to
			// switch
			// from GameStatusGUI to SCAN_DUMMY_GUI.

			if (m_switchFromGameStatusGUI == SWITCH_TO_SCAN_DUMMY)
			{

				m_currentScreen = SCAN_DUMMY_GUI;

			}
			else if (m_switchFromGameStatusGUI == SWITCH_TO_NEXT_HAND)
			{

				System.out.println("switching to Next Hand GUI");

				// refresh the display before switching screens

				// nextHandGUI.refreshDisplay();

				m_currentScreen = NEXT_HAND_GUI;

			}

			// else, do nothing

		}
		else if (m_currentScreen == NEXT_HAND_GUI)
		{

			m_currentScreen = SCANNING_BLIND_GUI;

		}
		else
		{

			m_currentScreen++;
		}

		// update text on refresh display
		if (m_currentScreen == NEXT_HAND_GUI)
		{

			m_nextHandGUI.refreshDisplay();
		}

		sig_debugMsg("Switching to screen " + m_cardNames[m_currentScreen]);

		System.out.println("Switching to screen " + m_cardNames[m_currentScreen]);

		// Note : its position/order is important
		/*
		 * rick if (Game.isTestMode()) { determineIfRightGUI(); }
		 */

		m_layout.show(m_cardPanel, m_cardNames[m_currentScreen]);
		requestFocusInWindow();

		// System.out.println("after change frame currentScreen " +
		// currentScreen);

		// debugMsg("currentScreen " + currentScreen);
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

	/**
	 * Adds a message to the debugging panel
	 */
	@Override
	public void sig_debugMsg ( String msg )
	{
		m_debugArea.append(msg + "\n");
		m_debugArea.setCaretPosition(m_debugArea.getText().length() - 1);
	}

	// Prints out a list of cards in the suit and hand specified.
	/* rick: no Player class currently, this method does not appear to be used
	@SuppressWarnings("unused")
	private void printCards ( Suit s, Player players )
	{
		Iterator<Card> cardIter = players.cards();
		while (cardIter.hasNext())
		{
			Card c = cardIter.next();
			if (c.getSuit() == s)
			{
				// print its suit and rank
				sig_debugMsg(c.getRank() + " of " + c.getSuit());

			}
		}
	}
	*/

	
	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener_sparse#sig_setNextPlayer(lerner.blindBridge.model.Direction)
	 */
	@Override
	public void sig_setNextPlayer ( Direction p_direction )
	{
		setAntennaLabel(m_game.getBridgeHand().getNextPlayer().toString());
	}

	/**
	 * Sets the bid label to the new contract
	 * 
	 * @param contract
	 *            the new contract
	 */
	@Override
	public void sig_contractSet ( Contract contract )
	{
		// bidLabel.setText(contract.toString());
	}

	/**
	 * Resets the game to start over.
	 */
	@Override
	public void sig_gameReset ()
	{

		System.out.println("Game reset");

		setAntennaLabel("N/A");
		setTrickLabel("N/A");
		// bidLabel.setText("N/A");

		// when game is reset, first show the winner of the last hand

		// currentScreen = SCANNING_BLIND_GUI;

		// nextHandGUI.refreshDisplay();

		// currentScreen = NEXT_HAND_GUI;

		m_currentScreen = SCANNING_BLIND_GUI;

		System.out.println("current screen is now " + m_cardNames[m_currentScreen]);

		m_screensViewed.clear();
		m_screensViewed.push(SCANNING_BLIND_GUI);
		// switchFromGameStatusGUI = SWITCH_TO_SCANNING_BLIND;
		m_switchFromGameStatusGUI = NONE;

		// gameStatusGUI.setFirstCardPlayed(false);

		if (Game.isTestMode())
		{

			m_bidSuitGUI.setHandNum(2);
			m_bidNumberGUI.setHandNum(2);
			m_bidPositionGUI.setHandNum(2);
		}

		m_layout.show(m_cardPanel, m_cardNames[m_currentScreen]);

		System.out.println("should show " + m_cardNames[m_currentScreen] + " " + m_currentScreen);

		this.repaint();

		this.requestFocusInWindow();
		
		// rick: you cannot create a new listener within a listener signal handler
		// since you cannot change the list of listeners while iterating over them.
		// If necessary, add a "reset" method to GameStatusGUI, so we do not have to
		// register a new listener.
		//m_gameStatusGUI = new GameStatusGUI(this, m_game);
	}

	/** Returns to the last card viewed. */
	@SuppressWarnings("boxing")
	public void reverse ()
	{

		System.out.println("GameGUI undo");

		// if user wants to change position of blind player
		// if(game.blindPayerHasNoCard()){

		// if the user wants to change position of blind player by goinf from
		// scanning blind cards gui to choose VI position gui
		if (m_currentScreen == SCANNING_BLIND_GUI)
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
		else if (m_currentScreen == BID_POSITION_GUI)
		{
			// TODO: implement this
			/*
			 * rick - probably need to implement this game.reverseBidPosition();
			 */

		}

		if (!m_screensViewed.isEmpty())
		{
			m_currentScreen = m_screensViewed.pop();

			System.out.println("current screen is " + m_currentScreen);

			// something extra is needed to revert to SCAN_DUMMY_GUI
			if (m_currentScreen == SCAN_DUMMY_GUI)
			{

				reverseToScanDummy();

				// if(Game.isTestMode()){
				//
				// TestAntennaHandler.undo();
				// }

			}

			m_layout.show(m_cardPanel, m_cardNames[m_currentScreen]);

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

	/**
	 * Updates the GUI to show whose turn it is and adds the card to the current trick
	 * 
	 * @param turn
	 *            the next player
	 * @param card
	 *            the card just played into the trick
	 */
	@Override
	public void sig_cardPlayed ( Direction turn, Card card )
	{
		setAntennaLabel(turn.getNextDirection().toString());
		setTrickLabel(m_game.getBridgeHand().getCurrentTrick().toString());
	}

	/* rick: this does not appear to be used
	@SuppressWarnings("boxing")
	private void showResetGUI ()
	{
		m_screensViewed.push(m_currentScreen);
		m_layout.show(m_cardPanel, "resetGUI");
		m_currentScreen = 7;
		requestFocusInWindow();
	}
	*/

	/**
	 * Updates the antenna label after a card is scanned
	 * 
	 * @param p_card
	 *            the card just scanned
	 */
	@Override
	public void sig_cardScanned ( Direction p_direction, Card p_card, boolean p_handComplete )
	{
		// setAntennaLabel(game.getCurrentHand());
	}

	/**
	 * Updates the display based on the trick being won. Updates the current player and the trick.
	 * 
	 * @param winner
	 *            the player who won the trick
	 */
	@Override
	public void sig_trickWon ( Direction winner )
	{
		setAntennaLabel(winner.toString());
		setTrickLabel(m_game.getBridgeHand().getCurrentTrick().toString());
	}

	/**
	 * Display a help message
	 */
	@SuppressWarnings("boxing")
	public void showHelp ()
	{
		m_screensViewed.push(m_currentScreen);
		m_layout.show(m_cardPanel, "help");
		m_currentScreen = 6;

	}

	public void sig_blindHandsScanned ()
	{

	}

	@Override
	public void sig_dummyHandScanned ()
	{
		// TODO Auto-generated method stub

	}

	public void setSwitchFromGameStatusGUI ( int switchFromGameStatusGUI )
	{

		this.m_switchFromGameStatusGUI = switchFromGameStatusGUI;

		System.out.println("switchFromGameStatusGUI " + switchFromGameStatusGUI);
	}

	public static boolean isScanBlindGUI ()
	{

		return m_currentScreen == SCANNING_BLIND_GUI;
	}

	public void undoButtonSetEnabled ( boolean enabled )
	{

		m_undoButton.setEnabled(enabled);
		repaint();
	}

	public void backButtonSetEnabled ( boolean enabled )
	{

		m_backButton.setEnabled(enabled);
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

	public Game getGame ()
	{

		return m_game;
	}
}
