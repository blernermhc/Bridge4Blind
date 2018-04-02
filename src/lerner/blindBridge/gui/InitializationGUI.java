package lerner.blindBridge.gui;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.BoxLayout;
import javax.swing.JLabel;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.GameListener_sparse;

public class InitializationGUI extends BridgeJPanel implements GameListener_sparse
{
	public InitializationGUI ( )
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		addInstructions("Please wait while the system hardware is set up.");
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.gui.BridgeJPanel#initialize(lerner.blindBridge.gui.GameGUI, lerner.blindBridge.main.Game)
	 */
	public void initialize ( GameGUI p_gameGUI, Game p_game )
	{
		// m_game = p_game;
		// m_gameGUI = p_gameGUI;
		p_game.addGameListener(this);

	}
	
	private void addInstructions ( String text )
	{
		JLabel instructions = new JLabel(text);
		instructions.setFont(GameGUI.INFO_FONT);
		instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(instructions);
	}

	@Override
	public void paintComponent ( Graphics g )
	{
		super.paintComponent(g);
	}

}
