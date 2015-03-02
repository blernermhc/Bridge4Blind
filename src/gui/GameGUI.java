package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
 **/

public class GameGUI extends JFrame implements GameListener {

	// the CardLayout controlling the display of GUIS
	private CardLayout layout;

	// the Game object associated with this GUI
	private Game game;

	// the currently visible GUI
	private int currentScreen;

	// The history of screens viewed, used for the back button
	private Stack<Integer> screensViewed = new Stack<Integer>();

	private JButton backButton;
	private JButton quitButton;
	private JButton resetButton;
	private JButton helpButton;
	// private JLabel bidLabel;
	// private JLabel antennaLabel;
	// private JLabel trickLabel;

	/** Font used to display messages on the main screen */
	public static final Font INFO_FONT = new Font("Verdana", Font.BOLD, 30);

	private static final String FIRST_CARD_GUI_NUMBER = "firstCardGUI";

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

		createCards();

		// create the main panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		// mainPanel.add(createInfoPanel(), BorderLayout.NORTH);
		mainPanel.add(cardPanel, BorderLayout.CENTER);
		mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

		add(mainPanel, BorderLayout.CENTER);

		// add the area for debugging messages
		add(new JScrollPane(debugArea), BorderLayout.SOUTH);

		// turn off focus traversal keys so that the tab key can be used as game
		// input
		setFocusTraversalKeysEnabled(false);

		this.pack();
		this.setVisible(true);
		this.requestFocusInWindow();
	}

	private JPanel createButtonPanel() {
		JPanel southPanel = new JPanel(new GridLayout(1, 0));
		southPanel.add(createBackButtonPanel());
		southPanel.add(createHelpButtonPanel());
		southPanel.add(createResetButtonPanel());
		southPanel.add(createQuitPanel());
		return southPanel;
	}

	@SuppressWarnings("boxing")
	private void createCards() {
		cardPanel = new JPanel();
		layout = new CardLayout();
		cardPanel.setLayout(layout);

		// create the GUI name array
		cardNames = new String[9];
		cardNames[0] = "viGUI";
		cardNames[1] = "scanningGUI";
		cardNames[2] = "bidPosGUI";
		cardNames[3] = "bidNumGUI";
		cardNames[4] = "trumpSuitGUI";
		cardNames[5] = "firstCardGUI";
		cardNames[6] = "scanDummyGUI";
		// cardNames[7] = "resetGUI";
		cardNames[7] = "gameStatusGUI";
		cardNames[8] = "help";

		// add all the GUIs to the card panel
		cardPanel.add(new VIPlayerGUI(this, game), cardNames[0]);
		cardPanel.add(new ScanningBlindGUI(this, game), cardNames[1]);
		cardPanel.add(new BidPositionGUI(this, game), cardNames[2]);
		cardPanel.add(new BidNumberGUI(this, game), cardNames[3]);
		cardPanel.add(new TrumpSuitGUI(this, game), cardNames[4]);
		cardPanel.add(new FirstCardGUI(this, game), cardNames[5]);
		cardPanel.add(new ScanDummyGUI(this, game), cardNames[6]);
		// cardPanel.add(new ResetGUI(game), cardNames[7]);
		cardPanel.add(new GameStatusGUI(game), cardNames[7]);
		cardPanel.add(new HelpGUI(), cardNames[8]);

		// show the first card
		layout.show(cardPanel, cardNames[0]);
		currentScreen = 0;
		screensViewed.push(0);
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
				game.quit();
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
	 * Creates the "reset" button panel at the bottom of the screen.
	 * 
	 * @return A JPanel containing a single, right-oriented "reset" button.
	 */
	protected JPanel createResetButtonPanel() {

		// create a new JPanel with a FlowLayout
		resetButton = GUIUtilities.createButton("Undo");
		resetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				showResetGUI();
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
	 * Creates the "back" button panel at the bottom of the screen.
	 * 
	 * @return A JPanel containing a single, left-oriented "back" button.
	 */
	protected JPanel createBackButtonPanel() {

		// create a new JPanel with a FlowLayout
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		backButton = GUIUtilities.createButton("Back");
		backButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				reverse();
			}

		});

		panel.add(GUIUtilities.packageButton(backButton, FlowLayout.LEFT));
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

		// advance to the appropriate screen
		screensViewed.push(currentScreen);

		debugMsg("current Screen before if " + currentScreen);
		if (cardNames[currentScreen].equals(FIRST_CARD_GUI_NUMBER)
				&& game.getBlindPosition().equals(game.getDummyDirection())) {

			debugMsg("Blind Player is Dummy player so no need to scan Dummy cards");
			currentScreen++;
		}
		
		
		debugMsg("current Screen after if " + currentScreen);
		currentScreen++;
		
		debugMsg("now current Screen is " + currentScreen);
		debugMsg("Switching to screen " + cardNames[currentScreen]);
		layout.show(cardPanel, cardNames[currentScreen]);
		requestFocusInWindow();
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
		setAntennaLabel("N/A");
		setTrickLabel("N/A");
		// bidLabel.setText("N/A");

		// Display the screen directing the players to scan the blind cards,
		// but set up the back button so that it goes to the screen to set
		// where the blind player is sitting.
		currentScreen = 1;
		screensViewed.clear();
		screensViewed.push(0);

		layout.show(cardPanel, cardNames[currentScreen]);
		this.requestFocusInWindow();
	}

	/** Returns to the last card viewed. */
	@SuppressWarnings("boxing")
	public void reverse() {
		if (!screensViewed.isEmpty()) {
			currentScreen = screensViewed.pop();
			layout.show(cardPanel, cardNames[currentScreen]);
			requestFocusInWindow();
		}
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

	/**
	 * The main program!!!! Start the server first.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Game game = new Game();
			game.activateAntennas();
			GameGUI gui = new GameGUI(game);
			game.addListener(new AudibleGameListener());
			game.addListener(gui);

			gui.debugMsg("main run");
		} catch (UnknownHostException e) {
			System.err.println("Could not connect to server.  Host unknown.");
		} catch (ConnectException connectExc) {
			System.err.println("The server is not running!");
		} catch (SocketException socketEsc) {
			System.err
					.println("Check that there is no virus scanner blocking IRC connections.");
			socketEsc.printStackTrace();
		} catch (IOException e) {
			System.err.println("Could not connect to server.");
			e.printStackTrace();
		}

	}

}