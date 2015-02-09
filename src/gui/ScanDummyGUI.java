package gui;

import javax.swing.JPanel;

import model.Card;
import model.Contract;
import model.Direction;
import model.Game;
import model.GameListener;

/**
 * The GUI that is displayed while the dummy's cards are being scanned in.
 * 
 * It listens to the game.  When the dummy hand is completely scanned in,
 * it advances to the next screen.
 *
 */
public class ScanDummyGUI extends JPanel implements GameListener {
	private GameGUI gameGUI;

	/**
	 * Creates the GUI
	 * @param gameGUI the main GUI window
	 * @param game the game being played
	 */
	public ScanDummyGUI(GameGUI gameGUI, Game game) {
		this.gameGUI = gameGUI;
		game.addListener(this);
		add(GUIUtilities.createTitleLabel("Please scan dummy cards"));
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void trickWon(Direction winner) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contractSet(Contract contract) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void blindHandScanned() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Advances to the next GUI frame
	 */
	@Override
	public void dummyHandScanned() {
		gameGUI.changeFrame();
	}

	@Override
	public void cardAddedToHand(Direction dir, Card c) {
		//Do Nothing.
		
	}


}
