package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import lerner.blindBridge.model.Suit;
import model.Game;

/**
 * The TrumpSuitGUI allows the sighted players to specify the trump suit.
 * 
 * @author Allison DeJordy
 * 
 * @version March 12, 2015
 */

public class TrumpSuitGUI extends JPanel {

	private Game game;
	private GameGUI gameGUI;

	// for test mode only. For now, hand number can be 1 or 2.
	private int handNum = 1;
	private JButton spadesButton;
	private JButton heartsButton;
	private JButton diamondsButton;
	private JButton clubsButton;
	private JButton noTrumpButton;

	/**
	 * Creates the GUI
	 * 
	 * @param gameGUI
	 *            the frame this is displayed in
	 * @param game
	 *            the game being played
	 */
	public TrumpSuitGUI(GameGUI gameGUI, Game game) {
		this.game = game;
		this.gameGUI = gameGUI;
		// create a panel with the suit buttons
		JPanel buttonPanel = createSuitButtons();
		// create a new JPanel that will hold the direction buttons
		JPanel flowPanel = new JPanel(new FlowLayout());
		// add the button panel to the flow panel
		flowPanel.add(buttonPanel);
		// create a new JPanel that will contain everything in the center of the
		// gui
		JPanel boxPanel = new JPanel();
		// set the new panel's layout
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		boxPanel.add(GUIUtilities.createTitleLabel("What is the trump suit?"));
		// add vertical glue before the buttons
		boxPanel.add(Box.createRigidArea(new Dimension(500, 100)));
		// add the button panel
		boxPanel.add(flowPanel);
		// add vertical glue after the buttons
		boxPanel.add(Box.createVerticalGlue());
		this.add(boxPanel, BorderLayout.CENTER);

	}

	private JPanel createSuitButtons() {

		// create the JPanel that will hold the buttons
		JPanel panel = new JPanel();
		// set the panel's layout to a grid with 3 rows and 1 column
		panel.setLayout(new GridLayout(2, 0));
		// create and add the spades button
		spadesButton = GUIUtilities.createButton("Spades");
		panel.add(GUIUtilities.packageButton(spadesButton, FlowLayout.CENTER));
		// set the action command for the spades button
		spadesButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				game.setTrump(Suit.SPADES);
				gameGUI.changeFrame();
			}

		});

		// create and add the hearts button
		heartsButton = GUIUtilities.createButton("Hearts");
		panel.add(GUIUtilities.packageButton(heartsButton, FlowLayout.CENTER));
		// set the action command for the hearts button
		heartsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				game.setTrump(Suit.HEARTS);
				gameGUI.changeFrame();
			}

		});

		// create and add the diamonds button
		diamondsButton = GUIUtilities.createButton("Diamonds");
		panel.add(GUIUtilities.packageButton(diamondsButton, FlowLayout.CENTER));
		// set the action command for the diamonds button
		diamondsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				game.setTrump(Suit.DIAMONDS);
				gameGUI.changeFrame();
			}

		});

		// create and add the clubs button
		clubsButton = GUIUtilities.createButton("Clubs");
		panel.add(GUIUtilities.packageButton(clubsButton, FlowLayout.CENTER));
		// set the action command for the clubs button
		clubsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				game.setTrump(Suit.CLUBS);
				gameGUI.changeFrame();
			}

		});

		// create and add the notrump button
		noTrumpButton = GUIUtilities.createButton("No Trump");
		panel.add(GUIUtilities.packageButton(noTrumpButton, FlowLayout.CENTER));
		// set the action command for the notrump button
		noTrumpButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				game.setTrump(Suit.NOTRUMP);
				gameGUI.changeFrame();
			}

		});

		if (Game.isTestMode()) {

			enableAndDisableButtons();
		}
		return panel;

	}

	/**
	 * Depending on what hand it is, it only enables the button corresponding to the appropriate trump suit for bid
	 * @throws AssertionError
	 */
	private void enableAndDisableButtons() throws AssertionError {
		
		if (handNum == 1) {

			// only heart can be the trump for the first hand
			spadesButton.setEnabled(false);
			diamondsButton.setEnabled(false);
			clubsButton.setEnabled(false);
			noTrumpButton.setEnabled(false);

		} else if (handNum == 2) {

			// only no trump can be the trump suit in second hand

			heartsButton.setEnabled(false);
			noTrumpButton.setEnabled(true);

		} else {

			throw new AssertionError(
					"TrumpSuitGUI : There are only two hands");
		}
	}

	/**
	 * 
	 * @param handNum
	 */
	public void setHandNum(int handNum) {
		this.handNum = handNum;
		
		enableAndDisableButtons();
	}
	
	@Override
	public void paintComponent(Graphics g){
		
		super.paintComponent(g);
		
		gameGUI.undoButtonSetEnabled(true);
	}

}
