package gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Card;
import model.Contract;
import model.Direction;
import model.Game;
import model.GameListener;

/**
 * This class visually represents what the visually impaired person hears.
 * 
 * @author Barbara Lerner, Humaira Orchee
 * @version March 12, 2015
 */
public class GameStatusGUI extends JPanel implements GameListener {

	public static Font STATUS_FONT = new Font("Helvetica", Font.BOLD, 60);
	private PlayerStatusGUI[] playerGUIs = new PlayerStatusGUI[4];
	private BidStatusGUI bidGUI = new BidStatusGUI();

	// keeps track of the current player
	private int currentPlayer = 0;

	// true if first card has been played already. Otherwise false. It starts
	// out with false because initially, first card has not been played. In
	// cardPlayed, after first card for each hand is played, it is set to true.
	// This needs to be reset to false each time a new hand is started.
	private boolean firstCardPlayed = false;

	private Game game;

	private GameGUI gameGUI;

	public GameStatusGUI(GameGUI gameGUI, Game game) {

		this.game = game;
		this.gameGUI = gameGUI;

		this.game.addListener(this);

		setLayout(new GridBagLayout());

		GridBagConstraints northConstraints = new GridBagConstraints();
		northConstraints.gridx = 1;
		// northConstraints.gridy = 0;
		northConstraints.gridy = 1;
		northConstraints.anchor = GridBagConstraints.NORTH;
		// northConstraints.weightx = 1;
		northConstraints.weighty = 1;
		playerGUIs[Direction.NORTH.ordinal()] = new PlayerStatusGUI(
				Direction.NORTH);
		add(playerGUIs[Direction.NORTH.ordinal()], northConstraints);
		addDirLabels(Direction.NORTH, 1, 0, GridBagConstraints.PAGE_START);

		GridBagConstraints eastConstraints = new GridBagConstraints();
		eastConstraints.gridx = 2;
		eastConstraints.gridy = 2;
		eastConstraints.anchor = GridBagConstraints.LINE_START;
		eastConstraints.weightx = 1;
		playerGUIs[Direction.EAST.ordinal()] = new PlayerStatusGUI(
				Direction.EAST);
		add(playerGUIs[Direction.EAST.ordinal()], eastConstraints);
		addDirLabels(Direction.EAST, 2, 2, GridBagConstraints.LINE_END);

		GridBagConstraints southConstraints = new GridBagConstraints();
		southConstraints.gridx = 1;
		// southConstraints.gridy = 2;
		southConstraints.gridy = 3;
		southConstraints.anchor = GridBagConstraints.SOUTH;
		// southConstraints.weightx = 1;
		southConstraints.weighty = 1;
		playerGUIs[Direction.SOUTH.ordinal()] = new PlayerStatusGUI(
				Direction.SOUTH);
		add(playerGUIs[Direction.SOUTH.ordinal()], southConstraints);
		addDirLabels(Direction.SOUTH, 1, 4, GridBagConstraints.PAGE_END);

		GridBagConstraints westConstraints = new GridBagConstraints();
		westConstraints.gridx = 0;
		westConstraints.gridy = 2;
		westConstraints.anchor = GridBagConstraints.LINE_END;
		westConstraints.weightx = 1;
		playerGUIs[Direction.WEST.ordinal()] = new PlayerStatusGUI(
				Direction.WEST);
		add(playerGUIs[Direction.WEST.ordinal()], westConstraints);
		addDirLabels(Direction.WEST, 0, 2, GridBagConstraints.LINE_START);

		GridBagConstraints bidConstraints = new GridBagConstraints();
		bidConstraints.gridx = 1;
		bidConstraints.gridy = 2;
		bidConstraints.fill = GridBagConstraints.BOTH;
		add(bidGUI, bidConstraints);
	}

	@Override
	public void debugMsg(String string) {
		// TODO Auto-generated method stub

	}

	@Override
	public void gameReset() {
		bidGUI.clear();
	}

	@Override
	public void cardPlayed(Direction turn, Card card) {

		// every time a card is played, current player is updated.
		currentPlayer++;

		System.out.println("game status gui card played: " + turn);

		playerGUIs[turn.ordinal()].cardPlayed(card);

		// if the current player is the last person to play for the trick, then
		// next player should not be determined here. Next player should be
		// determined by trickWon()
		if ((currentPlayer % 4) != 0) {

			playerGUIs[(turn.ordinal() + 1) % 4].nextPlayer();
		}

		// the frame should be changed to ScanDummyGUI only after the first card
		// has been played
		if (!firstCardPlayed) {

			// gameGUI.debugMsg("Switching to scan dummy screen");

			firstCardPlayed = true;

			// if blind player is NOT dummy player, then change frame to
			// SCAN_DUMMY_GUI

			debugMsg("Blind position " + game.getBlindPosition());

			debugMsg("Dummy Position " + game.getDummyPosition());

			if (!game.getBlindPosition().equals(game.getDummyPosition())) {

				// Do the task - changeFrame - after 2 seconds has passed.
				// without this, the screen changes to ScanDummyGUI too fast and
				// the first card is not shown
				TimerTask timertask = new TimerTask() {

					@Override
					public void run() {

						gameGUI.setSwitchFromGameStatusGUI(GameGUI.SWITCH_TO_DUMMY);

						gameGUI.changeFrame();

					}
				};

				Timer timer = new Timer(true);
				timer.schedule(timertask, 2000);

			}
		}
	}

	@Override
	public void cardScanned(Card card) {
		// TODO Auto-generated method stub

	}

	@Override
	public void trickWon(final Direction winner) {

		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {

				for (PlayerStatusGUI playerGUI : playerGUIs) {

					// problems here
					playerGUI.trickOver();

					playerGUIs[winner.ordinal()].nextPlayer();
				}

			}
		};

		Timer timer = new Timer(true);
		timer.schedule(timerTask, 4000);

	}

	@Override
	public void contractSet(Contract contract) {
		bidGUI.setBid(contract);
	}

	@Override
	public void blindHandScanned() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dummyHandScanned() {
		// TODO Auto-generated method stub

	}
	
	

	public void setFirstCardPlayed(boolean firstCardPlayed) {
		this.firstCardPlayed = firstCardPlayed;
	}

	/**
	 * Adds direction labels to the PlayerStatusGUIs
	 * 
	 * @param dir
	 *            The direction of a PlayerStatusGUI
	 * @param gridX
	 *            The gridx number for GridBagConstraints
	 * @param gridY
	 *            The gridy number for GridBagConstraints
	 * @param anchor
	 *            The anchor number for GridBagConstraints
	 */
	private void addDirLabels(Direction dir, int gridX, int gridY, int anchor) {

		GridBagConstraints dirLabelConstraint = new GridBagConstraints();
		dirLabelConstraint.gridx = gridX;
		dirLabelConstraint.gridy = gridY;
		dirLabelConstraint.anchor = anchor;

		add(new JLabel(dir.toString()), dirLabelConstraint);
	}
}
