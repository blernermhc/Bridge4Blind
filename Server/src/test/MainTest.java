package test;

import gui.GameGUI;
import audio.AudibleGameListener;
import model.Card;
import model.Direction;
import model.Game;
import model.Rank;
import model.Suit;

public class MainTest {
	
/*	private static final Direction DUMMY_DIRECTION = Direction.SOUTH;
	private static final Direction FIRST_PLAYER = Direction.EAST;
	private static final Direction BLIND_DIRECTION = Direction.NORTH;
	private static final Card ACE_SPADES = new Card (Rank.ACE, Suit.SPADES);
	private static final Card TWO_SPADES = new Card (Rank.DEUCE, Suit.SPADES);
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

	private static final Card KING_CLUBS = new Card (Rank.KING, Suit.CLUBS);*/

	private Game game;
	
	public MainTest(){
		
		game = new Game();
		//game.setBlindPosition(BLIND_DIRECTION);
		//game.initPlayingPhase(BLIND_DIRECTION);
		
		GameGUI gui = new GameGUI(game);
		game.addListener(new AudibleGameListener());
		game.addListener(gui);
	}
	
	public static void main(String[] args) {
		
		new MainTest() ;
		
	}

}
