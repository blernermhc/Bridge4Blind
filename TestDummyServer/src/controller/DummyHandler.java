package controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.JPanel;

import test.TestCards;
import model.Card;
import model.CardDatabase;
import model.Direction;

public class DummyHandler implements KeyListener {
	
	private CardDatabase cards;
	
	private CardListener[] hands = new CardListener[Direction.values().length];
	
	private CardListener iDListen;
	
	protected boolean cardRequestSent = false;
	
	private final TestCards testCards = new TestCards() ;
	
	private int position ;

	
	// Id of the current player antenna
	private String turnId;

	public DummyHandler(CardDatabase cardDatabase) {
		
		super() ;
		
		cards = cardDatabase ;
	}

	public void connect() {

		System.out.println("Dummy Connection has been made");
		
	}

	public void addHandListener(CardListener listener,Direction direction) {

		hands[direction.ordinal()] = listener;
		
	}

	public void addIdListener(CardIdentifier listener) {

		iDListen = listener;
		
	}

	public void switchHand(Direction turn) {


		System.out.println("Switching hand to " + turn + " ***");
		
		turnId = getDirectionCode(turn);

		switchHand(turnId);
		
	}
	
	/**
	 * Send a command to the hardware to server to switch which antenna it is
	 * listening to
	 * @param handID the antenna to listen to
	 * @throws IOException the connection failed
	 * @throws InterruptedException the thread was interrupted while waiting for
	 *   a card request command to happen
	 */
	protected void switchHand(String handID) {
		
			cardRequestSent = false;
		
		
	}
	
	/**
	 * Converts from a direction to the command to send to the hardware server
	 * @param turn the antenna to switch to
	 * @return the command to send
	 */
	private String getDirectionCode(final Direction turn) {
		switch(turn){
			case WEST:
				return "W";
			case NORTH:
				return "N";
			case EAST:
				return "E";
			default:
				return "S";
		}
	}

	public void quitServer() {

		System.out.println("Dummy Server quit");
		
	}


	@Override
	public void keyPressed(KeyEvent e) {
		
		if(hands == null ){
			
			System.out.println("Hands is null");
			
			return ;
		}
		
		System.out.println("position " + position);
		
		if(hands[position] == null ){
			
			System.out.println("Hands[" + position + "] is null");
			
			return ;
		}

		if(e.getKeyCode() == KeyEvent.VK_SPACE){
			
			
			cardRequestSent = true ;
			
			Card nextCard = testCards.getNextCard() ;
			
			hands[position].cardFound(nextCard);
			
			position = (position+1)%4 ;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
