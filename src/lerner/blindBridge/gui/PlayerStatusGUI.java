package lerner.blindBridge.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.Suit;

/**
 * This class displays the rank and suit of the current card of the players in each cardianl
 * direction
 * 
 * @author Barbara Lerner
 * @version March 12, 2015
 */
public class PlayerStatusGUI extends JComponent
{
	private static final Border	PLAYER_BORDER		= BorderFactory.createLineBorder(	Color.BLACK,
																						2);

	private static final Border	NEXT_PLAYER_BORDER	= BorderFactory.createLineBorder(Color.BLUE, 5);

	private static final double	SCALE				= .7;

	public static final int	CARD_WIDTH			= (int) (225 * SCALE);

	public static final int	CARD_HEIGHT			= (int) (350 * SCALE);

	private static final int	SPACING				= 20;



	private String				m_rankPlayed			= "";

	private Suit				m_suitPlayed			= null ;

	// private boolean				m_turn				= false; // did not appear to be used

	private double				m_rotation;

	private Direction			m_dir;

	private boolean				m_trickOver			= true;

	/***********************************************************************
	 * Creates the status GUI for a single player
	 * @param p_dir player's position
	 ***********************************************************************/
	public PlayerStatusGUI ( Direction p_dir )
	{
		setBorder(PLAYER_BORDER);

		m_rotation = p_dir.getRotation();
		// cardPlayed = dir.name();
		this.m_dir = p_dir;
		setPreferredSize();
	}

	/***********************************************************************
	 * Sets the state of this player's GUI.
	 * @param p_card				the card, if played
	 * @param p_isNextPlayer		true if this is the next player to play
	 ***********************************************************************/
	public void setState ( Card p_card, boolean p_isNextPlayer )
	{
		if (p_isNextPlayer)
			setBorder(NEXT_PLAYER_BORDER);
		else
			setBorder(PLAYER_BORDER);
		
		if (p_card == null)
		{
			m_suitPlayed = null;
			m_rankPlayed = "";
		}
		else
		{
			m_rankPlayed = p_card.getRank().toString();
			m_suitPlayed = p_card.getSuit();
		}
		repaint();
	}
	
	public void paintComponent ( Graphics g )
	{

		Graphics2D g2d = (Graphics2D) g;
		// g2d.drawRect((getWidth() - CARD_WIDTH) / 2, (getHeight() -
		// CARD_HEIGHT)/2, CARD_WIDTH, CARD_HEIGHT);

		drawWhiteBackground(g);

		setFont(GameStatusGUI.STATUS_FONT);
		FontMetrics cardMetrics = g2d.getFontMetrics();
		g2d.rotate(m_rotation);

		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT);

		if (m_suitPlayed != null) {
			setColor(g2d);
			g2d.drawString(m_rankPlayed, getLeft(m_rankPlayed, cardMetrics), getRankBottom());
			String suitSymbol = m_suitPlayed.getSymbol();
			g2d.drawString(suitSymbol, getLeft(suitSymbol, cardMetrics), getSuitBottom(cardMetrics));
		}
		g2d.rotate(-m_rotation);
	}

	private void setColor ( Graphics g )
	{
		if (m_suitPlayed.equals(Suit.DIAMONDS) || m_suitPlayed.equals(Suit.HEARTS))
		{
			g.setColor(Color.RED);
		}
		else
		{
			g.setColor(Color.BLACK);
		}
	}

	/**
	 * The background of the cards is white and not gray
	 * 
	 * @param g
	 */
	private void drawWhiteBackground ( Graphics g )
	{

		g.setColor(Color.WHITE);

		switch (m_dir)
		{
			case NORTH:
			case SOUTH:
				g.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT);

				break;
			case EAST:
			case WEST:
				g.fillRect(0, 0, CARD_HEIGHT, CARD_WIDTH);

		}
	}

	private void setPreferredSize ()
	{
		switch (m_dir)
		{
			case NORTH:
			case SOUTH:
				setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
				setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
				break;
			case EAST:
			case WEST:
				setPreferredSize(new Dimension(CARD_HEIGHT, CARD_WIDTH));
				setMinimumSize(new Dimension(CARD_HEIGHT, CARD_WIDTH));
		}
		// assert false;
	}

	private int getLeft ( String text, FontMetrics metrics )
	{
		int textWidth = metrics.stringWidth(text);
		switch (m_dir)
		{
			case NORTH:
				return (-getWidth() - textWidth) / 2;
			case EAST:
				return (-getHeight() - textWidth) / 2;
			case SOUTH:
				return (getWidth() - textWidth) / 2;
			case WEST:
				return (getHeight() - textWidth) / 2;
		}
		// assert false;
		return 0;
	}

	private int getRankBottom ()
	{
		switch (m_dir)
		{
			case NORTH:
				return -2 * getHeight() / 3;
			case WEST:
				return -2 * getWidth() / 3;
			case EAST:
				return getWidth() / 3;
			case SOUTH:
				return getHeight() / 3;
		}
		assert false;
		return 0;
	}

	private int getSuitBottom ( FontMetrics rankMetrics )
	{
		int rankHeight = rankMetrics.getHeight();
		switch (m_dir)
		{
			case NORTH:
				return -2 * getHeight() / 3 + rankHeight + SPACING;
			case WEST:
				return -2 * getWidth() / 3 + rankHeight + SPACING;
			case EAST:
				return getWidth() / 3 + rankHeight + SPACING;
			case SOUTH:
				return getHeight() / 3 + rankHeight + SPACING;
		}
		assert false;
		return 0;
	}

	public void cardPlayed ( Card card )
	{

		System.out.println("player status gui card played " + m_dir);

		setBorder(PLAYER_BORDER);
		m_rankPlayed = card.getRank().toString();
		m_suitPlayed = card.getSuit();


		// so that this thread is called first before the audio starts playing
		// and the painting of the last card is not delayed

		synchronized (this)
		{

			try
			{
				while (!m_trickOver)
				{

					System.out.println("waiting for trick to be over ");

					wait();
				}
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("Player status gui card found. trick is over so repainting");

			paintImmediately(0, 0, getWidth(), getHeight());
			m_trickOver = false;
		}

	}

	public void trickOver ()
	{

		System.out.println("player status gui trick over " + m_dir);

		m_rankPlayed = "";
		m_suitPlayed = null;
		setBorder(PLAYER_BORDER);

		synchronized (this)
		{

			m_trickOver = true;

			System.out.println("notified trick over");

			paintImmediately(0, 0, getWidth(), getHeight());

			notifyAll();

		}

	}

	public void nextPlayer ()
	{

		System.out.println("player status gui next player " + m_dir);

		// Thread.dumpStack();

		setBorder(NEXT_PLAYER_BORDER);
		repaint();
	}

	public void clear ()
	{

		m_suitPlayed = null;
		m_rankPlayed = "";
		setBorder(PLAYER_BORDER);
		repaint();

	}

	public void undo ()
	{

		System.out.println("player status gui undo");

		m_suitPlayed = null;
		m_rankPlayed = "";
		setBorder(NEXT_PLAYER_BORDER);
		// repaint();
		paintImmediately(0, 0, getWidth(), getHeight());

	}

	public static Border getPlayerBorder ()
	{
		return PLAYER_BORDER;
	}

	public void setTrickOver ( boolean p_trickOver )
	{

		this.m_trickOver = p_trickOver;
	}

}
