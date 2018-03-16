package model;

/**
 * Interface to implement to be notified of important events during the
 * playing of the game.
 * 
 * @author Barbara Lerner
 * @version Aug 9, 2012
 *
 */
public interface GameListener
{
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
	 * Called when waiting for all of the blind hands to be scanned in
	 */
	public void scanBlindHands();

	/**
	 * Called when waiting for the dummy hand to be scanned in
	 */
	public void scanDummyHand();
	
	/**
	 * Called when a card is scanned into the blind or dummy hand
	 * @param p_direction	the player whose hand the card belongs to
	 * @param p_card			the card scanned
	 * @param p_handComplete true if card completes hand
	 */
	public void cardScanned(Direction p_direction, Card p_card, boolean p_handComplete);

	/**
	 * Called when all of the blind hands have been completely scanned in
	 */
	public void blindHandsScanned();
	
	/**
	 * Called when the dummy hand is completely scanned in
	 */
	public void dummyHandScanned();

	/**
	 * Called when contract needs to be entered
	 */
	public void enterContract();
	
	/**
	 * Called when bidding is complete
	 * @param contract the contract for the hand
	 */
	public void contractSet(Contract contract);
	
	/***********************************************************************
	 * Called when the dummy position has been determined.
	 * @param p_direction the position of the dummy
	 ***********************************************************************/
	public void setDummyPosition ( Direction p_direction );

	/***********************************************************************
	 * Called to indicate the position of the next player to play a card.
	 * @param p_direction the position of the player
	 ***********************************************************************/
	public void setNextPlayer ( Direction p_direction );

	/***********************************************************************
	 * Called when a suit is determined for the trick (from first card played).
	 * @param p_suit the suit
	 ***********************************************************************/
	public void setCurrentSuit ( Suit p_suit );
	
	/**
	 * Called when a card is added to the trick
	 * @param turn the player who played the card
	 * @param card the card played
	 */
	public void cardPlayed(Direction turn, Card card);

	/**
	 * Called at the end of a trick
	 * @param winner the player who won the trick
	 */
	public void trickWon(Direction winner);
	
	/***********************************************************************
	 * The current score of the bridge game
	 * @param p_score score
	 ***********************************************************************/
	public void handComplete (BridgeScore p_score );

}
