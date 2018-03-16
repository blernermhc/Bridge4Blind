// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Category;

import model.Card;
import model.Direction;
import model.Suit;

/***********************************************************************
 * TODO
 ***********************************************************************/
public class PlayerHand
{

	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(PlayerHand.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	private Direction	m_myPlayer;
	Set<Card> m_cards = new HashSet<>();

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	public PlayerHand (Direction p_direction)
	{
		m_myPlayer = p_direction;
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	public void addCard (Card p_card)
	{
		m_cards.add(p_card);
	}
	
	public boolean useCard (Card p_card)
	{
		if (! m_cards.remove(p_card))
		{
			s_cat.error("useCard: something is wrong, played card: " + p_card + " was not in hand for player: " + m_myPlayer);
			s_cat.error("useCard: hand is:" + this.toString());
			return false;
		}
		return true;
	}

	/***********************************************************************
	 * Checks if the card can be played.
	 * If the card's suit does not match the current suit and 
	 * the hand contains another card with the suit, play is bad
	 * @param p_card		the card being played
	 * @param p_suit		the current suit
	 * @return true if OK, false otherwise
	 ***********************************************************************/
	public boolean testPlay (Card p_card, Suit p_suit)
	{
		if (p_suit == null) return true; // any card can be played before a current suit is set
		
		// if suit played is the current suit, play is OK
		if (p_card.getSuit() == p_suit) return true;
		for (Card card : m_cards)
		{
			// if played suit differs, and another card in the hand has the current suit, play is bad
			if (card.getSuit() == p_suit) return false;
		}
		return true;
	}
	
	/***********************************************************************
	 * Returns true if the hand has been completely scanned.
	 * @return true if hand is complete, false if we need to wait for more cards
	 ***********************************************************************/
	public boolean isComplete ()
	{
		if (m_cards.size() == BridgeHand.CARDS_IN_HAND) return true;
		return false;
	}
	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
	public String toString()
	{
		return "Player: " + m_myPlayer + " Known Cards: " + listCards();
	}

	public String listCards()
	{
		StringBuilder out = new StringBuilder();
		String sep = "";
		for (Card card : m_cards)
		{
			out.append(sep + card.abbreviation());
			sep = " ";
		}
		return out.toString();
	}
	
	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

}
