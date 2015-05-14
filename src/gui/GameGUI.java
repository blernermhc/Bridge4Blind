package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import controller.TestAntennaHandler;
import main.BridgeActualGame;
import model.Card;
import model.Contract;
import model.Direction;
import model.Game;
import model.GameListener;
import model.Player;
import model.Suit;
import audio.AudibleGameListener;

/**
 * The Game class controls the UI for a game of bridge
 * 
 * @author Allison DeJordy
 * @version March 12, 2015
 **/

public class GameGUI extends JFrame implements GameListener {

	// number of GUIs in the relevant to the play. Exclude the HelpGUI
	private static final int NUM_GUIS = 9;

	private static final int HELP_GUI = 8;

	private static final int GAME_STATUS_GUI = 5;

	private static final int SCAN_DUMMY_GUI = 6;

	private static final int NEXT_HAND_GUI = 7;

	private static final int TRUMP_SUIT_GUI = 4;

	private static final int BID_NUMBER_GUI = 3;

	private static final int BID_POSITION_GUI = 2;

	private static final int SCANNING_BLIND_GUI = 1;

	private static final int VI_PLAYER_GUI = 0;

	// GameStatusGUI should switch to the following three screens
	protected static final int NONE = 11;

	protected static final int SWITCH_TO_SCAN_DUMMY = 12;

	protected static final int SWITCH_TO_NEXT_HAND = 13;

	// protected static final int SWITCH_TO_SCANNING_BLIND = 13;

	// remembers to which screen should be displayed if GameStatusGUI invokes
	// changeFrame()
	private int switchFromGameStatusGUI = NONE;

	// the CardLayout controlling the display of GUIS
	private CardLayout layout;

	// the Game object associated with this GUI
	private Game game;

	// the currently visible GUI
	private static int currentScreen;

	// The history of screens viewed, used for the back button
	private Stack<Integer> screensViewed = new Stack<Integer>();

	// game status giu
	private GameStatusGUI gameStatusGUI;

	// bid position gui
	private BidPositionGUI bidPositionGUI;

	// bid number gui
	private BidNumberGUI bidNumberGUI;

	// trump suit gui
	private TrumpSuitGUI trumpSuitGUI;

	// next hand gui
	private NextHandGUI nextHandGUI;
	
	// VIPlayerGUI
	VIPlayerGUI viPlayerGUI ;

	private JButton resumeButton;
	private JButton resetButton;
	private JButton helpButton;
	private JButton quitButton;

	// private JLabel bidLabel;
	// private JLabel antennaLabel;
	// private JLabel trickLabel;

	/** Font used to display messages on the main screen */
	public static final Font INFO_FONT = new Font("Verdana", Font.BOLD, 30);

	private String[] cardNames;

	// Where debugging information shows up
	private JTextArea debugArea = new JTextArea(10, 80);

	private JPanel cardPanel;

