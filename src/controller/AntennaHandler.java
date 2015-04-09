package controller;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import main.BridgeActualGame;
import model.Card;
import model.CardDatabase;
import model.Direction;

/**
 * This AntennaHandler class is a runnable class that listens for Cards to come from the server 
 * and sends them to the apporiate Hands or CardId events
 * 
 * @author Caden Friedenbach
 * @version March 12, 2015
 */
public class AntennaHandler extends Handler {
	// Length of time to pause after requesting a card and not finding one
	private static final int CARD_REQUEST_PAUSE = 200;
	
	// Thread that cycles between the id antenna and current hand antenna
	private Thread cyclingThread;
	
	// Remembers if it is a player antenna or id antenna being listened to
	private boolean onPlayerHand = false;

	// Index where the antenna id starts
	private static final int POSITION = 16;
	
	// Length of the rfid tag identifier
	private static final int ID_LENGTH = 14;
	
	/** Length of entire message from the server **/
	protected static final int MESSAGE_LENGTH = 17;

	/** Stream to send commands to server on **/
	protected OutputStream out;
	
	/** Stream to read input from the server **/
	protected InputStream in;

	/*Port and and host name*/
	private static final int PORT = 6666;
	private static final String HOST = "localhost";

	/*Database of Cards*/
	private CardDatabase cards;
	
	/*CardListeners the hands and the idListener*/
	private CardListener[] hands = new CardListener[Direction.values().length];
	private CardListener iDListen;
	
	/** Condition variable to ensure that at least one card request gets sent between each
	    antenna change. */
	protected boolean cardRequestSent = false;
	
	/** Connection to the server */
	protected Socket requestSocket;
	
	// Id of the current player antenna
	private String turnId;
	

	
	/**
	 * Creates a new AntennaHandler with the appropriate Card Database
	 * @param data the database to be used
	 */
	public AntennaHandler(CardDatabase data){
		this.cards = data;
	}
	
	/**
	 * Connects to the server
	 * @throws UnknownHostException host name for the server is unknown
	 * @throws IOException could not complete the connection
	 */
	public void connect() throws UnknownHostException, IOException {
		System.out.println("Connecting...");
		requestSocket = new Socket(HOST, PORT);
		System.out.println("Connected to " + HOST +" in port " + Integer.toString(PORT));
		
		// get Input and Output streams
		out = requestSocket.getOutputStream();
		in = requestSocket.getInputStream();

	}

	/**
	 * Repeatedly sends card requests to the server.
	 */
	@Override
	public void run()
	{
		try{
			try {
				
				String message;			

				// Repeatedly request cards and handle the responses.
				// Stop if receive a "quit" command from the server.

				do{
					message = waitForMessage();
					if (message.startsWith("quit")) {
						break;
					}
					//System.out.println("Calling process");
					process(message);
					//System.out.println("Returned from process");
					
					// Avoid sending card requests too quickly.
					Thread.sleep(CARD_REQUEST_PAUSE);

				} while(true);

			} finally{
				if (in != null) {
					// Closing connection
					in.close();
					out.close();
					requestSocket.close();
					System.out.println("Everything is closed");
				}
			}
		}
		catch(IOException ioException){
			System.err.println("Lost connection to the server!");
			ioException.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println("Antenna server was interrupted");
			e.printStackTrace();
		}
	}
	
	/**
	 * Request a card from the server.  Return when something other than NOCARD is received.
	 * @return the message received
	 * @throws IOException if the connection fails
	 * @throws InterruptedException if the thread waiting between card requests is interrupted
	 */
	private String waitForMessage() throws IOException, InterruptedException{
		//System.out.println("Card request being sent from thread " + Thread.currentThread().getName());
		byte[] messageRec = new byte[3000];

		String message = "";
		//accept the message
		//System.out.println("Waiting for next Message");
		//set the message to a string
		//System.out.println("Requesting a card");
		while (message.startsWith("NOCARD") || message.equals("")){
			message = requestCard(messageRec);
			
			if (message.startsWith("NOCARD")) {
				//System.out.println("No card");
				Thread.sleep(CARD_REQUEST_PAUSE);
			}
		}
		//System.out.println("Received " + message.substring(0, MESSAGE_LENGTH));
		message = message.substring(0, MESSAGE_LENGTH);
		return message;
		
	}

	/**
	 * Sends a command to the server requesting it to tell what card is on the 
	 * current antenna
	 * @param messageRec the buffer to fill with the server response
	 * @return the server response
	 * @throws IOException the connection failed
	 */
	public String requestCard(byte[] messageRec)
			throws IOException {
		synchronized(out) {
			
			try{
			out.write("T".getBytes());
			out.flush();
			//System.out.println("Card request sent");
			
			// Thread that switches antennas waits until a card is requested
			// so that for each antenna switch at least one attempt to 
			// read a card occurs.  Avoiding starvation.
			cardRequestSent = true;
		
			// Blocking read
			//System.out.println("Waiting for card");
			in.read(messageRec);
			//System.out.println("Got a card");

			out.notify();
			
			}catch(SocketException e){
				
				
				//e.printStackTrace();
			}

		}
		
		return new String(messageRec);
	}
	
