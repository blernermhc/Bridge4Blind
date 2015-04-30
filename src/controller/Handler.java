package controller;

import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.UnknownHostException;

import model.Card;
import model.Direction;
import model.Game;

/**
 * This abstract class can either be an AntennaHandler that actually reads RFID
 * tags from playing cards or a DummyHandler. It needs to implement Runnable
 * because AntennaHandler needs to implement Runnable and it needs to implement
 * KeyListener because DummyHandler needs to implement KeyListener.
 * 
 * @author Humaira Orchee
 * @version March 12, 2015
 *
 */
public abstract class Handler implements Runnable, KeyListener {

	// direction of the blind player
	protected Direction blindDirection;

	// the bridge game
	protected Game game;

	/* CardListeners the hands and the idListener */
	protected CardListener[] hands = new CardListener[Direction.values().length];

	/**
	 * Connects to the server
	 * 
	 * @throws UnknownHostException
	 *             host name for the server is unknown
	 * @throws IOException
	 *             could not complete the connection
	 */
	public abstract void connect() throws UnknownHostException, IOException;
	
	/**
	 * Repeatedly sends card requests to the server.
	 */
	public abstract void run() ;
	

	/**
	 * Adds a listener to the Hand array
	 * @param listener the listener to be added
	 * @param direction the antenna to listen to
	 */
	public void addHandListener(CardListener listener,Direction direction){
		hands[direction.ordinal()] = listener;
	}

	/**
	 * Sends a command to the server requesting it to tell what card is on the
	 * current antenna
	 * 
	 * @param messageRec
	 *            the buffer to fill with the server response
	 * @return the server response
	 * @throws IOException
	 *             the connection failed
	 */
	public abstract String requestCard(byte[] messageRec) throws IOException;



	/**
	 * Adds the special IDCard listener
	 * @param listener - lister to be added
	 */
	public void addIdListener(CardListener listener){
	//	iDListen = listener;
	}

	/**
	 * Sends the quit command to the C# server. Closes the socket. Stops the
	 * cycling timer.
	 */
	public abstract void quitServer();

	/**
	 * Sends a command to the hardware to server to switch which antenna it is
	 * listening to
	 * 
	 * @param handID
	 *            the antenna to listen to
	 * @throws IOException
	 *             the connection failed
	 * @throws InterruptedException
	 *             the thread was interrupted while waiting for a card request
	 *             command to happen
	 */
	public abstract void switchHand(String handID) throws IOException,
			InterruptedException;

	/**
	 * Switch the hardware to an antenna
	 * 
	 * @param turn
	 *            the antenna to listen to
	 * @throws IOException
	 *             if the connection to the server is lost.
	 * @throws InterruptedException
	 */
	public abstract void switchHand(final Direction turn) throws IOException,
			InterruptedException;

	/**
	 * 
	 * @param blindDirection
	 */
	public void setBlindDirection(Direction blindDirection) {

		this.blindDirection = blindDirection;

	}


	/**
	 * 
	 * @param game
	 */
	public void setGame(Game game) {
		this.game = game;
	}
	
	public abstract void setCyclingThread(Thread cyclingThread) ;
	

}
