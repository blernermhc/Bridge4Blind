package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import model.Contract;
import model.Direction;
import model.Suit;

/**
 * CLASS HAS NOT BEEN USED
 *
 */
public class BidStatusGUI extends JComponent {
	
	private static final Font BID_FONT = GameStatusGUI.STATUS_FONT.deriveFont(36f);
	private static final int SPACING = 20;
	private String bid = "3" + Suit.DIAMONDS;
	private String declarer = Direction.NORTH.toString();
	
	public void paintComponent(Graphics g) {
		System.out.println("Painting the bid");
		Graphics2D g2d = (Graphics2D) g;
		//g2d.drawRect((getWidth() - CARD_WIDTH) / 2, (getHeight() - CARD_HEIGHT)/2, CARD_WIDTH, CARD_HEIGHT);

		setFont(BID_FONT);
		g2d.setColor (new Color(15,148,20));
		FontMetrics bidMetrics = g2d.getFontMetrics();
		g2d.drawString(bid, getLeft(bid, bidMetrics), getBidBottom());
		g2d.drawString(declarer, getLeft(declarer, bidMetrics), getDeclarerBottom(bidMetrics));
	}
	
	private int getLeft(String text, FontMetrics metrics) {
		int textWidth = metrics.stringWidth(text);
		//System.out.println("component width = " + getWidth());
		//System.out.println("left = " + (getWidth()-textWidth)/2);
		return (getWidth()-textWidth)/2;
	}
	
	private int getBidBottom() {
		//System.out.println("component height = " + getHeight());
		//System.out.println("Bottom = " + getHeight()/3);
		return getHeight() / 3;
	}
	
	private int getDeclarerBottom(FontMetrics bidMetrics) {
		int bidHeight = bidMetrics.getHeight();
		return getHeight() / 3 + bidHeight + SPACING;
	}

	public void setBid(Contract contract) {
		bid = contract.getContractNum() + contract.getTrump().toString();
		declarer = contract.getBidWinner().toString();
	}

	public void clear() {
		bid = "";
		declarer = "";
	}

}
