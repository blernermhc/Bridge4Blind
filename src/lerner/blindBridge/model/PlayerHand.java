// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.model;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Category;

/***********************************************************************
 * Represent the known hand of a player.
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
	
	/** Position of player */
	private Direction	m_myPlayer;

	/** Cards in the player's hand */
	private TreeSet<Card>	m_cards			= new TreeSet<>();
	
	/** Alternate view broken down by suit */
	private Map<Suit, TreeSet<Card>>		m_suitCards	= new HashMap<>();

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	public PlayerHand (Direction p_direction)
	{
		m_myPlayer = p_direction;
		for (Suit suit : Suit.values()) {
			m_suitCards.put(suit, new TreeSet<>());	
		}
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Adds a card to the hand, usually from a scan.
	 * @param p_card the card
	 ***********************************************************************/
	public void addCard (Card p_card)
	{
		m_cards.add(p_card);
		
		TreeSet<Card> cards = m_suitCards.get(p_card.getSuit());
		cards.add(p_card);
	}
	
	/***********************************************************************
	 * Removes a card from the hand (invoked by undo)
	 * @param p_card the card
	 ***********************************************************************/
	public boolean removeCard (Card p_card)
	{
		boolean removed = m_cards.remove(p_card);
		
		TreeSet<Card> cards = m_suitCards.get(p_card.getSuit());
		if (cards == null)
		{
			cards = new TreeSet<>();
			m_suitCards.put(p_card.getSuit(), cards);
		}
		cards.remove(p_card);
		
		return removed;
	}
	
	/***********************************************************************
	 * Removes a card from the hand in order to play it.
	 * @param p_card the card
	 * @return true if the card is found, false otherwise.
	 ***********************************************************************/
	public boolean useCard (Card p_card)
	{
		if (! removeCard(p_card))
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "Player: " + m_myPlayer + " Known Cards: " + listCardsToString();
	}

	/***********************************************************************
	 * Writes the hand to a string using card abbreviations.
	 * @return hand as string
	 ***********************************************************************/
	public String listCardsToString()
	{
		StringBuilder out = new StringBuilder();
		String sep = "";
		for (Card card : m_cards.descendingSet())
		{
			out.append(sep + card.abbreviation());
			sep = " ";
		}
		return out.toString();
	}

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * Position of player
	 * @return position
	 ***********************************************************************/
	public Direction getMyPlayer ()
	{
		return m_myPlayer;
	}

	/***********************************************************************
	 * Cards in the hand
	 * @return set of cards
	 ***********************************************************************/
	public TreeSet<Card> getCards ()
	{
		return m_cards;
	}
	
	/***********************************************************************
	 * Cards in the hand
	 * @return set of cards
	 ***********************************************************************/
	public Map<Suit, TreeSet<Card>> getSuitCards ()
	{
		return m_suitCards;
	}
	
}
