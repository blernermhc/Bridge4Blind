package gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import model.Card;
import model.CardDatabase;
import model.Contract;
import model.Direction;
import model.Game;
import model.Player;
import model.Suit;
import model.Trick;
import audio.AudioPlayer;
import audio.SoundManager;
import controller.AntennaHandler;

/**
 * Manages the input from the numeric keypad.
 * 
 * @author Barbara Lerner
 * @version March 12, 2015
 *
 */
public class KeyPad extends KeyAdapter {
	private static final int ENTER_CODE = 10;
	private static final int ZERO_CODE = 96;
	private static final int ONE_CODE = 97;
	private static final int TWO_CODE = 98;
	private static final int THREE_CODE = 99;
	private static final int FOUR_CODE = 100;
	private static final int FIVE_CODE = 101;
	private static final int SIX_CODE = 102;
	private static final int PLUS_CODE = 107;
	private static final int SEVEN_CODE = 103;
	private static final int EIGHT_CODE = 104;
	private static final int NINE_CODE = 105;
	private static final int DASH_CODE = 109;
	private static final int TAB_CODE = 9;
	private static final int BACKSLASH_CODE = 111;
	private static final int ASTERISK_CODE = 106;
	private static final int BACKSPACE_CODE = 8;
	private static final int SPACE_CODE = 32;

	private boolean ignoringKeys = false;

	private int lastCode = 0;
	
	private char lastKeyChar ;

	private SoundManager soundMgr = SoundManager.getInstance();

	// the tutorial audio player
	private AudioPlayer tutorialPlayer;

	/**
	 * 
	 */
	private final GameGUI gameGUI;

	private final Game game;

	/**
	 * Initializes the keypad.
	 * 
	 * @param gameGUI
	 *            the gui controlling the game
	 * @param game
	 *            the game being played
	 */
	public KeyPad(GameGUI gameGUI, Game game) {
		this.gameGUI = gameGUI;
		this.game = game;
		tutorialPlayer = new AudioPlayer();
		tutorialPlayer.init("/sounds/orientation/tutorial.WAV");
	}
//	KEY PRESSED WAS NOT WORKING PROPERLY BECAUSE DEPENDING ON HOW LONG A KEY IS PRESSED OR IF IT IS PRESSED OR TYPED, THE VALUE OF THE KEY CHANGED
//	/**
//	 * Called when a key is pressed
//	 */
//	@Override
//	public void keyPressed(KeyEvent e) {
//		
//		System.out.println("-----------------------KEY PRESSED---------------------");
//
//		// System.out.println("key pressed");
//		if (!ignoringKeys) {
//			gameGUI.debugMsg("key pressed");
//			//interpretKeyCode(e.getKeyCode());			
//			
//		}
//		
//		
//
//	}
	
	// KEY TYPED WORKS PROPERLY
	
	@Override
	public void keyTyped(KeyEvent e){
		
//		System.out.println("-----------------------KEY TYPED---------------------");
		
//		System.out.println(e.getKeyCode());
//		System.out.println(e.getKeyChar());
//		System.out.println(e.getKeyLocation());
//		System.out.println(e.getExtendedKeyCode());
//		System.out.println(e.getSource());
		
		//System.out.println("tab ? " + (e.getKeyChar() == KeyEvent.VK_TAB));
		//System.out.println("back? " + (e.getKeyChar() == KeyEvent.VK_BACK_SPACE));
		//System.out.println("enter? " + (e.getKeyChar() == KeyEvent.VK_ENTER));
		
		// multiple keys had the same keycode (144) so the interpretKeyCode method did not work properly/
		interpretKeyChar(e.getKeyChar()) ;
	}

