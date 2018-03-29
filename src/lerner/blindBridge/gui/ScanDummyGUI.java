package lerner.blindBridge.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Contract;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener_sparse;
import lerner.blindBridge.model.Rank;
import lerner.blindBridge.model.Suit;
import lerner.blindBridge.stateMachine.BridgeHandState;

/**
 * The GUI that is displayed while the dummy's cards are being scanned in.
 * 
 * It listens to the game. When the dummy hand is completely scanned in, it advances to the next
 * screen.
 *
 * @version March 12, 2015
 */
public class ScanDummyGUI extends JPanel implements GameListener_sparse
{

	private GameGUI	m_gameGUI;

	private Game		m_game;

	// private Player	m_dummy;		// did not appear to be used

	private JLabel	m_clubsScanned		= new JLabel("Clubs: ");

	private JLabel	m_diamondsScanned	= new JLabel("Diamonds: ");

	private JLabel	m_heartsScanned		= new JLabel("Hearts: ");

	private JLabel	m_spadesScanned		= new JLabel("Spades: ");

	/**
	 * Creates the GUI
	 * 
	 * @param m_gameGUI
	 *            the main GUI window
	 * @param m_game
	 *            the game being played
	 */
	public ScanDummyGUI ( GameGUI p_gameGUI, Game p_game )
	{
		m_gameGUI = p_gameGUI;
		m_game = p_game;
		m_game.addGameListener(this);

		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		// add(GUIUtilities.createTitleLabel("Please scan dummy cards"));
		infoPanel.add(Box.createRigidArea(new Dimension(0, 50)));
		JLabel title = new JLabel("Please scan dummy cards");
		title.setFont(GameStatusGUI.STATUS_FONT);
		infoPanel.add(title);
		m_clubsScanned.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(Box.createRigidArea(new Dimension(0, 50)));
		m_clubsScanned.setFont(GameStatusGUI.STATUS_FONT);
		m_clubsScanned.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoPanel.add(m_clubsScanned);
		m_diamondsScanned.setFont(GameStatusGUI.STATUS_FONT);
		infoPanel.add(m_diamondsScanned);
		m_heartsScanned.setFont(GameStatusGUI.STATUS_FONT);
		infoPanel.add(m_heartsScanned);
		m_spadesScanned.setFont(GameStatusGUI.STATUS_FONT);
		infoPanel.add(m_spadesScanned);

		add(infoPanel);
	}

	@Override
	public void sig_gameReset ()
	{

		m_clubsScanned.setText("Clubs: ");
		m_diamondsScanned.setText("Diamonds: ");
		m_heartsScanned.setText("Hearts: ");
		m_spadesScanned.setText("Spades: ");
		repaint();
	}

	@Override
	public void sig_cardScanned ( Direction p_direction, Card p_card, boolean p_handComplete )
	{
		if (m_game.getStateController().getCurrentState() == BridgeHandState.SCAN_DUMMY)
		{
			switch (p_card.getSuit())
			{
				case CLUBS:
					updateCardsScanned(p_card, m_clubsScanned);
					break;
				case DIAMONDS:
					updateCardsScanned(p_card, m_diamondsScanned);
					break;
				case HEARTS:
					updateCardsScanned(p_card, m_heartsScanned);
					break;
				case SPADES:
					updateCardsScanned(p_card, m_spadesScanned);
					break;
				case NOTRUMP:
					break;
			}
		}
	}

	private void updateCardsScanned ( Card card, JLabel suitCards )
	{
		suitCards.setText(suitCards.getText() + "  " + card.getRank());
	}

	@Override
	public void sig_contractSet ( Contract contract )
	{
		// m_dummy = m_game.getBridgeHand().getDummyPosition();
	}

	@Override
	public void sig_blindHandsScanned ()
	{
	}

	/**
	 * Advances to the next GUI frame
	 */
	@Override
	public void sig_dummyHandScanned ()
	{

		// wait 2 seconds before switching screen so that the last dummy card is
		// visible
		TimerTask timertask = new TimerTask()
		{

			@Override
			public void run ()
			{

				m_gameGUI.changeFrame();

			}
		};

		Timer timer = new Timer(true);
		timer.schedule(timertask, 2000);

	}

	public void undo ( Card toRemove )
	{

		System.out.println("Scan Dummy GUI umdo ");

		Suit suit = toRemove.getSuit();

		Rank rank = toRemove.getRank();

		System.out.println("rank is -" + rank + "-");

		String ranks = "";
		JLabel label = null;

		switch (suit)
		{

			case CLUBS:
				ranks = m_clubsScanned.getText();
				label = m_clubsScanned;
				break;

			case DIAMONDS:
				ranks = m_diamondsScanned.getText();
				label = m_diamondsScanned;
				break;

			case HEARTS:
				ranks = m_heartsScanned.getText();
				label = m_heartsScanned;
				break;

			case SPADES:
				ranks = m_spadesScanned.getText();
				label = m_spadesScanned;
				break;

			default:
				System.err.println("There should not be a fifth suit");
				return;
		}

		// getting rid of that rank

		// separate text before and after the ":"
		int colonIndex = ranks.indexOf(":");

		String beforeColon = ranks.substring(0, colonIndex + 1);

		String afterColon = ranks.substring(colonIndex + 1);

		// split into array to find the appropriate rank
		String[] rankArray = afterColon.split(" ");

		String newAfterColon = "";

		for (int i = 0; i < rankArray.length; i++)
		{

			// dont want to add the rank to be removed
			if (!rankArray[i].equals(rank.toString()))
			{

				newAfterColon += " " + rankArray[i];
			}
		}

		label.setText(beforeColon + newAfterColon);

		System.out.println("should say " + beforeColon + newAfterColon);

		repaint();

	}

	@Override
	public void paintComponent ( Graphics g )
	{

		super.paintComponent(g);

		m_gameGUI.backButtonSetEnabled(false);

		boolean noClubs = m_clubsScanned.getText().trim()
				.charAt(m_clubsScanned.getText().trim().length() - 1) == ':';
		boolean noDiamonds = m_diamondsScanned.getText().trim()
				.charAt(m_diamondsScanned.getText().trim().length() - 1) == ':';
		boolean noHearts = m_heartsScanned.getText().trim()
				.charAt(m_heartsScanned.getText().trim().length() - 1) == ':';
		boolean noSpades = m_spadesScanned.getText().trim()
				.charAt(m_spadesScanned.getText().trim().length() - 1) == ':';

		if (noClubs && noDiamonds && noHearts && noSpades)
		{

			m_gameGUI.undoButtonSetEnabled(false);

		}
		else
		{

			m_gameGUI.undoButtonSetEnabled(true);

		}

		m_gameGUI.repaint();
	}
}
