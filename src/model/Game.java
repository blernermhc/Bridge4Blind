package model;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import audio.SoundManager;
import controller.HandAntenna;
import controller.Handler;
import controller.TestAntennaHandler;

/**
 * The Game class controls the logic for a game of bridge.
 * 
 * @author Allison DeJordy, Humaira Orchee
 * 
 */

public class Game {

	private static final int DELAY_AFTER_SCANNING_DUMMY = 1000;

	/** Number of tricks that make up a hand */
	private static final int TRICKS_IN_HAND = 13;

	/** The number of players in the game */
	public static final int NUM_PLAYERS = 4;

	// true is Bridge is being played in Test mode (i.e. without hardware).
	// False if Bridge is actually being played (i.e. with the hardware)
	private static boolean isTestMode;

	// The collection of players
	private Player[] players;

	// the position of the blind player
	private Direction blindDirection;

	// the position of the dummy hand
	private Direction dummyDirection;

	// whose turn it is. */
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

	// who won the last hand
	private Direction lastHandWinner;

	// the last card the blind player scanned
	private Card lastBlindCard;
	
	// keeps track of whose turn it is
	//private Stack<Direction> turnStack ;
	
	// keeps track of all the tricks
	//private Stack<Trick> trickStack = new Stack<Trick>() ;

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

		handler.setGame(this);
		
		//trickStack.push(currentTrick) ;

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

		System.out.println("Game : activate antennas");

		HandAntenna[] handAntennas = new HandAntenna[NUM_PLAYERS];

		// construct the card identifier
		// CardIdentifier id = new CardIdentifier(this);
		handler.connect();

		Direction[] directions = Direction.values();
		for (int i = 0; i < players.length; i++) {
			handAntennas[i] = new HandAntenna(directions[i], this);
		}

