package lerner.blindBridge.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener_sparse;
import lerner.blindBridge.model.PlayerHand;
import lerner.blindBridge.model.Suit;

/**
 * Panel to display the dummy hand.
 * @author blerner
 *
 */
public class DummyHandPanel extends BridgeJPanel implements GameListener_sparse {
	// Contains a subpanel for each suit
	private DummySuitPanel m_clubPanel = new DummySuitPanel(Suit.CLUBS);
	private DummySuitPanel m_diamondPanel = new DummySuitPanel(Suit.DIAMONDS);
	private DummySuitPanel m_heartPanel = new DummySuitPanel(Suit.HEARTS);
	private DummySuitPanel m_spadePanel = new DummySuitPanel(Suit.SPADES);
	
	private Map<Suit,DummySuitPanel>	m_dummySuitGUIs			= new HashMap<>();

	
	// Panel that contains the dummy hand panel
	private GameStatusGUI m_gameStatusGUI;
	
	private Game m_game;
	
	// Dummy's direction
	private Direction m_dummyDirection;
	private double m_rotation;
	
	public DummyHandPanel (GameStatusGUI gameStatusPanel) {
		m_dummySuitGUIs.put(Suit.CLUBS, m_clubPanel);
		m_dummySuitGUIs.put(Suit.DIAMONDS, m_diamondPanel);
		m_dummySuitGUIs.put(Suit.HEARTS, m_heartPanel);
		m_dummySuitGUIs.put(Suit.SPADES, m_spadePanel);
		m_gameStatusGUI = gameStatusPanel;
		setPreferredSize();
	}
	
	@Override
	public void initialize(GameGUI p_gameGUI, Game p_game) {
		m_game = p_game;
	}
	
	private void setPreferredSize ()
	{
		setPreferredSize(new Dimension(PlayerStatusGUI.CARD_HEIGHT, PlayerStatusGUI.CARD_HEIGHT));
		setMinimumSize(new Dimension(PlayerStatusGUI.CARD_HEIGHT, PlayerStatusGUI.CARD_HEIGHT));
	}


//	@Override
//	public void paintChildren ( Graphics g )
//	//public void paintComponent ( Graphics g )
//	{
//
//		Graphics2D g2d = (Graphics2D) g;
//		g2d.rotate(m_rotation);
//		System.out.println("Rotating " + m_rotation);
//		super.paintChildren(g2d);
//		//super.paintComponent(g2d);
//		System.out.println("Unrotating " + m_rotation);
//		g2d.rotate(-m_rotation);
//	}


	/**
	 * Adds a card to the display of the dummy hand
	 */
//	@Override
//	public void sig_cardScanned(Direction p_direction, Card p_card, boolean p_handComplete) {
//		// TODO Auto-generated method stub
//		GameListener_sparse.super.sig_cardScanned(p_direction, p_card, p_handComplete);
//		
//		DummySuitPanel suitGUI = m_dummySuitGUIs.get(p_card.getSuit());
//		suitGUI.repaint();
//		System.out.println ("Dummy scanned " + p_card);
//	}


	
	/**
	 * Remembers which hand is the dummy
	 */
	@Override
	public void sig_setDummyPosition(Direction p_direction) {
		// TODO Auto-generated method stub
		m_dummyDirection = p_direction;
		m_rotation = p_direction.getRotation() + Math.PI;
		
		switch (m_dummyDirection) {
		case NORTH:
		case SOUTH:
			setLayout (new GridLayout (1, 4));
			break;
		case EAST:
		case WEST:
			setLayout (new GridLayout (4, 1));
			break;
		}
		
		switch (m_dummyDirection) {
		case NORTH:
		case EAST:
			add (m_clubPanel);
			add (m_diamondPanel);
			add (m_heartPanel);
			add (m_spadePanel);
			break;
		case SOUTH:
		case WEST:
			add (m_spadePanel);
			add (m_heartPanel);
			add (m_diamondPanel);
			add (m_clubPanel);
			break;
		}
		
		setBorder(BorderFactory.createLineBorder(Color.BLUE));

		m_gameStatusGUI.addDummyPanel (p_direction);
	}

	/**
	 * Removes a card from the displayed dummy hand
	 */
	@Override
	public void sig_cardPlayed(Direction p_turn, Card p_card) {
		// TODO Auto-generated method stub
		GameListener_sparse.super.sig_cardPlayed(p_turn, p_card);

		DummySuitPanel suitGUI = m_dummySuitGUIs.get(p_card.getSuit());
		suitGUI.repaint();
	}
	
	/**
	 * Panel to hold all the cards in one suit in the dummy's hand.
	 * @author blerner
	 *
	 */
	private class DummySuitPanel extends JPanel {
		//private JLabel m_suitName;
		private Suit m_suitName;
		
		public DummySuitPanel (Suit suit) {
			//m_suitName = new JLabel (suit);
			//add (m_suitName);
			m_suitName = suit;
			setBorder(BorderFactory.createLineBorder(Color.RED));
		}
		
		public void paintComponent (Graphics g) {
			// Go back to rotating the DummyHandPanel instead, maybe in paintChildren
			System.out.println ("Painting " + m_suitName);
			Graphics2D g2d = (Graphics2D) g;
			System.out.println("Rotating " + m_rotation);
			g2d.rotate(m_rotation);
			super.paintComponent(g);
			int x = 0;
			int y = 0;
			switch (m_dummyDirection) {
			case NORTH:
				x = 10;
				y = 20;
				break;
			case SOUTH:
				x = -getWidth() + 10; 
				y = -getHeight() + 20;
				break;
			case WEST:
				x = -getHeight() + 10;
				y = 20;
				break;
			case EAST:
				x = 10;
				y = -getWidth() + 20;  // -222
			}
			System.out.println ("x = " + x + "   y = " + y);
			g2d.drawString(m_suitName.toString(), x, y);
			PlayerHand hand = m_game.getBridgeHand().getDummyHand();
			
			if (hand != null) {
				TreeSet<Card> cards = hand.getSuitCards().get(m_suitName);
				for (Card c : cards.descendingSet()) {
					y = y + 15;
					g2d.drawString(c.getRank().toString(), x, y);
				}
			}
			System.out.println("Unrotating " + m_rotation);
			g2d.rotate(-m_rotation);
		}
	}

	

}
