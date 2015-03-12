package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import model.Contract;
import model.Game;

/**
 * The panel that allows the user to set the number of tricks to win for the
 * contract.
 * 
 * @version March 12, 2015
 */
public class BidNumberGUI extends JPanel implements ActionListener {

	private JButton[] buttons;
	private Game game;
	private GameGUI gameGUI;

	/**
	 * Create the panel
	 * 
	 * @param game
	 *            the game being played.
	 */
	public BidNumberGUI(GameGUI gameGUI, Game game) {
		this.game = game;
		this.gameGUI = gameGUI;
		buttons = new JButton[Contract.MAX_BID];
		// create a new JPanel that will contain everything in the center of the
		// gui
		JPanel boxPanel = new JPanel();
		// set the new panel's layout
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		JLabel titleLabel = GUIUtilities
				.createTitleLabel("How many tricks in the bid?");
		boxPanel.add(titleLabel);
		// add vertical glue before the buttons
		boxPanel.add(Box.createRigidArea(new Dimension(500, 100)));
		boxPanel.add(Box.createVerticalGlue());
		// add the button panel
		boxPanel.add(createButtonPanel());
		// add vertical glue after the buttons
		boxPanel.add(Box.createVerticalGlue());
		this.add(boxPanel, BorderLayout.CENTER);

	}

	/** Creates the button panel. */
	private JPanel createButtonPanel() {

		// create a JPanel to hold the buttons
		JPanel mainPanel = new JPanel();
		// set the panel's layout to a GridLayout with 2 rows and 4 columns
		mainPanel.setLayout(new GridLayout(3, 0, 20, 20));

		// create the buttons
		for (int i = 0; i < buttons.length; i++) {

			String s = "" + (i + 1);
			buttons[i] = GUIUtilities.createButton(s);
			buttons[i].addActionListener(this);
			mainPanel.add(GUIUtilities.packageButton(buttons[i],
					FlowLayout.CENTER));

			// for testing, only 4 bids is allowed for first hand
			if (Game.isTestMode()) {

				buttons[i].setEnabled(false);
			}
		}

		// for testing, only 4 bids is allowed for first hand
		if (Game.isTestMode()) {

			buttons[3].setEnabled(true);
		}

		return mainPanel;

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (game != null) {
			int bidNumber = Integer.parseInt(e.getActionCommand());
			game.setContractNum(bidNumber);
		}
		gameGUI.changeFrame();

	}

}