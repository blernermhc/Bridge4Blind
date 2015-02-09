package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class TrickTest {
	private Trick emptyTrick = new Trick();
	private Trick fullTrick = new Trick();
	private ArrayList<Integer> spades;
	private ArrayList<Integer> hearts;
	private ArrayList<Integer> clubs;

	
	@Before
	public void setUp() throws Exception {
		fullTrick.add(new Card(Rank.ACE, Suit.SPADES), 0);
		fullTrick.add(new Card(Rank.KING, Suit.SPADES), 1);
		fullTrick.add(new Card(Rank.QUEEN, Suit.SPADES), 2);
		fullTrick.add(new Card(Rank.JACK, Suit.HEARTS), 3);
		
		spades = fullTrick.getCardsOfSuit(Suit.SPADES);
		hearts = fullTrick.getCardsOfSuit(Suit.HEARTS);
		clubs = fullTrick.getCardsOfSuit(Suit.CLUBS);
	}

	@Test
	public void testIsOver() {
	}

	@Test
	public void testIsEmpty() {
		assertFalse(fullTrick.isEmpty());
		assertTrue(emptyTrick.isEmpty());
	}

	@Test
	public void testGetHighCardPlayer() {
		assertEquals (0, fullTrick.getHighCardPlayer(spades));
		assertEquals (3, fullTrick.getHighCardPlayer(hearts));
	}

	@Test
	public void testGetCardsOfSuit() {
		assertEquals(3, spades.size());
		assertEquals(1, hearts.size());
		assertEquals(0, clubs.size());
	}

	@Test
	public void testDetermineWinner() {
		fullTrick.setLedSuit(Suit.SPADES);
		assertEquals(0, fullTrick.determineWinner(Suit.DIAMONDS));
		assertEquals(3, fullTrick.determineWinner(Suit.HEARTS));
		
		fullTrick.setLedSuit(Suit.HEARTS);
		assertEquals(3, fullTrick.determineWinner(Suit.DIAMONDS));
	}

}
