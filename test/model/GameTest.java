package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import controller.AntennaHandler;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.Rank;
import lerner.blindBridge.model.Suit;

public class GameTest {
	private static final Direction DUMMY_DIRECTION = Direction.SOUTH;
	private static final Direction FIRST_PLAYER = Direction.EAST;
	private static final Direction BLIND_DIRECTION = Direction.NORTH;
	private static final Card ACE_SPADES = new Card (Rank.ACE, Suit.SPADES);
	private static final Card TWO_SPADES = new Card (Rank.TWO, Suit.SPADES);
	private static final Card THREE_SPADES = new Card (Rank.THREE, Suit.SPADES);
	private static final Card FOUR_SPADES = new Card (Rank.FOUR, Suit.SPADES);
	private static final Card FIVE_SPADES = new Card (Rank.FIVE, Suit.SPADES);
	private static final Card SIX_SPADES = new Card (Rank.SIX, Suit.SPADES);
	private static final Card SEVEN_SPADES = new Card (Rank.SEVEN, Suit.SPADES);
	private static final Card EIGHT_SPADES = new Card (Rank.EIGHT, Suit.SPADES);
	private static final Card NINE_SPADES = new Card (Rank.NINE, Suit.SPADES);
	private static final Card TEN_SPADES = new Card (Rank.TEN, Suit.SPADES);
	private static final Card JACK_SPADES = new Card (Rank.JACK, Suit.SPADES);
	private static final Card QUEEN_SPADES = new Card (Rank.QUEEN, Suit.SPADES);
	private static final Card KING_SPADES = new Card (Rank.KING, Suit.SPADES);

	private static final Card KING_CLUBS = new Card (Rank.KING, Suit.CLUBS);

	private Game game;

	@Before
	public void setUp() throws Exception {
		game = new Game(new AntennaHandler(new CardDatabase()), false);
		game.setBlindPosition(BLIND_DIRECTION);
		game.initPlayingPhase(BLIND_DIRECTION);
	}

	@Test
	public void testInitPlayingPhase() {
		assertEquals (DUMMY_DIRECTION, game.getDummyPosition());
		assertEquals(Direction.EAST, game.turn);
	}
	
	@Test
	public void testScanCardIntoHand() {
		game.scanCardIntoHand(BLIND_DIRECTION, ACE_SPADES);
		Player[] players = game.getPlayers();
		assertTrue(players[BLIND_DIRECTION.ordinal()].getHand().containsCard(ACE_SPADES));
	}
	
	@Test 
	public void testScanBlindPlayerCard() {
		game.cardFound(BLIND_DIRECTION, ACE_SPADES);
		Player[] players = game.getPlayers();
		assertTrue(players[BLIND_DIRECTION.ordinal()].getHand().containsCard(ACE_SPADES));
	}

	@Test 
	public void testScanBlindPlayerHand() {
		game.cardFound(BLIND_DIRECTION, ACE_SPADES);
		game.cardFound(BLIND_DIRECTION, ACE_SPADES);  // make sure it doesn't end up in there twice
		game.cardFound(BLIND_DIRECTION, TWO_SPADES);
		game.cardFound(BLIND_DIRECTION, THREE_SPADES);
		game.cardFound(BLIND_DIRECTION, FOUR_SPADES);
		game.cardFound(BLIND_DIRECTION, FIVE_SPADES);
		game.cardFound(BLIND_DIRECTION, SIX_SPADES);
		game.cardFound(BLIND_DIRECTION, SEVEN_SPADES);
		game.cardFound(BLIND_DIRECTION, EIGHT_SPADES);
		game.cardFound(BLIND_DIRECTION, NINE_SPADES);
		game.cardFound(BLIND_DIRECTION, TEN_SPADES);
		game.cardFound(BLIND_DIRECTION, JACK_SPADES);
		game.cardFound(BLIND_DIRECTION, QUEEN_SPADES);
		game.cardFound(BLIND_DIRECTION, KING_SPADES);

		Player[] players = game.getPlayers();
		
		// Check first and last
		assertTrue(players[BLIND_DIRECTION.ordinal()].getHand().containsCard(ACE_SPADES));
		assertTrue(players[BLIND_DIRECTION.ordinal()].getHand().containsCard(KING_SPADES));
		
		assertEquals(GameState.FIRSTCARD, game.gameState);
	}
	
	@Test
	public void testScanOtherHandDuringDealing() {
		game.cardFound(DUMMY_DIRECTION, ACE_SPADES);

		Player[] players = game.getPlayers();
		assertFalse(players[BLIND_DIRECTION.ordinal()].getHand().containsCard(ACE_SPADES));
		assertFalse(players[DUMMY_DIRECTION.ordinal()].getHand().containsCard(ACE_SPADES));
		
		assertEquals(GameState.DEALING, game.gameState);
	}
	
