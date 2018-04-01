package lerner.blindBridge.audio;

import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener_sparse;
import lerner.blindBridge.model.Trick;
import lerner.blindBridge.stateMachine.BridgeHandState;

/***********************************************************************
 * Listens to game events and says thing out loud in response.
 * 
 * @author Barbara Lerner
 * @version May 20, 2012
 *
 ***********************************************************************/
public class AudibleGameListener implements GameListener_sparse
{
	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------

	/** The Game object (so we can find out if a position is a blind player) */
	private Game m_game;
	
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	/** The object used to generate audio announcements */
	private SoundManager m_soundMgr = SoundManager.getInstance();
	
	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Creates a listener to generate audio announcements for specific game events.
	 ***********************************************************************/
	public AudibleGameListener (Game p_game)
	{
		m_game = p_game;
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Speaks the name of the card.
	 * 
	 * Only plays audio when scanning the Dummy.
	 * Useful so folks know that cards have been scanned.
	 *  
	 * @param p_card		the card to speak
	 ***********************************************************************/
	@Override
	public synchronized void sig_cardScanned (	Direction p_direction,
												Card p_card,
												boolean p_handComplete )
	{
		if (m_game.getStateController().getCurrentState() == BridgeHandState.SCAN_DUMMY)
		{
			m_soundMgr.addSound(p_card.getSound());
			m_soundMgr.playSounds();
		}
	}

	/***********************************************************************
	 * Called when a card is played.
	 * 
	 * Only plays audio when a card is being played from a blind hand.
	 * Audio is helpful in this case because a physical card might not be placed on the table.
	 * 
	 * @param p_direction
	 *            The position at which the card was played.
	 * @param p_card
	 *            The card that was played.
	 ***********************************************************************/
	@Override
	public synchronized void sig_cardPlayed ( Direction p_direction, Card p_card )
	{
		if (m_game.getKeyboardControllers().get(p_direction) == null)
		{
			// Position is not a blind player, do not generate audio
			return;
		}

		// add the appropriate direction sound
		switch (p_direction)
		{
			case NORTH:
				m_soundMgr.addSound("/sounds/directions/northplays.WAV");
				break;
			case EAST:
				m_soundMgr.addSound("/sounds/directions/eastplays.WAV");
				break;
			case SOUTH:
				m_soundMgr.addSound("/sounds/directions/southplays.WAV");
				break;
			case WEST:
				m_soundMgr.addSound("/sounds/directions/westplays.WAV");
				break;
		}

		// add the card's sound
		m_soundMgr.addSound(p_card.getSound());
		m_soundMgr.playSounds();
	}


	/***********************************************************************
	 * Speaks which direction won the trick
	 * 
	 * @param p_trick	the trick
	 ***********************************************************************/
	@Override
	public synchronized void sig_trickWon ( Trick p_trick )
	{
		m_soundMgr.addSound("/sounds/warnings/trickover.WAV");
		switch (p_trick.getWinner())
		{

			case NORTH:
				m_soundMgr.addSound("/sounds/directions/north.WAV");
				break;
			case EAST:
				m_soundMgr.addSound("/sounds/directions/east.WAV");
				break;
			case SOUTH:
				m_soundMgr.addSound("/sounds/directions/south.WAV");
				break;
			case WEST:
				m_soundMgr.addSound("/sounds/directions/west.WAV");
				break;

		}

		m_soundMgr.playSounds();

	}
}
