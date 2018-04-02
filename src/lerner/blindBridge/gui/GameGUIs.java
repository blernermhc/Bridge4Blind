// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gui;

import javax.swing.JPanel;

import lerner.blindBridge.main.Game;

/***********************************************************************
 * Enumerates the GUIs that are part of the Bridge Game GUI.
 * Each GUI must have a name.  Actual GUI components should also
 * have a JPanel (actually a BridgeJPanel) associated with them.
 * Some method (typically the GameGUI constructor) should call
 * the static initialize methods to complete the configuration. 
 ***********************************************************************/
public enum GameGUIs
{
	INITIALIZING_GUI   		("initializingGUI",		0000, new InitializationGUI())
	, SCANNING_BLIND_GUI		("scanningGUI",			0000, new ScanningBlindGUI())
	, BID_POSITION_GUI		("bidPosGUI",			0000, new BidPositionGUI())
	, BID_NUMBER_GUI			("bidNumGUI",			0000, new BidNumberGUI())
	, TRUMP_SUIT_GUI			("trumpSuitGUI",			0000, new BidSuitGUI())
	, NEXT_HAND_GUI			("nextHandGUI",			0000, new NextHandGUI())
	, SCAN_DUMMY_GUI			("scanDummyGUI",			1000, new ScanDummyGUI())
	, GAME_STATUS_GUI		("gameStatusGUI",		3000, new GameStatusGUI())
	, HELP_GUI				("help",					0000, new HelpGUI())
	;

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(GameGUIs.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------

	/** Name of the GUI panel */
	String			m_name;
	
	/** Minimum amount of time to display this GUI, before switching to a different GUI */
	int				m_minDisplayTimeMillis;

	/** The GUI panel */
	BridgeJPanel		m_panel;
	
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Associates various information with each enumerated GUI.
	 * @param p_name						GUI's name
	 * @param p_minDisplayTimeMillis		Min time to display before switching to a different GUI
	 * @param p_panel					The JPanel implementing the GUI
	 ***********************************************************************/
	private GameGUIs ( String p_name, int p_minDisplayTimeMillis, BridgeJPanel p_panel )
	{
		m_name = p_name;
		m_minDisplayTimeMillis = p_minDisplayTimeMillis;
		m_panel = p_panel;
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Adds GameGUI and Game references to the JPanel objects associated with
	 * each of the enumeration values.
	 * @param p_gameGUI	the main GUI object
	 * @param p_game		the main Game object
	 ***********************************************************************/
	public static void initialize ( GameGUI p_gameGUI, Game p_game )
	{
		for (GameGUIs gui : GameGUIs.values())
		{
			if (gui.m_panel != null) gui.m_panel.initialize(p_gameGUI, p_game);
		}
	}

	/***********************************************************************
	 * Returns the next GUI in order.  Returns null, if this is the last GUI.
	 * @return the next GUI
	 ***********************************************************************/
	public GameGUIs nextGUI ()
	{
		int nextGUI_ordinal = this.ordinal() + 1;
		if (nextGUI_ordinal >= GameGUIs.values().length) return null;
		return GameGUIs.values()[nextGUI_ordinal];
	}

	
	/***********************************************************************
	 * Returns the previous GUI in order.  Returns null, if this is the first GUI.
	 * @return the previous GUI
	 ***********************************************************************/
	public GameGUIs prevGUI ()
	{
		int prevGUI_ordinal = this.ordinal() - 1;
		if (prevGUI_ordinal < 0) return null;
		return GameGUIs.values()[prevGUI_ordinal];
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	public String toString()
	{
		return m_name;
	}

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * Name of the GUI panel.
	 * @return name
	 ***********************************************************************/
	public String getName ()
	{
		return m_name;
	}

	/***********************************************************************
	 * The GUI panel
	 * @return the panel
	 ***********************************************************************/
	public JPanel getPanel ()
	{
		return m_panel;
	}

	/***********************************************************************
	 * Minimum amount of time to display this GUI, before switching to a different GUI.
	 * ChangeFrame(GUI) enforces these minimum display times.
	 * @return time in milliseconds
	 ***********************************************************************/
	public int getMinDisplayTimeMillis ()
	{
		return m_minDisplayTimeMillis;
	}
	
}