	@Test
	public void testPlayingFirstCard() {
		game.setGameState(GameState.FIRSTCARD);
		game.turn = FIRST_PLAYER;
		game.cardFound(FIRST_PLAYER, ACE_SPADES);
		assertEquals(Suit.SPADES, game.currentTrick.getLedSuit());
		assertEquals(ACE_SPADES, game.currentTrick.getCard(FIRST_PLAYER.ordinal()));
		assertEquals(DUMMY_DIRECTION, game.turn);
		assertEquals(GameState.SCANNING_DUMMY, game.gameState);
	}

	@Test
	public void testPlayingFirstCardReadTwice() {
		game.setGameState(GameState.FIRSTCARD);
		game.turn = FIRST_PLAYER;
		game.cardFound(FIRST_PLAYER, ACE_SPADES);
		game.cardFound(FIRST_PLAYER, ACE_SPADES);
		assertEquals(Suit.SPADES, game.currentTrick.getLedSuit());
		assertEquals(ACE_SPADES, game.currentTrick.getCard(FIRST_PLAYER.ordinal()));
		assertEquals(DUMMY_DIRECTION, game.turn);
		assertNull(game.currentTrick.getCard(DUMMY_DIRECTION.ordinal()));
	}

	@Test
	public void testPlayingFirstCardBlindIsDummy() {
		game.initPlayingPhase(BLIND_DIRECTION.getPartner());
		game.setGameState(GameState.FIRSTCARD);
		Direction firstPlayer = BLIND_DIRECTION.getPreviousDirection(); 
		game.turn = firstPlayer;
		game.cardFound(firstPlayer, ACE_SPADES);
		assertEquals(Suit.SPADES, game.currentTrick.getLedSuit());
		assertEquals(ACE_SPADES, game.currentTrick.getCard(firstPlayer.ordinal()));
		assertEquals(BLIND_DIRECTION, game.turn);
		assertEquals(GameState.PLAYING, game.gameState);
	}
	
	@Test
	public void testScanningDummy() {
		game.setGameState(GameState.SCANNING_DUMMY);
		game.turn = DUMMY_DIRECTION;
		game.cardFound(DUMMY_DIRECTION, ACE_SPADES);
		
		Player[] players = game.getPlayers();
		assertTrue(players[DUMMY_DIRECTION.ordinal()].getHand().containsCard(ACE_SPADES));
		assertEquals(GameState.SCANNING_DUMMY, game.gameState);
	}

	@Test 
	public void testScanDummyHand() {
		game.setGameState(GameState.SCANNING_DUMMY);
		game.turn = DUMMY_DIRECTION;

		game.cardFound(DUMMY_DIRECTION, ACE_SPADES);
		game.cardFound(DUMMY_DIRECTION, ACE_SPADES);  // make sure it doesn't end up in there twice
		game.cardFound(DUMMY_DIRECTION, TWO_SPADES);
		game.cardFound(DUMMY_DIRECTION, THREE_SPADES);
		game.cardFound(DUMMY_DIRECTION, FOUR_SPADES);
		game.cardFound(DUMMY_DIRECTION, FIVE_SPADES);
		game.cardFound(DUMMY_DIRECTION, SIX_SPADES);
		game.cardFound(DUMMY_DIRECTION, SEVEN_SPADES);
		game.cardFound(DUMMY_DIRECTION, EIGHT_SPADES);
		game.cardFound(DUMMY_DIRECTION, NINE_SPADES);
		game.cardFound(DUMMY_DIRECTION, TEN_SPADES);
		game.cardFound(DUMMY_DIRECTION, JACK_SPADES);
		game.cardFound(DUMMY_DIRECTION, QUEEN_SPADES);
		game.cardFound(DUMMY_DIRECTION, KING_SPADES);

		Player[] players = game.getPlayers();
		
		// Check first and last
		assertTrue(players[DUMMY_DIRECTION.ordinal()].getHand().containsCard(ACE_SPADES));
		assertTrue(players[DUMMY_DIRECTION.ordinal()].getHand().containsCard(KING_SPADES));
		
		assertEquals(GameState.PLAYING, game.gameState);
	}
	
	@Test
	public void testPlaying() {
		game.setGameState(GameState.SCANNING_DUMMY);
		game.turn = DUMMY_DIRECTION;
		game.cardFound(DUMMY_DIRECTION, ACE_SPADES);

		game.setGameState(GameState.PLAYING);
		game.turn = DUMMY_DIRECTION;
		game.cardFound(DUMMY_DIRECTION, ACE_SPADES);
		
		Player[] players = game.getPlayers();
		assertFalse(players[DUMMY_DIRECTION.ordinal()].getHand().containsCard(ACE_SPADES));
		assertEquals(ACE_SPADES, game.currentTrick.getCard(DUMMY_DIRECTION.ordinal()));
		assertEquals(DUMMY_DIRECTION.getNextDirection(), game.turn);
	}
	