	/**
	 * Interpets a keycode as a command.
	 * 
	 * @param keyCode
	 *            The keycode being interpreted.
	 */
	protected void interpretKeyCode(int keyCode) {

		if (keyCode != lastCode || !ignoringKeys) {

			if (game.getBlindPosition() != null) {

				Player blindPlayer = game.getBlindPlayer();

				// if the backspace key was pressed
				if (blindPlayer != null) {
					if (keyCode == TAB_CODE) {

						// read the visually impaired player's clubs
						gameGUI.debugMsg("Own clubs:");
						// printCards(Suit.CLUBS, blindPlayer);
						readBlindSuit(Suit.CLUBS, blindPlayer);

						// if the asterisk was pressed
					} else if (keyCode == BACKSLASH_CODE) {

						// read the visually impaired player's diamonds
						// gameGUI.debugMsg("Own diamonds:");
						// printCards(Suit.DIAMONDS, blindPlayer);
						readBlindSuit(Suit.DIAMONDS, blindPlayer);

						// if the backslash was pressed
					} else if (keyCode == ASTERISK_CODE) {

						// read the visually impaired player's hearts
						gameGUI.debugMsg("Own hearts:");
						// printCards(Suit.HEARTS, blindPlayer);
						readBlindSuit(Suit.HEARTS, blindPlayer);

						// if the tab key was pressed
					} else if (keyCode == BACKSPACE_CODE) {

						// read the visually impaired player's spades
						gameGUI.debugMsg("Own spades:");
						// printCards(Suit.SPADES, blindPlayer);
						readBlindSuit(Suit.SPADES, blindPlayer);

						// if the four key was pressed
					} else if (keyCode == FOUR_CODE) {

						// read the VI player's entire hand
						gameGUI.debugMsg("Own hand:");
						readBlindHand(blindPlayer);
					}
				}
			}

			System.out.println("Dummy Position " + game.getDummyPosition());
			
			if (game.getDummyPosition() != null) {
				Player dummyPlayer = game.getDummyPlayer();
//				
//				System.out.println("Dummy Player " + game.getDummyPlayer());
//				
//				System.out.println("keyCode " + keyCode);
//				
//				System.out.println("DASH " + DASH_CODE);
//				
//				System.out.println("NINE " + NINE_CODE);
//				
//				System.out.println("EIGHT " + EIGHT_CODE);
//				
//				System.out.println("SEVEN " + SEVEN_CODE);

				if (keyCode == SEVEN_CODE) {

					// read the dummy's clubs
					gameGUI.debugMsg("Dummy clubs:");
					
					System.out.println("Dummy Clubs");
					
					// printCards(Suit.CLUBS, dummyPlayer);
					readDummySuit(Suit.CLUBS, dummyPlayer);

					// if the 9 was pressed
				} else if (keyCode == EIGHT_CODE) {

					// read the dummy's diamonds
					gameGUI.debugMsg("Dummy diamonds:");
					
					System.out.println("Dummy diamonds");
					
					// printCards(Suit.DIAMONDS, dummyPlayer);
					readDummySuit(Suit.DIAMONDS, dummyPlayer);

					// if the 8 was pressed
				} else if (keyCode == NINE_CODE) {

					// read the dummy's hearts
					gameGUI.debugMsg("Dummy hearts:");
					
					System.out.println("Dummy hearts");
					// printCards(Suit.HEARTS, dummyPlayer);
					readDummySuit(Suit.HEARTS, dummyPlayer);

					// if the 7 was pressed
				} else if (keyCode == DASH_CODE) {

					// read the dummy's spades
					gameGUI.debugMsg("Dummy spades:");
					
					System.out.println("Dummy spades");
					// printCards(Suit.SPADES, dummyPlayer);
					readDummySuit(Suit.SPADES, dummyPlayer);

					// if the five key was pressed
				} else if (keyCode == FIVE_CODE) {

					// read the dummy's entire hand
					gameGUI.debugMsg("Dummy hand:");
					
					System.out.println("Dummy Hand");
					
					readDummyHand(dummyPlayer);
				}
			}

			if (keyCode == PLUS_CODE) {

				// read the cards in the current trick
				gameGUI.debugMsg("Current trick");
				readTrick();

				// if the 1 was pressed
			} else if (keyCode == ONE_CODE) {

				// read the contract
				gameGUI.debugMsg("Contract");
				playContract(game.getContract());

				// if the 2 was pressed
			} else if (keyCode == TWO_CODE) {

				// read N/S's current tricks won
				gameGUI.debugMsg("N/S tricks won");
				playTricksWonNS();

				// if the 3 was pressed
			} else if (keyCode == THREE_CODE) {

				// read E/W's current tricks won
				gameGUI.debugMsg("E/W tricks won");
				playTricksWonEW();

				// if the 0 was pressed
			} else if (keyCode == ZERO_CODE) {

				// repeat the last thing said
				gameGUI.debugMsg("Repeat");
				soundMgr.playLastSound();

				// if the enter key is pressed then play the blid player's card
			} else if (keyCode == ENTER_CODE) {
				//playTutorial();
				
				System.out.println("################ Enter is pressed #########################");
				
				try{
				game.playBlindCard();
				}catch(NullPointerException e){
					
					System.err.println("Blind person pressed the Enter button by mistake");
				}
				
			} else {

				if (keyCode != SPACE_CODE) {
					gameGUI.debugMsg("Unexpected key: " + keyCode);
				}
			}

			lastCode = keyCode;
			// start ignoring key presses
			ignoringKeys = true;

			// create a timer to stop ignoring after 1 second
			Timer t = new Timer(true);
			t.schedule(new TimerTask() {
				@Override
				public void run() {
					ignoringKeys = false;
				}

			}, 1000);

		}

	}

