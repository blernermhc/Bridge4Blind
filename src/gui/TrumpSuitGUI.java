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
import javax.swing.JPanel;

import model.Game;
import model.Suit;

/** The TrumpSuitGUI allows the sighted players to specify the trump suit.
 * 
 * @author Allison DeJordy
 * 
 * @version March 12, 2015
 */

public class TrumpSuitGUI extends JPanel  {
	
	private Game game;
	private GameGUI gameGUI;
	
	/**
	 * Creates the GUI
	 * @param gameGUI the frame this is displayed in
	 * @param game the game being played
	 */
	public TrumpSuitGUI(GameGUI gameGUI, Game game) {
		this.game = game;
		this.gameGUI = gameGUI;
		//create a panel with the suit buttons
		JPanel buttonPanel = createSuitButtons();
		//create a new JPanel that will hold the direction buttons
		JPanel flowPanel = new JPanel(new FlowLayout());
		//add the button panel to the flow panel
		flowPanel.add(buttonPanel);
		//create a new JPanel that will contain everything in the center of the gui
		JPanel boxPanel = new JPanel();
		//set the new panel's layout
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		boxPanel.add(GUIUtilities.createTitleLabel("What is the trump suit?"));
		//add vertical glue before the buttons
		boxPanel.add(Box.createRigidArea (new Dimension (500, 100)));
		//add the button panel
		boxPanel.add(flowPanel);
		//add vertical glue after the buttons
		boxPanel.add(Box.createVerticalGlue());
		this.add(boxPanel, BorderLayout.CENTER);
		
	}
	
	private JPanel createSuitButtons(){
		
		//create the JPanel that will hold the buttons
		JPanel panel = new JPanel();
		//set the panel's layout to a grid with 3 rows and 1 column
		panel.setLayout(new GridLayout(2, 0));
		//create and add the spades button
		JButton spadesButton = GUIUtilities.createButton("Spades");
		panel.add(GUIUtilities.packageButton(spadesButton, FlowLayout.CENTER));
		//set the action command for the spades button
		spadesButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				game.setTrump(Suit.SPADES);
				gameGUI.changeFrame();
			}
			
		});

		//create and add the hearts button
		JButton heartsButton = GUIUtilities.createButton("Hearts");
		panel.add(GUIUtilities.packageButton(heartsButton, FlowLayout.CENTER));
		//set the action command for the hearts button
		heartsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				game.setTrump(Suit.HEARTS);
				gameGUI.changeFrame();
			}
			
		});

		//create and add the diamonds button
		JButton diamondsButton = GUIUtilities.createButton("Diamonds");
		panel.add(GUIUtilities.packageButton(diamondsButton, FlowLayout.CENTER));
		//set the action command for the diamonds button
		diamondsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				game.setTrump(Suit.DIAMONDS);
				gameGUI.changeFrame();
			}
			
		});
		
		//create and add the clubs button
		JButton clubsButton = GUIUtilities.createButton("Clubs");
		panel.add(GUIUtilities.packageButton(clubsButton, FlowLayout.CENTER));
		//set the action command for the clubs button
		clubsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				game.setTrump(Suit.CLUBS);
				gameGUI.changeFrame();
			}
			
		});
		
		//create and add the notrump button
		JButton noTrumpButton = GUIUtilities.createButton("No Trump");
		panel.add(GUIUtilities.packageButton(noTrumpButton, FlowLayout.CENTER));
		//set the action command for the notrump button
		noTrumpButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				game.setTrump(Suit.NOTRUMP);
				gameGUI.changeFrame();
			}
			
		});
		
		if(Game.isTestMode()){
			
			// only hear can be the trump for the first hand
			spadesButton.setEnabled(false);
			diamondsButton.setEnabled(false);
			clubsButton.setEnabled(false);
			noTrumpButton.setEnabled(false);
		}
		return panel;
		
	}

}
