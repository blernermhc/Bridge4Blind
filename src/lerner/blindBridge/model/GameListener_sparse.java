// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.model;

/***********************************************************************
 * Version of GameListener with default (empty) implementations of all methods
 ***********************************************************************/
public interface GameListener_sparse extends GameListener
{

	/**
	 * Used to collect logging output for this class
	 */
	//private static Category s_cat = Category.getInstance(GameListener_sparse.class.getName());

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_debugMsg(java.lang.String)
	 */
	@Override
	public default void sig_debugMsg ( String p_string )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_initializing()
	 */
	@Override
	public default void sig_initializing ()
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_gameReset()
	 */
	@Override
	public default void sig_gameReset ()
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_gameReset_undo(boolean, boolean)
	 */
	@Override
	public default void sig_gameReset_undo ( boolean p_redoFlag, boolean p_confirmed )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_scanBlindHands()
	 */
	@Override
	public default void sig_scanBlindHands ()
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_scanDummyHand()
	 */
	@Override
	public default void sig_scanDummyHand ()
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_cardScanned(lerner.blindBridge.model.Direction, lerner.blindBridge.model.Card, boolean)
	 */
	@Override
	public default void sig_cardScanned ( Direction p_direction, Card p_card, boolean p_handComplete )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_cardScanned_undo(boolean, boolean, lerner.blindBridge.model.Direction, lerner.blindBridge.model.Card, boolean)
	 */
	@Override
	public default void sig_cardScanned_undo (	boolean p_redoFlag,
										boolean p_confirmed,
										Direction p_direction,
										Card p_card,
										boolean p_handComplete )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_blindHandsScanned()
	 */
	@Override
	public default void sig_blindHandsScanned ()
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_dummyHandScanned()
	 */
	@Override
	public default void sig_dummyHandScanned ()
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_enterContract()
	 */
	@Override
	public default void sig_enterContract ()
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_contractSet(lerner.blindBridge.model.Contract)
	 */
	@Override
	public default void sig_contractSet ( Contract p_contract )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_contractSet_undo(boolean, boolean, lerner.blindBridge.model.Contract)
	 */
	@Override
	public default void sig_contractSet_undo (	boolean p_redoFlag,
										boolean p_confirmed,
										Contract p_contract )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_setDummyPosition(lerner.blindBridge.model.Direction)
	 */
	@Override
	public default void sig_setDummyPosition ( Direction p_direction )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_setNextPlayer(lerner.blindBridge.model.Direction)
	 */
	@Override
	public default void sig_setNextPlayer ( Direction p_direction )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_setCurrentSuit(lerner.blindBridge.model.Suit)
	 */
	@Override
	public default void sig_setCurrentSuit ( Suit p_suit )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_cardPlayed(lerner.blindBridge.model.Direction, lerner.blindBridge.model.Card)
	 */
	@Override
	public default void sig_cardPlayed ( Direction p_turn, Card p_card )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_cardPlayed_undo(boolean, boolean, lerner.blindBridge.model.Direction, lerner.blindBridge.model.Card)
	 */
	@Override
	public default void sig_cardPlayed_undo (	boolean p_redoFlag,
										boolean p_confirmed,
										Direction p_direction,
										Card p_card )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_trickWon(lerner.blindBridge.model.Direction)
	 */
	@Override
	public default void sig_trickWon ( Trick p_winner )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_handComplete(lerner.blindBridge.model.BridgeScore)
	 */
	@Override
	public default void sig_handComplete ( BridgeScore p_score )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_error(lerner.blindBridge.model.ErrorCode, lerner.blindBridge.model.Direction, lerner.blindBridge.model.Card, lerner.blindBridge.model.Suit, int)
	 */
	@Override
	public default void sig_error (	ErrorCode p_errorCode,
							Direction p_direction,
							Card p_card,
							Suit p_suit,
							int p_num )
	{
		// nothing to do
	}

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

}
