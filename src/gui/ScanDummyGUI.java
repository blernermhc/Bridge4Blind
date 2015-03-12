package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Card;
import model.Contract;
import model.Direction;
import model.Game;
import model.GameListener;
import model.Player;

/**
 * The GUI that is displayed while the dummy's cards are being scanned in.
 * 
 * It listens to the game.  When the dummy hand is completely scanned in,
 * it advances to the next screen.
 *
 *@version March 12, 2015
 */
public class ScanDummyGUI extends JPanel implements GameListener {
	private GameGUI gameGUI;
	private Game game;
	private Player dummy;
	
	private JLabel clubsScanned = new JLabel ("Clubs:  ");
	private JLabel diamondsScanned = new JLabel ("Diamonds:  ");
	private JLabel heartsScanned = new JLabel ("Hearts:  ");
	private JLabel spadesScanned = new JLabel ("Spades:  ");

	/**
	 * Creates the GUI
	 * @param gameGUI the main GUI window
	 * @param game the game being played
	 */
	public ScanDummyGUI(GameGUI gameGUI, Game game) {
		this.gameGUI = gameGUI;
		this.game = game;
		game.addListener(this);
		
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout (new BoxLayout (infoPanel, BoxLayout.Y_AXIS));
		//add(GUIUtilities.createTitleLabel("Please scan dummy cards"));
		infoPanel.add(Box.createRigidArea(new Dimension(0, 50)));
		JLabel title = new JLabel("Please scan dummy cards");
		title.setFont(GameStatusGUI.STATUS_FONT);
		infoPanel.add(title);
		clubsScanned.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(Box.createRigidArea(new Dimension(0, 50)));
		clubsScanned.setFont(GameStatusGUI.STATUS_FONT);
		clubsScanned.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(clubsScanned);
		diamondsScanned.setFont(GameStatusGUI.STATUS_FONT);
		infoPanel.add(diamondsScanned);
		heartsScanned.setFont(GameStatusGUI.STATUS_FONT);
		infoPanel.add(heartsScanned);
		spadesScanned.setFont(GameStatusGUI.STATUS_FONT);
		infoPanel.add(spadesScanned);		
		
		add(infoPanel);
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
		if (game.isScanningDummy()) {
			switch (card.getSuit()) {
			case CLUBS: 
				updateCardsScanned(card, clubsScanned);
				break;
			case DIAMONDS:
				updateCardsScanned(card, diamondsScanned);
				break;
			case HEARTS:
				updateCardsScanned(card, heartsScanned);
				break;
			case SPADES:
				updateCardsScanned(card, spadesScanned);
				break;
			}
		}
	}

	private void updateCardsScanned(Card card, JLabel suitCards) {
		suitCards.setText(suitCards.getText() + "  " + card.getRank());
	}

	@Override
	public void trickWon(Direction winner) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contractSet(Contract contract) {
		dummy = game.getDummyPlayer();
	}

	@Override
	public void blindHandScanned() {
	}

	/**
	 * Advances to the next GUI frame
	 */
	@Override
	public void dummyHandScanned() {

		// wait 2 seconds before switching screen so that the last dummy card is visible
		TimerTask timertask = new TimerTask() {
			
			@Override
			public void run() {

				gameGUI.changeFrame();
				
			}
		};
				
		Timer timer = new Timer(true) ;
		timer.schedule(timertask, 2000);
		

	}


}
