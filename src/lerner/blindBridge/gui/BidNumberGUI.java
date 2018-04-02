package lerner.blindBridge.gui;

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
import javax.swing.JLabel;
import javax.swing.JPanel;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Contract;

/**
 * The panel that allows the user to set the number of tricks to win for the contract.
 * 
 * @version March 12, 2015
 */
public class BidNumberGUI extends BridgeJPanel implements ActionListener
{

	private JButton[]	m_buttons;

	/** the Game object associated with this GUI */
	private Game			m_game;

	/** the GUI manager object */
	private GameGUI		m_gameGUI;

	// for test mode only. For now, hand number can be 1 or 2.
	private int			m_handNum	= 1;

	/**
	 * Create the panel
	 * 
	 * @param m_game
	 *            the game being played.
	 */
	public BidNumberGUI ( )
	{
		m_buttons = new JButton[Contract.MAX_BID];
		// create a new JPanel that will contain everything in the center of the
		// gui
		JPanel boxPanel = new JPanel();
		// set the new panel's layout
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		JLabel titleLabel = GUIUtilities.createTitleLabel("How many tricks in the bid?");
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

	/* (non-Javadoc)
	 * @see lerner.blindBridge.gui.BridgeJPanel#initialize(lerner.blindBridge.gui.GameGUI, lerner.blindBridge.main.Game)
	 */
	public void initialize ( GameGUI p_gameGUI, Game p_game )
	{
		m_game = p_game;
		m_gameGUI = p_gameGUI;
	}
	
	
	
	/** Creates the button panel. */
	private JPanel createButtonPanel ()
	{

		// create a JPanel to hold the buttons
		JPanel mainPanel = new JPanel();
		// set the panel's layout to a GridLayout with 2 rows and 4 columns
		mainPanel.setLayout(new GridLayout(3, 0, 20, 20));

		// create the buttons
		for (int i = 0; i < m_buttons.length; i++)
		{

			String s = "" + (i + 1);
			m_buttons[i] = GUIUtilities.createButton(s);
			m_buttons[i].addActionListener(this);
			mainPanel.add(GUIUtilities.packageButton(m_buttons[i], FlowLayout.CENTER));

			// for testing, only 4 bids is allowed for first hand
			if (Game.isTestMode())
			{

				m_buttons[i].setEnabled(false);
			}
		}

		// for testing, only 4 bids is allowed for first hand. 3 bids is allowed
		// in the second hand
		if (Game.isTestMode())
		{

			enableAndDisableButtons();
		}

		return mainPanel;

	}

	/**
	 * Depending on what hand it is, it only enables the button corresponding to the appropriate bid
	 * number
	 * 
	 * @throws AssertionError
	 */
	private void enableAndDisableButtons () throws AssertionError
	{
		if (m_handNum == 1)
		{

			m_buttons[3].setEnabled(true);

		}
		else if (m_handNum == 2)
		{

			m_buttons[3].setEnabled(false);
			m_buttons[2].setEnabled(true);

		}
		else
		{

			throw new AssertionError("BidNumberGUI : There are only two hands");
		}
	}

	@Override
	public void actionPerformed ( ActionEvent e )
	{
		if (m_game != null)
		{
			int bidNumber = Integer.parseInt(e.getActionCommand());
			m_game.getBridgeHand().evt_setContractNum(bidNumber);
		}
		m_gameGUI.changeFrame(GameGUIs.TRUMP_SUIT_GUI);
	}

	/**
	 * 
	 * @param m_handNum
	 */
	public void setHandNum ( int p_handNum )
	{
		this.m_handNum = p_handNum;

		enableAndDisableButtons();
	}

	@Override
	public void paintComponent ( Graphics g )
	{
		super.paintComponent(g);
	}

}