package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Direction;
import model.Game;

/**
 * Announces the winner of each hand and allows the players to move on to the
 * next hand.
 * 
 * @author Humaira Orchee
 * @version April 13, 2015
 *
 */
public class NextHandGUI extends JPanel {

	private GameGUI gameGUI;

	private Game game;

	private JLabel winnerLabel;

	private JLabel tricksLabel;

	/**
	 * 
	 * @param gameGUI
	 * @param game
	 */
	public NextHandGUI(GameGUI gameGUI, Game game) {

		this.gameGUI = gameGUI;

		this.game = game;

		JPanel mainPanel = new JPanel();

		BoxLayout boxLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);

		mainPanel.setLayout(boxLayout);

		// add the JLabel that shows who the winners of the last hand are
		winnerLabel = GUIUtilities.createTitleLabel("");

		winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		mainPanel.add(winnerLabel);

		// adds the JLabel that shows how many tricks the winning pair won
		tricksLabel = GUIUtilities.createTitleLabel("");

		tricksLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		mainPanel.add(tricksLabel);

		// add vertical glue before the buttons
		mainPanel.add(Box.createRigidArea(new Dimension(500, 100)));
		mainPanel.add(Box.createVerticalGlue());

		mainPanel.add(createNextHandButton());

		add(mainPanel, BorderLayout.CENTER);
	}

	/**
	 * Returns a String[] announcing the winners of the last hand and the tricks
	 * they won in the last hand
	 * 
	 * @return A String announcing the winners of the last hand and the tricks
	 *         they won in the last hand
	 */
	private String[] getWinnerText() {

		// ask game who the winners are
		String winnerText = "Hand is won by ";
		int tricksWon = game.determineHandWinner();

		Direction winner = game.getLastHandWinner();

		// TODO : add sound here

		// SoundManager soundManager = SoundManager.getInstance() ;
		//
		// soundManager.addSound(filename);

		try {

			// figure out what the text should be
			if (winner.equals(Direction.NORTH)
					|| winner.equals(Direction.SOUTH)) {

				winnerText += "North and South";

			} else if (winner.equals(Direction.EAST)
					|| winner.equals(Direction.WEST)) {

				winnerText = "East and West";

			}
		} catch (NullPointerException e) {

			new AssertionError("String text is null");

			System.out.println("There should be no null pointer Exception");

		}

		String trickText = "They won " + tricksWon + " tricks in total.";

		return new String[] { winnerText, trickText };

	}

	/**
	 * 
	 * @return
	 */
	private JButton createNextHandButton() {

		JButton nextHandButton = GUIUtilities.createButton("Next Hand");

		nextHandButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				gameGUI.changeFrame();

				game.resetGame();

				System.out.println("resetting game in Next Hand GUI");

			}
		});

		nextHandButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		return nextHandButton;
	}

	/**
	 * Refreshes the display after each hand
	 */
	public void refreshDisplay() {

		System.out.println("Refreshing display");

		String[] text = getWinnerText();

		winnerLabel.setText(text[0]);

		tricksLabel.setText(text[1]);

	}

}
