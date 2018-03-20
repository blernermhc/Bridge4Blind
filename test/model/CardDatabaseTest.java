package model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Rank;
import lerner.blindBridge.model.Suit;

public class CardDatabaseTest {

	@Test
	public void testmakeCard() {
		Card c = new CardDatabase().makeCard("042E657A831E80.042B6E7A831E80|AH");
		assertEquals(Rank.ACE, c.getRank());
		assertEquals(Suit.HEARTS, c.getSuit());
	}

}
