package lerner.blindBridge.gui;

import java.awt.Graphics;
import java.awt.event.ActionEvent;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Direction;

/**
 * The BidGUI allows the sighted players to specify which player made the winning bid.
 * 
 * @author Allison DeJordy
 * @version March 12, 2015
 * 
 **/

public class BidPositionGUI extends DirectionGUI
{

	/** the Game object associated with this GUI */
	private Game		m_game;

	/** the GUI manager object */
	private GameGUI	m_gameGUI;

	// for test mode only. For now, hand number can be 1 or 2.
	private int		m_handNum	= 1;

	/**
	 * Create the panel
	 * 
	 * @param m_game
	 *            the game being played
	 */
	public BidPositionGUI ( )
	{
		super("What position is the Bid Winner?");
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.gui.BridgeJPanel#initialize(lerner.blindBridge.gui.GameGUI, lerner.blindBridge.main.Game)
	 */
	public void initialize ( GameGUI p_gameGUI, Game p_game )
	{
		m_game = p_game;
		m_gameGUI = p_gameGUI;
		
		if (Game.isTestMode())
		{
			enableAndDisableButtons();
		}
	}
	

	/**
	 * Depending on what hand it is, it only enables the button corresponding to the appropriate bid
	 * position
	 * 
	 * @throws AssertionError
	 */
	private void enableAndDisableButtons () throws AssertionError
	{
		if (m_handNum == 1)
		{

			// the declarer should be south for hand 1. North is dummy. East
			// is blind.
			northButton.setEnabled(false);
			eastButton.setEnabled(false);
			westButton.setEnabled(false);

			southButton.setEnabled(true);

		}
		else if (m_handNum == 2)
		{

			// the declarer should be west. East is blind and dummy
			southButton.setEnabled(false);
			westButton.setEnabled(true);

		}
		else
		{

			throw new AssertionError("BidPositionGUI : There are only two hands");
		}
	}

	@Override
	public void actionPerformed ( ActionEvent e )
	{
		if (m_game != null)
		{
			Direction bidWinner = Direction.valueOf(e.getActionCommand().toUpperCase());
			m_game.getBridgeHand().evt_setContractWinner(bidWinner);
		}
		m_gameGUI.changeFrame(GameGUIs.BID_NUMBER_GUI);
	}

	/**
	 * 
	 * @param m_handNum
	 */
	public void setHandNum ( int p_handNum )
	{
		m_handNum = p_handNum;

		enableAndDisableButtons();
	}

	@Override
	public void paintComponent ( Graphics g )
	{
		super.paintComponent(g);
	}
	
	//--------------------------------------------------
	// Game Event Signal Handlers
	//--------------------------------------------------

}