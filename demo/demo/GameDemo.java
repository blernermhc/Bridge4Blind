package demo;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controller.AntennaHandler;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.Suit;
import model.CardDatabase;
import model.Game;
import model.Player;
import audio.AudioPlayer;

public class GameDemo extends JFrame{
	
	//the game for the demo
	private Game game;
	//the hands in the demo game
	private Player[] players;
	//the deck of cards
	private Deck deck;
	//the position of the next sound to be played
	private int nextSound = 0;
	//the vector of sounds to play
	private Vector<String> toPlay;
	private AudioPlayer ap;
	private Thread thread;
	
	public GameDemo() throws UnknownHostException, IOException{
		
		ap = new AudioPlayer();
		//set the JFrame to exit the program on close
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
		//create a JPanel to place inside the frame
		JPanel demoPanel = new JPanel();
		//create a JLabel to show that the demo is running
		JLabel demoLabel = new JLabel("Demo running...");
		//add the label to the panel
		demoPanel.add(demoLabel);
		//add the panel to the JFrame
		this.add(demoPanel);
		this.pack();
		this.setVisible(true);
		setFocusTraversalKeysEnabled(false);
		
		//initialize the game
		game = new Game(new AntennaHandler(new CardDatabase()), false);
		//get the hands from the game
		players = game.getPlayers();
		//initialize the deck
		deck = new Deck();
		//set the declarer to be East
		game.initPlayingPhase(Direction.EAST);
		
		//deal cards to both the visually impaired player's hand and the dummy
		//hand until both are full
		while(!players[0].hasFullHand() && !players[game.getDummyPosition().ordinal()].hasFullHand()){
			
			players[0].addCard(deck.draw());
			players[game.getDummyPosition().ordinal()].addCard(deck.draw());
			
		}
		
		//add a KeyListener to this JFrame
		this.addKeyListener(new KeyAdapter(){
			
			public void keyPressed(KeyEvent e){
				
				//get the code from the key
				int keyCode = e.getKeyCode();
				
				System.out.println(keyCode);
				
				//if the backspace key was pressed
				if (keyCode == 8){
					
					//read the visually impaired player's spades
					System.out.println("Own spades:");
					printCards(Suit.SPADES, players[0]);
					readCards(Suit.SPADES, players[0], false);
				
				//if the asterisk was pressed
				} else if (keyCode == 106){
					
					//read the visually impaired player's hearts
					System.out.println("Own hearts:");
					printCards(Suit.HEARTS, players[0]);
					readCards(Suit.HEARTS, players[0], false);
					
				//if the backslash was pressed
				} else if (keyCode == 111){
					
					//read the visually impaired player's diamonds
					System.out.println("Own diamonds:");
					printCards(Suit.DIAMONDS, players[0]);
					readCards(Suit.DIAMONDS, players[0], false);
				
				//if the tab key was pressed
				} else if (keyCode == 9){
					
					//read the visually impaired player's clubs
					System.out.println("Own clubs:");
					printCards(Suit.CLUBS, players[0]);
					readCards(Suit.CLUBS, players[0], false);
				
				//if the dash was pressed
				} else if (keyCode == 109){
					
					//read the dummy's spades
					System.out.println("Dummy spades:");
					printCards(Suit.SPADES, players[game.getDummyPosition().ordinal()]);
					readCards(Suit.SPADES, players[game.getDummyPosition().ordinal()], true);
				
				//if the 9 was pressed
				} else if (keyCode == 105){
					
					//read the dummy's hearts
					System.out.println("Dummy hearts:");
					printCards(Suit.HEARTS, players[game.getDummyPosition().ordinal()]);
					readCards(Suit.HEARTS, players[game.getDummyPosition().ordinal()], true);
				
				//if the 8 was pressed
				} else if (keyCode == 104){
					
					//read the dummy's diamonds
					System.out.println("Dummy diamonds:");
					printCards(Suit.DIAMONDS, players[game.getDummyPosition().ordinal()]);
					readCards(Suit.DIAMONDS, players[game.getDummyPosition().ordinal()], true);
				
				//if the 7 was pressed
				} else if (keyCode == 103){
					
					//read the dummy's clubs
					System.out.println("Dummy clubs:");
					printCards(Suit.CLUBS, players[game.getDummyPosition().ordinal()]);
					readCards(Suit.CLUBS, players[game.getDummyPosition().ordinal()], true);
					
				}
				
			}
			
		});
		
	}
	
	//Prints out a list of cards in the suit and hand specified.
	private void printCards(Suit s, Player players2){
		Iterator<Card> cardIter = players2.cards();
		while (cardIter.hasNext()) {
			
			//if this card is of the correct suit
			Card c = cardIter.next();
			if (c.getSuit() == s) {
				//print its suit and rank
				System.out.println(c.getRank() + " of " + c.getSuit());
				
			}
			
		}
		
	}
	
	//Audibly reads out the cards in the suit and hand specified.
	private void readCards(Suit s, Player players2, boolean dummy){
		
		//initialize the new toPlay vector
		toPlay = new Vector<String>();
		
		//add the appropriate ownership sound
		if (dummy){
			
			toPlay.add("/sounds/ownership/dummy2.WAV");
			
		} else {
			
			toPlay.add("/sounds/ownership/you2.WAV");
			
		}
		
		//add the appropriate number
		int num = players2.getNumOfSuit(s);
		
		if (num == 0){
			
			toPlay.add("/sounds/cards/none.WAV");
			
		} else {
			
			toPlay.add("/sounds/bidding/" + num + ".WAV");
			toPlay.add(s.getSound());
		
			//walk over every card in the hand
			Iterator<Card> cardIter = players2.cards();
			while (cardIter.hasNext()) {
			
				//if the card is of the correct suit
				Card c = cardIter.next();
				if (c.getSuit() == s){
				
					//add its sound string to the vector
					toPlay.add(c.getRank().getSound());
				
				}
			
			}
		}
		thread = new Thread(){
			
			public void run(){
				playNextSound();
			}
			
		};
		
		// Barb commented out
//		thread.start();
//		
//		try {
//			Thread.sleep(100);
//		}
//		catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		while (true){
			playNextSound();

			do {
				//System.out.println("playing");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} while (ap.isPlaying());

			//if the next sound is outside the bounds of the vector
			if (toPlay.size() == nextSound){
				
				//reset the next sound
				nextSound = 0;
				break;
				
			}
			
		}
		
	}
	
	//Plays the next sound in line in the toPlay vector.
	private void playNextSound(){
		//initialize the audio player with the correct sound
		ap.init (toPlay.get(nextSound));
		//play the sound
		ap.play();
		
		//increment the next sound
		nextSound++;
		
	}
	
	public static void main(String[] args){
		
		try {
			new GameDemo();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
