package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Contains some useful methods for formatting the GUI
 * @author Barbara Lerner
 * @version Jul 11, 2012
 *
 */
public class GUIUtilities {
	/**
	 * Font to display screen titles with
	 */
	public static final Font TITLE_FONT = new Font("Verdana", Font.BOLD, 40);
	
	private GUIUtilities() {
		// No need to create any instances
	}

	/**Creates a JPanel containing a 300x100 button with the specified text.
	 * 
	 * @param text The text to be placed on the button
	 * @return A JPanel containing the button
	 */
	public static JButton createButton(String text){
		
		//create the font for the button
		Font font = new Font("Verdana", Font.PLAIN, 36);
		//create the button
		JButton button = new JButton(text);
		//set the button's font
		button.setFont(font);
		//set the button's size
		button.setPreferredSize(new Dimension(300, 75));
		button.setFocusable(false);
		return button;
		
		
	}

	/**Packages a JButton inside a FlowLayout JPanel, allowing it to assume
	 * its "natural" size.
	 * 
	 * @param button The JButton to be packaged.
	 * @param alignment The alignment of the button within the panel.
	 * 
	 * @return A JPanel containing the button.
	 */
	public static JPanel packageButton(JButton button, int alignment){
		
		JPanel panel = new JPanel(new FlowLayout(alignment, 0, 0));
		panel.add(button);
		return panel;
		
	}

	/**
	 * Creates a centered screen title
	 * @param title the string to display
	 * @return the formatted label
	 */
	public static JLabel createTitleLabel(String title) {
		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		return titleLabel;
	}

}