	private void playTutorial() {
		// gameGUI.debugMsg("Tutorial");

		// if the tutorial is already playing, stop it
		// is this code executing?
		if (soundMgr.isPlaying()) {

			// ap.stop();
			soundMgr.requestStop();
			gameGUI.debugMsg("tutorial stopped");

		} else if (tutorialPlayer.isPlaying()) {

			tutorialPlayer.stop();

		} else {

			tutorialPlayer.play();

			// gameGUI.debugMsg("tutorial playing");

		}
	}

	private void playTricksWonNS() {
		int numTricksWon = game.getTricksWon(Direction.NORTH)
				+ game.getTricksWon(Direction.SOUTH);

		playTricksWon("/sounds/bidding/northsouth.WAV", numTricksWon);

	}

	private void playTricksWonEW() {
		int numTricksWon = game.getTricksWon(Direction.EAST)
				+ game.getTricksWon(Direction.WEST);

		playTricksWon("/sounds/bidding/eastwest.WAV", numTricksWon);

	}

	private void playTricksWon(String teamFile, int numTricksWon) {
		soundMgr.clearSounds();
		soundMgr.addSound(teamFile);

		String numberSound = "/sounds/bidding/";

		numberSound += numTricksWon;
		numberSound += ".WAV";

		soundMgr.addSound(numberSound);
		soundMgr.addSound("/sounds/bidding/tricks.WAV");
		soundMgr.playSounds();
	}

	private void readBlindHand(Player p) {
		soundMgr.addSound("/sounds/ownership/you2.wav");
		readHand(p);
		soundMgr.playSounds();
	}

	// TODO: Need to test this
	private void readBlindSuit(Suit s, Player p) {
		// add the appropriate ownership sound
		soundMgr.addSound("/sounds/ownership/you2.wav");

		readCardsOfSuit(s, p);
		soundMgr.playSounds();
	}

	private void readDummyHand(Player p) {
		soundMgr.addSound("/sounds/ownership/dummy2.wav");
		readHand(p);
		soundMgr.playSounds();
	}

	private void readDummySuit(Suit s, Player p) {
		// add the appropriate ownership sound
		soundMgr.addSound("/sounds/ownership/dummy2.wav");

		readCardsOfSuit(s, p);
		soundMgr.playSounds();
	}

	private void readHand(Player p) {
		readCardsOfSuit(Suit.CLUBS, p);
		pause();

		// Can't get the separator sounds to play.
		// soundMgr.addSound("/sounds/warnings/pop.WAV");

		readCardsOfSuit(Suit.DIAMONDS, p);
		pause();
		// soundMgr.addSound("/sounds/warnings/separationbeep.WAV");

		readCardsOfSuit(Suit.HEARTS, p);
		pause();
		// soundMgr.addSound("/sounds/warnings/separationbeep.WAV");

		readCardsOfSuit(Suit.SPADES, p);
	}

