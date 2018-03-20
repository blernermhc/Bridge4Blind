package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Rank;
import lerner.blindBridge.model.Suit;

public class HandTest {
	private Hand h;
	private Hand fullSpadesHand;
	private Card aceSpades = new Card(Rank.ACE, Suit.SPADES);
	private Card twoSpades = new Card(Rank.TWO, Suit.SPADES);
	private Card threeSpades = new Card(Rank.THREE, Suit.SPADES);
	private Card fourSpades = new Card(Rank.FOUR, Suit.SPADES);
	private Card fiveSpades = new Card(Rank.FIVE, Suit.SPADES);
	private Card sixSpades = new Card(Rank.SIX, Suit.SPADES);
	private Card sevenSpades = new Card(Rank.SEVEN, Suit.SPADES);
	private Card eightSpades = new Card(Rank.EIGHT, Suit.SPADES);
	private Card nineSpades = new Card(Rank.NINE, Suit.SPADES);
	private Card tenSpades = new Card(Rank.TEN, Suit.SPADES);
	private Card jackSpades = new Card(Rank.JACK, Suit.SPADES);
	private Card queenSpades = new Card(Rank.QUEEN, Suit.SPADES);
	private Card kingSpades = new Card(Rank.KING, Suit.SPADES);
	private Card aceDiamonds = new Card(Rank.ACE, Suit.DIAMONDS);
	
	@Before
	public void setUp() {
		h = new Hand();
		
		fullSpadesHand = new Hand();
		fullSpadesHand.addCard(aceSpades);
		fullSpadesHand.addCard(twoSpades);
		fullSpadesHand.addCard(threeSpades);
		fullSpadesHand.addCard(fourSpades);
		fullSpadesHand.addCard(fiveSpades);
		fullSpadesHand.addCard(sixSpades);
		fullSpadesHand.addCard(sevenSpades);
		fullSpadesHand.addCard(eightSpades);
		fullSpadesHand.addCard(nineSpades);
		fullSpadesHand.addCard(tenSpades);
		fullSpadesHand.addCard(jackSpades);
		fullSpadesHand.addCard(queenSpades);
		fullSpadesHand.addCard(kingSpades);

	}

	@Test
	public void testAddOneCard() {
		h.addCard(aceSpades);
		assertEquals(aceSpades, h.cards[0]);
		assertEquals(1, h.getNumCards());
		assertTrue(h.containsCard(aceSpades));
		assertEquals(1, h.getNumOfSuit(Suit.SPADES));
		assertEquals(0, h.getNumOfSuit(Suit.CLUBS));
		assertEquals(0, h.getNumOfSuit(Suit.HEARTS));
		assertEquals(0, h.getNumOfSuit(Suit.DIAMONDS));
	}

	@Test
	public void testAddTwoCardsSorted() {
		h.addCard(twoSpades);
		h.addCard(aceSpades);
		
		assertEquals(twoSpades, h.cards[0]);
		assertEquals(aceSpades, h.cards[1]);
		assertEquals(2, h.getNumCards());
		assertTrue(h.containsCard(aceSpades));
		assertTrue(h.containsCard(twoSpades));
		assertFalse(h.containsCard(threeSpades));
		assertEquals(2, h.getNumOfSuit(Suit.SPADES));
		assertEquals(0, h.getNumOfSuit(Suit.CLUBS));
		assertEquals(0, h.getNumOfSuit(Suit.HEARTS));
		assertEquals(0, h.getNumOfSuit(Suit.DIAMONDS));
	}
	
	@Test
	public void testAddTwoCardsNotSorted() {
		h.addCard(aceSpades);
		h.addCard(twoSpades);
		
		assertEquals(twoSpades, h.cards[0]);
		assertEquals(aceSpades, h.cards[1]);
		assertEquals(2, h.getNumCards());
		assertTrue(h.containsCard(aceSpades));
		assertTrue(h.containsCard(twoSpades));
	}

