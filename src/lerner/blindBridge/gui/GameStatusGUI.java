package lerner.blindBridge.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Contract;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener_sparse;
import lerner.blindBridge.model.Rank;
import lerner.blindBridge.model.Suit;

/**
 * This class visually represents what the visually impaired person hears.
 * 
 * @author Barbara Lerner, Humaira Orchee
 * @version March 12, 2015
 */
public class GameStatusGUI extends JPanel implements GameListener_sparse
{

	public static Font			STATUS_FONT			= new Font("Helvetica", Font.BOLD, 60);

	private PlayerStatusGUI[]	m_playerGUIs			= new PlayerStatusGUI[4];

	private BidStatusGUI			m_bidGUI				= new BidStatusGUI();

	// private int count = 0 ;

	// keeps track of the current player
	private int					m_currentPlayer		= 0;

	// true if first card has been played already. Otherwise false. It starts
	// out with false because initially, first card has not been played. In
	// cardPlayed, after first card for each hand is played, it is set to true.
	// This needs to be reset to false each time a new hand is started.
	private boolean				m_firstCardPlayed		= false;

	private Game					m_game;

	private GameGUI				m_gameGUI;

	private boolean				m_trickOverHandled	= true;

	public GameStatusGUI ( GameGUI p_gameGUI, Game p_game )
	{

		m_game = p_game;
		m_gameGUI = p_gameGUI;

		if (m_game != null)
		{
			m_game.addGameListener(this);
		}

		setLayout(new GridBagLayout());

		GridBagConstraints northConstraints = new GridBagConstraints();
		northConstraints.gridx = 1;
		// northConstraints.gridy = 0;
		northConstraints.gridy = 1;
		northConstraints.anchor = GridBagConstraints.NORTH;
		// northConstraints.weightx = 1;
		northConstraints.weighty = 1;
		m_playerGUIs[Direction.NORTH.ordinal()] = new PlayerStatusGUI(Direction.NORTH);
		add(m_playerGUIs[Direction.NORTH.ordinal()], northConstraints);
		addDirLabels(Direction.NORTH, 1, 0, GridBagConstraints.PAGE_START);

		GridBagConstraints eastConstraints = new GridBagConstraints();
		eastConstraints.gridx = 2;
		eastConstraints.gridy = 2;
		eastConstraints.anchor = GridBagConstraints.LINE_START;
		eastConstraints.weightx = 1;
		m_playerGUIs[Direction.EAST.ordinal()] = new PlayerStatusGUI(Direction.EAST);
		add(m_playerGUIs[Direction.EAST.ordinal()], eastConstraints);
		addDirLabels(Direction.EAST, 2, 2, GridBagConstraints.LINE_END);

		GridBagConstraints southConstraints = new GridBagConstraints();
		southConstraints.gridx = 1;
		// southConstraints.gridy = 2;
		southConstraints.gridy = 3;
		southConstraints.anchor = GridBagConstraints.SOUTH;
		// southConstraints.weightx = 1;
		southConstraints.weighty = 1;
		m_playerGUIs[Direction.SOUTH.ordinal()] = new PlayerStatusGUI(Direction.SOUTH);
		add(m_playerGUIs[Direction.SOUTH.ordinal()], southConstraints);
		addDirLabels(Direction.SOUTH, 1, 4, GridBagConstraints.PAGE_END);

		GridBagConstraints westConstraints = new GridBagConstraints();
		westConstraints.gridx = 0;
		westConstraints.gridy = 2;
		westConstraints.anchor = GridBagConstraints.LINE_END;
		westConstraints.weightx = 1;
		m_playerGUIs[Direction.WEST.ordinal()] = new PlayerStatusGUI(Direction.WEST);
		add(m_playerGUIs[Direction.WEST.ordinal()], westConstraints);
		addDirLabels(Direction.WEST, 0, 2, GridBagConstraints.LINE_START);

		GridBagConstraints bidConstraints = new GridBagConstraints();
		bidConstraints.gridx = 1;
		bidConstraints.gridy = 2;
		bidConstraints.fill = GridBagConstraints.BOTH;
		add(m_bidGUI, bidConstraints);

		GridBagConstraints trickConstraints = new GridBagConstraints();
		trickConstraints.gridx = 2;
		trickConstraints.gridy = 1;
		TricksWonPanel tricksWonPanel = new TricksWonPanel();

		if (m_game != null)
		{
			m_game.addGameListener(tricksWonPanel);
		}
		add(tricksWonPanel, trickConstraints);
	}

