package controller;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import model.Card;
import model.CardDatabase;
import model.Game;
import model.Rank;
import model.Suit;

/**
 * Listens to the id antenna. Tells the game what card has been seen, but if the
 * same card is seen repeatedly, it only reports the card once every 500
 * milliseconds.
 * 
 * @version March 12, 2015
 * 
 */
public class CardIdentifier implements CardListener {

	/** the game that is being played */
	protected Game game;

	// the last card reported by the id antenna
	private Card lastIded;

	private static final Timer DUPLICATE_TIMER = new Timer(true);
	
	private TimerTask clearLastFoundTask;

	/**
	 * Creates a card identifier
	 * 
	 * @param g
	 *            the game to update when a card is scanned.
	 */
	public CardIdentifier(Game g) {
		game = g;
	}

	/** 
	 * Called when a card is found on an antenna
	 * @param c the card found
	 */
	@Override
	public synchronized void cardFound(Card c) {

		if (!c.equals(lastIded)) {
			tellGameAboutCard(c);
			lastIded = c;
			if (clearLastFoundTask != null) {
				clearLastFoundTask.cancel();
			}
			clearLastFoundTask = new TimerTask() {
				@Override
				public void run() {
					synchronized (CardIdentifier.this) {
						System.out.println("Resetting lastfound.  Was " + lastIded.toString());
						lastIded = null;
					}
				}
				
			};
			DUPLICATE_TIMER.schedule(clearLastFoundTask, 5000);
		}
	}

	/**
	 * Tell the game that the card was found by the hardware
	 * @param c the card found
	 */
	protected void tellGameAboutCard(Card c) {
		game.cardIded(c);
	}
	
	/**
	 * Tests the CardIdentifier class
	 * @param args none
	 */
	public static void main(String[] args) {
		final CardIdentifier ider = new CardIdentifier(new Game(new AntennaHandler(new CardDatabase()), false) {
			@Override
			public void cardIded(Card c) {
				System.out.println("Game sees " + c.toString());
			}
		});
		
		final Card c = new Card(Rank.ACE, Suit.CLUBS);
		Card c2 = new Card(Rank.ACE, Suit.DIAMONDS);
		ider.cardFound(c);
		ider.cardFound(c);
		ider.cardFound(c2);
		ider.cardFound(c);
		
		// Bizarrely, if I just call Thread.sleep, the thread never wakes up.
		Timer t = new Timer(true);
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				ider.cardFound(c);
				System.exit(0);
			}
			
		}, 1000, 1000);
		
		// Prevent the main method from exiting.
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}