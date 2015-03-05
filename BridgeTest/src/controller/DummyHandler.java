package controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.DebugGraphics;
import javax.swing.JPanel;

import test.TestCards;
import model.Card;
import model.CardDatabase;
import model.Direction;

public class DummyHandler implements KeyListener {

	private static final int NORTH = 0;

	private static final int EAST = 1;

	private static final int SOUTH = 2;

	private static final int WEST = 3;

	// should equal 78
	private static final int PLAYER_INDEX_SIZE = 13 + 52 + 13;

	private CardDatabase cards;

	private CardListener[] hands = new CardListener[Direction.values().length];

	private CardListener iDListen;

	protected boolean cardRequestSent = false;

	// the GUIs for which the handler is supposed to be reading cards
	private boolean rightGUI = false;

	private final TestCards testCards = new TestCards();

	// east gets the first card
	private int position = 0;

	// the order in which player hand is scanned
	private int[] playerIndex = { EAST, EAST, EAST, EAST, EAST, EAST, EAST,
			EAST, EAST, EAST, EAST, EAST, EAST, WEST, NORTH, NORTH, NORTH,
			NORTH, NORTH, NORTH, NORTH, NORTH, NORTH, NORTH, NORTH, NORTH,
			NORTH, NORTH, EAST, SOUTH, WEST, NORTH, EAST, SOUTH, SOUTH, WEST,
			NORTH, EAST, SOUTH, WEST, NORTH, EAST, SOUTH, WEST, NORTH, EAST,
			SOUTH, WEST, NORTH, EAST, SOUTH, WEST, NORTH, EAST, NORTH, EAST,
			SOUTH, WEST, SOUTH, WEST, NORTH, EAST, NORTH, EAST, SOUTH, WEST,
			NORTH, EAST, SOUTH, WEST, EAST, SOUTH, WEST, NORTH, WEST, NORTH,
			EAST, SOUTH };

	// Id of the current player antenna
	private String turnId;

	public DummyHandler(CardDatabase cardDatabase) {

		super();

		cards = cardDatabase;

		assert playerIndex.length == PLAYER_INDEX_SIZE;
	}

	public void connect() {

		System.out.println("Dummy Connection has been made");

	}

	public void addHandListener(CardListener listener, Direction direction) {

		hands[direction.ordinal()] = listener;

	}

	public void addIdListener(CardIdentifier listener) {

		iDListen = listener;

	}

	public void switchHand(Direction turn) {

		System.out.println("Switching hand to " + turn + " ***");

		turnId = getDirectionCode(turn);

		switchHand(turnId);

	}

	/**
	 * Send a command to the hardware to server to switch which antenna it is
	 * listening to
	 * 
	 * @param handID
	 *            the antenna to listen to
	 * @throws IOException
	 *             the connection failed
	 * @throws InterruptedException
	 *             the thread was interrupted while waiting for a card request
	 *             command to happen
	 */
	protected void switchHand(String handID) {

		cardRequestSent = false;

	}

	/**
	 * Converts from a direction to the command to send to the hardware server
	 * 
	 * @param turn
	 *            the antenna to switch to
	 * @return the command to send
	 */
	private String getDirectionCode(final Direction turn) {
		switch (turn) {
		case WEST:
			return "W";
		case NORTH:
			return "N";
		case EAST:
			return "E";
		default:
			return "S";
		}
	}

	public void quitServer() {

		System.out.println("Dummy Server quit");

	}

	@Override
	public void keyPressed(KeyEvent e) {

		// no need to do anything if its not the right gui
		if (!rightGUI) {

			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_SPACE) {

			cardRequestSent = true;

			// // Card nextCard = testCards.getNextCard();
			//
			// // blind player cards (east)
			// if (position < 13) {
			// scanBlindCards();
			//
			// } else if (position > 13 && position < 27) {// dummy player cards
			// // (north)
			// scanDummyCards();
			//
			// } else { // for all other purposes

			Card nextCard = testCards.getNextCard();

			hands[playerIndex[position]].cardFound(nextCard);

			position++;

			assert position < PLAYER_INDEX_SIZE;
			// }
		}
	}

	private void scanDummyCards() {
		while (position < 27) {

			Card nextCard = testCards.getNextCard();

			hands[playerIndex[position]].cardFound(nextCard);

			position++;

			assert position < PLAYER_INDEX_SIZE;

		}
	}

	private void scanBlindCards() {
		while (position < 13) {

			Card nextCard = testCards.getNextCard();

			hands[playerIndex[position]].cardFound(nextCard);

			position++;

			assert position < PLAYER_INDEX_SIZE;

		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public void setRightGUI(boolean rightGUI) {
				
		this.rightGUI = rightGUI;
	}


	
	

}