	@Test
	public void testPlayingTwice() {
		game.setGameState(GameState.SCANNING_DUMMY);
		game.turn = DUMMY_DIRECTION;
		game.cardFound(DUMMY_DIRECTION, ACE_SPADES);

		game.setGameState(GameState.PLAYING);
		game.turn = DUMMY_DIRECTION;
		game.cardFound(DUMMY_DIRECTION, ACE_SPADES);
		game.cardFound(DUMMY_DIRECTION, ACE_SPADES);
		
		Player[] players = game.getPlayers();
		assertFalse(players[DUMMY_DIRECTION.ordinal()].getHand().containsCard(ACE_SPADES));
		assertEquals(ACE_SPADES, game.currentTrick.getCard(DUMMY_DIRECTION.ordinal()));
		assertEquals(DUMMY_DIRECTION.getNextDirection(), game.turn);
	}
	
	@Test
	public void testPlayingOutOfOrder() {
		game.setGameState(GameState.PLAYING);
		game.turn = DUMMY_DIRECTION;
		game.cardFound(DUMMY_DIRECTION.getNextDirection(), ACE_SPADES);
		
		Player[] players = game.getPlayers();
		assertEquals(null, game.currentTrick.getCard(DUMMY_DIRECTION.ordinal()));
		assertEquals(null, game.currentTrick.getCard(DUMMY_DIRECTION.getNextDirection().ordinal()));
	}
	
	@Test
	public void testPlayingTrick() {
		game.setGameState(GameState.PLAYING);
		game.turn = DUMMY_DIRECTION;
		game.cardFound(DUMMY_DIRECTION, KING_SPADES);
		game.cardFound(DUMMY_DIRECTION.getNextDirection(), ACE_SPADES);
		game.cardFound(DUMMY_DIRECTION.getPartner(), QUEEN_SPADES);
		game.cardFound(DUMMY_DIRECTION.getPreviousDirection(), JACK_SPADES);
		
		Player[] players = game.getPlayers();
		assertEquals(1, players[DUMMY_DIRECTION.getNextDirection().ordinal()].getTricksWon());
		assertEquals(DUMMY_DIRECTION.getNextDirection(), game.turn);
	}
	
	@Test
	public void testPlayingTwoTricks() {
		game.setGameState(GameState.PLAYING);
		game.turn = DUMMY_DIRECTION;
		
		game.cardFound(DUMMY_DIRECTION, ACE_SPADES);
		game.cardFound(DUMMY_DIRECTION.getNextDirection(), KING_SPADES);
		game.cardFound(DUMMY_DIRECTION.getPartner(), QUEEN_SPADES);
		game.cardFound(DUMMY_DIRECTION.getPreviousDirection(), JACK_SPADES);

		assertEquals(DUMMY_DIRECTION, game.turn);
		assertTrue(game.getCurrentTrick().isOver());

		game.cardFound(DUMMY_DIRECTION, TEN_SPADES);
		game.cardFound(DUMMY_DIRECTION.getNextDirection(), NINE_SPADES);
		game.cardFound(DUMMY_DIRECTION.getPartner(), EIGHT_SPADES);
		game.cardFound(DUMMY_DIRECTION.getPreviousDirection(), SEVEN_SPADES);

		assertEquals(DUMMY_DIRECTION, game.turn);
		assertTrue(game.getCurrentTrick().isOver());
		Player[] players = game.getPlayers();
		assertEquals(2, players[DUMMY_DIRECTION.ordinal()].getTricksWon());
		
	}
	
	@Test
	public void testNoAccidentalScanningWhenCollectingTrick() {
		game.setGameState(GameState.PLAYING);
		game.turn = DUMMY_DIRECTION;
		game.cardFound(DUMMY_DIRECTION, ACE_SPADES);
		game.cardFound(DUMMY_DIRECTION.getNextDirection(), KING_SPADES);
		game.cardFound(DUMMY_DIRECTION.getPartner(), QUEEN_SPADES);
		game.cardFound(DUMMY_DIRECTION.getPreviousDirection(), JACK_SPADES);
		
		Player[] players = game.getPlayers();
		assertEquals(DUMMY_DIRECTION, game.turn);
		
		game.cardFound(DUMMY_DIRECTION, ACE_SPADES);
		assertEquals(DUMMY_DIRECTION, game.turn);
		assertTrue(game.getCurrentTrick().isOver());
	}
	
	@Test
	public void testIllegalCard() {
		game.scanCardIntoHand(BLIND_DIRECTION, ACE_SPADES);

		game.setGameState(GameState.PLAYING);
		game.turn = BLIND_DIRECTION.getPreviousDirection();
		game.cardFound(game.turn, KING_SPADES);
		
		// This is illegal since blind player has spades
		game.cardFound(BLIND_DIRECTION, KING_CLUBS);
		
		Player[] players = game.getPlayers();
		assertEquals(BLIND_DIRECTION, game.turn);
		assertNull(game.getCurrentTrick().getCard(BLIND_DIRECTION.ordinal()));
	}
	
	@Test
	public void testUndo() {
		fail("Not yet implemented");
	}

	@Test
	public void testUndoTrick() {
		fail("Not yet implemented");
	}

}
