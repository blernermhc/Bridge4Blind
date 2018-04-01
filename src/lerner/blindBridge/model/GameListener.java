package lerner.blindBridge.model;

/**
 * Interface to implement to be notified of important events during the
 * playing of the game.  Listener signal methods all begin with "sig_".
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
	public void sig_debugMsg(String string);

	/**
	 * Called at first startup to indicate we are waiting
	 * for hardware to finish initialization.
	 */
	public void sig_initializing();
	
	/**
	 * Called when beginning a new hand
	 */
	public void sig_gameReset();
	public void sig_gameReset_undo ( boolean p_redoFlag, boolean p_confirmed );

	/**
	 * Called when waiting for all of the blind hands to be scanned in
	 */
	public void sig_scanBlindHands();

	/**
	 * Called when waiting for the dummy hand to be scanned in
	 */
	public void sig_scanDummyHand();
	
	/**
	 * Called when a card is scanned into the blind or dummy hand
	 * @param p_direction	the player whose hand the card belongs to
	 * @param p_card			the card scanned
	 * @param p_handComplete true if card completes hand
	 */
	public void sig_cardScanned(Direction p_direction, Card p_card, boolean p_handComplete);
	public void sig_cardScanned_undo ( boolean p_redoFlag, boolean p_confirmed, Direction p_direction, Card p_card, boolean p_handComplete );
	
	/**
	 * Called when all of the blind hands have been completely scanned in
	 */
	public void sig_blindHandsScanned();
	
	/**
	 * Called when the dummy hand is completely scanned in
	 */
	public void sig_dummyHandScanned();

	/**
	 * Called when contract needs to be entered
	 */
	public void sig_enterContract();
	
	/**
	 * Called when bidding is complete
	 * @param contract the contract for the hand
	 */
	public void sig_contractSet(Contract contract);
	public void sig_contractSet_undo ( boolean p_redoFlag, boolean p_confirmed, Contract p_contract );
	
	/***********************************************************************
	 * Called when the dummy position has been determined.
	 * @param p_direction the position of the dummy
	 ***********************************************************************/
	public void sig_setDummyPosition ( Direction p_direction );

	/***********************************************************************
	 * Called to indicate the position of the next player to play a card.
	 * @param p_direction the position of the player
	 ***********************************************************************/
	public void sig_setNextPlayer ( Direction p_direction );

	/***********************************************************************
	 * Called when a suit is determined for the trick (from first card played).
	 * @param p_suit the suit
	 ***********************************************************************/
	public void sig_setCurrentSuit ( Suit p_suit );
	
	/**
	 * Called when a card is added to the trick
	 * @param turn the player who played the card
	 * @param card the card played
	 */
	public void sig_cardPlayed(Direction turn, Card card);
	public void sig_cardPlayed_undo ( boolean p_redoFlag, boolean p_confirmed, Direction p_direction, Card p_card );

	/***********************************************************************
	 * Called at the end of a trick
	 * @param p_trick the completed trick
	 ***********************************************************************/
	public void sig_trickWon(Trick p_trick);
	
	/***********************************************************************
	 * The current score of the bridge game
	 * @param p_score score
	 ***********************************************************************/
	public void sig_handComplete (BridgeScore p_score );
	
	/***********************************************************************
	 * Notifies listeners of an error that should be made known to some or all players.
	 * Each listener must determine if the error should be announced or ignored
	 * (e.g., do not announce a "cannot play card" error from one player to another player).
	 * 
	 * @param p_errorCode	The error being reported
	 * @param p_direction	player position (optional)
	 * @param p_card			a card (optional)
	 * @param p_suit			a suit (optional)
	 * @param p_num			a number (optional)
	 ***********************************************************************/
	public void sig_error (ErrorCode p_errorCode, Direction p_direction, Card p_card, Suit p_suit, int p_num);

}
