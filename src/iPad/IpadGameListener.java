package iPad;

import model.Card;
import model.Contract;
import model.Direction;
import model.Game;
import model.GameListener;
import model.Hand;
import model.Player;
import model.Rank;
import model.Suit;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class IpadGameListener implements GameListener{
	//Message to be sent to the iPad
	String msg;

	//Socket to connect to the Ipad on
	private Socket socket;

	//Input/Output streams
	BufferedReader in;
	PrintWriter out;

	//Number of tricks won for each team, starts at 0
	int numTricksWonNE = 0;
	int numTricksWonSW = 0;
	
	//Insance of the game class
	Game game;
	
	public static void main(String[] args)
	{
		Game g = new Game();
		try {
			IpadGameListener i = new IpadGameListener(g);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//Constructor
	//Takes in an instaence of the game class in order to get the blind/dummy positions
	public IpadGameListener(Game game) throws IOException, InterruptedException
	{
		this.game = game;

		// Attach the server to the correct port number
		ServerSocket serverSocket = new ServerSocket(5000);

		//This shouldn't happen until the Ipad has made a connection I THINK????
		System.out.println("Waiting for Ipad to connect");
		Socket socket = serverSocket.accept();
		
		System.out.println("CONNECTION MADE");

		//socket.close();

		try {
			// Set up the streams to allow 2-way communication with the client.
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);		

			//Waits about 9 seconds
			Thread.currentThread().sleep(10000);


			//Send the message
			out.print("DH2,3,4/EW1/B4 of Hearts/");
			out.flush();

			//Waits about 9 seconds
			Thread.currentThread().sleep(10000);


			out.print("DH4/DC45/EW2/NS1/C5 of Hearts/");
			out.flush();

			//			in.close();
			//			out.close();

		} catch (IOException e){
			// Give up on this client connection if there is an exception.
			System.err.println("Connection with client failed. IOException");
			return;
		} catch (InterruptedException e){
			// Give up on this client connection if there is an exception.
			System.err.println("Connection with client failed. InterruptedException");
			return;
		}
	}


	@Override
	public void debugMsg(String string) {
		// TODO Auto-generated method stub

	}

	@Override
	public void gameReset() {
		//NEED TO ADD FUNCTIONALITY IN THE IPAD PROGRAM, SO IF A CERTAIN COMMAND IS SENT, THEN EVERY FIELD GETS RESET
		//Have the command be R for reset
		out.print("R/");
		out.flush();

	}

	/**
	 * Tell the user who just played what card
	 * If the card is from the dummy hand or the players hand, then update their hand
	 */
	@Override
	public void cardPlayed(Direction turn, Card card) {
		//Read to the player who just played what card
		out.print("ts"+turn+ " played "+card.getRank()+" of "+card.getSuit()+"/");
		out.flush();
		
		//Update dummy hand
		updateDummyHand();
		//Update user's hand 
		updateBlindHand();
	}

	/**
	 * Tell the user what card was just scanned
	 * Set the text To speech(repeat) to the card just scanned
	 */
	@Override
	public void cardScanned(Card card) {
		//This will read, for example "4 of Hearts". 
		out.print("ts"+card.getRank()+" of "+card.getSuit()+"/");
		out.flush();
	}

	/**
	 * Tell the user who just won a trick
	 * Increment the correct numberOfTricksWonCounter
	 */
	@Override
	public void trickWon(Direction winner) {
		
		switch(winner){

		case NORTH:
			numTricksWonNE++;
			out.print("NE"+numTricksWonNE+"/"+"tsNorth won a trick!"+"/");
			out.flush();
			break;
		case EAST:
			numTricksWonNE++;
			out.print("NE"+numTricksWonNE+"/"+"tsEast won a trick!"+"/");
			out.flush();
			break;
		case SOUTH:
			numTricksWonSW++;
			out.print("SW"+numTricksWonSW+"/"+"tsSouth won a trick!"+"/");
			out.flush();
			break;
		case WEST:
			numTricksWonSW++;
			out.print("SW"+numTricksWonSW+"/"+"tsWest won a trick!"+"/");
			out.flush();
			break;

		}
	}

	@Override
	public void contractSet(Contract contract) {
		//I'm not sure if this will actually set the text that we want????
		out.print("C"+contract.toString()+"/");
		out.flush();

	}

	@Override
	public void blindHandScanned() {
		out.print("tsBlind hand complete/");
		out.flush();

	}

	@Override
	public void dummyHandScanned() {
		out.print("tsDummy hand complete/");
		out.flush();

	}

	@Override
	public void cardAddedToHand(Direction dir, Card c) {
		//Determine if it's the dummy or blind hand
		if(dir == game.getDummyPosition())
		{
			updateDummyHand();
		}
		else if(dir == game.getBlindPosition())
		{
			updateBlindHand();
		}
	}
	
	/**
	 * Iterates through and updates the dummy's hand accordingly
	 */
	private void updateDummyHand()
	{
		Player p = game.getDummyPlayer();	
		String str = "";
			
		//Iterate though for each suit, creating the string as you go
		//Hearts
		str = str+"DH";
		while(p.cards(Suit.HEARTS).hasNext())
		{
			str = str + p.cards(Suit.HEARTS).next().getRank() +", ";
		}
		str = str+ "/";
		
		//Clubs
		str = str +"DC";
		while(p.cards(Suit.CLUBS).hasNext())
		{
			str = str + p.cards(Suit.CLUBS).next().getRank() +", ";
		}
		str = str+ "/";
		
		//Diamonds
		str = str +"DD";
		while(p.cards(Suit.DIAMONDS).hasNext())
		{
			str = str + p.cards(Suit.DIAMONDS).next().getRank() +", ";
		}
		str = str+ "/";
		
		//Spades
		str = str +"DS";
		while(p.cards(Suit.SPADES).hasNext())
		{
			str = str + p.cards(Suit.SPADES).next().getRank() +", ";
		}
		str = str+ "/";
		
		out.print(str);
		out.flush();
	}
	
	/**
	 * Iterates through and updates the dummy's hand accordingly
	 */
	private void updateBlindHand()
	{
		Player p = game.getBlindPlayer();	
		String str = "";
			
		//Iterate though for each suit, creating the string as you go
		//Hearts
		str = str+"PH";
		while(p.cards(Suit.HEARTS).hasNext())
		{
			str = str + p.cards(Suit.HEARTS).next().getRank() +", ";
		}
		str = str+ "/";
		
		//Clubs
		str = str +"PC";
		while(p.cards(Suit.CLUBS).hasNext())
		{
			str = str + p.cards(Suit.CLUBS).next().getRank() +", ";
		}
		str = str+ "/";
		
		//Diamonds
		str = str +"PD";
		while(p.cards(Suit.DIAMONDS).hasNext())
		{
			str = str + p.cards(Suit.DIAMONDS).next().getRank() +", ";
		}
		str = str+ "/";
		
		//Spades
		str = str +"PS";
		while(p.cards(Suit.SPADES).hasNext())
		{
			str = str + p.cards(Suit.SPADES).next().getRank() +", ";
		}
		str = str+ "/";
		
		out.print(str);
		out.flush();
	}

}
