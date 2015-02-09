package model;

/**
 * Interface to implement to be notified of important events during the
 * playing of the game.
 * 
 * @author Barbara Lerner
 * @version Aug 9, 2012
 *
 */
public interface GameListener {
	/** 
	 * Informs the listener of a debugging message to display.
	 * @param string the message
	 */
	public void debugMsg(String string);

	/**
	 * Called when beginning a new hand
	 */
	public void gameReset();

	/**
	 * Called when a card is added to the trick
	 * @param turn the player who played the card
	 * @param card the card played
	 */
	public void cardPlayed(Direction turn, Card card);

	/**
	 * Called when a card is scanned into the blind or dummy hand or 
	 * scanned over the id antenna
	 * @param card the card scanned
	 */
	public void cardScanned(Card card);

	/**
	 * Called at the end of a trick
	 * @param winner the player who won the trick
	 */
	public void trickWon(Direction winner);

	/**
	 * Called when bidding is complete
	 * @param contract the contract for the hand
	 */
	public void contractSet(Contract contract);
	
	/**
	 * Called when the blind hand is completely scanned in
	 */
	public void blindHandScanned();
	
	/**
	 * Called when the dummy hand is completely scanned in
	 */
	public void dummyHandScanned();
	
	/**
	 * Called when a card is added to any hand
	 * @param dir
	 */
	public void cardAddedToHand(Direction dir, Card c);

}
