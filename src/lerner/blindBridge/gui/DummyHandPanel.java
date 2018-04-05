package lerner.blindBridge.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
	// Font used to draw the dummy cards
	private static final Font	DUMMY_FONT				= GameStatusGUI.STATUS_FONT.deriveFont(30f);

	
	// Contains a subpanel for each suit
	private DummySuitPanel m_clubPanel = new DummySuitPanel(Suit.CLUBS);
	private DummySuitPanel m_diamondPanel = new DummySuitPanel(Suit.DIAMONDS);
	private DummySuitPanel m_heartPanel = new DummySuitPanel(Suit.HEARTS);
	private DummySuitPanel m_spadePanel = new DummySuitPanel(Suit.SPADES);
	
	// Map from suit to its panel
	private Map<Suit,DummySuitPanel>	m_dummySuitGUIs			= new HashMap<>();

	
	// Panel that contains the dummy hand panel
	private GameStatusGUI m_gameStatusGUI;
	
	// The game being played
	private Game m_game;
	
	// Dummy's direction
	private Direction m_dummyDirection;
	
	// The amount that the dummy panel is rotated for the current dummy's direction.
	private double m_rotation;
	
	/**
	 * Creates the dummy panel containing a subpanel for each suit.
	 * @param gameStatusPanel the panel displaying the current trick
	 */
	public DummyHandPanel (GameStatusGUI gameStatusPanel) {
		m_dummySuitGUIs.put(Suit.CLUBS, m_clubPanel);
		m_dummySuitGUIs.put(Suit.DIAMONDS, m_diamondPanel);
		m_dummySuitGUIs.put(Suit.HEARTS, m_heartPanel);
		m_dummySuitGUIs.put(Suit.SPADES, m_spadePanel);
		m_gameStatusGUI = gameStatusPanel;
		setPreferredSize();
	}
	
	/**
	 * Initialize the panel for the current game.
	 */
	@Override
	public void initialize(GameGUI p_gameGUI, Game p_game) {
		m_game = p_game;
	}
	
	/**
	 * Set the size of the dummy panel.
	 */
	private void setPreferredSize ()
	{
		setPreferredSize(new Dimension(PlayerStatusGUI.CARD_HEIGHT*2, PlayerStatusGUI.CARD_HEIGHT));
		setMinimumSize(new Dimension(PlayerStatusGUI.CARD_HEIGHT*2, PlayerStatusGUI.CARD_HEIGHT));
	}


	/**
	 * Puts the dummy panel in the appropriate location based on which
	 * hand is the dummy.
	 * @param p_direction the direction of the dummy hand
	 */
	@Override
	public void sig_setDummyPosition(Direction p_direction) {
		// Remember the direction and rotation
		m_dummyDirection = p_direction;
		m_rotation = p_direction.getRotation() + Math.PI;
		
		// Set the layout based on whether the suits are horizontal or
		// vertical for this position.
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
		
		// Add the suits in the right order so they will be C, D, H, S from
		// left to right from the users' perspectives.
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
		
		// Add the dummy panel in the right location.
		m_gameStatusGUI.addDummyPanel (p_direction);
	}

	/**
	 * Removes a card from the displayed dummy hand
	 */
	@Override
	public void sig_cardPlayed(Direction p_turn, Card p_card) {
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

		// The suit displayed by this panel.
		private Suit m_suit;
		
		/**
		 * Creates the panel
		 * @param suit the suit that this panel displays
		 */
		public DummySuitPanel (Suit suit) {
			m_suit = suit;
			setBorder(BorderFactory.createLineBorder(Color.RED));
		}

		/**
		 * Draw the suit label and the cards in the suit.
		 */
		@Override
		public void paintComponent (Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			
			// Rotate the panel according to the dummy's direction.
			g2d.rotate(m_rotation);
			super.paintComponent(g);

			// Calculate the location for drawing the suit symbol
			int x = 0;
			int y = 0;
			switch (m_dummyDirection) {
			case NORTH:
				x = 10;
				y = 30;
				break;
			case SOUTH:
				x = -getWidth() + 10; 
				y = -getHeight() + 30;
				break;
			case WEST:
				x = -getHeight() + 10;
				y = 30;
				break;
			case EAST:
				x = 10;
				y = -getWidth() + 30;
			}

			// Select the color based on the suit.
			if (m_suit.equals(Suit.DIAMONDS) || m_suit.equals(Suit.HEARTS))
			{
				g.setColor(Color.RED);
			}
			else
			{
				g.setColor(Color.BLACK);
			}
			
			// Draw the suit symbol.
			g2d.setFont(DUMMY_FONT);
			g2d.drawString(m_suit.getSymbol(), x, y);
			
			// Draw the cards in the suit.
			drawCards(g2d, x, y);
			g2d.rotate(-m_rotation);
		}

		/**
		 * Draw the cards that the dummy has in the suit.
		 * @param g2d the graphics to draw on
		 * @param x the starting x location
		 * @param y the starting y location
		 */
		private void drawCards(Graphics2D g2d, int x, int y) {
			PlayerHand hand = m_game.getBridgeHand().getDummyHand();
			if (hand != null) {
				TreeSet<Card> cards = hand.getSuitCards().get(m_suit);
				int numDrawn = 0;
				for (Card c : cards.descendingSet()) {
					y = y + 35;
					g2d.drawString(c.getRank().toString(), x, y);
					numDrawn++;
					
					// For north/south dummies, use 2 columns if there are
					// more than 6 cards.
					if (numDrawn == 6) {
						if (m_dummyDirection == Direction.NORTH) {
							y = 30;
							x = x + 60;
						}
						else if (m_dummyDirection == Direction.SOUTH) {
							y = -getHeight() + 30 ;
							x = x + 60;
						}
					}
				}
			}
		}
	}



}
