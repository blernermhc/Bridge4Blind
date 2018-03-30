// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gui;

import javax.swing.JPanel;

import lerner.blindBridge.main.Game;

/***********************************************************************
 * Adds initialization method used by GameStatusGUIs enumeration,
 * since we cannot pass these parameters during Java class loading.
 ***********************************************************************/
public abstract class BridgeJPanel extends JPanel
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(BridgeJPanel.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Adds GameGUI and Game references to the JPanel
	 * @param p_gameGUI	the main GUI object
	 * @param p_game		the main Game object
	 ***********************************************************************/
	public abstract void initialize ( GameGUI p_gameGUI, Game p_game );

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

}
