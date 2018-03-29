package lerner.blindBridge.gui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener_sparse;

public class ScanningBlindGUI extends JPanel implements GameListener_sparse
{
	private GameGUI m_gameGUI;

	public ScanningBlindGUI ( GameGUI p_gameGUI, Game p_game )
	{
		m_gameGUI = p_gameGUI;
		p_game.addGameListener(this);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		addInstructions("Please scan in the blind player's cards.");
		addInstructions("Use BACK button to change position of blind player.");
	}

	private void addInstructions ( String text )
	{
		JLabel instructions = new JLabel(text);
		instructions.setFont(GameGUI.INFO_FONT);
		instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(instructions);
	}

	@Override
	public void sig_cardScanned ( Direction p_direction, Card p_card, boolean p_handComplete )
	{
	}

	@Override
	public void sig_blindHandsScanned ()
	{
		m_gameGUI.changeFrame();
	}

	@Override
	public void paintComponent ( Graphics g )
	{

		super.paintComponent(g);

		m_gameGUI.undoButtonSetEnabled(true);
		m_gameGUI.backButtonSetEnabled(true);

	}

}
