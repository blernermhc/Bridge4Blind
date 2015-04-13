package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sun.org.glassfish.gmbal.ManagedAttribute;

import model.Direction;
import model.Game;

public class NextHandGUI extends JPanel {

	private GameGUI gameGUI;

	private Game game;
	
	private JLabel winnerLabel ;

	public NextHandGUI(GameGUI gameGUI , Game game) {

		this.gameGUI = gameGUI;

		this.game = game;
		
		JPanel mainPanel = new JPanel() ;

		BoxLayout boxLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS) ;
		
		mainPanel.setLayout(boxLayout);
		
		// add the JLabel that shows who the winners of the last hand are
		winnerLabel = GUIUtilities.createTitleLabel("Hand won by...");
		
		mainPanel.add(winnerLabel) ;
		
		// add vertical glue before the buttons
		mainPanel.add(Box.createRigidArea(new Dimension(500, 100)));
		mainPanel.add(Box.createVerticalGlue());
		
		mainPanel.add(createNextHandButton()) ;
		
		add(mainPanel, BorderLayout.CENTER) ;
	}

	/**
	 * Returns a String announcing the winners of the last hand
	 * @return A String announcing the winners of the last hand
	 */
	private String getWinnerText() {

		// ask game who the winners are
		String winnerText = "Hand is won by ";
		
		System.out.println("game is null " + game);

		game.determineHandWinner();

		Direction winner = game.getLastHandWinner();

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
			
			new AssertionError("String text is null") ;

			System.out.println("There should be no null pointer Exception");

		}

		

		return winnerText;

	}
	
	private JButton createNextHandButton(){
		
		JButton nextHandButton = GUIUtilities.createButton("Next Hand") ;
		
		nextHandButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				game.resetGame();
	
				gameGUI.changeFrame();
				
			}
		});
		
		return nextHandButton ;
	}
	
	/**
	 * Refreshes the display after each hand
	 */
	public void refreshDisplay(){
		
		System.out.println("Refreshing display");
		
		winnerLabel.setText(getWinnerText());
		
	}


}
