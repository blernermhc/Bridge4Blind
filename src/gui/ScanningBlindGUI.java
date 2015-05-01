package gui;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Card;
import model.Contract;
import model.Direction;
import model.Game;
import model.GameListener;

public class ScanningBlindGUI extends JPanel implements GameListener {
	private GameGUI gameGUI;
	
	public ScanningBlindGUI(GameGUI gameGUI, Game game) {
		this.gameGUI = gameGUI;
		game.addListener(this);
		
		setLayout (new BoxLayout(this, BoxLayout.Y_AXIS));
		addInstructions("Please scan in the blind player's cards.");
		addInstructions("Use UNDO button to change position of blind player.");
	}

	private void addInstructions(String text) {
		JLabel instructions = new JLabel(text);
		instructions.setFont(GameGUI.INFO_FONT);
		instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
		add (instructions);
	}

	@Override
	public void debugMsg(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameReset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cardPlayed(Direction turn, Card card) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cardScanned(Card card) {
	}

	@Override
	public void trickWon(Direction winner) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contractSet(Contract contract) {
		// TODO Auto-generated method stub
		
	}
	
	public void blindHandScanned() {
		gameGUI.changeFrame();
	}

	@Override
	public void dummyHandScanned() {
		// TODO Auto-generated method stub
		
	}

}
