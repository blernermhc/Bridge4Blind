package model;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import audio.SoundManager;
import controller.CardIdentifier;
import controller.HandAntenna;
import controller.Handler;

/**
 * The Game class controls the logic for a game of bridge.
 * 
 * @author Allison DeJordy
 * 
 */

public class Game {

	private static final int DELAY_AFTER_SCANNING_DUMMY = 1000;

	/** Number of tricks that make up a hand */
	private static final int TRICKS_IN_HAND = 13;

	/** The number of players in the game */
	public static final int NUM_PLAYERS = 4;

	// true is Bridege is being played in Test mode (i.e. without hardware).
	// False if Bridge is actually being played (i.e. with the hardware)
	private static boolean isTestMode;

	// The collection of players
	private Player[] players;

	// the position of the blind player
	private Direction blindDirection;

	// the position of the dummy hand
	private Direction dummyDirection;

	/** whose turn it is */
	protected Direction turn;

	// who won the last trick
	private Direction lastWinner;

	// the antenna handler
	private Handler handler;
	// private AntennaHandler handler;

	// the current contract that the team winning the bid must make
	private Contract contract = new Contract();

	/** The current trick */
	protected Trick currentTrick = new Trick();

	// the GUI associated with this game
	private ArrayList<GameListener> listeners = new ArrayList<GameListener>();

	/** the current state of the game: DEALING, FIRSTCARD, PLAYING */
	protected GameState gameState = GameState.DEALING;

	// All the cards that have been played in the current hand
	private Set<Card> cardsPlayed = new HashSet<Card>();

	/**
	 * Create a new game
	 * 
	 * @param handler
	 *            TODO
	 * @param isTestMode
	 *            TODO
	 */
	public Game(Handler handler, boolean isTestMode) {

		this.isTestMode = isTestMode;

		// construct the four hands
		players = new Player[NUM_PLAYERS];
		for (int i = 0; i < players.length; i++) {
			players[i] = new Player();
		}

		this.handler = handler;

		// construct the antenna handler
		// handler = new AntennaHandler(new CardDatabase());
	}

	/**
	 * Connect to the RFID server and begin listening for messages from the
	 * server. There is a separate object listening to each of the 5 antennas. *
	 * 
	 * @throws UnknownHostException
	 *             if the host name being used to connect to he server is wrong
	 * @throws IOException
	 *             any other I/O problem when establishing the connection
	 */
	public void activateAntennas() throws UnknownHostException, IOException {
		HandAntenna[] handAntennas = new HandAntenna[NUM_PLAYERS];
		Direction[] directions = Direction.values();
		for (int i = 0; i < players.length; i++) {
			handAntennas[i] = new HandAntenna(directions[i], this);
		}

		// construct the card identifier
		CardIdentifier id = new CardIdentifier(this);
		handler.connect();

		// add the listeners for the antenna handler
		handler.addHandListener(handAntennas[Direction.NORTH.ordinal()],
				Direction.NORTH);
		handler.addHandListener(handAntennas[Direction.EAST.ordinal()],
				Direction.EAST);
		handler.addHandListener(handAntennas[Direction.SOUTH.ordinal()],
				Direction.SOUTH);
		handler.addHandListener(handAntennas[Direction.WEST.ordinal()],
				Direction.WEST);
		handler.addIdListener(id);
	}

	/**
	 * Sets the dummy and prepares for the player after the declarer to go.
	 * 
	 * @param declarer
	 *            The position of the declarer.
	 */
	public void initPlayingPhase(Direction declarer) {

		contract.setBidWinner(declarer);

		dummyDirection = declarer.getPartner();

		players[dummyDirection.ordinal()].setDummy(true);

		turn = declarer.getNextDirection();
		try {
			handler.switchHand(turn);
		} catch (IOException e) {
			System.out.println("Lost connection to server!");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Cycling timer interrupted!");
			e.printStackTrace();
		}
	}

	private void switchHand(Direction toPlayer) {
		try {
			handler.switchHand(toPlayer);
		} catch (IOException e) {
			System.out.println("Lost connection to server!");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Cycling timer interrupted!");
			e.printStackTrace();
		}
	}