	@Test
	public void testFullHand() {
		
		assertEquals(twoSpades, fullSpadesHand.cards[0]);
		assertEquals(threeSpades, fullSpadesHand.cards[1]);
		assertEquals(fourSpades, fullSpadesHand.cards[2]);
		assertEquals(fiveSpades, fullSpadesHand.cards[3]);
		assertEquals(sixSpades, fullSpadesHand.cards[4]);
		assertEquals(sevenSpades, fullSpadesHand.cards[5]);
		assertEquals(eightSpades, fullSpadesHand.cards[6]);
		assertEquals(nineSpades, fullSpadesHand.cards[7]);
		assertEquals(tenSpades, fullSpadesHand.cards[8]);
		assertEquals(jackSpades, fullSpadesHand.cards[9]);
		assertEquals(queenSpades, fullSpadesHand.cards[10]);
		assertEquals(kingSpades, fullSpadesHand.cards[11]);
		assertEquals(aceSpades, fullSpadesHand.cards[12]);
		
		assertEquals(13, fullSpadesHand.getNumCards());
		assertTrue(fullSpadesHand.containsCard(aceSpades));
		assertTrue(fullSpadesHand.containsCard(twoSpades));
		
		assertEquals(13, fullSpadesHand.getNumOfSuit(Suit.SPADES));
		assertEquals(0, fullSpadesHand.getNumOfSuit(Suit.CLUBS));
		assertEquals(0, fullSpadesHand.getNumOfSuit(Suit.HEARTS));
		assertEquals(0, fullSpadesHand.getNumOfSuit(Suit.DIAMONDS));

	}

	@Test
	public void testMultipleSuits() {
		h.addCard(aceSpades);
		h.addCard(twoSpades);
		h.addCard(aceDiamonds);
		h.addCard(fourSpades);
		h.addCard(fiveSpades);
		h.addCard(sixSpades);
		h.addCard(sevenSpades);
		h.addCard(eightSpades);
		h.addCard(nineSpades);
		h.addCard(tenSpades);
		h.addCard(jackSpades);
		h.addCard(queenSpades);
		h.addCard(kingSpades);
		
		assertEquals(aceDiamonds, h.cards[0]);
		assertEquals(twoSpades, h.cards[1]);
		assertEquals(fourSpades, h.cards[2]);
		assertEquals(fiveSpades, h.cards[3]);
		assertEquals(sixSpades, h.cards[4]);
		assertEquals(sevenSpades, h.cards[5]);
		assertEquals(eightSpades, h.cards[6]);
		assertEquals(nineSpades, h.cards[7]);
		assertEquals(tenSpades, h.cards[8]);
		assertEquals(jackSpades, h.cards[9]);
		assertEquals(queenSpades, h.cards[10]);
		assertEquals(kingSpades, h.cards[11]);
		assertEquals(aceSpades, h.cards[12]);
		
		assertEquals(13, h.getNumCards());
		assertTrue(h.containsCard(aceSpades));
		assertTrue(h.containsCard(twoSpades));
		
		assertEquals(12, h.getNumOfSuit(Suit.SPADES));
		assertEquals(0, h.getNumOfSuit(Suit.CLUBS));
		assertEquals(0, h.getNumOfSuit(Suit.HEARTS));
		assertEquals(1, h.getNumOfSuit(Suit.DIAMONDS));

	}
	
	@Test
	public void testAddDuplicate() {
		h.addCard(aceSpades);
		h.addCard(aceSpades);
		assertEquals(1, h.getNumCards());
	}
	
	@Test
	public void testRemoveNotInHand() {
		h.removeCard(aceSpades);
		assertEquals(0, h.getNumCards());
		
		assertEquals(0, h.getNumOfSuit(Suit.SPADES));
		assertEquals(0, h.getNumOfSuit(Suit.CLUBS));
		assertEquals(0, h.getNumOfSuit(Suit.HEARTS));
		assertEquals(0, h.getNumOfSuit(Suit.DIAMONDS));

	}

	@Test
	public void testRemoveOnlyCard() {
		h.addCard(aceSpades);
		h.removeCard(aceSpades);
		assertEquals(0, h.getNumCards());
		assertFalse(h.containsCard(aceSpades));
	}

