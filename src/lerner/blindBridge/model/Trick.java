// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.model;

import java.util.ArrayDeque;
import java.util.Deque;

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
	
	/** 
	 * The cards played in the current trick, in order of play.
	 * Use addLast (add last) to insert new elements or redo removals.
	 * Use pollLast (remove last) to pop off elements during undo.
	 */
	private Deque<CardPlay> 					m_cardsPlayed = new ArrayDeque<>();

	/** The suit of the trick. Set from first card played. */
	private Suit								m_currentSuit;

	/** The contract suit used in determining the winner of the trick. */
	private Suit								m_contractSuit;

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
	public Trick ( Suit p_contractSuit, Direction p_firstPlayer )
	{
		m_contractSuit = p_contractSuit;
		m_nextPlayer = p_firstPlayer;
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Returns true if no cards have been played in the current trick.
	 * @return true if empty, false otherwise
	 ***********************************************************************/
	public boolean isEmpty ()
	{
		return (m_cardsPlayed.size() == 0);
	}
	
	/***********************************************************************
	 * Adds a card to the trick.
	 * @param p_direction	position of player playing card 
	 * @param p_card			the card played
	 ***********************************************************************/
	public void playCard ( Direction p_direction, Card p_card )
	{
		CardPlay cardPlay = new CardPlay(p_direction, p_card);
		m_cardsPlayed.addLast(cardPlay);
		if (m_currentSuit == null) m_currentSuit = p_card.getSuit();
		m_nextPlayer = m_nextPlayer.getNextDirection();
	}
	
	/***********************************************************************
	 * Removes a card from the trick.
	 * Ignores the parameters.
	 * @param p_direction	position of player playing card 
	 * @param p_card			the card played
	 * @return	the played card removed from the trick, or null if the trick was empty.
	 ***********************************************************************/
	public CardPlay unplayCard ( Direction p_direction, Card p_card )
	{
		CardPlay cardPlay = m_cardsPlayed.pollLast();
		if (cardPlay != null)
		{
			m_nextPlayer = m_nextPlayer.getPreviousDirection();
			m_winner = null;
		}
		return cardPlay;
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
	
	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Completes the trick and determines the winner
	 * @return the winner
	 * @throws IllegalStateException if there are no cards played into the trick
	 ***********************************************************************/
	private Direction completeTrick ( )
	{
		// determine winner
		CardPlay best = null;
		
		// NOTE compareCards assumes that first "best" card is the first card
		// played, as it uses that to determine the first lead suit.
		assert !m_cardsPlayed.isEmpty();
		for (CardPlay cardPlay : m_cardsPlayed)
		{
			if (best == null || (compareCards(best.getCard(), cardPlay.getCard(), m_contractSuit) > 0))
			{
				best = cardPlay;
			}
		}
		
		if (best == null) throw new IllegalStateException ("completeTrick: m_cardsPlayed is empty");
		m_winner = best.getPlayer();
		
		return m_winner;
	}

	/***********************************************************************
	 * Compares a card played against the current best in the trick.
	 * @param p_curBest		Current best card (assumes first card played is first curBest)
	 * @param p_testCard		Subsequent card played
	 * @param p_trumpSuit	Current trump
	 * @return 1 if test card is better, 0 if they are the same and -1 otherwise
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
		
		// neither card is trump (assume current suit is the suit of cur best)
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
	public Deque<CardPlay> getCardsPlayed ()
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
	 * Position of player winning trick (null until complete).
	 * @return winner
	 ***********************************************************************/
	public Direction getWinner ()
	{
		if (! isComplete()) return null;
		
		if (m_winner == null) completeTrick();
		
		return m_winner;
	}
	
	/***********************************************************************
	 * The contract suit used in determining the winner of the trick.
	 * @return the contract suit
	 ***********************************************************************/
	public Suit getContractSuit ()
	{
		return m_contractSuit;
	}

	/***********************************************************************
	 * The contract suit used in determining the winner of the trick.
	 * @param p_trumpSuit the contract suit
	 ***********************************************************************/
	public void setContractSuit ( Suit p_trumpSuit )
	{
		m_contractSuit = p_trumpSuit;
	}


}