	/**
	 * Constructor; constructs a new GameGUI with the specified listener
	 * 
	 * @param game
	 */
	public GameGUI(Game game) {
		// Maximize the window
		// this.setExtendedState(JFrame.MAXIMIZED_BOTH);

		// set the program to exit when the JFrame is closed
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.game = game;
		addKeyListener(new KeyPad(this, game));

		if (Game.isTestMode()) {

			addKeyListener(game.getHandler());
			setFocusable(true);
		}

		// initialize some of the guis before createCards()

		// game status giu
		gameStatusGUI = new GameStatusGUI(this, game);

		// bid position gui
		bidPositionGUI = new BidPositionGUI(this, game);

		// bid number gui
		bidNumberGUI = new BidNumberGUI(this, game);

		// trump suit gui
		trumpSuitGUI = new TrumpSuitGUI(this, game);

		// next hand gui
		nextHandGUI = new NextHandGUI(this, game);
		
		// VIPlayerGUI
		viPlayerGUI = new VIPlayerGUI(this, game);

		createCards();

		// create the main panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		// mainPanel.add(createInfoPanel(), BorderLayout.NORTH);
		mainPanel.add(cardPanel, BorderLayout.CENTER);
		mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

		add(mainPanel, BorderLayout.CENTER);

		// add the area for debugging messages
		//add(new JScrollPane(debugArea), BorderLayout.SOUTH);

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
	 * When the user tries to close the game, it displays a pop-up that asks the
	 * user if the user is sure about closing the window. If the user presses
	 * yes, it should quit the game and close the SkyeTekReader window.
	 */
	private void detectGUIClosed() {

		// code from : http://stackoverflow.com/questions/9093448/do-something-when-the-close-button-is-clicked-on-a-jframe
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent windowEvent) {

				JOptionPane.showMessageDialog(GameGUI.this,
						"Press on the Quit button to close this application");
			}

		});
	}

	private JPanel createButtonPanel() {
		JPanel southPanel = new JPanel(new GridLayout(1, 0));
		southPanel.add(createResumeButtonPanel());
		southPanel.add(createBackButtonPanel());
		southPanel.add(createUndoButtonPanel());
		southPanel.add(createHelpButtonPanel());
		southPanel.add(createQuitPanel());
		return southPanel;
	}

	@SuppressWarnings("boxing")
	private void createCards() {
		cardPanel = new JPanel();
		layout = new CardLayout();
		cardPanel.setLayout(layout);

		// create the GUI name array
		cardNames = new String[NUM_GUIS];
		cardNames[VI_PLAYER_GUI] = "viGUI";
		cardNames[SCANNING_BLIND_GUI] = "scanningGUI";
		cardNames[BID_POSITION_GUI] = "bidPosGUI";
		cardNames[BID_NUMBER_GUI] = "bidNumGUI";
		cardNames[TRUMP_SUIT_GUI] = "trumpSuitGUI";
		// cardNames[FIRST_CARD_GUI] = "firstCardGUI";
		cardNames[GAME_STATUS_GUI] = "gameStatusGUI";
		cardNames[SCAN_DUMMY_GUI] = "scanDummyGUI";
		// cardNames[7] = "resetGUI";
		// cardNames[GAME_STATUS_GUI] = "gameStatusGUI";
		cardNames[NEXT_HAND_GUI] = "nextHandGUI";
		cardNames[HELP_GUI] = "help";

		// add all the GUIs to the card panel

		cardPanel.add(viPlayerGUI, cardNames[VI_PLAYER_GUI]);
		cardPanel.add(new ScanningBlindGUI(this, game),
				cardNames[SCANNING_BLIND_GUI]);

		cardPanel.add(bidPositionGUI, cardNames[BID_POSITION_GUI]);
		cardPanel.add(bidNumberGUI, cardNames[BID_NUMBER_GUI]);

		cardPanel.add(trumpSuitGUI, cardNames[TRUMP_SUIT_GUI]);
		// cardPanel.add(new FirstCardGUI(this, game),
		// cardNames[FIRST_CARD_GUI]);

		cardPanel.add(gameStatusGUI, cardNames[GAME_STATUS_GUI]);

		cardPanel.add(new ScanDummyGUI(this, game), cardNames[SCAN_DUMMY_GUI]);

		// cardPanel.add(new ResetGUI(game), cardNames[7]);
		// cardPanel.add(new GameStatusGUI(game), cardNames[GAME_STATUS_GUI]);

		cardPanel.add(nextHandGUI, cardNames[NEXT_HAND_GUI]);

		cardPanel.add(new HelpGUI(), cardNames[HELP_GUI]);

		// show the first card
		layout.show(cardPanel, cardNames[VI_PLAYER_GUI]);
		currentScreen = VI_PLAYER_GUI;
		screensViewed.push(VI_PLAYER_GUI);
	}

	/**
	 * Creates a JPanel which displays debugging information.
	 * 
	 * @return a JPanel displaying the bid, current antenna and trick
	 */
	protected JPanel createInfoPanel() {
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
	protected JPanel createQuitPanel() {

		quitButton = GUIUtilities.createButton("Quit");
		quitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				// a pop-up that asks the user if the user is sure about closing
				// the window. If the user presses yes, it should quit the game
				// and close the SkyeTekReader window.
				
				// code from : http://stackoverflow.com/questions/9093448/do-something-when-the-close-button-is-clicked-on-a-jframe
				
				if (JOptionPane.showConfirmDialog(GameGUI.this,
						"Are you sure you want to close this window?",
						"Confirm Exit", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {

					// close the SkyeTex window when playing the actual game
					if (!Game.isTestMode()) {

						BridgeActualGame.closeServerWindow();
					}

					game.quit();

				}

			}

		});

		JPanel quitPanel = GUIUtilities.packageButton(quitButton,
				FlowLayout.CENTER);
		return quitPanel;

	}

	/**
	 * Creates a JPanel which displays the current bid.
	 * 
	 * @return a JPanel with a JLabel that displays the bid
	 */
	protected JPanel createBidPanel() {

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
	protected JPanel createAntennaPanel() {

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
	protected JPanel createTrickPanel() {

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
	protected JPanel createUndoButtonPanel() {

		// create a new JPanel with a FlowLayout
		resetButton = GUIUtilities.createButton("Undo");
		resetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				//showResetGUI();
				
				//reverse();
			}

		});

		return GUIUtilities.packageButton(resetButton, FlowLayout.RIGHT);
		// panel.add(GUIUtilities.packageButton(resetButton, FlowLayout.RIGHT));
		// return panel;

	}

	/**
	 * Creates the "Back" button panel at the bottom of the screen.
	 * 
	 * @return A JPanel containing a single, right-oriented "Back" button.
	 */
	protected JPanel createBackButtonPanel() {

		// create a new JPanel with a FlowLayout
		resetButton = GUIUtilities.createButton("Back");
		resetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				//showResetGUI();
				
				reverse();
			}

		});

		return GUIUtilities.packageButton(resetButton, FlowLayout.RIGHT);
		// panel.add(GUIUtilities.packageButton(resetButton, FlowLayout.RIGHT));
		// return panel;

	}

	
	/**
	 * Creates the "help" button panel at the bottom of the screen.
	 * 
	 * @return A JPanel containing a single, center-oriented "help" button.
	 */
	protected JPanel createHelpButtonPanel() {

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		helpButton = GUIUtilities.createButton("Help");

		helpButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				showHelp();
			}

		});

		panel.add(GUIUtilities.packageButton(helpButton, FlowLayout.CENTER));
		return panel;

	}

	/**
	 * Creates the "resume" button panel at the bottom of the screen.
	 * 
	 * @return A JPanel containing a single, left-oriented "Resume" button.
	 */
	protected JPanel createResumeButtonPanel() {

		// create a new JPanel with a FlowLayout
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		resumeButton = GUIUtilities.createButton("Resume");
		resumeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// reverse();

				// game.resumeGame();

				BridgeActualGame.startServer();

				// start the game 15 seconds after starting the C# Server
				TimerTask timerTask = new TimerTask() {

					@Override
					public void run() {

						try {

							game.activateAntennas();

						} catch (UnknownHostException e) {

							// TODO Auto-generated catch block
							e.printStackTrace();

						} catch (IOException e) {

							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						game.resumeGame();

					}

				};

				// wait 4 seconds after starting the server to start the game
				Timer timer = new Timer(true);
				timer.schedule(timerTask, 4000);

			}

		});

		if (Game.isTestMode()) {

			resumeButton.setEnabled(false);
		}

		panel.add(GUIUtilities.packageButton(resumeButton, FlowLayout.LEFT));
		return panel;
	}

	private void setAntennaLabel(String text) {
		// antennaLabel.setText(text);
	}

	private void setTrickLabel(String text) {
		// trickLabel.setText(text);
	}

	/** Advances to the next GUI in line. */
	@SuppressWarnings("boxing")
	protected void changeFrame() {

		System.out.println("change frame current screen "
				+ cardNames[currentScreen]);

		// System.out.println("before change frame currentScreen " +
		// currentScreen);

		// advance to the appropriate screen
		screensViewed.push(currentScreen);

		// If the current gui is the scan dummy gui, then change frame to the
		// game status gui. Otherwise, increment value of current screen
		// by 1
		if (currentScreen == SCAN_DUMMY_GUI) {

			currentScreen = GAME_STATUS_GUI;

		} else if (currentScreen == GAME_STATUS_GUI) {

			// System.out.println("current screen is game status gui");

			// if a new hand is started, then screen should change from
			// GameStatusGUI to NEXT_HAND_GUI. If, after the first card has
			// been played and blind person is not dummy, then screen needs to
			// switch
			// from GameStatusGUI to SCAN_DUMMY_GUI.

			if (switchFromGameStatusGUI == SWITCH_TO_SCAN_DUMMY) {

				currentScreen = SCAN_DUMMY_GUI;

			} else if (switchFromGameStatusGUI == SWITCH_TO_NEXT_HAND) {

				System.out.println("switching to Next Hand GUI");

				// refresh the display before switching screens

				// nextHandGUI.refreshDisplay();

				currentScreen = NEXT_HAND_GUI;
			}

			// else, do nothing

		} else if (currentScreen == NEXT_HAND_GUI) {

			currentScreen = SCANNING_BLIND_GUI;

		} else {

			currentScreen++;
		}

		// update text on refresh display
		if (currentScreen == NEXT_HAND_GUI) {

			nextHandGUI.refreshDisplay();
		}

		debugMsg("Switching to screen " + cardNames[currentScreen]);

		System.out.println("Switching to screen " + cardNames[currentScreen]);

		// Note : its position/order is important
		if (Game.isTestMode()) {
			determineIfRightGUI();
		}

		layout.show(cardPanel, cardNames[currentScreen]);
		requestFocusInWindow();

		// System.out.println("after change frame currentScreen " +
		// currentScreen);

		// debugMsg("currentScreen " + currentScreen);
	}

	private void determineIfRightGUI() {
		// figure out if it is the right gui for listening to key press
		if (currentScreen == VI_PLAYER_GUI || currentScreen == HELP_GUI
				|| currentScreen == TRUMP_SUIT_GUI
				|| currentScreen == BID_NUMBER_GUI
				|| currentScreen == BID_POSITION_GUI
				|| currentScreen == NEXT_HAND_GUI) {

			((TestAntennaHandler) game.getHandler()).setRightGUI(false);

		} else {

			((TestAntennaHandler) game.getHandler()).setRightGUI(true);
		}
	}

	/**
	 * Adds a message to the debugging panel
	 */
	@Override
	public void debugMsg(String msg) {
		debugArea.append(msg + "\n");
		debugArea.setCaretPosition(debugArea.getText().length() - 1);
	}

	// Prints out a list of cards in the suit and hand specified.
	@SuppressWarnings("unused")
	private void printCards(Suit s, Player players) {
		Iterator<Card> cardIter = players.cards();
		while (cardIter.hasNext()) {
			Card c = cardIter.next();
			if (c.getSuit() == s) {
				// print its suit and rank
				debugMsg(c.getRank() + " of " + c.getSuit());

			}
		}
	}

	/**
	 * Sets the bid label to the new contract
	 * 
	 * @param contract
	 *            the new contract
	 */
	@Override
	public void contractSet(Contract contract) {
		// bidLabel.setText(contract.toString());
		setAntennaLabel(game.getCurrentHand());
	}

	/**
	 * Resets the game to start over.
	 */
	@Override
	public void gameReset() {

		System.out.println("Game reset");

		setAntennaLabel("N/A");
		setTrickLabel("N/A");
		// bidLabel.setText("N/A");

		// when game is reset, first show the winner of the last hand

		// currentScreen = SCANNING_BLIND_GUI;

		// nextHandGUI.refreshDisplay();

		// currentScreen = NEXT_HAND_GUI;

		currentScreen = SCANNING_BLIND_GUI;

		System.out.println("current screen is now " + cardNames[currentScreen]);

		screensViewed.clear();
		screensViewed.push(SCANNING_BLIND_GUI);
		// switchFromGameStatusGUI = SWITCH_TO_SCANNING_BLIND;
		switchFromGameStatusGUI = NONE;

		// gameStatusGUI.setFirstCardPlayed(false);

		if (Game.isTestMode()) {

			trumpSuitGUI.setHandNum(2);
			bidNumberGUI.setHandNum(2);
			bidPositionGUI.setHandNum(2);
		}

		layout.show(cardPanel, cardNames[currentScreen]);

		System.out.println("should show " + cardNames[currentScreen] + " "
				+ currentScreen);

		this.repaint();

		this.requestFocusInWindow();
	}

	/** Returns to the last card viewed. */
	@SuppressWarnings("boxing")
	public void reverse() {
		
		System.out.println("GameGUI undo");
		
		// if user wants to change position of blind player
		//if(game.blindPayerHasNoCard()){
		
		// if the user wants to change position of blind player
		if(currentScreen == SCANNING_BLIND_GUI){
			
			viPlayerGUI.stopHandlerThread();
			
			game.resetVIPlayer();
		}
		
		if (!screensViewed.isEmpty()) {
			currentScreen = screensViewed.pop();
			layout.show(cardPanel, cardNames[currentScreen]);
			requestFocusInWindow();
		}
	}
	
	/**
	 * Resets the last move
	 */
	public void undo(){
		
		
	}
	
	

	/**
	 * Updates the GUI to show whose turn it is and adds the card to the current
	 * trick
	 * 
	 * @param turn
	 *            the next player
	 * @param card
	 *            the card just played into the trick
	 */
	@Override
	public void cardPlayed(Direction turn, Card card) {
		setAntennaLabel(turn.getNextDirection().toString());
		setTrickLabel(game.getCurrentTrick().toString());
	}

	@SuppressWarnings("boxing")
	private void showResetGUI() {
		screensViewed.push(currentScreen);
		layout.show(cardPanel, "resetGUI");
		currentScreen = 7;
		requestFocusInWindow();
	}

	/**
	 * Updates the antenna label after a card is scanned
	 * 
	 * @param card
	 *            the card just scanned
	 */
	@Override
	public void cardScanned(Card card) {
		// setAntennaLabel(game.getCurrentHand());
	}

	/**
	 * Updates the display based on the trick being won. Updates the current
	 * player and the trick.
	 * 
	 * @param winner
	 *            the player who won the trick
	 */
	@Override
	public void trickWon(Direction winner) {
		setAntennaLabel(winner.toString());
		setTrickLabel(game.getCurrentTrick().toString());
	}

	/**
	 * Display a help message
	 */
	@SuppressWarnings("boxing")
	public void showHelp() {
		screensViewed.push(currentScreen);
		layout.show(cardPanel, "help");
		currentScreen = 6;

	}

	public void blindHandScanned() {

	}

	@Override
	public void dummyHandScanned() {
		// TODO Auto-generated method stub

	}

	public void setSwitchFromGameStatusGUI(int switchFromGameStatusGUI) {

		this.switchFromGameStatusGUI = switchFromGameStatusGUI;

		System.out
				.println("switchFromGameStatusGUI " + switchFromGameStatusGUI);
	}
	
	public static boolean isScanBlindGUI(){
		
		return currentScreen == SCANNING_BLIND_GUI ;
	}


}