		// add the listeners for the antenna handler
		handler.addHandListener(handAntennas[Direction.NORTH.ordinal()],
				Direction.NORTH);
		handler.addHandListener(handAntennas[Direction.EAST.ordinal()],
				Direction.EAST);
		handler.addHandListener(handAntennas[Direction.SOUTH.ordinal()],
				Direction.SOUTH);
		handler.addHandListener(handAntennas[Direction.WEST.ordinal()],
				Direction.WEST);
		// handler.addIdListener(id);
	}
	
	public void closeHandler() throws IOException {
		System.out.println("Closing handler");
		handler.disconnect();
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
		//turnStack.push(turn) ;
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
		//trickStack = new Stack<Trick>() ;
		//trickStack.push(currentTrick) ;
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

		debugMsg("Game : card found");

		if (gameState == GameState.DEALING) {

			System.out.println("Game : Dealing state");

			cardFoundInDealingState(direction, card);

		} else if (gameState == GameState.FIRSTCARD) {
			
			System.out.println("FIRST CARD");

//			System.out.println("Game : First card state");
//			
//			boolean blindPlayerContainsCard = players[blindDirection.ordinal()].getHand().containsCard(card);
//			if(direction == blindDirection && blindPlayerContainsCard){
//				
//				cardIded(card);
//			}

			assert dummyDirection != null;
			// System.out.println("State is FIRSTCARD");
			// System.out.println("direction = " + direction);
			// System.out.println("turn = " + turn);
			// System.out.println("blindDirection = " + blindDirection);
			// System.out.println("dummyDirection = " + dummyDirection);

			// First card is being played before the dummy is revealed.
			
			System.out.println("direction " + direction);
			
			System.out.println("turn " + turn);
			
			if (direction == turn) {

				System.out.println("Game : Playing first card");

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
		// Scanning dummy card
		else if (gameState == GameState.SCANNING_DUMMY) {

			System.out.println("Game : scan dummy state");

			cardFoundInScanDummyState(direction, card);

			// PLAYING
		} else if (direction == turn) {

			System.out.println("Game : Playing state");

			cardFoundInPlayingState(card);

		}

	}

	/**
	 * 
	 * @param card
	 */
	private void cardFoundInPlayingState(Card card) {

		debugMsg("playing phase");

		System.out.println("Game : cardFoundInPlayingState()");

		if (playCard(card)) {

			// check if trick over after non-blind person was the last person to
			// play
			if (currentTrick.isOver()) {

				debugMsg("Trick over");

				System.out
						.println("trick over after non-blind person was the last person to play");

				endTrick();
			}

			// check if hand over after non-blind person was the last person to
			// play
			if (allTricksOver()) {

				debugMsg("Round over");

				System.out
						.println("hand over after non-blind person was the last person to play");
				gameState = GameState.DEALING;

				// resetGame();
			}
		}
	}

	private void cardFoundInScanDummyState(Direction direction, Card card) {
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
	}

	private void cardFoundInDealingState(Direction direction, Card card) {
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
		//turnStack.push(turn) ;
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

		System.out.println("Game : playCard");

		int position = turn.ordinal();

		// if it is the blind player's turn and blind player is not the dummy
		// player, then remember the last card
		// else if it is not the blind players turn, then proceed with play
		if (position == getBlindPosition().ordinal()
				&& !getBlindPosition().equals(getDummyPosition())) {

			System.out.println("Remembering blind playeer's last card");

			lastBlindCard = card;

			cardIded(lastBlindCard);

			return false;

		} else {

			return canPlayCard(card, position);
		}

		// This should never happen. It would mean that the wrong antenna
		// was being listened to.
		// assert false;
		// return false;

		// System.out.println("Returning from playCard");
	}

	/**
	 * Returns true if the player at the specified position can play the
	 * specified card
	 * 
	 * @param card
	 * @param position
	 * @return
	 */
	private boolean canPlayCard(Card card, int position) {

		System.out.println("Game : canPlayCard ?");

		// Check if card was already played in this hand.
		// Avoid accidental scanning as a trick is collected
		// and passes over the antenna or of a card held
		// over an antenna for too long.
		if (cardsPlayed.contains(card)) {

			System.out.println("cannot play card because card has been played");

			return false;
		}

		// System.out.println("Staring playCard");
		// Ignore multiple antenna readings of the same card

		// First card in game or first card in a new trick
		if (currentTrick.isEmpty() || currentTrick.isOver()) {

			System.out.println("First card in game or first card in new trick");

			// System.out.println("Starting new trick");
			currentTrick = new Trick();
			//trickStack.push(currentTrick) ;
			currentTrick.setLedSuit(card.getSuit());
		}

		// if the player at position has not played the card yet
		if (currentTrick.getCard(position) == null) {

			System.out.println("Player yet to play card");

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

			playCardIntoGame(card, position);

			return true;
		}
		return false;
	}

	/**
	 * Lets the player at the specified position play the specified card
	 * 
	 * @param card
	 *            The card to play
	 * @param position
	 *            The position of the player trying to play the card
	 */
	private void playCardIntoGame(Card card, int position) {

		System.out.println("Game : play card into game");

		cardsPlayed.add(card);

		players[position].removeCard(card);

		currentTrick.add(card, position);

		// System.out.println("Notifying listeners");

		for (GameListener listener : listeners) {
			listener.cardPlayed(turn, card);
		}

		// System.out.println("Done notifying listeners");

		turn = turn.getNextDirection();
		//turnStack.push(turn) ;

		// System.out.println("Switching antenna");

		switchHand(turn);
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

		// for blind player, don't say the card out loud if the player already
		// has the card
		if (players[pos].isBlind()) {

			if (players[pos].getHand().containsCard(card)) {

				return;
			}
		}

		// for dummy player, the card is not scanned if the dummy player already
		// has the card or the blind player already has the card or it was the
		// first card in the play
		if (players[pos].isDummy()) {

			if ((players[pos].getHand().containsCard(card))
					|| (getBlindPlayer().getHand().containsCard(card))
					|| (cardsPlayed.contains(card))) {

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

		// TODO : may cause error
		turn = blindPosition;
		//turnStack.push(turn) ;

		players[blindDirection.ordinal()].setBlind(true);
		handler.setBlindDirection(blindPosition);
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
	 * @return TODO
	 */
	public Direction undo() {

		System.out.println("Game : undo card");

		if (!currentTrick.isEmpty()) {
			
			int predecessorPos = turn.getPreviousDirection().ordinal();
			
			Card undoCard = currentTrick
					.getCard(predecessorPos);
			
			players[predecessorPos].addCard(undoCard);
			
			currentTrick.clearCard(predecessorPos);
			
			cardsPlayed.remove(undoCard) ;
			
			turn = turn.getPreviousDirection();
			
			switchHand(turn);
			
			System.out.println("turn is " + turn);
			
			return turn ;
		}
		
		return null ;
	}

	/**
	 * 
	 * Not sure if this is needed
	 * 
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
		//System.out.println("State set to " + gameState);
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

	/**
	 * Figures out who won the last hand
	 * 
	 * @return TODO
	 */
	public int determineHandWinner() {

		// get the bid winner and his/her partner
		Direction bidWinner = getBidWinner();

		Direction bidWinnerPartner = bidWinner.getPartner();

		// calculate the total number of tricks they won together
		int totalTricksWon = players[bidWinner.ordinal()].getTricksWon()
				+ players[bidWinnerPartner.ordinal()].getTricksWon();

		// check if they could fulfill their contract. If they did, they win.
		// Otherwise, the other pair wins.
		if (totalTricksWon >= 6 + contract.getContractNum()) {

			lastHandWinner = bidWinner;

		} else {

			// if the bid winner and their partner did not make the bid, then
			// the other pair wins. So calculate the total tricks the winning
			// pair won.

			lastHandWinner = bidWinner.getNextDirection();

			Direction lastHandWinnerPartner = lastHandWinner.getPartner();

			totalTricksWon = players[lastHandWinner.ordinal()].getTricksWon()
					+ players[lastHandWinnerPartner.ordinal()].getTricksWon();

		}

		return totalTricksWon;
	}

	/**
	 * Returns the players that won the hand
	 * 
	 * @return The array of the directions of the two players that won the hand
	 */
	public Direction getLastHandWinner() {
		return lastHandWinner;
	}

	/**
	 * When a hew hand starts, the game forgets about the winner of the last
	 * hand
	 */
	public void resetLastHandWinner() {

		lastHandWinner = null;
	}

	/**
	 * 
	 */
	public void playBlindCard() {
		System.out.println("In playBlindCard");
		boolean startedPlaying = (gameState == GameState.FIRSTCARD) || (gameState == GameState.SCANNING_DUMMY		) || (gameState == GameState.PLAYING);
		System.out.println("gameState = " + gameState);
		if(!startedPlaying){
			System.out.println("playBlindCard: !startedPlaying; returning");
			return ;
		}
		System.out.println("Game : play blind card");

		// System.out.println("blind direction " + blindDirection.ordinal());

		System.out.println("card " + lastBlindCard);

		if (canPlayCard(lastBlindCard, getBlindPosition().ordinal())) {
			System.out.println("Can play card");
			// check if current trick is over
			if (currentTrick.isOver()) {

				debugMsg("Trick over");
				System.out.println("trick over after blind person");
				endTrick();
			}

			// check if hand is over
			if (allTricksOver()) {

				debugMsg("Round over");

				System.out.println("round over after blind person");

				gameState = GameState.DEALING;

			}
		}
		else {
			System.out.println("Cannot play card");
		}

	}

	/**
	 * 
	 * @return last blind card
	 */
	public Card getLastBlindCard() {
		return lastBlindCard;
	}

	/**
	 * 
	 */
	public void resumeGame() {
		debugMsg("Setting cycling thread");
		handler.setCyclingThread(null);
		debugMsg("Switching hand");
		switchHand(turn);
		debugMsg("Done switching hand");
	}
	
	/**
	 * Allows the user to reselect the position of the visually impaired player
	 */
	public void resetVIPlayer(){
		
		System.out.println("Game : undoVIPlayer");
		
		// the direction of the blind player has changed. So the player at the previous position has no cards.
		players[blindDirection.ordinal()].newHand();
		players[blindDirection.ordinal()].setBlind(false);
		
		if(Game.isTestMode()){
		
		 TestAntennaHandler.reverseScanBlind();
		}
	
	}
	
	/**
	 * Returns true if the blind player has no cards. Otherwise returns false.
	 * @return True if the blind player has no cards. Otherwise returns false.
	 */
	public boolean blindPayerHasNoCard(){
		
		return players[blindDirection.ordinal()].getHand().isEmpty() ;
	}
	
	/**
	 * Lets the user remove the most recent card added to the blind player's hand
	 */
	public void undoBlindPlayerCard(){
		
		Card toRemove = players[blindDirection.ordinal()].getHand().removeRecentCard() ;
		
		players[blindDirection.ordinal()].getHand().removeCard(toRemove);
	}
	
	/**
	 * Lets the user remove the most recent card added to the dummy player's hand
	 */
	public Card undoDummyPlayerCard(){
		
		Card toRemove = players[dummyDirection.ordinal()].getHand().removeRecentCard() ;
		
		players[dummyDirection.ordinal()].getHand().removeCard(toRemove);
		
		return toRemove ;
	}
	
	public void removeCurrentPlayerCardPlayed(){
		
		players[turn.ordinal()].getHand().removeRecentCard() ;
	}
	
	public void undoFirstCardPlayed(){
		
		assert (cardsPlayed.size() == 1) ;
		
		Iterator<Card> iter = cardsPlayed.iterator() ;
		
		while(iter.hasNext()){
			
			Card toRemove = iter.next() ;
			cardsPlayed.remove(toRemove) ;
						
		}
		
		
		
	}

	public Direction getTurn() {
		return turn;
	}

	public void reverseBidPosition(){
		
		gameState = GameState.DEALING ;
		turn = blindDirection ;
		
		players[blindDirection.ordinal()].newHand();
		switchHand(blindDirection);
		
		if(isTestMode()){
			
			TestAntennaHandler.reverseScanBlind();
		}
		
	}
	
	public boolean isGameState(GameState state){
		
		return this.gameState == state ;
	}
	
	

}