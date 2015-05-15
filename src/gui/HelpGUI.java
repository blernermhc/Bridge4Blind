package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Displays help message describing how the keypad buttons work.
 *
 */
public class HelpGUI extends JPanel {
	
	private GameGUI gameGUI ;

	/**
	 * Create the gui
	 * @param gameGUI TODO
	 */
	public HelpGUI(GameGUI gameGUI) {
		
		this.gameGUI = gameGUI ;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(GUIUtilities.createTitleLabel("Help"));
		add(createHelpPanel(), BorderLayout.CENTER);
	}

	private JPanel createHelpPanel() {

		JPanel helpPanel = new JPanel(new GridLayout(0, 4));
		helpPanel.add(new JLabel(""));

		Font f = new Font("helpFont", Font.PLAIN, 18);

		JLabel helpLabel1 = new JLabel(
				"<html>Backspace: own spades<br>"
						+ "Asterisk: own hearts<br>Backslash: own diamonds<br>"
						+ "Tab: own clubs<br><br>Dash: dummy spades<br>Nine: dummy hearts<br>"
						+ "Eight: dummy diamonds<br>Seven: dummy clubs</html>");

		helpLabel1.setFont(f);

		helpPanel.add(helpLabel1);

		JLabel helpLabel2 = new JLabel(
				"<html>Plus: current trick<br>Five: Dummy's full hand<br>"
						+ "Four: Own full hand<br><br>Three: E/W tricks won<br>"
						+ "Two: N/S tricks won<br>One: contract<br><br>"
						+ "Zero: repeat last thing spoken<br>Enter: tutorial</html>");

		helpLabel2.setFont(f);

		helpPanel.add(helpLabel2);


		helpPanel.add(new JLabel(""));

		return helpPanel;
	}

	@Override
	public void paintComponent(Graphics g){
		
		super.paintComponent(g);
		
		gameGUI.backButtonSetEnabled(true);
		gameGUI.undoButtonSetEnabled(true);
	}

}