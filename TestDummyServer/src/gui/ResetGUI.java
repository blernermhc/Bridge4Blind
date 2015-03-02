package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import model.Game;

/**
 * The GUI to allow the user to undo the playing of cards or tricks.
 *
 */
public class ResetGUI extends JPanel {
	
	/**
	 * Creates the gui
	 * @param game the game being played
	 */
	public ResetGUI(final Game game) {
		setLayout (new BoxLayout(this, BoxLayout.Y_AXIS));
		add(GUIUtilities.createTitleLabel("Game in Progress"));
		JPanel gridPanel = new JPanel(new GridLayout(2,1));
		
		JButton undoCardButton = GUIUtilities.createButton("Undo Card");
		undoCardButton.addActionListener (new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				game.undo();
			}
			
		});
		
		JPanel undoCardPanel = GUIUtilities.packageButton(undoCardButton, FlowLayout.CENTER);
		gridPanel.add(undoCardPanel);
		JButton undoTrickButton = GUIUtilities.createButton("Undo Trick");
		undoTrickButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				game.undoTrick();
			}
		});
		
		JPanel undoTrickPanel = GUIUtilities.packageButton(undoTrickButton, FlowLayout.CENTER);
		gridPanel.add(undoTrickPanel);
		this.add(gridPanel, BorderLayout.CENTER);
	}


}