	private void pause() {
		soundMgr.playSounds();
		soundMgr.pauseSounds();
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
		}
	}

	private void readCardsOfSuit(Suit s, Player p) {
		// add the appropriate number
		int num = p.getNumOfSuit(s);

		soundMgr.addSound("/sounds/bidding/" + num + ".WAV");
		soundMgr.addSound(s.getSound());

		// walk over every card in the suit
		Iterator<Card> cardIter = p.cards(s);
		while (cardIter.hasNext()) {
			// add its sound string to the vector
			Card c = cardIter.next();
			soundMgr.addSound(c.getRank().getSound());
		}

	}

	/**
	 * Play the current contract
	 * 
	 * @param contract
	 *            the current contract
	 */
	private void playContract(Contract contract) {
		if (contract == null) {
			return;
		}

		soundMgr.addSound("/sounds/bidding/contractis.WAV");
		soundMgr.addSound("/sounds/bidding/" + contract.getContractNum()
				+ ".WAV");
		soundMgr.addSound("/sounds/suits/" + contract.getTrump() + ".WAV");

		switch (contract.getBidWinner()) {

		case SOUTH:
			soundMgr.addSound("/sounds/directions/south.WAV");
			break;
		case WEST:
			soundMgr.addSound("/sounds/directions/west.WAV");
			break;
		case NORTH:
			soundMgr.addSound("/sounds/directions/north.WAV");
			break;
		case EAST:
			soundMgr.addSound("/sounds/directions/east.WAV");
			break;

		}

		soundMgr.playSounds();
		soundMgr.pauseSounds();
	}

	private void readTrick() {

		Trick trick = game.getCurrentTrick();
		if (trick == null || trick.isEmpty()) {
			return;
		}

		Direction start; // Direction that played the lead card in the trick

		// If this is not the first hand, the winner of the last hand led the
		// trick.
		if (game.getLastWinner() != null) {
			start = game.getLastWinner();
		}

		// If this is the first hand, the player who precedes the dummy hand
		// plays
		// the lead card
		else if (game.getDummyPosition() != null) {
			start = game.getDummyPosition().getPreviousDirection();
		}

		// If the dummy hand is unknown, it means that bidding is not complete
		// so there is no trick to read.
		else {
			return;
		}

		// System.out.println("Start = " + start);

		int nextPlayer = start.ordinal();
		for (int i = 0; i < 4; i++) {

			Card nextCard = trick.getCard(nextPlayer);
			if (nextCard != null) {
				switch (Direction.values()[nextPlayer]) {
				case NORTH:
					soundMgr.addSound("/sounds/directions/north.WAV");
					break;
				case EAST:
					soundMgr.addSound("/sounds/directions/east.WAV");
					break;
				case SOUTH:
					soundMgr.addSound("/sounds/directions/south.WAV");
					break;
				case WEST:
					soundMgr.addSound("/sounds/directions/west.WAV");
					break;

				}

				soundMgr.addSound(trick.getCard(nextPlayer).getSound());
			}
			nextPlayer = (nextPlayer + 1) % 4;
		}

		soundMgr.playSounds();
	}

