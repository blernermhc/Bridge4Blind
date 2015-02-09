package model;

import static org.junit.Assert.*;

import org.junit.Test;

public class CardDatabaseTest {

	@Test
	public void testmakeCard() {
		Card c = new CardDatabase().makeCard("042E657A831E80.042B6E7A831E80|AH");
		assertEquals(Rank.ACE, c.getRank());
		assertEquals(Suit.HEARTS, c.getSuit());
	}

}
