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
	INITIALIZING_GUI   		("initializingGUI",		new InitializationGUI())
	, SCANNING_BLIND_GUI		("scanningGUI",			new ScanningBlindGUI())
	, BID_POSITION_GUI		("bidPosGUI",			new BidPositionGUI())
	, BID_NUMBER_GUI			("bidNumGUI",			new BidNumberGUI())
	, TRUMP_SUIT_GUI			("trumpSuitGUI",			new BidSuitGUI())
	, NEXT_HAND_GUI			("nextHandGUI",			new NextHandGUI())
	, SCAN_DUMMY_GUI			("scanDummyGUI",			new ScanDummyGUI())
	, GAME_STATUS_GUI		("gameStatusGUI",		new GameStatusGUI())
	, HELP_GUI				("help",					new HelpGUI())
	, SWITCH_TO_SCAN_DUMMY	("switchToScanHand")
	, SWITCH_TO_NEXT_HAND	("switchToNextHand")
	, NONE					("none")
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

	/** The GUI panel */
	BridgeJPanel		m_panel;
	
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	private GameGUIs ( String p_name )
	{
		m_name = p_name;
		m_panel = null;
	}

	private GameGUIs ( String p_name, BridgeJPanel p_panel )
	{
		m_name = p_name;
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
	
}
