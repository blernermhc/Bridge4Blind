package lerner.blindBridge.gui;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener_sparse;

public class TricksWonPanel extends JPanel implements GameListener_sparse
{
	private static final Font	TRICK_FONT				= GameStatusGUI.STATUS_FONT.deriveFont(24f);

	private int					m_eastWestTricks			= 0;

	private int					m_northSouthTricks		= 0;

	private JLabel				m_eastWestTrickLabel		= new JLabel("0");

	private JLabel				m_northSouthTrickLabel	= new JLabel("0");

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

	@Override
	public void sig_gameReset ()
	{
		m_eastWestTricks = 0;
		m_northSouthTricks = 0;
		m_eastWestTrickLabel.setText("0");
		m_northSouthTrickLabel.setText("0");
	}

	@Override
	public void sig_trickWon ( Direction winner )
	{
		if (winner == Direction.EAST || winner == Direction.WEST)
		{
			m_eastWestTricks++;
			m_eastWestTrickLabel.setText("" + m_eastWestTricks);
		}
		else
		{
			m_northSouthTricks++;
			m_northSouthTrickLabel.setText("" + m_northSouthTricks);
		}
	}

	public static void main ( String[] args )
	{
		JFrame f = new JFrame();
		f.add(new TricksWonPanel(), BorderLayout.CENTER);
		f.pack();
		f.setVisible(true);
	}
}
