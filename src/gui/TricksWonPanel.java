package gui;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Contract;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener;

public class TricksWonPanel extends JPanel implements GameListener {
	private static final Font TRICK_FONT = GameStatusGUI.STATUS_FONT.deriveFont(24f);
	private int eastWestTricks = 0;
	private int northSouthTricks = 0;
	
	private JLabel eastWestTrickLabel = new JLabel("0");
	private JLabel northSouthTrickLabel = new JLabel("0");
	
	public TricksWonPanel () {
		JPanel eastWestPanel = new JPanel();
		JLabel ewLabel = new JLabel("East-West tricks:  ");
		ewLabel.setFont(TRICK_FONT);
		eastWestPanel.add(ewLabel);
		eastWestPanel.add(eastWestTrickLabel);
		eastWestTrickLabel.setFont(TRICK_FONT);
		
		JPanel northSouthPanel = new JPanel();
		JLabel nsLabel = new JLabel("North-South tricks:  ");
		nsLabel.setFont(TRICK_FONT);
		northSouthPanel.add(nsLabel);
		northSouthPanel.add(northSouthTrickLabel);
		northSouthTrickLabel.setFont(TRICK_FONT);
		
		setLayout (new BoxLayout (this, BoxLayout.Y_AXIS));
		add(northSouthPanel);
		add(eastWestPanel);
	}
	
	@Override
	public void sig_debugMsg(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sig_gameReset() {
		eastWestTricks = 0;
		northSouthTricks = 0;
		eastWestTrickLabel.setText("0");
		northSouthTrickLabel.setText("0");
	}

	@Override
	public void sig_cardPlayed(Direction turn, Card card) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sig_cardScanned(Direction p_direction, Card p_card, boolean p_handComplete) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sig_trickWon(Direction winner) {
		if (winner == Direction.EAST || winner == Direction.WEST) {
			eastWestTricks++;
			eastWestTrickLabel.setText("" + eastWestTricks);
		}
		else {
			northSouthTricks++;
			northSouthTrickLabel.setText("" + northSouthTricks);
		}
	}

	@Override
	public void sig_contractSet(Contract contract) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sig_blindHandsScanned() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sig_dummyHandScanned() {
		// TODO Auto-generated method stub
		
	}

	public static void main (String[] args) {
		JFrame f = new JFrame();
		f.add(new TricksWonPanel(), BorderLayout.CENTER);
		f.pack();
		f.setVisible(true);
	}
}
