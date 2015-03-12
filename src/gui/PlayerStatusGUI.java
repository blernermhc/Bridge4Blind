package gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.border.Border;

import model.Card;
import model.Direction;

/**
 * This class displays the rank and suit of the current card of the players in each cardianl direction
 * 
 * @author Barbara Lerner
 * @version March 12, 2015
 */
public class PlayerStatusGUI extends JComponent {
	private static final Border PLAYER_BORDER = BorderFactory.createLineBorder(
			Color.BLACK, 2);
	private static final Border NEXT_PLAYER_BORDER = BorderFactory
			.createLineBorder(Color.BLUE, 5);
	private static final double SCALE = .7;
	private static final int CARD_WIDTH = (int) (225 * SCALE);
	private static final int CARD_HEIGHT = (int) (350 * SCALE);
	private static final int SPACING = 20;
	private static final String SPADES = "\u2660";
	private static final String CLUBS = "\u2663";
	private static final String HEARTS = "\u2665";
	private static final String DIAMONDS = "\u2666";
	private String rankPlayed = "";
	private String suitPlayed = "";
	private boolean turn = false;
	private double rotation;
	private Direction dir;

	public PlayerStatusGUI(Direction dir) {
		setBorder(PLAYER_BORDER);

		rotation = (dir.ordinal() + 2) * .5 * Math.PI;
		// cardPlayed = dir.name();
		this.dir = dir;
		setPreferredSize();
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		// g2d.drawRect((getWidth() - CARD_WIDTH) / 2, (getHeight() -
		// CARD_HEIGHT)/2, CARD_WIDTH, CARD_HEIGHT);

		setColor(g2d);
		setFont(GameStatusGUI.STATUS_FONT);
		FontMetrics cardMetrics = g2d.getFontMetrics();
		g2d.rotate(rotation);
		g2d.drawString(rankPlayed, getLeft(rankPlayed, cardMetrics),
				getRankBottom());
		g2d.drawString(suitPlayed, getLeft(suitPlayed, cardMetrics),
				getSuitBottom(cardMetrics));
		g2d.rotate(-rotation);
	}

	private void setColor(Graphics g) {
		if (suitPlayed.equals(DIAMONDS) || suitPlayed.equals(HEARTS)) {
			g.setColor(Color.RED);
		} else {
			g.setColor(Color.BLACK);
		}
	}

	private void setPreferredSize() {
		switch (dir) {
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

	private int getLeft(String text, FontMetrics metrics) {
		int textWidth = metrics.stringWidth(text);
		switch (dir) {
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

	private int getRankBottom() {
		switch (dir) {
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

	private int getSuitBottom(FontMetrics rankMetrics) {
		int rankHeight = rankMetrics.getHeight();
		switch (dir) {
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

	public void cardPlayed(Card card) {
		setBorder(PLAYER_BORDER);
		rankPlayed = card.getRank().toString();

		switch (card.getSuit()) {
		case DIAMONDS:
			suitPlayed = DIAMONDS;
			break;
		case HEARTS:
			suitPlayed = HEARTS;
			break;
		case CLUBS:
			suitPlayed = CLUBS;
			break;
		case SPADES:
			suitPlayed = SPADES;
			break;
		default:
			throw new AssertionError(
					"Should not have come here since only 4 suits are available");

		}

		// so that this thread is called first before the audio starts playing
		// and the painting of the last card is delayed
		paintImmediately(0, 0, getWidth(), getHeight());
		
	

	}

	public void trickOver() {
		rankPlayed = "";
		suitPlayed = "";
		setBorder(PLAYER_BORDER);
		repaint();
	}

	public void nextPlayer() {
		setBorder(NEXT_PLAYER_BORDER);
		repaint();
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		Container contentPane = f.getContentPane();
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints northConstraints = new GridBagConstraints();
		northConstraints.gridx = 1;
		northConstraints.gridy = 0;
		northConstraints.anchor = GridBagConstraints.PAGE_START;
		// northConstraints.weightx = 1;
		northConstraints.weighty = 1;
		contentPane.add(new PlayerStatusGUI(Direction.NORTH), northConstraints);

		GridBagConstraints eastConstraints = new GridBagConstraints();
		eastConstraints.gridx = 2;
		eastConstraints.gridy = 1;
		eastConstraints.anchor = GridBagConstraints.LINE_START;
		eastConstraints.weightx = 1;
		contentPane.add(new PlayerStatusGUI(Direction.EAST), eastConstraints);

		GridBagConstraints southConstraints = new GridBagConstraints();
		southConstraints.gridx = 1;
		southConstraints.gridy = 2;
		southConstraints.anchor = GridBagConstraints.PAGE_END;
		// southConstraints.weightx = 1;
		southConstraints.weighty = 1;
		contentPane.add(new PlayerStatusGUI(Direction.SOUTH), southConstraints);

		GridBagConstraints westConstraints = new GridBagConstraints();
		westConstraints.gridx = 0;
		westConstraints.gridy = 1;
		westConstraints.anchor = GridBagConstraints.LINE_END;
		westConstraints.weightx = 1;
		contentPane.add(new PlayerStatusGUI(Direction.WEST), westConstraints);
		f.setMinimumSize(new Dimension(CARD_HEIGHT * 3, CARD_HEIGHT * 3));
		f.setVisible(true);
	}

}
