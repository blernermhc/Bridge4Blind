package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**The DirectionGUI allows the sighted player to specify one of four
* cardinal directions.
*
* @author Allison DeJordy
**/

public abstract class DirectionGUI extends JPanel implements ActionListener {
	
	private JPanel buttonPanel;
	protected JButton northButton;
	protected JButton westButton;
	protected JButton eastButton;
	protected JButton southButton;
	
	/**
	 * Create the panel that displays directional buttons
	 * @param title the prompt to display above the buttons
	 */
	public DirectionGUI(String title){
		
		//create a panel with the direction buttons
		buttonPanel = createDirectionButtonPanel();
		//create a new JPanel that will hold the direction buttons
		JPanel flowPanel = new JPanel(new FlowLayout());
		//add the button panel to the flow panel
		flowPanel.add(buttonPanel);

		//create a new JPanel that will contain everything in the center of the gui
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		
		JLabel titleLabel = GUIUtilities.createTitleLabel(title);
		boxPanel.add(titleLabel);
		
		//add vertical glue before the buttons
		boxPanel.add(Box.createVerticalGlue());
		//add the button panel
		boxPanel.add(flowPanel);
		//add vertical glue after the buttons
		boxPanel.add(Box.createVerticalGlue());
		this.add(boxPanel, BorderLayout.CENTER);
		
	}

	/**Creates the panel containing the buttons used to indicate the
	 * visually-impaired player's position.
	 * 
	 * @return A panel containing appropriately-placed "North," "South," "East"
	 * and "West" buttons.
	 */
	protected JPanel createDirectionButtonPanel(){
		//create the JPanel that will hold the buttons
		JPanel panel = new JPanel();
		//set the panel's layout to a grid with 3 rows and 1 column
		panel.setLayout(new GridLayout(3, 0));
		//create and add the north button
		northButton = GUIUtilities.createButton("North");
		JPanel northButtonPanel = GUIUtilities.packageButton(northButton, FlowLayout.CENTER);
		panel.add(northButtonPanel);
		//set the north button's action command
		northButton.addActionListener(this);
		//create an inner panel to hold the east and west buttons
		JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		//create and add the west button
		westButton = GUIUtilities.createButton("West");
		JPanel westButtonPanel = GUIUtilities.packageButton(westButton, FlowLayout.CENTER);
		innerPanel.add(westButtonPanel);
		//set the west button's action command
		westButton.addActionListener(this);
		//create and add the east button
		eastButton = GUIUtilities.createButton("East");
		JPanel eastButtonPanel = GUIUtilities.packageButton(eastButton, FlowLayout.CENTER);
		innerPanel.add(eastButtonPanel);
		//set the east button's action command
		eastButton.addActionListener(this);
		//add the inner panel
		panel.add(innerPanel);
		//create and add the south button
		southButton = GUIUtilities.createButton("South");
		JPanel southButtonPanel = GUIUtilities.packageButton(southButton, FlowLayout.CENTER);
		panel.add(southButtonPanel);
		//set the south button's action command
		southButton.addActionListener(this);
		
		JPanel outerPanel = new JPanel();
		outerPanel.setLayout (new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
		outerPanel.add(Box.createRigidArea(new Dimension(500, 100)));
		//outerPanel.add(Box.createVerticalGlue());
		outerPanel.add(panel);
		//outerPanel.add(Box.createVerticalGlue());

		
		return outerPanel;
		
	}
	
	
}