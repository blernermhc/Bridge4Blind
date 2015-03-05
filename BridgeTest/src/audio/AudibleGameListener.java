package audio;

import model.Card;
import model.Contract;
import model.Direction;
import model.GameListener;
import model.Rank;
import model.Suit;

/**
 * Listens to game events and says thing out loud in response.
 * 
 * @author Barbara Lerner
 * @version May 20, 2012
 *
 */
public class AudibleGameListener implements GameListener {
	private SoundManager soundMgr = SoundManager.getInstance();

	/**
	 * Speaks the name of the card
	 * @param c the card to speak
	 */
	@Override
	public synchronized void cardScanned(Card c){
		//System.out.println("AudibleGameListener: Scanned card sound added: " + c);
		//System.out.println("SoundManager.addSound: Thread " + Thread.currentThread().getName() + " trying to get lock");
		soundMgr.addSound(c.getSound());
		//System.out.println("AudibleGameListener.calling playSounds");
		soundMgr.playSounds();
		//System.out.println("AudibleGameListener.cardScanned returning");
	}

	/**
	 * Speaks which direction one the trick
	 * @param winner the winner of the trick
	 */
	@Override
	public synchronized void trickWon(Direction winner){
		//System.out.println("SoundManager.addSound: Thread " + Thread.currentThread().getName() + " trying to get lock");
		soundMgr.addSound("/sounds/warnings/trickover.WAV");
		switch(winner){
			
			case NORTH:
				soundMgr.addSound("/sounds/directions/north.WAV");
				break;
			case EAST:
				soundMgr.addSound("/sounds/directions/east.WAV");
				break;
			case SOUTH:
				soundMgr.addSound("/sounds/directions/south.WAV");
				break;
			case WEST:
				soundMgr.addSound("/sounds/directions/west.WAV");
				break;
			
		}
		
		soundMgr.playSounds();
		
	}
	
	/**Called when a card is played.
	 * 
	 * @param direction The position at which the card was played.
	 * @param c The card that was played.
	 */
	@Override
	public synchronized void cardPlayed(Direction direction, Card c){
		//System.out.println("SoundManager.addSound: Thread " + Thread.currentThread().getName() + " trying to get lock");
		
		//add the appropriate direction sound
		switch (direction){
			
			case NORTH:
				soundMgr.addSound("/sounds/directions/northplays.WAV");
				break;
			case EAST:
				soundMgr.addSound("/sounds/directions/eastplays.WAV");
				break;
			case SOUTH:
				soundMgr.addSound("/sounds/directions/southplays.WAV");
				break;
			case WEST:
				soundMgr.addSound("/sounds/directions/westplays.WAV");
				break;
			
		}
		
		//add the card's sound
		soundMgr.addSound(c.getSound());
		
		soundMgr.playSounds();
		
	}
	
	
	@Override
	public void gameReset() {
		// Do nothing
	}
	
	@Override
	public void contractSet(Contract contract) {
		// Do nothing
	}


	@Override
	public void debugMsg(String msg) {
		//System.out.println(msg);
	}

	@Override
	public void blindHandScanned() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dummyHandScanned() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Test method
	 * @param args none
	 */
	public static void main(String[] args) {
		AudibleGameListener listener = new AudibleGameListener();
		
		System.out.println("Should say \"North plays Ace of Clubs\"");
		listener.cardPlayed(Direction.NORTH, new Card(Rank.ACE, Suit.CLUBS));
		listener.soundMgr.pauseSounds();
		
		System.out.println("Should say \"King of Diamonds\"");
		listener.cardScanned(new Card(Rank.KING, Suit.DIAMONDS));
		listener.soundMgr.pauseSounds();
		
		System.out.println("Should say \"Trick is over and won by South\"");
		listener.trickWon(Direction.SOUTH);
	}


}
