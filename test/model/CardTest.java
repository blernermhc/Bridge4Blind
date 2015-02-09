package model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CardTest {
	private Card aceSpades = new Card(Rank.ACE, Suit.SPADES);
	private Card kingSpades = new Card(Rank.KING, Suit.SPADES);
	private Card aceHearts = new Card(Rank.ACE, Suit.HEARTS);
	private Card aceDiamonds = new Card(Rank.ACE, Suit.DIAMONDS);
	private Card aceClubs = new Card(Rank.ACE, Suit.CLUBS);
	private Card tenClubs = new Card(Rank.TEN, Suit.CLUBS);

	@Test
	public void compareSameCard() {
		assertEquals(0, aceSpades.compareTo(aceSpades));
	}
	
	@Test
	public void compareSameSuitDiffRank() {
		assertTrue(kingSpades.compareTo(aceSpades) < 0);
		assertTrue(aceSpades.compareTo(kingSpades) > 0);
	}

	@Test
	public void compareSameRankDiffSuit() {
		assertTrue(aceHearts.compareTo(aceSpades) < 0);
		assertTrue(aceDiamonds.compareTo(aceHearts) < 0);
		assertTrue(aceClubs.compareTo(aceDiamonds) < 0);
		assertTrue(aceSpades.compareTo(aceClubs) > 0);
	}
	
	@Test
	public void checkSoundFileName() {
		assertEquals("/sounds/cards/AS.WAV", aceSpades.getSound());
		assertEquals("/sounds/cards/KS.WAV", kingSpades.getSound());
		assertEquals("/sounds/cards/AH.WAV", aceHearts.getSound());
		assertEquals("/sounds/cards/AD.WAV", aceDiamonds.getSound());
		assertEquals("/sounds/cards/AC.WAV", aceClubs.getSound());
		assertEquals("/sounds/cards/10C.WAV", tenClubs.getSound());
	}

}
