// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.model;

import java.util.ArrayList;
import java.util.List;

/***********************************************************************
 * Represents a round of play (the four cards played and other information)
 ***********************************************************************/
public class Trick
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(Trick.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	/** The cards played in the current trick, in order of play */
	private List<CardPlay> 					m_cardsPlayed = new ArrayList<>();

	/** The suit of the trick. Set from first card played. */
	private Suit								m_currentSuit;

	/** The position of the player that must play a card next. */
	private Direction						m_nextPlayer;

	/** Position of player winning trick (null until complete) */
	private Direction						m_winner;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------
	
	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Creates a new trick
	 * @param p_firstPlayer	position of the first player
	 ***********************************************************************/
	public Trick ( Direction p_firstPlayer )
	{
		m_nextPlayer = p_firstPlayer;
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Adds a card to the trick.
	 * @param p_direction	position of player playing card 
	 * @param p_card			the card played
	 ***********************************************************************/
	public void playCard ( Direction p_direction, Card p_card )
	{
		CardPlay cardPlay = new CardPlay(p_direction, p_card);
		m_cardsPlayed.add(cardPlay);
		if (m_currentSuit == null) m_currentSuit = p_card.getSuit();
		m_nextPlayer = m_nextPlayer.getNextDirection();
	}
	
	/***********************************************************************
	 * Returns the card played by the given player, or null if not played yet.
	 * @param p_direction	the player's position
	 * @return the card played, or null
	 ***********************************************************************/
	public CardPlay hasPlayed ( Direction p_direction )
	{
		for (CardPlay cardPlay : m_cardsPlayed)
		{
			if (cardPlay.getPlayer() == p_direction) return cardPlay;
		}
		return null;
	}

	/***********************************************************************
	 * Returns true if all of the players have played cards.
	 * @return true if complete, false otherwise
	 ***********************************************************************/
	public boolean isComplete()
	{
		return (m_cardsPlayed.size() == BridgeHand.NUMBER_OF_PLAYERS);
	}
	
	/***********************************************************************
	 * Completes the trick and determines the winner
	 * @param p_trump	the Trump suit
	 * @return the winner
	 ***********************************************************************/
	public Direction completeTrick ( Suit p_trump )
	{
		// determine winner
		CardPlay best = null;
		
		for (CardPlay cardPlay : m_cardsPlayed)
		{
			if (best == null || (compareCards(best.getCard(), cardPlay.getCard(), p_trump) > 0))
			{
				best = cardPlay;
			}
		}
		
		m_winner = best.getPlayer();
		
		return m_winner;
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Compares a card played against the current best in the trick.
	 * @param p_curBest		Current best card (assumes first card played is first curBest)
	 * @param p_testCard		Subsequent card played
	 * @param p_trumpSuit	Current trump
	 * @return 1 if test card is better, 0 otherwise
	 ***********************************************************************/
	private int compareCards (Card p_curBest, Card p_testCard, Suit p_trumpSuit)
	{
		if (p_trumpSuit != null && p_trumpSuit != Suit.NOTRUMP)
		{	// check trump
			if (p_curBest.getSuit() == p_trumpSuit)
			{
				if (p_testCard.getSuit() != p_trumpSuit) return -1;	// trump better than non-trump 
				if (p_testCard.getRank().ordinal() > p_curBest.getRank().ordinal())
					return 1;	// higher trump is better
				else
					return -1;
			}
			else if (p_testCard.getSuit() == p_trumpSuit) return 1;	// trump better than non-trump 
		}
		
		// neither card is trump (assume current suit is the suit of cur best
		if (p_curBest.getSuit() != p_testCard.getSuit()) return -1;	// test is not the right suit
		
		// suits match
		if (p_testCard.getRank().ordinal() > p_curBest.getRank().ordinal())
			return 1;	// higher is better when suits match
		else
			return -1;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString ()
	{
		StringBuilder out = new StringBuilder();
		
		out.append("\n  Current Suit: " + m_currentSuit);
		out.append("\n  Next Player: " + m_nextPlayer);
		if (m_winner != null)
		{
			out.append("\n  Winner: " + m_winner);
		}
		out.append("\n  Cards Played: ");
		for (CardPlay cardPlay : m_cardsPlayed)
		{
			out.append("\n    " + cardPlay);
		}
		return out.toString();
	}
	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * The cards played in the current trick, in order of play.
	 * @return list of cards played
	 ***********************************************************************/
	public List<CardPlay> getCardsPlayed ()
	{
		return m_cardsPlayed;
	}

	/***********************************************************************
	 * The suit of the trick. Set from first card played.
	 * @return the current suit
	 ***********************************************************************/
	public Suit getCurrentSuit ()
	{
		return m_currentSuit;
	}

	/***********************************************************************
	 * The position of the player that must play a card next.
	 * @return next player position
	 ***********************************************************************/
	public Direction getNextPlayer ()
	{
		return m_nextPlayer;
	}

	/***********************************************************************
	 * The position of the player that must play a card next.
	 * @param p_nextPlayer next player position
	 ***********************************************************************/
	public void setNextPlayer (Direction p_nextPlayer)
	{
		m_nextPlayer = p_nextPlayer;
	}

	/***********************************************************************
	 * Position of player winning trick (null until complete.
	 * @return winner
	 ***********************************************************************/
	public Direction getWinner ()
	{
		return m_winner;
	}
	
}
