package lerner.blindBridge.gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.log4j.Category;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.CardPlay;
import lerner.blindBridge.model.Contract;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener_sparse;
import lerner.blindBridge.model.Rank;
import lerner.blindBridge.model.Suit;
import lerner.blindBridge.model.Trick;

/**
 * This class visually represents what the visually impaired person hears.
 * 
 * @author Barbara Lerner, Humaira Orchee
 * @version March 12, 2015
 */
public class GameStatusGUI extends BridgeJPanel implements GameListener_sparse
{

	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(GameStatusGUI.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	public static Font			STATUS_FONT			= new Font("Helvetica", Font.BOLD, 60);

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	/** the screen components for each player */
	private Map<Direction,PlayerStatusGUI>	m_playerGUIs			= new HashMap<>();

	/** the screen component showing the current contract */
	private BidStatusGUI						m_bidGUI				= new BidStatusGUI();

	/** the screen component showing the tricks taken by each team */
	private TricksWonPanel 					m_tricksWonPanel;
	
	/** the screen component showing the contents of the dummy's hand */
	private DummyHandPanel					m_dummyHandPanel;
	
	// private int count = 0 ;

	// keeps track of the current player
	// private int					m_currentPlayer		= 0;

	// true if first card has been played already. Otherwise false. It starts
	// out with false because initially, first card has not been played. In
	// cardPlayed, after first card for each hand is played, it is set to true.
	// This needs to be reset to false each time a new hand is started.
	// private boolean				m_firstCardPlayed		= false;

	/** the Game object associated with this GUI */
	private Game					m_game;

	/** the GUI manager object */
	private GameGUI				m_gameGUI;


	// private boolean				m_trickOverHandled	= true;

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------

	/***********************************************************************
	 * Creates the GUI
	 ***********************************************************************/
	public GameStatusGUI ( )
	{
		PlayerStatusGUI playerGUI;
		Direction direction;
		
		setLayout(new GridBagLayout());

		GridBagConstraints northConstraints = new GridBagConstraints();
		northConstraints.gridx = 1;
		// northConstraints.gridy = 0;
		northConstraints.gridy = 1;
		northConstraints.anchor = GridBagConstraints.NORTH;
		// northConstraints.weightx = 1;
		northConstraints.weighty = 1;
		// add GUI
		direction = Direction.NORTH;
		playerGUI =  new PlayerStatusGUI(direction);
		m_playerGUIs.put(direction, playerGUI);
		add(playerGUI, northConstraints);

		GridBagConstraints eastConstraints = new GridBagConstraints();
		eastConstraints.gridx = 2;
		eastConstraints.gridy = 2;
		eastConstraints.anchor = GridBagConstraints.LINE_START;
		eastConstraints.weightx = 1;
		// add GUI
		direction = Direction.EAST;
		playerGUI =  new PlayerStatusGUI(direction);
		m_playerGUIs.put(direction, playerGUI);
		add(playerGUI, eastConstraints);

		GridBagConstraints southConstraints = new GridBagConstraints();
		southConstraints.gridx = 1;
		// southConstraints.gridy = 2;
		southConstraints.gridy = 3;
		southConstraints.anchor = GridBagConstraints.SOUTH;
		// southConstraints.weightx = 1;
		southConstraints.weighty = 1;
		// add GUI
		direction = Direction.SOUTH;
		playerGUI =  new PlayerStatusGUI(direction);
		m_playerGUIs.put(direction, playerGUI);
		add(playerGUI, southConstraints);

		GridBagConstraints westConstraints = new GridBagConstraints();
		westConstraints.gridx = 0;
		westConstraints.gridy = 2;
		westConstraints.anchor = GridBagConstraints.LINE_END;
		westConstraints.weightx = 1;
		// add GUI
		direction = Direction.WEST;
		playerGUI =  new PlayerStatusGUI(direction);
		m_playerGUIs.put(direction, playerGUI);
		add(playerGUI, westConstraints);

		GridBagConstraints bidConstraints = new GridBagConstraints();
		bidConstraints.gridx = 1;
		bidConstraints.gridy = 2;
		bidConstraints.fill = GridBagConstraints.BOTH;
		add(m_bidGUI, bidConstraints);

		GridBagConstraints trickConstraints = new GridBagConstraints();
		trickConstraints.gridx = 2;
		trickConstraints.gridy = 1;
		m_tricksWonPanel = new TricksWonPanel();
		add(m_tricksWonPanel, trickConstraints);

		m_dummyHandPanel = new DummyHandPanel(this);
	
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.gui.BridgeJPanel#initialize(lerner.blindBridge.gui.GameGUI, lerner.blindBridge.main.Game)
	 */
	public void initialize ( GameGUI p_gameGUI, Game p_game )
	{
		m_game = p_game;
		m_gameGUI = p_gameGUI;

		m_tricksWonPanel.initialize (p_gameGUI, p_game);
		m_dummyHandPanel.initialize(p_gameGUI, p_game);
		
		m_game.addGameListener(this);
		m_game.addGameListener(m_tricksWonPanel);
		m_game.addGameListener(m_dummyHandPanel);
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------

	/***********************************************************************
	 * Redraws the display using the current state of play
	 ***********************************************************************/
	private void updateDisplay ()
	{
		Contract		curContract	= m_game.getBridgeHand().getContract();
		Trick		curTrick		= m_game.getBridgeHand().getCurrentTrick();
		Direction	nextPlayer	= m_game.getBridgeHand().getNextPlayer();
		
		//------------------------------
		// update player GUIs
		//------------------------------
		
		Map<Direction, Card> cardsPlayed = new HashMap<>();
		for (CardPlay cardPlay : curTrick.getCardsPlayed())
		{
			cardsPlayed.put(cardPlay.getPlayer(), cardPlay.getCard());
		}
		
		for (Direction direction : Direction.values())
		{
			PlayerStatusGUI playerGUI = m_playerGUIs.get(direction);
			
			playerGUI.setState(cardsPlayed.get(direction), (nextPlayer == direction));
		}
		
		//------------------------------
		// update current contract
		//------------------------------
		
		m_bidGUI.setBid(curContract);
		
		m_gameGUI.resetTimeOfLastDisplayChange();
	}
	
	//--------------------------------------------------
	// Game Event Signal Handlers
	//--------------------------------------------------

	/***********************************************************************
	 * Indicates that the card in the dummy's hand has been scanned.
	 ***********************************************************************/
	@Override
	public void sig_setNextPlayer ( Direction p_direction )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_setNextPlayer: entered. direction: " + p_direction);

		updateDisplay();
	}

	/***********************************************************************
	 * Indicates that the card in the dummy's hand has been scanned.
	 ***********************************************************************/
	@Override
	public void sig_cardPlayed ( Direction turn, Card card )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_cardPlayed: entered");

		updateDisplay();
	}
	public void addDummyPanel(Direction p_direction) {
		GridBagConstraints dummyConstraints = new GridBagConstraints();
		
		switch (p_direction) {
		case NORTH:
			dummyConstraints.gridx = 0;
			dummyConstraints.gridy = 1;
			break;
			
		case EAST:
			dummyConstraints.gridx = 2;
			dummyConstraints.gridy = 3;
			break;
			
		case SOUTH:
			dummyConstraints.gridx = 2;
			dummyConstraints.gridy = 3;
			break;
			
		case WEST:
			dummyConstraints.gridx = 0;
			dummyConstraints.gridy = 1;
			break;
		}
		
		add(m_dummyHandPanel, dummyConstraints);
	}

	public static void main (String[] args) {
		JFrame f = new JFrame("Game Status Test");
		GameStatusGUI gamePanel = new GameStatusGUI();
		Direction dir = Direction.SOUTH;
		gamePanel.m_dummyHandPanel.sig_setDummyPosition(dir);
		gamePanel.addDummyPanel (dir);
		gamePanel.m_dummyHandPanel.sig_cardScanned(dir, new Card(Rank.KING, Suit.CLUBS), false);
		gamePanel.m_dummyHandPanel.sig_cardScanned(dir, new Card(Rank.QUEEN, Suit.CLUBS), false);
		gamePanel.m_dummyHandPanel.sig_cardPlayed(dir, new Card(Rank.KING, Suit.CLUBS));
		f.add(gamePanel);
		f.pack();
		f.setVisible(true);
	}

}
