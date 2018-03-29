package lerner.blindBridge.audio;

import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.GameListener_sparse;
import lerner.blindBridge.model.Rank;
import lerner.blindBridge.model.Suit;

/**
 * Listens to game events and says thing out loud in response.
 * 
 * @author Barbara Lerner
 * @version May 20, 2012
 *
 */
public class AudibleGameListener implements GameListener_sparse
{
	private SoundManager m_soundMgr = SoundManager.getInstance();

	/**
	 * Speaks the name of the card
	 * 
	 * @param p_card
	 *            the card to speak
	 */
	@Override
	public synchronized void sig_cardScanned (	Direction p_direction,
												Card p_card,
												boolean p_handComplete )
	{
		// System.out.println("AudibleGameListener: Scanned card sound added: " + c);
		// System.out.println("SoundManager.addSound: Thread " + Thread.currentThread().getName() +
		// " trying to get lock");
		m_soundMgr.addSound(p_card.getSound());
		// System.out.println("AudibleGameListener.calling playSounds");
		m_soundMgr.playSounds();
		// System.out.println("AudibleGameListener.cardScanned returning");
	}

	/**
	 * Speaks which direction one the trick
	 * 
	 * @param p_winner
	 *            the winner of the trick
	 */
	@Override
	public synchronized void sig_trickWon ( Direction p_winner )
	{
		// System.out.println("SoundManager.addSound: Thread " + Thread.currentThread().getName() +
		// " trying to get lock");
		m_soundMgr.addSound("/sounds/warnings/trickover.WAV");
		switch (p_winner)
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

	/**
	 * Called when a card is played.
	 * 
	 * @param p_direction
	 *            The position at which the card was played.
	 * @param p_card
	 *            The card that was played.
	 */
	@Override
	public synchronized void sig_cardPlayed ( Direction p_direction, Card p_card )
	{
		// System.out.println("SoundManager.addSound: Thread " + Thread.currentThread().getName() +
		// " trying to get lock");

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


	/**
	 * Test method
	 * 
	 * @param p_args
	 *            none
	 */
	public static void main ( String[] p_args )
	{
		AudibleGameListener listener = new AudibleGameListener();

		System.out.println("Should say \"North plays Ace of Clubs\"");
		listener.sig_cardPlayed(Direction.NORTH, new Card(Rank.ACE, Suit.CLUBS));
		listener.m_soundMgr.pauseSounds();

		System.out.println("Should say \"King of Diamonds\"");
		listener.sig_cardScanned(Direction.SOUTH, new Card(Rank.KING, Suit.DIAMONDS), false);
		listener.m_soundMgr.pauseSounds();

		System.out.println("Should say \"Trick is over and won by South\"");
		listener.sig_trickWon(Direction.SOUTH);
	}

}