	@Override
	public void sig_gameReset ()
	{

		//
		// //count++ ;
		//
		// //System.out.println("GameStatusGui gameReset " + count);
		//
		// bidGUI.clear();
		//
		// firstCardPlayed = false;
		// trickOverHandled = true;
		// currentPlayer = 0;

	}

	@Override
	public void sig_cardPlayed ( Direction turn, Card card )
	{

		// every time a card is played, current player is updated.
		m_currentPlayer++;

		System.out.println("game status gui card played: " + turn);

		System.out.println("current player " + m_currentPlayer);

		// playerGUIs[turn.ordinal()].cardPlayed(card);

		// the first player of the next hand
		if ((m_currentPlayer % 4) == 1)
		{

			synchronized (GameStatusGUI.this)
			{

				try
				{

					while (!m_trickOverHandled)
					{

						System.out.println("waiting for trickOverHandled");

						GameStatusGUI.this.wait();
					}

				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				m_trickOverHandled = false;
			}

		}

		m_playerGUIs[turn.ordinal()].cardPlayed(card);

		// if the current player is the last person to play for the trick, then
		// next player should not be determined here. Next player should be
		// determined by trickWon()
		if ((m_currentPlayer % 4) != 0)
		{

			System.out.println("not last player so choosing next player from cardPlayed()");

			m_playerGUIs[(turn.ordinal() + 1) % 4].nextPlayer();

			if (m_gameGUI != null)
			{
				m_gameGUI.undoButtonSetEnabled(true);
			}

		}
		else
		{

			m_gameGUI.undoButtonSetEnabled(false);
		}

		// the frame should be changed to ScanDummyGUI only after the first card
		// has been played
		if (!m_firstCardPlayed && m_game != null)
		{

			switchToScanDummy();
		}
	}

	/**
	 * 
	 */
	private void switchToScanDummy ()
	{
		// gameGUI.debugMsg("Switching to scan dummy screen");

		m_firstCardPlayed = true;

		// if blind player is NOT dummy player, then change frame to
		// SCAN_DUMMY_GUI

		Direction dummyPostion = m_game.getBridgeHand().getDummyPosition();
		boolean dummyIsSighted = (m_game.getKeyboardControllers().get(dummyPostion) == null);
		if (dummyIsSighted)
		{
			// Do the task - changeFrame - after 2 seconds has passed.
			// without this, the screen changes to ScanDummyGUI too fast and
			// the first card is not shown
			try        
			{
			    Thread.sleep(2000);
			} 
			catch(InterruptedException ex) 
			{
			    // Thread.currentThread().interrupt();
			}
			
			m_gameGUI.setSwitchFromGameStatusGUI(GameGUI.SWITCH_TO_SCAN_DUMMY);
			m_gameGUI.changeFrame();
		}
	}

	@Override
	public void sig_trickWon ( final Direction winner )
	{
		// wait 4 seconds
		try        
		{
		    Thread.sleep(4000);
		} 
		catch(InterruptedException ex) 
		{
		    // Thread.currentThread().interrupt();
		}

		System.out.println("game status gui trickWon " + winner);


		for (PlayerStatusGUI playerGUI : m_playerGUIs)
		{

			playerGUI.trickOver();

		}

		System.out.println("back to game status gui trichWon run");

		// decides if the hand has ended or not
		if (m_currentPlayer != 13 * 4)
		{

			System.out.println("hand has not ended");

			System.out.println("currentplayer " + m_currentPlayer);

			m_playerGUIs[winner.ordinal()].nextPlayer();

		}
		else
		{

			System.out.println("hand has ended");

			System.out.println("currentplayer " + m_currentPlayer);

			// hand has ended

			m_gameGUI.setSwitchFromGameStatusGUI(GameGUI.SWITCH_TO_NEXT_HAND);

			// TODO : might need to call gameReset() here as well
			// gameReset();

			m_gameGUI.changeFrame();
		}

		synchronized (GameStatusGUI.this)
		{

			// System.out.println("trickOverHandled before changing value "
			// + trickOverHandled);

			m_trickOverHandled = true;

			// System.out.println("trickOverHandled after changing value "
			// + trickOverHandled);

			GameStatusGUI.this.notify();

			// System.out.println("notified in trickWon");
		}

	}

	@Override
	public void sig_contractSet ( Contract contract )
	{
		m_bidGUI.setBid(contract);

		for (int i = 0; i < m_playerGUIs.length; i++)
		{

			m_playerGUIs[i].clear();
		}

		// highlight the first player of each hand
		m_playerGUIs[contract.getBidWinner().getNextDirection().ordinal()].nextPlayer();
	}

	public void setFirstCardPlayed ( boolean p_firstCardPlayed )
	{
		this.m_firstCardPlayed = p_firstCardPlayed;
	}

	/**
	 * Adds direction labels to the PlayerStatusGUIs
	 * 
	 * @param dir
	 *            The direction of a PlayerStatusGUI
	 * @param gridX
	 *            The gridx number for GridBagConstraints
	 * @param gridY
	 *            The gridy number for GridBagConstraints
	 * @param anchor
	 *            The anchor number for GridBagConstraints
	 */
	private void addDirLabels ( Direction dir, int gridX, int gridY, int anchor )
	{

		GridBagConstraints dirLabelConstraint = new GridBagConstraints();
		dirLabelConstraint.gridx = gridX;
		dirLabelConstraint.gridy = gridY;
		dirLabelConstraint.anchor = anchor;

		add(new JLabel(dir.toString()), dirLabelConstraint);
	}

	/**
	 * 
	 * @param currentPlayerIndex
	 *            The index of the player whose GUI will be cleared
	 * @param nextPlayerIndex
	 *            The index of the next player before undo was pressed
	 */
	public void undoCardPlayed ( int currentPlayerIndex, int nextPlayerIndex )
	{

		System.out.println("Game status gui undo");

		m_currentPlayer--;

		assert m_currentPlayer >= 0;

		System.out.println("current player " + m_currentPlayer);

		// the first player of the next hand has not played the card yet
		if ((m_currentPlayer % 4) == 0)
		{

			System.out.println("first player of trick yet to play card");

			m_trickOverHandled = true;

		}

		System.out.println("now trickOverhandled " + m_trickOverHandled);

		for (int i = 0; i < m_playerGUIs.length; i++)
		{

			m_playerGUIs[i].setTrickOver(true);
		}

		/* rick: this is not implemented
		if (Game.isTestMode())
		{

			if (nextPlayerIndex != -1 && m_game.getBgetBlindPosition().ordinal() == nextPlayerIndex)
			{

				TestAntennaHandler.undo();
			}
		}
		*/

		m_playerGUIs[currentPlayerIndex].undo();

		// -1 means there was no previous player
		if (nextPlayerIndex != -1)
		{
			m_playerGUIs[nextPlayerIndex].setBorder(PlayerStatusGUI.getPlayerBorder());
		}

		repaint();
	}

	@Override
	public void paintComponent ( Graphics g )
	{

		super.paintComponent(g);

		/* rick: I think undo works across hands
		if (m_game != null && m_game.isGameState(GameState.FIRSTCARD))
		{

			m_gameGUI.undoButtonSetEnabled(false);
			m_gameGUI.backButtonSetEnabled(false);

		}
		*/
	}

	public static void main ( String[] args )
	{
		JFrame testFrame = new JFrame();
		GameStatusGUI gameStatusGUI = new GameStatusGUI(null, null);
		testFrame.add(gameStatusGUI, BorderLayout.CENTER);
		gameStatusGUI.sig_cardPlayed(Direction.SOUTH, new Card(Rank.ACE, Suit.CLUBS));
		gameStatusGUI.sig_cardPlayed(Direction.WEST, new Card(Rank.TEN, Suit.CLUBS));
		testFrame.pack();
		testFrame.setVisible(true);
	}

}