	/**
	 * Determines who won the current trick.
	 * 
	 * @return the position in the players array of the winner
	 */
	private int determineWinner() {

		int winner = -1;

		if (currentTrick.isOver()) {
			winner = currentTrick.determineWinner(contract.getTrump());

			lastWinner = Direction.values()[winner];

		}

		return winner;
	}

	/** Clears all hands of cards and sets the game back into the dealing phase. */
	// TODO: Do I need this method or just resetGame?
	public void resetHands() {

		System.out.println("Game resetHand");

		for (int i = 0; i < players.length; i++) {
			players[i].newHand();
		}
	}

	/**
	 * Reinitializes everything to prepare to play a new hand.
	 */
	public void resetGame() {

		System.out.println("Game resetGame");

		resetHands();
		currentTrick = new Trick();
		contract = new Contract();
		cardsPlayed.clear();
		lastWinner = null;
		players[dummyDirection.ordinal()].setDummy(false);
		switchHand(blindDirection);

		for (GameListener listener : listeners) {
			listener.gameReset();
		}

	}

	/**
	 * <p>
	 * Takes the appropriate action when a card is read over a directional
	 * antenna.
	 * </p>
	 * 
	 * <p>
	 * During dealing, the card is added to the blind person's hand if scanned
	 * over that person's antenna. Does nothing if scanned over somebody else's
	 * antenna.
	 * </p>
	 * 
	 * <p>
	 * After dealing but before the first card is complete, if the player
	 * following the declarer scans a card, it is played into the trick. If the
	 * dummy scans cards, they are added to the dummy's hand.
	 * </p>
	 * 
	 * <p>
	 * After the dummy's hand is scanned in, any further cards found are played
	 * into the trick if they are scanned over the appropriate person's antenna
	 * </p>
	 * 
	 * @param direction
	 *            the antenna direction that saw a card
	 * @param card
	 *            the card seen
	 */
	public synchronized void cardFound(Direction direction, Card card) {

		debugMsg("card found");

		if (gameState == GameState.DEALING) {
			debugMsg("dealing phase");
			if (direction == blindDirection) {
				System.out.println("dealing card into blind hand: " + card);
				debugMsg("Card added to hand " + blindDirection);
				scanCardIntoHand(blindDirection, card);
				if (players[blindDirection.ordinal()].hasFullHand()) {
					setGameState(GameState.FIRSTCARD);

					for (GameListener listener : listeners) {
						listener.blindHandScanned();
					}

				}
			}

		} else if (gameState == GameState.FIRSTCARD) {
			assert dummyDirection != null;
			// System.out.println("State is FIRSTCARD");
			// System.out.println("direction = " + direction);
			// System.out.println("turn = " + turn);
			// System.out.println("blindDirection = " + blindDirection);
			// System.out.println("dummyDirection = " + dummyDirection);

			// First card is being played before the dummy is revealed.
			if (direction == turn) {
				assert dummyDirection.follows(direction);
				playCard(card);

				if (dummyDirection == blindDirection) {
					for (GameListener listener : listeners) {
						listener.dummyHandScanned();
					}
					setGameState(GameState.PLAYING);
				} else {
					setGameState(GameState.SCANNING_DUMMY);
				}
				return;
			}
		}

		else if (gameState == GameState.SCANNING_DUMMY) {

			// First card has been played. Time to expose the dummy hand.
			if (direction == dummyDirection) {
				debugMsg("Adding card to dummy hand at " + dummyDirection);
				scanCardIntoHand(dummyDirection, card);
				if (players[dummyDirection.ordinal()].hasFullHand()) {
					for (GameListener listener : listeners) {
						listener.dummyHandScanned();
					}

					// Delay going into the playing state so that the last
					// card scanned is not immediately played.
					try {
						Thread.sleep(DELAY_AFTER_SCANNING_DUMMY);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					gameState = GameState.PLAYING;
				}
			}

			// PLAYING
		} else if (direction == turn) {
			debugMsg("playing phase");
			if (playCard(card)) {

				if (currentTrick.isOver()) {
					debugMsg("Trick over");
					endTrick();
				}

				if (allTricksOver()) {
					debugMsg("Round over");
					gameState = GameState.DEALING;
					resetGame();
				}
			}

		}

	}

	private void debugMsg(String msg) {
		for (GameListener listener : listeners) {
			listener.debugMsg(msg);
		}
	}

	/**
	 * Determine who won and prepare for the next trick.
	 */
	private void endTrick() {
		int winner = determineWinner();
		players[winner].wonTrick();
		turn = Direction.values()[winner];
		switchHand(turn);
		for (GameListener listener : listeners) {
			listener.trickWon(turn);
		}
	}

	/**
	 * Play a card into the trick. Does not add the card if the card has been
	 * previously played in this hand, or if it is not legal to play the card.
	 * 
	 * @param card
	 *            the card played
	 * @return true if the card was added to the trick.
	 */
	private boolean playCard(Card card) {
		// Check if card was already played in this hand.
		// Avoid accidental scanning as a trick is collected
		// and passes over the antenna or of a card held
		// over an antenna for too long.
		if (cardsPlayed.contains(card)) {
			return false;
		}

		// System.out.println("Staring playCard");
		// Ignore multiple antenna readings of the same card
		int position = turn.ordinal();

		// First card in game or first card in a new trick
		if (currentTrick.isEmpty() || currentTrick.isOver()) {
			// System.out.println("Starting new trick");
			currentTrick = new Trick();
			currentTrick.setLedSuit(card.getSuit());
		}

		// TODO LOOK HERE
		// isLegal
		if (currentTrick.getCard(position) == null) {
			// System.out.println("Adding card to trick");
			if (!players[position].isLegal(card, currentTrick.getLedSuit())) {

				System.out.println("*** Not a legal card: " + card.toString());
				SoundManager soundManager = SoundManager.getInstance();

				// Need a better sound here!!!
				soundManager.addSound("/sounds/bidding/0.WAV");
				soundManager.playSounds();

				return false;
			}

			debugMsg("Adding card to trick at " + turn);

			cardsPlayed.add(card);
			players[position].removeCard(card);
			currentTrick.add(card, position);
			// System.out.println("Notifying listeners");
			for (GameListener listener : listeners) {
				listener.cardPlayed(turn, card);
			}
			// System.out.println("Done notifying listeners");
			turn = turn.getNextDirection();
			// System.out.println("Switching antenna");
			switchHand(turn);
			return true;
		}

		// This should never happen. It would mean that the wrong antenna
		// was being listened to.
		assert false;
		return false;

		// System.out.println("Returning from playCard");
	}

	/**
	 * Scan a card into a hand, reading it out loud
	 * 
	 * @param dir
	 *            the direction of the hand being scanned into
	 * @param card
	 *            the card being scanned
	 */
	protected void scanCardIntoHand(Direction dir, Card card) {

		int pos = dir.ordinal();

		// dummy and blind, dont say the card out loud if the player already has
		// the card
		if (players[pos].isBlind() || players[pos].isDummy()) {
			if (players[pos].getHand().containsCard(card)) {

				return;
			}
		}
		for (GameListener listener : listeners) {
			listener.cardScanned(card);
		}

		// int pos = dir.ordinal();
		players[pos].addCard(card);
		// System.out.println("Game.scanCardIntoHand returning");
	}

	/**
	 * Called when a card is identified on the id antenna. Passes the message on
	 * to all the listeners of the game using their cardScanned method.
	 * 
	 * @param c
	 *            the card that was identified.
	 */
	public void cardIded(Card c) {
		for (GameListener listener : listeners) {
			listener.cardScanned(c);
		}
	}

	/**
	 * 
	 * @param direction
	 *            the position of the player whose number of tricks won is
	 *            reported
	 * @return the number of tricks won by that player's team.
	 */
	public int getTricksWon(Direction direction) {
		return (players[direction.ordinal()]).getTricksWon();
	}

	public boolean allTricksOver() {

		int totalTricks = 0;

		for (int i = 0; i < players.length; i++) {
			totalTricks += players[i].getTricksWon();
		}

		System.out.println("All tricks over? totalTricks " + totalTricks);

		if (totalTricks == TRICKS_IN_HAND) {
			return true;
		}

		return false;
	}

	@SuppressWarnings("javadoc")
	public void setContractNum(int contractNum) {
		contract.setContractNum(contractNum);
	}

	@SuppressWarnings("javadoc")
	public int getContractNum() {
		return contract.getContractNum();
	}

	@SuppressWarnings("javadoc")
	public void addListener(GameListener listener) {
		listeners.add(listener);
	}

	@SuppressWarnings("javadoc")
	public Player[] getPlayers() {
		return players;
	}

	/**
	 * Set the position of the blind player and switches the server to listen to
	 * the blind person's antenna.
	 * 
	 * @param blindPosition
	 *            the position of the blind player
	 */
	public void setBlindPosition(Direction blindPosition) {
		this.blindDirection = blindPosition;
		players[blindDirection.ordinal()].setBlind(true);
		switchHand(blindPosition);
	}

	@SuppressWarnings("javadoc")
	public Direction getBlindPosition() {
		return blindDirection;
	}

	@SuppressWarnings("javadoc")
	public Player getBlindPlayer() {
		return players[getBlindPosition().ordinal()];
	}

	@SuppressWarnings("javadoc")
	public Direction getDummyPosition() {
		return dummyDirection;
	}

	@SuppressWarnings("javadoc")
	public Player getDummyPlayer() {
		return players[getDummyPosition().ordinal()];
	}

	@SuppressWarnings("javadoc")
	public Trick getCurrentTrick() {
		return currentTrick;
	}

	/**
	 * Undoes the playing of the last card into the trick. Does nothing if the
	 * trick is empty.
	 */
	public void undo() {

		System.out.println("undo card");

		if (!currentTrick.isEmpty()) {
			int predecessorPos = turn.getPreviousDirection().ordinal();
			players[predecessorPos].addCard(currentTrick
					.getCard(predecessorPos));
			currentTrick.clearCard(predecessorPos);
			turn = turn.getPreviousDirection();
			switchHand(turn);
		}
	}

	/**
	 * Undo an entire trick. Does nothing if the trick is empty or over.
	 */
	public void undoTrick() {
		if (currentTrick.isEmpty() || currentTrick.isOver()) {
			return;
		}

		System.out.println("undo trick");

		for (int i = 0; i < 4; i++) {
			Card c = currentTrick.getCard(i);
			if (c != null) {
				players[i].addCard(c);
			}
		}

		currentTrick = new Trick();
		if (lastWinner != null) {
			turn = lastWinner;
		} else {
			turn = getDummyPosition().getPartner();
		}

		switchHand(turn);
	}

	/**
	 * Sets the trump suit of the contract.
	 * 
	 * @param suit
	 *            The suit of the contract.
	 */
	public void setTrump(Suit suit) {
		contract.setTrump(suit);

		for (GameListener listener : listeners) {
			listener.contractSet(contract);
		}

	}

	@SuppressWarnings("javadoc")
	public Suit getTrumpSuit() {
		return contract.getTrump();
	}

	@SuppressWarnings("javadoc")
	public Handler getHandler() {
		return handler;
	}

	@SuppressWarnings("javadoc")
	public void setGameState(GameState gameState) {
		this.gameState = gameState;
		System.out.println("State set to " + gameState);
	}

	public boolean isScanningDummy() {
		return gameState == GameState.SCANNING_DUMMY;
	}

	@SuppressWarnings("javadoc")
	public String getCurrentHand() {
		if (turn == null) {
			return "";
		}
		return turn.toString();
	}

	@SuppressWarnings("javadoc")
	public Direction getLastWinner() {
		return lastWinner;
	}

	@SuppressWarnings("javadoc")
	public boolean trickIsEmpty() {
		return currentTrick.isEmpty();
	}

	@SuppressWarnings("javadoc")
	public Contract getContract() {
		return contract;
	}

	/**
	 * Sends the quit message to the server and quits this program.
	 */
	public void quit() {
		if (handler != null) {
			handler.quitServer();
		}
		System.exit(0);
	}

	public static boolean isTestMode() {
		return isTestMode;
	}

	public Direction getBidWinner() {

		return contract.getBidWinner();
	}

}