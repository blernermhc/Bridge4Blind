package lerner.blindBridge.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.BridgeHand;
import lerner.blindBridge.model.Direction;

/**
 * Announces the winner of each hand and allows the players to move on to the next hand.
 * 
 * @author Humaira Orchee
 * @version April 30, 2015
 *
 */
public class NextHandGUI extends JPanel
{

	private GameGUI	m_gameGUI;

	private Game		m_game;

	private JLabel	m_winnerLabel;

	private JLabel	m_tricksLabel;

	/**
	 * 
	 * @param m_gameGUI
	 * @param m_game
	 */
	public NextHandGUI ( GameGUI p_gameGUI, Game p_game )
	{

		m_gameGUI = p_gameGUI;

		m_game = p_game;

		JPanel mainPanel = new JPanel();

		BoxLayout boxLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);

		mainPanel.setLayout(boxLayout);

		// add the JLabel that shows who the winners of the last hand are
		m_winnerLabel = GUIUtilities.createTitleLabel("");

		m_winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		mainPanel.add(m_winnerLabel);

		// adds the JLabel that shows how many tricks the winning pair won
		m_tricksLabel = GUIUtilities.createTitleLabel("");

		m_tricksLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		mainPanel.add(m_tricksLabel);

		// add vertical glue before the buttons
		mainPanel.add(Box.createRigidArea(new Dimension(500, 100)));
		mainPanel.add(Box.createVerticalGlue());

		mainPanel.add(createNextHandButton());

		add(mainPanel, BorderLayout.CENTER);
	}

	/**
	 * Returns a String[] announcing the winners of the last hand and the tricks they won in the
	 * last hand
	 * 
	 * @return A String announcing the winners of the last hand and the tricks they won in the last
	 *         hand
	 */
	private String[] getWinnerText ()
	{
		// ask game who the winners are
		String winnerText = "Hand is won by ";
		
		BridgeHand.HandWinner handWinner = m_game.getBridgeHand().determineHandWinner();

		Direction winner = handWinner.direction;
		int tricksWon = handWinner.tricksTaken;


		// TODO : add sound here
		// SoundManager soundManager = SoundManager.getInstance() ;
		// soundManager.addSound(filename);

		try
		{
			// figure out what the text should be
			if (winner.equals(Direction.NORTH) || winner.equals(Direction.SOUTH))
			{
				winnerText += "North and South";
			}
			else if (winner.equals(Direction.EAST) || winner.equals(Direction.WEST))
			{
				winnerText += "East and West";
			}
		}
		catch (NullPointerException e)
		{
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
	private JButton createNextHandButton ()
	{

		JButton nextHandButton = GUIUtilities.createButton("Next Hand");

		nextHandButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent e )
			{

				m_gameGUI.changeFrame();

				m_game.evt_startNewHand();

				System.out.println("resetting game in Next Hand GUI");

			}
		});

		nextHandButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		return nextHandButton;
	}

	/**
	 * Refreshes the display after each hand
	 */
	public void refreshDisplay ()
	{

		System.out.println("Refreshing display");

		String[] text = getWinnerText();

		m_winnerLabel.setText(text[0]);

		m_tricksLabel.setText(text[1]);

	}

	@Override
	public void paintComponent ( Graphics g )
	{

		super.paintComponent(g);

		m_gameGUI.undoButtonSetEnabled(false);
		m_gameGUI.backButtonSetEnabled(false);
	}

}
