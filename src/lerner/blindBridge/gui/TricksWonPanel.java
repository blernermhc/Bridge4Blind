package lerner.blindBridge.gui;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener_sparse;
import lerner.blindBridge.model.Trick;

public class TricksWonPanel extends BridgeJPanel implements GameListener_sparse
{
	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	private static final Font	TRICK_FONT				= GameStatusGUI.STATUS_FONT.deriveFont(24f);

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	/** The screen component that displays the number of tricks won by East/West */
	private JLabel				m_eastWestTrickLabel		= new JLabel("0");

	/** The screen component that displays the number of tricks won by North/South */
	private JLabel				m_northSouthTrickLabel	= new JLabel("0");

	/** the Game object associated with this GUI */
	private Game					m_game;

	/** the GUI manager object */
	private GameGUI				m_gameGUI;
	
	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------

	public TricksWonPanel ()
	{
		JPanel eastWestPanel = new JPanel();
		JLabel ewLabel = new JLabel("East-West tricks:  ");
		ewLabel.setFont(TRICK_FONT);
		eastWestPanel.add(ewLabel);
		eastWestPanel.add(m_eastWestTrickLabel);
		m_eastWestTrickLabel.setFont(TRICK_FONT);

		JPanel northSouthPanel = new JPanel();
		JLabel nsLabel = new JLabel("North-South tricks:  ");
		nsLabel.setFont(TRICK_FONT);
		northSouthPanel.add(nsLabel);
		northSouthPanel.add(m_northSouthTrickLabel);
		m_northSouthTrickLabel.setFont(TRICK_FONT);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(northSouthPanel);
		add(eastWestPanel);
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.gui.BridgeJPanel#initialize(lerner.blindBridge.gui.GameGUI, lerner.blindBridge.main.Game)
	 */
	public void initialize ( GameGUI p_gameGUI, Game p_game )
	{
		m_game = p_game;
		m_gameGUI = p_gameGUI;
	}

	private void updateDisplay ()
	{
		// just get the numbers from the BridgeHand state
		m_northSouthTrickLabel.setText("" + m_game.getBridgeHand().getTricksTaken().getNumTricksWon(Direction.NORTH));
		m_eastWestTrickLabel.setText("" + m_game.getBridgeHand().getTricksTaken().getNumTricksWon(Direction.EAST));
		m_gameGUI.resetTimeOfLastDisplayChange();
	}
	//--------------------------------------------------
	// Game Event Signal Handlers
	//--------------------------------------------------

	@Override
	public void sig_gameReset ()
	{
		m_eastWestTrickLabel.setText("0");
		m_northSouthTrickLabel.setText("0");
	}

	@Override
	public void sig_trickWon ( Trick p_trick )
	{
		updateDisplay();
	}

	@Override
	public void sig_setNextPlayer ( Direction winner )
	{
		updateDisplay();
	}

	public static void main ( String[] args )
	{
		JFrame f = new JFrame();
		f.add(new TricksWonPanel(), BorderLayout.CENTER);
		f.pack();
		f.setVisible(true);
	}
}
