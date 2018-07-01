package audio;

import org.junit.Before;
import org.junit.Test;

import lerner.blindBridge.audio.AudibleGameListener;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.Rank;
import lerner.blindBridge.model.Suit;

public class AudibleGameListenerTest {
	private AudibleGameListener audio = new AudibleGameListener();

	@Before
	public void setUp() throws Exception {
		
	}

	//@Test
	public void testCardScanned() {
		audio.sig_cardScanned(direction, new Card (Rank.ACE, Suit.SPADES), null);
		pause(3000);
	}

	/**
	 * Need to pause so that the test program doesn't end before the sound plays.
	 * @param millis
	 */
	private void pause(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//@Test
	public void testMultipleCardsScanned() {
		audio.sig_cardScanned(direction, new Card (Rank.ACE, Suit.SPADES), null);
		audio.sig_cardScanned(direction, new Card (Rank.KING, Suit.SPADES), null);
		pause(3000);
	}
	
	//@Test
	public void testTrickOver() {
		audio.sig_trickWon(Direction.NORTH);
		pause(4000);
	}
	
	//@Test
//	public void testPlayContract() {
//		audio.playContract(3, Suit.SPADES, Direction.NORTH);
//		audio.playContract(3, Suit.NOTRUMP, Direction.NORTH);
//		pause(9000);
//	}
//	
//	//@Test
//	public void testPlayLastSound() {
//		audio.cardScanned(new Card (Rank.ACE, Suit.SPADES));
//		
//		// This pause needs to be here so that the scanned card gets a 
//		// chance to play.
//		pause(2000);
//		audio.playLastSound();
//		pause(2000);
//	}
//
//	@Test
//	public void testPlayLastLongSound() {
//		audio.playContract(3, Suit.SPADES, Direction.NORTH);
//		
//		// This pause needs to be here so that the scanned card gets a 
//		// chance to play.
//		pause(4000);
//		audio.playLastSound();
//		pause(5000);
//	}



}