	/**
	 * Processing the string to make a card and send to Listeners
	 * @param str - String that contains the ID number and the location found
	 */
	private void process(final String str){
		new Thread("Card processor") {
			public void run() {
				String cardID = str.substring(0, ID_LENGTH);
				Card thisCard = cards.getCard(cardID);
				System.out.println("Found : " + thisCard.toString());
				
				//if the message ends with C, the card is on the id antenna
				if (str.endsWith("C")){
					iDListen.cardFound(thisCard);
					//System.out.println("message sent to ID listener");
					
				//otherwise, the card is on one of the player antennas
				} else {
					int position = Integer.parseInt(str.substring(POSITION,MESSAGE_LENGTH)) - 1;
					hands[position].cardFound(thisCard);
					//System.out.println("message sent to " + position);
				}
			}
			
		}.start();
	}

	/**
	 * Adds a listener to the Hand array
	 * @param listener the listener to be added
	 * @param direction the antenna to listen to
	 */
	public void addHandListener(CardListener listener,Direction direction){
		hands[direction.ordinal()] = listener;
	}

	/**
	 * Adds the special IDCard listener
	 * @param listener - lister to be added
	 */
	public void addIdListener(CardListener listener){
		iDListen = listener;
	}
	
	/**
	 * Sends the quit command to the C# server.  Closes the socket.
	 * Stops the cycling timer.
	 */
	public void quitServer(){
		if (out == null) {
			return;
		}
			
		byte[] output = "quit".getBytes();
			
		try {
			synchronized(out) {
				out.write(output);
				out.flush();
				//System.out.println("Command sent: quit");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Send a command to the hardware to server to switch which antenna it is
	 * listening to
	 * @param handID the antenna to listen to
	 * @throws IOException the connection failed
	 * @throws InterruptedException the thread was interrupted while waiting for
	 *   a card request command to happen
	 */
	public void switchHand(String handID) throws IOException, InterruptedException{
		if (out == null) {
			System.out.println("Server is not running.");
			return;
		}
		
		byte[] output = handID.getBytes();

		// Make sure only one thread is talking to the server at a time
		//System.out.println("switchHand waiting for lock");
		synchronized(out) {
			//System.out.println("switchHand got lock");
			out.write(output);
			out.flush();
			//System.out.println("Command sent: " + handID + ".");
			
			// If a card hasn't been requested since the last antenna switch, wait
			while (!cardRequestSent) {
				//System.out.println("switchHand waiting for card request to happen");
				//System.out.println("Waiting thread " + Thread.currentThread().getName());
				out.wait();
				//System.out.println("Awakened thread " + Thread.currentThread().getName());
			}
			//System.out.println("switchHand not waiting");
			cardRequestSent = false;
		}
		
		
	}
	
	/**
	 * Switch the hardware to an antenna
	 * @param turn the antenna to listen to
	 * @throws IOException if the connection to the server is lost.
	 * @throws InterruptedException 
	 */
	public void switchHand(final Direction turn) throws IOException, InterruptedException {
		
		System.out.println("*** SwitchHand called; switching to " + turn + " ***");
		
		//System.out.println("Cycling timer reset");
		turnId = getDirectionCode(turn);
		//switchHand(currentHand);
		switchHand(turnId);
		//System.out.println("Current hand: " + turn);

		
		if (cyclingThread == null) {
			cyclingThread = new Thread("Cycling thread") {
				@Override
				public void run() {
					while (!isInterrupted()) {
						try {
							cycleHands();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return;
						} catch (InterruptedException e) {
							e.printStackTrace();
							return;
						}
					}
				}
			};
			cyclingThread.start();
		}
		//System.out.println("*** SwitchHand returning; switched to " + turn + " ***");		
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

	/**
	 * Alternate between the current hand and the id antenna
	 * @throws IOException the connection failed
	 * @throws InterruptedException the cycling thread was interrupted
	 */
	private void cycleHands() throws IOException, InterruptedException {

		System.out.println("Cycle hands");
		
		System.out.println("onPlayerHand " + onPlayerHand);
		
		if (onPlayerHand) {
			//System.out.println("Switching to ID");
			switchHand("P");
		} 
		
		else {
			//System.out.println("Switching to player antenna " + turnId);
			switchHand(turnId);
		}

		onPlayerHand = !onPlayerHand;
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setCyclingThread(Thread cyclingThread) {
		this.cyclingThread = cyclingThread;
	}
	
	
}


