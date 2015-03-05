package gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Card;
import model.Contract;
import model.Direction;
import model.Game;
import model.GameListener;

public class GameStatusGUI extends JPanel implements GameListener {

	public static Font STATUS_FONT = new Font("Helvetica", Font.BOLD, 60);
	private PlayerStatusGUI[] playerGUIs = new PlayerStatusGUI[4];
	private BidStatusGUI bidGUI = new BidStatusGUI();

	// true if first card has been played already. Otherwise false.
	private boolean firstCardPalyed = false;

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

		playerGUIs[turn.ordinal()].cardPlayed(card);

		playerGUIs[turn.ordinal()].repaint();

		gameGUI.debugMsg("repainted first card");

		// the frame should be changed to ScanDummyGUI only after the first card
		// has been played
		if (!firstCardPalyed) {

			gameGUI.debugMsg("Switching to scan dummy screen");

			firstCardPalyed = true;

			// if blind player is NOT dummy player, then change frame to
			// SCAN_DUMMY_GUI
			if (!game.getBlindPosition().equals(game.getDummyPosition())) {

				gameGUI.changeFrame();

			}
		}

		playerGUIs[(turn.ordinal() + 1) % 4].nextPlayer();

		playerGUIs[(turn.ordinal() + 1) % 4].repaint();

	}

	@Override
	public void cardScanned(Card card) {
		// TODO Auto-generated method stub

	}

	@Override
	public void trickWon(Direction winner) {
		for (PlayerStatusGUI playerGUI : playerGUIs) {

			playerGUI.repaint();
		}

		for (PlayerStatusGUI playerGUI : playerGUIs) {

			playerGUI.trickOver();
		}

		playerGUIs[winner.ordinal()].nextPlayer();
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

	/**
	 * Adds direction labels to the PlayerStatusGUIs
	 * @param dir The direction of a PlayerStatusGUI
	 * @param gridX The gridx number for GridBagConstraints
	 * @param gridY The gridy number for GridBagConstraints
	 * @param anchor The anchor number for GridBagConstraints
	 */
	private void addDirLabels(Direction dir, int gridX, int gridY, int anchor) {

		GridBagConstraints dirLabelConstraint = new GridBagConstraints();
		dirLabelConstraint.gridx = gridX;
		dirLabelConstraint.gridy = gridY;
		dirLabelConstraint.anchor = anchor;

		add(new JLabel(dir.toString()), dirLabelConstraint);
	}

}
