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
import javax.swing.JPanel;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Suit;

/**
 * The TrumpSuitGUI allows the sighted players to specify the trump suit.
 * 
 * @author Allison DeJordy
 * 
 * @version March 12, 2015
 */

public class BidSuitGUI extends BridgeJPanel
{

	private Game		m_game;

	private GameGUI	m_gameGUI;

	// for test mode only. For now, hand number can be 1 or 2.
	private int		m_handNum	= 1;

	private JButton	m_spadesButton;

	private JButton	m_heartsButton;

	private JButton	m_diamondsButton;

	private JButton	m_clubsButton;

	private JButton	m_noTrumpButton;

	/**
	 * Creates the GUI
	 * 
	 * @param m_gameGUI
	 *            the frame this is displayed in
	 * @param m_game
	 *            the game being played
	 */
	public BidSuitGUI ( )
	{
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

	/* (non-Javadoc)
	 * @see lerner.blindBridge.gui.BridgeJPanel#initialize(lerner.blindBridge.gui.GameGUI, lerner.blindBridge.main.Game)
	 */
	public void initialize ( GameGUI p_gameGUI, Game p_game )
	{
		m_game = p_game;
		m_gameGUI = p_gameGUI;
	}
	

	private JPanel createSuitButtons ()
	{

		// create the JPanel that will hold the buttons
		JPanel panel = new JPanel();
		// set the panel's layout to a grid with 3 rows and 1 column
		panel.setLayout(new GridLayout(2, 0));
		// create and add the spades button
		m_spadesButton = GUIUtilities.createButton("Spades");
		panel.add(GUIUtilities.packageButton(m_spadesButton, FlowLayout.CENTER));
		// set the action command for the spades button
		m_spadesButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent e )
			{
				if (m_game != null)
				{
					m_game.getBridgeHand().evt_setContractSuit(Suit.SPADES);
				}
				m_gameGUI.changeFrame();
			}

		});

		// create and add the hearts button
		m_heartsButton = GUIUtilities.createButton("Hearts");
		panel.add(GUIUtilities.packageButton(m_heartsButton, FlowLayout.CENTER));
		// set the action command for the hearts button
		m_heartsButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent e )
			{
				if (m_game != null)
				{
					m_game.getBridgeHand().evt_setContractSuit(Suit.HEARTS);
				}
				m_gameGUI.changeFrame();
			}

		});

		// create and add the diamonds button
		m_diamondsButton = GUIUtilities.createButton("Diamonds");
		panel.add(GUIUtilities.packageButton(m_diamondsButton, FlowLayout.CENTER));
		// set the action command for the diamonds button
		m_diamondsButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent e )
			{
				if (m_game != null)
				{
					m_game.getBridgeHand().evt_setContractSuit(Suit.DIAMONDS);
				}
				m_gameGUI.changeFrame();
			}

		});

		// create and add the clubs button
		m_clubsButton = GUIUtilities.createButton("Clubs");
		panel.add(GUIUtilities.packageButton(m_clubsButton, FlowLayout.CENTER));
		// set the action command for the clubs button
		m_clubsButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent e )
			{
				if (m_game != null)
				{
					m_game.getBridgeHand().evt_setContractSuit(Suit.CLUBS);
				}
				m_gameGUI.changeFrame();
			}

		});

		// create and add the notrump button
		m_noTrumpButton = GUIUtilities.createButton("No Trump");
		panel.add(GUIUtilities.packageButton(m_noTrumpButton, FlowLayout.CENTER));
		// set the action command for the notrump button
		m_noTrumpButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent e )
			{
				if (m_game != null)
				{
					m_game.getBridgeHand().evt_setContractSuit(Suit.NOTRUMP);
				}
				m_gameGUI.changeFrame();
			}

		});

		if (Game.isTestMode())
		{

			enableAndDisableButtons();
		}
		return panel;

	}

	/**
	 * Depending on what hand it is, it only enables the button corresponding to the appropriate
	 * trump suit for bid
	 * 
	 * @throws AssertionError
	 */
	private void enableAndDisableButtons () throws AssertionError
	{

		if (m_handNum == 1)
		{

			// only heart can be the trump for the first hand
			m_spadesButton.setEnabled(false);
			m_diamondsButton.setEnabled(false);
			m_clubsButton.setEnabled(false);
			m_noTrumpButton.setEnabled(false);

		}
		else if (m_handNum == 2)
		{

			// only no trump can be the trump suit in second hand

			m_heartsButton.setEnabled(false);
			m_noTrumpButton.setEnabled(true);

		}
		else
		{

			throw new AssertionError("TrumpSuitGUI : There are only two hands");
		}
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

		m_gameGUI.undoButtonSetEnabled(true);
	}

}
