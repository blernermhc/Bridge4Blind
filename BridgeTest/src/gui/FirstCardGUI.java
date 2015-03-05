package gui;

import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Card;
import model.Contract;
import model.Direction;
import model.Game;
import model.GameListener;

public class FirstCardGUI extends JPanel implements GameListener {
	private GameGUI gameGUI;
	private boolean alreadyShown = false;
	
	private Game game ;
	
	public FirstCardGUI(GameGUI gameGUI, Game game) {
		this.gameGUI = gameGUI;
		this.game = game ;
		this.game.addListener(this);
		JLabel instructions = new JLabel("Please play the first card.");
		instructions.setFont(GameGUI.INFO_FONT);
		add (instructions);
		
	}

	@Override
	public void debugMsg(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gameReset() {
		alreadyShown = false;
	}

	@Override
	public void cardPlayed(Direction turn, Card card) {
		if (!alreadyShown) {
			
			gameGUI.changeFrame();
		}
		alreadyShown = true;
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
	}

	@Override
	public void dummyHandScanned() {
		// TODO Auto-generated method stub
		
	}

}
