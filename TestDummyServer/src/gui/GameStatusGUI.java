package gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

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

	public GameStatusGUI (Game game) {
		game.addListener(this);
		
		setLayout(new GridBagLayout());
		GridBagConstraints northConstraints = new GridBagConstraints();
		northConstraints.gridx = 1;
		northConstraints.gridy = 0;
		northConstraints.anchor = GridBagConstraints.PAGE_START;
		//northConstraints.weightx = 1;
		northConstraints.weighty = 1;
		playerGUIs[Direction.NORTH.ordinal()] = new PlayerStatusGUI(Direction.NORTH);
		add(playerGUIs[Direction.NORTH.ordinal()], northConstraints);

		GridBagConstraints eastConstraints = new GridBagConstraints();
		eastConstraints.gridx = 2;
		eastConstraints.gridy = 1;
		eastConstraints.anchor = GridBagConstraints.LINE_START;
		eastConstraints.weightx = 1;
		playerGUIs[Direction.EAST.ordinal()] = new PlayerStatusGUI(Direction.EAST);
		add(playerGUIs[Direction.EAST.ordinal()], eastConstraints);

		GridBagConstraints southConstraints = new GridBagConstraints();
		southConstraints.gridx = 1;
		southConstraints.gridy = 2;
		southConstraints.anchor = GridBagConstraints.PAGE_END;
		//southConstraints.weightx = 1;
		southConstraints.weighty = 1;
		playerGUIs[Direction.SOUTH.ordinal()] = new PlayerStatusGUI(Direction.SOUTH);
		add(playerGUIs[Direction.SOUTH.ordinal()], southConstraints);

		GridBagConstraints westConstraints = new GridBagConstraints();
		westConstraints.gridx = 0;
		westConstraints.gridy = 1;
		westConstraints.anchor = GridBagConstraints.LINE_END;
		westConstraints.weightx = 1;
		playerGUIs[Direction.WEST.ordinal()] = new PlayerStatusGUI(Direction.WEST);
		add(playerGUIs[Direction.WEST.ordinal()], westConstraints);
		
		GridBagConstraints bidConstraints = new GridBagConstraints();
		bidConstraints.gridx = 1;
		bidConstraints.gridy = 1;
		bidConstraints.fill = GridBagConstraints.BOTH;
		add (bidGUI, bidConstraints);
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
		playerGUIs[(turn.ordinal()+1)%4].nextPlayer();
	}

	@Override
	public void cardScanned(Card card) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void trickWon(Direction winner) {
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
}
