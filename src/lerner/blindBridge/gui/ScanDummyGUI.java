package lerner.blindBridge.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Category;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener_sparse;
import lerner.blindBridge.model.PlayerHand;
import lerner.blindBridge.model.Suit;

/**
 * The GUI that is displayed while the dummy's cards are being scanned in.
 * 
 * It listens to the game. When the dummy hand is completely scanned in, it advances to the next
 * screen.
 *
 * @version March 12, 2015
 */
public class ScanDummyGUI extends BridgeJPanel implements GameListener_sparse
{
	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(ScanDummyGUI.class.getName());


	private GameGUI	m_gameGUI;

	private Game		m_game;

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
	public ScanDummyGUI ( )
	{
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

	/* (non-Javadoc)
	 * @see lerner.blindBridge.gui.BridgeJPanel#initialize(lerner.blindBridge.gui.GameGUI, lerner.blindBridge.main.Game)
	 */
	public void initialize ( GameGUI p_gameGUI, Game p_game )
	{
		m_game = p_game;
		m_gameGUI = p_gameGUI;
		
		m_game.addGameListener(this);
	}
	
	//--------------------------------------------------
	// Game Event Signal Handlers
	//--------------------------------------------------

	/***********************************************************************
	 * Indicates that the system is now waiting for the dummy to scan their hand.
	 * NOTE: this is invoked after undo, so display the current dummy hand.
	 ***********************************************************************/
	@Override
	public void sig_scanDummyHand ()
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_scanDummyHand: entered");
		displayHand();
	}

	/***********************************************************************
	 * Indicates that the card in the dummy's hand has been scanned.
	 ***********************************************************************/
	@Override
	public void sig_cardScanned ( Direction p_direction, Card p_card, boolean p_handComplete )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_cardScanned: entered");

		displayHand();
	}

	/***********************************************************************
	 * Removes or restores a scanned card.
	 ***********************************************************************/
	@Override
	public void sig_cardScanned_undo (	boolean p_redoFlag,
										boolean p_confirmed,
										Direction p_direction,
										Card p_card,
										boolean p_handComplete )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_cardScanned_undo: entered");

		if (! p_confirmed) return;
		
		displayHand();
	}

	/***********************************************************************
	 * Displays the dummy hand, as known by the Bridge Hand object.
	 ***********************************************************************/
	public void displayHand ()
	{
		PlayerHand hand = m_game.getBridgeHand().getHands().get(m_game.getBridgeHand().getDummyPosition());
		
		for (Suit suit : Suit.values())
		{
			updateCardsScanned(suit, (hand == null ? null : hand.getSuitCards().get(suit)));
		}
		repaint();
	}

	/***********************************************************************
	 * Updates the display with the modified list of cards for a suit
	 * @param p_suit		the suit being modified
	 * @param p_cards	an ordered set of cards in that suit that have been scanned so far
	 ***********************************************************************/
	private void updateCardsScanned ( Suit p_suit, TreeSet<Card> p_cards )
	{
		JLabel suitLabel;
		String prefix;
		switch (p_suit)
		{
			case CLUBS:
				suitLabel = m_clubsScanned;
				prefix = "Clubs:";
				break;
			case DIAMONDS:
				suitLabel = m_diamondsScanned;
				prefix = "Diamonds:";
				break;
			case HEARTS:
				suitLabel = m_heartsScanned;
				prefix = "Hearts:";
				break;
			case SPADES:
				suitLabel = m_spadesScanned;
				prefix = "Spades:";
				break;
			default:
				return;
		}
		
		StringBuilder text = new StringBuilder();
		text.append(prefix);
		if (p_cards != null)
		{
			for (Card card : p_cards.descendingSet())
			{
				text.append(' ');
				text.append(card.getRank().toString());
			}
		}
		suitLabel.setText(text.toString());
	}


	@Override
	public void paintComponent ( Graphics g )
	{
		super.paintComponent(g);
		m_gameGUI.repaint();
	}
}