//	/**
//	 * Tests the keypad
//	 * 
//	 * @param args
//	 *            none
//	 */
//	public static void main(String[] args) {
//		Game game = new Game(new AntennaHandler(new CardDatabase()), false);
//		game.setBlindPosition(Direction.EAST);
//		KeyPad keypad = new KeyPad(null, game);
//		Player blindPlayer = game.getPlayers()[game.getBlindPosition()
//				.ordinal()];
//		Player dummyPlayer = game.getPlayers()[1];
//
//		System.out.println("Should say \"Contract is 3 No Trump NORTH\"");
//		Contract contract = new Contract();
//		contract.setBidWinner(Direction.NORTH);
//		contract.setContractNum(3);
//		contract.setTrump(Suit.NOTRUMP);
//		keypad.playContract(contract);
//		keypad.soundMgr.pauseSounds();
//
//		System.out.println("Should say \"East West team has one no tricks\"");
//		keypad.playTricksWonEW();
//		keypad.soundMgr.pauseSounds();
//
//		System.out.println("Should say \"North South team has one no tricks\"");
//		keypad.playTricksWonNS();
//		keypad.soundMgr.pauseSounds();
//
//		System.out.println("Should say \"You have no clubs\"");
//		keypad.readBlindSuit(Suit.CLUBS, blindPlayer);
//		keypad.soundMgr.pauseSounds();
//
//		System.out.println("Should say \"You have no diamonds\"");
//		keypad.readBlindSuit(Suit.DIAMONDS, blindPlayer);
//		keypad.soundMgr.pauseSounds();
//
//		System.out.println("Should say \"You have no hearts\"");
//		keypad.readBlindSuit(Suit.HEARTS, blindPlayer);
//		keypad.soundMgr.pauseSounds();
//
//		System.out.println("Should say \"You have no spades\"");
//		keypad.readBlindSuit(Suit.SPADES, blindPlayer);
//		keypad.soundMgr.pauseSounds();
//
//		System.out.println("Should say \"Dummy has no clubs\"");
//		keypad.readDummySuit(Suit.CLUBS, dummyPlayer);
//		keypad.soundMgr.pauseSounds();
//
//		System.out.println("Should say \"Dummy has no diamonds\"");
//		keypad.readDummySuit(Suit.DIAMONDS, dummyPlayer);
//		keypad.soundMgr.pauseSounds();
//
//		System.out.println("Should say \"Dummy has no hearts\"");
//		keypad.readDummySuit(Suit.HEARTS, dummyPlayer);
//		keypad.soundMgr.pauseSounds();
//
//		System.out.println("Should say \"Dummy has no spades\"");
//		keypad.readDummySuit(Suit.SPADES, dummyPlayer);
//		keypad.soundMgr.pauseSounds();
//
//		// Not tested because it's not easy to add a card to a trick.
//		// keypad.readTrick();
//		// keypad.soundMgr.pauseSounds();
//
//		keypad.readBlindHand(blindPlayer);
//		keypad.soundMgr.pauseSounds();
//
//		keypad.readDummyHand(dummyPlayer);
//		keypad.soundMgr.pauseSounds();
//
//		keypad.interpretKeyCode(ASTERISK_CODE);
//
//		// This one should not play -- it should be like a repeated key.
//		keypad.interpretKeyCode(ASTERISK_CODE);
//		try {
//			Thread.sleep(1100);
//			// This one should play because it is after a long pause.
//			keypad.interpretKeyCode(ASTERISK_CODE);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		keypad.playTutorial();
//		try {
//			Thread.sleep(2000);
//			// This should stop the tutorial
//			keypad.playTutorial();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
	
	protected void interpretKeyChar(char keyCharacter) {

		if (keyCharacter != KeyEvent.VK_0 || !ignoringKeys) {

			if (game.getBlindPosition() != null) {

				Player blindPlayer = game.getBlindPlayer();

				// if the backspace key was pressed
				if (blindPlayer != null) {
					readBlindPlayerHand(keyCharacter, blindPlayer);
				}
			}
			
			if (game.getDummyPosition() != null) {
				readDummyHand(keyCharacter);
			}

			if (keyCharacter == KeyEvent.VK_PLUS) {

				// read the cards in the current trick
				gameGUI.debugMsg("Current trick");
				readTrick();

				// if the 1 was pressed
			} else if (keyCharacter == KeyEvent.VK_1) {

				// read the contract
				gameGUI.debugMsg("Contract");
				playContract(game.getContract());

				// if the 2 was pressed
			} else if (keyCharacter == KeyEvent.VK_2) {

				// read N/S's current tricks won
				gameGUI.debugMsg("N/S tricks won");
				playTricksWonNS();

				// if the 3 was pressed
			} else if (keyCharacter == KeyEvent.VK_3) {

				// read E/W's current tricks won
				gameGUI.debugMsg("E/W tricks won");
				playTricksWonEW();

				// if the 0 was pressed
			} else if (keyCharacter == KeyEvent.VK_0) {

				// repeat the last thing said
				gameGUI.debugMsg("Repeat");
				soundMgr.playLastSound();

				// if the enter key is pressed then play the blid player's card
			} else if (keyCharacter == KeyEvent.VK_ENTER) {
				//playTutorial();
				
				System.out.println("################ Enter is pressed #########################");
				
				try{
					game.playBlindCard();
				}catch(NullPointerException e){
					
					System.err.println("Blind person pressed the Enter button by mistake");
				}
				
			} 

			//lastCode = keyCode;
			
			lastKeyChar = keyCharacter ;
			// start ignoring key presses
			ignoringKeys = true;

			// create a timer to stop ignoring after 1 second
			Timer t = new Timer(true);
			t.schedule(new TimerTask() {
				@Override
				public void run() {
					ignoringKeys = false;
				}

			}, 1000);

		}

	}

	private void readDummyHand(char keyCharacter) {
		Player dummyPlayer = game.getDummyPlayer();
		
		if (keyCharacter == KeyEvent.VK_MINUS) {

			// read the dummy's clubs
			gameGUI.debugMsg("Dummy spades:");
			
			
			// printCards(Suit.CLUBS, dummyPlayer);
			readDummySuit(Suit.SPADES, dummyPlayer);

			// if the 9 was pressed
		} else if (keyCharacter == KeyEvent.VK_9) {

			// read the dummy's diamonds
			gameGUI.debugMsg("Dummy hearts:");
			
			// printCards(Suit.DIAMONDS, dummyPlayer);
			readDummySuit(Suit.HEARTS, dummyPlayer);

			// if the 8 was pressed
		} else if (keyCharacter == KeyEvent.VK_8) {

			// read the dummy's hearts
			gameGUI.debugMsg("Dummy diamonds:");
			
			// printCards(Suit.HEARTS, dummyPlayer);
			readDummySuit(Suit.DIAMONDS, dummyPlayer);

			// if the 7 was pressed
		} else if (keyCharacter == KeyEvent.VK_7) {

			// read the dummy's spades
			gameGUI.debugMsg("Dummy clubs:");
			
			
			// printCards(Suit.SPADES, dummyPlayer);
			readDummySuit(Suit.CLUBS, dummyPlayer);

			// if the five key was pressed
		} else if (keyCharacter == KeyEvent.VK_5) {

			// read the dummy's entire hand
			gameGUI.debugMsg("Dummy hand:");
			
			readDummyHand(dummyPlayer);
		}
	}

	private void readBlindPlayerHand(char keyCharacter, Player blindPlayer) {
		
//		System.out.println("multiply is " + KeyEvent.VK_MULTIPLY);
//		System.out.println("asterick is " + KeyEvent.VK_ASTERISK);
//		
//		System.out.println("key char " + keyCharacter);
		
		if (keyCharacter == KeyEvent.VK_BACK_SPACE) {

			// read the visually impaired player's clubs
			gameGUI.debugMsg("Own spades:");
			// printCards(Suit.CLUBS, blindPlayer);
			readBlindSuit(Suit.SPADES, blindPlayer);

			// if the asterisk was pressed
		} else if (keyCharacter == '*') {
			
			// VK_MULTILPLY and VK_ASTERICK did dot work for this else if statement so I hard coded the '*'
			// read the visually impaired player's diamonds
			// gameGUI.debugMsg("Own diamonds:");
			// printCards(Suit.DIAMONDS, blindPlayer);
			readBlindSuit(Suit.HEARTS, blindPlayer);

			// if the backslash was pressed
		} else if (keyCharacter == KeyEvent.VK_SLASH) {

			// read the visually impaired player's hearts
			gameGUI.debugMsg("Own hearts:");
			// printCards(Suit.HEARTS, blindPlayer);
			readBlindSuit(Suit.DIAMONDS, blindPlayer);

			// if the tab key was pressed
		} else if (keyCharacter == KeyEvent.VK_TAB) {

			// read the visually impaired player's spades
			gameGUI.debugMsg("Own spades:");
			// printCards(Suit.SPADES, blindPlayer);
			readBlindSuit(Suit.CLUBS, blindPlayer);

			// if the four key was pressed
		} else if (keyCharacter == KeyEvent.VK_4) {

			// read the VI player's entire hand
			gameGUI.debugMsg("Own hand:");
			readBlindHand(blindPlayer);
		}
	}

}