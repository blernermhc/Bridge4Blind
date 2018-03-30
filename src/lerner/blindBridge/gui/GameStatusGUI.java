package lerner.blindBridge.gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;

import org.apache.log4j.Category;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.CardPlay;
import lerner.blindBridge.model.Contract;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener_sparse;
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
	private static Category s_cat = Category.getInstance(GameGUI.class.getName());

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

	//private GameGUI				m_gameGUI;

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
		addDirLabels(direction, 1, 0, GridBagConstraints.PAGE_START);

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
		addDirLabels(direction, 2, 2, GridBagConstraints.LINE_END);

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
		addDirLabels(direction, 1, 4, GridBagConstraints.PAGE_END);

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
		addDirLabels(direction, 0, 2, GridBagConstraints.LINE_START);

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
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.gui.BridgeJPanel#initialize(lerner.blindBridge.gui.GameGUI, lerner.blindBridge.main.Game)
	 */
	public void initialize ( GameGUI p_gameGUI, Game p_game )
	{
		m_game = p_game;
		// m_gameGUI = p_gameGUI;

		m_tricksWonPanel.initialize (p_gameGUI, p_game);
		
		m_game.addGameListener(this);
		m_game.addGameListener(m_tricksWonPanel);
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
	}
	
	/***********************************************************************
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
	 ***********************************************************************/
	private void addDirLabels ( Direction dir, int gridX, int gridY, int anchor )
	{

		GridBagConstraints dirLabelConstraint = new GridBagConstraints();
		dirLabelConstraint.gridx = gridX;
		dirLabelConstraint.gridy = gridY;
		dirLabelConstraint.anchor = anchor;

		add(new JLabel(dir.toString()), dirLabelConstraint);
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
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_setNextPlayer: entered");

		updateDisplay();
	}

	/***********************************************************************
	 * Indicates that the card in the dummy's hand has been scanned.
	 ***********************************************************************/
	@Override
	public void sig_cardPlayed ( Direction turn, Card card )
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sig_setNextPlayer: entered");

		updateDisplay();
	}

}
