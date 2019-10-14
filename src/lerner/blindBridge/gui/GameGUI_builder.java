// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import lerner.blindBridge.hardware.CommandController;
import lerner.blindBridge.main.Game;
import lerner.blindBridge.main.JTextAreaAppender;

/***********************************************************************
 * Methods for constructing the GUI frames
 ***********************************************************************/
public class GameGUI_builder
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(GameGUI_builder.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	/** Where debugging information shows up */
	private JTextArea			m_debugArea				= new JTextArea(10, 80);

	/** The main panel */
	private JPanel				m_mainPanel;
	
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------
	
	/** The collection of GUIs */
	private JPanel				m_cardPanel;
	
	/** The CardLayout controlling the display of GUIS */
	private CardLayout			m_layout;

	// buttons
	private JButton				m_consoleButton;

	private JButton				m_undoButton;

	private JButton				m_redoButton;

	private JButton				m_helpButton;

	private JButton				m_quitButton;
	
	private JPanel				m_consolePanel;
	
	private JSplitPane			m_splitPane;

	private GameGUI				m_gameGUI;
	
	private Game					m_game;
	
	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	public GameGUI_builder (GameGUI p_gameGUI, Game p_game)
	{
		m_game = p_game;
		m_gameGUI = p_gameGUI;
		
		createCards();

		// create the main panel
		JPanel gamePanel = new JPanel();
		gamePanel.setLayout(new BorderLayout());
		// gamePanel.add(createInfoPanel(), BorderLayout.NORTH);
		gamePanel.add(m_cardPanel, BorderLayout.CENTER);
		gamePanel.add(createButtonPanel(), BorderLayout.SOUTH);
		
		m_consolePanel = createConsolePanel();
		
		m_splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
		                                      gamePanel, m_consolePanel);
		m_splitPane.setOneTouchExpandable(true);
		m_splitPane.setDividerSize((int) (m_splitPane.getDividerSize() * 2.0));
		// Invoke restoreDefaults() to hide bottom initially 
		
		m_mainPanel = new JPanel(new BorderLayout());
		m_mainPanel.add(m_splitPane, BorderLayout.CENTER);

	}
	
    /***********************************************************************
     * Invoke after display is visible so we can set divider to initially hide console.
     ***********************************************************************/
    public void restoreDefaults(boolean p_open)
    {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run()
            {
            		double proportionalLocation = 0d;
            		if (! p_open) proportionalLocation = 0.97d;
            		m_splitPane.setDividerLocation(proportionalLocation);
            }
        });
    }

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------

	private JPanel createButtonPanel ()
	{
		//JPanel southPanel = new JPanel(new GridLayout(1, 0));
		JPanel southPanel = new JPanel(new FlowLayout());
		southPanel.add(createConsoleButtonPanel());
		southPanel.add(createUndoButtonPanel());
		southPanel.add(createRedoButtonPanel());
		southPanel.add(createHelpButtonPanel());
		southPanel.add(createQuitPanel());
		
		//JPanel bottomPanel = new JPanel(new GridLayout(0,1));
		//bottomPanel.add(southPanel);
		//bottomPanel.add(createConsolePanel());
		
		return southPanel;
	}

	private JPanel createConsolePanel()
	{
		m_consolePanel = new JPanel(new BorderLayout());

		JPanel outPanel = new JPanel();
		outPanel.setLayout(new BoxLayout(outPanel, BoxLayout.Y_AXIS));

		//----------------------------------------
		// Create debug output panel
		//----------------------------------------
		JTextArea debugOutputArea = new JTextArea(5,0); // 5 lines high here
		debugOutputArea.setLineWrap(true);
		debugOutputArea.setWrapStyleWord(true);
		debugOutputArea.setEditable (false);
		//jLoggingConsole.setFont(new Font("Courier", Font.PLAIN, 12));

		// Make scrollable console pane
		JScrollPane jConsoleScroll = new JScrollPane(debugOutputArea);
		jConsoleScroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

		// Subscribe the text area to JTextAreaAppender
		JTextAreaAppender.addLog4j2TextAreaAppender(debugOutputArea);

		outPanel.add(jConsoleScroll);

		//----------------------------------------
		// Create standard output panel
		//----------------------------------------
		JTextArea stdOutArea = new JTextArea(5,80);
		outPanel.add( new JScrollPane( stdOutArea ) );
		MessageConsole mc = new MessageConsole(stdOutArea);
		mc.redirectOut(null, System.out);
		mc.redirectErr(Color.RED, System.err);
		// mc.setMessageLines(100);

		m_consolePanel.add(outPanel,BorderLayout.CENTER);
		
		//----------------------------------------
		// Create standard input panel
		//----------------------------------------
		JTextField field = new JTextField();
		field.setUI(new HintTextFieldUI("Command", true));
		Action action = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String line = field.getText();
				System.out.println(CommandController.COMMAND_PROMPT + line);
				m_game.getCommandController().processCommand(line);
				field.setText("");
			}
		};
		field.addActionListener( action );
		m_consolePanel.add(field,BorderLayout.SOUTH);

		//m_consolePanel.setVisible(false);	// console button toggles visibility
		return m_consolePanel;
	}

	private void createCards ()
	{
		m_cardPanel = new JPanel();
		m_layout = new CardLayout();
		m_cardPanel.setLayout(m_layout);

		// add all the GUIs to the card panel
		GameGUIs gui;

		gui = GameGUIs.INITIALIZING_GUI;
		m_cardPanel.add(gui.getPanel(), gui.getName());

		gui = GameGUIs.SCANNING_BLIND_GUI;
		m_cardPanel.add(gui.getPanel(), gui.getName());

		gui = GameGUIs.BID_POSITION_GUI;
		m_cardPanel.add(gui.getPanel(), gui.getName());

		gui = GameGUIs.BID_NUMBER_GUI;
		m_cardPanel.add(gui.getPanel(), gui.getName());

		gui = GameGUIs.TRUMP_SUIT_GUI;
		m_cardPanel.add(gui.getPanel(), gui.getName());

		gui = GameGUIs.GAME_STATUS_GUI;
		m_cardPanel.add(gui.getPanel(), gui.getName());

		gui = GameGUIs.SCAN_DUMMY_GUI;
		m_cardPanel.add(gui.getPanel(), gui.getName());

		gui = GameGUIs.NEXT_HAND_GUI;
		m_cardPanel.add(gui.getPanel(), gui.getName());

		gui = GameGUIs.HELP_GUI;
		m_cardPanel.add(gui.getPanel(), gui.getName());
	}

	/**
	 * Creates a JPanel which displays debugging information.
	 * 
	 * @return a JPanel displaying the bid, current antenna and trick
	 */
	protected JPanel createInfoPanel ()
	{
		JPanel infoPanel = new JPanel(new GridLayout(1, 0));
		infoPanel.add(createBidPanel());
		infoPanel.add(createAntennaPanel());
		infoPanel.add(createTrickPanel());
		return infoPanel;
	}

	/**
	 * Creates the panel with the quit button
	 * 
	 * @return the panel created
	 */
	protected JPanel createQuitPanel ()
	{

		m_quitButton = GUIUtilities.createButton("Quit");
		m_quitButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent e )
			{

				// a pop-up that asks the user if the user is sure about closing
				// the window. If the user presses yes, it should quit the game
				// and close the SkyeTekReader window.

				// code from :
				// http://stackoverflow.com/questions/9093448/do-something-when-the-close-button-is-clicked-on-a-jframe

				if (JOptionPane
						.showConfirmDialog(	m_gameGUI,
											"Are you sure you want to close this window?",
											"Confirm Exit", JOptionPane.YES_NO_OPTION,
											JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
				{

					m_game.evt_exit();
				}

			}

		});

		JPanel quitPanel = GUIUtilities.packageButton(m_quitButton, FlowLayout.CENTER);
		return quitPanel;

	}

	/**
	 * Creates a JPanel which displays the current bid.
	 * 
	 * @return a JPanel with a JLabel that displays the bid
	 */
	protected JPanel createBidPanel ()
	{

		JPanel bidPanel = new JPanel(new FlowLayout());
		// JLabel bid = new JLabel("Bid: ");
		// bid.setFont(INFO_FONT);
		// bidPanel.add(bid);
		// bidLabel = new JLabel("N/A");
		// bidLabel.setFont(INFO_FONT);
		// bidPanel.add(bidLabel);
		return bidPanel;

	}

	/**
	 * Creates a JPanel displaying the current antenna.
	 * 
	 * @return a JPanel with a JLabel displaying the current antenna
	 */
	protected JPanel createAntennaPanel ()
	{

		JPanel antennaPanel = new JPanel(new FlowLayout());
		// JLabel antenna = new JLabel("Next player: ");
		// antenna.setFont(INFO_FONT);
		// antennaPanel.add(antenna);
		// antennaLabel = new JLabel("N/A");
		// antennaLabel.setFont(INFO_FONT);
		// antennaPanel.add(antennaLabel);
		return antennaPanel;

	}

	/**
	 * Creates a JPanel which displays the current trick.
	 * 
	 * @return a JPanel with a JLabel displaying the current trick
	 */
	protected JPanel createTrickPanel ()
	{

		JPanel trickPanel = new JPanel(new FlowLayout());
		// JLabel trick = new JLabel("Trick: ");
		// trick.setFont(INFO_FONT);
		// trickPanel.add(trick);
		// trickLabel = new JLabel("N/A");
		// trickLabel.setFont(INFO_FONT);
		// trickPanel.add(trickLabel);
		return trickPanel;

	}

	/**
	 * Creates the "Undo" button panel at the bottom of the screen.
	 * Action invokes hand's undo event.
	 * GUI updates occur when changes are signaled as game events (e.g., sig_setContract_undo).
	 * @return A JPanel containing a single, right-oriented "Undo" button.
	 */
	protected JPanel createUndoButtonPanel ()
	{

		// create a new JPanel with a FlowLayout
		m_undoButton = GUIUtilities.createButton("Undo");
		m_undoButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent evt )
			{
				m_game.getBridgeHand().evt_undo(true);
			}

		});

		return GUIUtilities.packageButton(m_undoButton, FlowLayout.RIGHT);
		// panel.add(GUIUtilities.packageButton(resetButton, FlowLayout.RIGHT));
		// return panel;

	}

	/**
	 * Creates the "Redo" button panel at the bottom of the screen.
	 * Action invokes hand's redo event.
	 * GUI updates occur when changes are signaled as game events (e.g., sig_setContract_undo).
	 * 
	 * @return A JPanel containing a single, right-oriented "Redo" button.
	 */
	protected JPanel createRedoButtonPanel ()
	{

		// create a new JPanel with a FlowLayout
		m_redoButton = GUIUtilities.createButton("Redo");
		m_redoButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent evt )
			{
				m_game.getBridgeHand().evt_redo(true);
			}

		});

		return GUIUtilities.packageButton(m_redoButton, FlowLayout.RIGHT);
		// panel.add(GUIUtilities.packageButton(resetButton, FlowLayout.RIGHT));
		// return panel;

	}

	/**
	 * Creates the "help" button panel at the bottom of the screen.
	 * 
	 * @return A JPanel containing a single, center-oriented "help" button.
	 */
	protected JPanel createHelpButtonPanel ()
	{
		m_helpButton = GUIUtilities.createButton("Help");
		m_helpButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent evt )
			{
				m_gameGUI.showHelp();
			}

		});

		return GUIUtilities.packageButton(m_helpButton, FlowLayout.CENTER);
	}

	/**
	 * Creates the "console" button panel at the bottom of the screen.
	 * 
	 * @return A JPanel containing a single, left-oriented "Console" button.
	 */
	protected JPanel createConsoleButtonPanel ()
	{
		m_consoleButton = GUIUtilities.createButton("Console");
		m_consoleButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed ( ActionEvent evt )
			{
				restoreDefaults(true);  // open console
			}

		});

		return GUIUtilities.packageButton(m_consoleButton, FlowLayout.CENTER);
	}

	/***********************************************************************
	 * Flips to the component of this layout with the name specified
	 * in the GameGUIs enum value. If no such component exists, then nothing happens.
	 * @param p_gui the GUI to show
	 ***********************************************************************/
	public void show ( GameGUIs p_gui )
	{
		// IMPORTANT: do not generate log4j messages here.
		// This will cause some sort of loop in JTextAreaAppender
		m_layout.show(m_cardPanel, p_gui.getName());
	}
	
	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * Where debugging information shows up
	 * @return the debug area
	 ***********************************************************************/
	public JTextArea getDebugArea ()
	{
		return m_debugArea;
	}

	/***********************************************************************
	 * The main panel
	 * @return The main panel
	 ***********************************************************************/
	public JPanel getMainPanel ()
	{
		return m_mainPanel;
	}

}