	@Test
	public void testRemoveLastCard() {
		fullSpadesHand.removeCard(aceSpades);
		assertEquals(12, fullSpadesHand.getNumCards());
		assertFalse(fullSpadesHand.containsCard(aceSpades));
		
		assertEquals(twoSpades, fullSpadesHand.cards[0]);
		assertEquals(threeSpades, fullSpadesHand.cards[1]);
		assertEquals(fourSpades, fullSpadesHand.cards[2]);
		assertEquals(fiveSpades, fullSpadesHand.cards[3]);
		assertEquals(sixSpades, fullSpadesHand.cards[4]);
		assertEquals(sevenSpades, fullSpadesHand.cards[5]);
		assertEquals(eightSpades, fullSpadesHand.cards[6]);
		assertEquals(nineSpades, fullSpadesHand.cards[7]);
		assertEquals(tenSpades, fullSpadesHand.cards[8]);
		assertEquals(jackSpades, fullSpadesHand.cards[9]);
		assertEquals(queenSpades, fullSpadesHand.cards[10]);
		assertEquals(kingSpades, fullSpadesHand.cards[11]);

	}

	@Test
	public void testRemoveFirstCard() {
		fullSpadesHand.removeCard(twoSpades);
		assertEquals(12, fullSpadesHand.getNumCards());
		assertFalse(fullSpadesHand.containsCard(twoSpades));
		
		assertEquals(threeSpades, fullSpadesHand.cards[0]);
		assertEquals(fourSpades, fullSpadesHand.cards[1]);
		assertEquals(fiveSpades, fullSpadesHand.cards[2]);
		assertEquals(sixSpades, fullSpadesHand.cards[3]);
		assertEquals(sevenSpades, fullSpadesHand.cards[4]);
		assertEquals(eightSpades, fullSpadesHand.cards[5]);
		assertEquals(nineSpades, fullSpadesHand.cards[6]);
		assertEquals(tenSpades, fullSpadesHand.cards[7]);
		assertEquals(jackSpades, fullSpadesHand.cards[8]);
		assertEquals(queenSpades, fullSpadesHand.cards[9]);
		assertEquals(kingSpades, fullSpadesHand.cards[10]);
		assertEquals(aceSpades, fullSpadesHand.cards[11]);


	}

	@Test
	public void testRemoveMiddleCard() {
		fullSpadesHand.removeCard(sixSpades);
		assertEquals(12, fullSpadesHand.getNumCards());
		assertFalse(fullSpadesHand.containsCard(sixSpades));
		
		assertEquals(twoSpades, fullSpadesHand.cards[0]);
		assertEquals(threeSpades, fullSpadesHand.cards[1]);
		assertEquals(fourSpades, fullSpadesHand.cards[2]);
		assertEquals(fiveSpades, fullSpadesHand.cards[3]);
		assertEquals(sevenSpades, fullSpadesHand.cards[4]);
		assertEquals(eightSpades, fullSpadesHand.cards[5]);
		assertEquals(nineSpades, fullSpadesHand.cards[6]);
		assertEquals(tenSpades, fullSpadesHand.cards[7]);
		assertEquals(jackSpades, fullSpadesHand.cards[8]);
		assertEquals(queenSpades, fullSpadesHand.cards[9]);
		assertEquals(kingSpades, fullSpadesHand.cards[10]);
		assertEquals(aceSpades, fullSpadesHand.cards[11]);


	}
	
	@Test
	public void testIteratorSuit() {
		h.addCard(aceSpades);
		h.addCard(twoSpades);
		h.addCard(aceDiamonds);
		
		Iterator<Card> cardIter = h.cards(Suit.SPADES);
		assertTrue (cardIter.hasNext());
		assertEquals(twoSpades, cardIter.next());
		assertTrue (cardIter.hasNext());
		assertEquals(aceSpades, cardIter.next());
		assertFalse(cardIter.hasNext());
	}

}
