import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import model.Direction;
import model.Game;
import model.Suit;
import server.StubCSharpServer;
import audio.AudibleGameListener;
import controller.AntennaHandler;


public class GameSimulation implements Runnable {
	private InputStream in;
	private OutputStream out;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	
	private String[] blindCards = new String[13];
	private Game game;
	
	public GameSimulation () throws IOException {
		blindCards[0] = "042E657A831E80";  // AH
		blindCards[1] = "041B797A831E80";  // 2H
		blindCards[2] = "04A3837A831E80";  // 3H
		blindCards[3] = "04665F7A831E80";  // 4H
		blindCards[4] = "0455FB7A831E80";  // 5H
		blindCards[5] = "0447CD7A831E80";  // 6H
		blindCards[6] = "04605A7A831E80";  // 7H
		blindCards[7] = "0447487A831E80";  // 8H
		blindCards[8] = "0459CB7A831E80";  // 9H
		blindCards[9] = "0421B27A831E80";  // 10H
		blindCards[10] = "04462C7A831E80";  // JH
		blindCards[11] = "0427537A831E80";  // QH
		blindCards[12] = "0449A17A831E80";  // KH
		
		try {
	        serverSocket = new ServerSocket(6666);
	    } catch (IOException e) {
	        System.err.println("Could not listen on port: 6666.");
	        System.exit(1);
	    }
	    System.out.println("Socket opened");

	}
	
	private void initialize() throws UnknownHostException, IOException {
		game = new Game();
		
		// Don't start the antenna handler until after the blind position is set.
		// This is not a problem for the real game since it would be 
		// getting the command to set the blind position from the
		// Swing event thread, not the main thread.
		AntennaHandler handler = game.getHandler();
		handler.connect();
		
		game.addListener(new AudibleGameListener());

		// Set the blind player position
		System.out.println("Blind player set to North");
		game.setBlindPosition(Direction.NORTH);


		if (handler != null) {
			// Yikes!  This used to just be handler.start.  Was that causing
			// our timing problems???
			new Thread(handler).start();
			Thread.yield();
		}
		else {
			System.out.println("Server is not running!");
		}

	}

	@Override
	public void run() {
		
        try {
    		clientSocket = serverSocket.accept();
    		System.out.println("Client connected");

        	in = clientSocket.getInputStream();
        	out = clientSocket.getOutputStream();
        	
    		// Scan in blind player's cards
        	scanBlindCards();
        	
        	// Set the bid
        	System.out.println("Setting bid to 3C East");
			game.initPlayingPhase(Direction.EAST);
			game.setContractNum(3);
			game.setTrump(Suit.CLUBS);

			// Expect the antenna to switch to NORTH
			checkForSwitchToNorth();
    		
    		// Identify the first card played
        	playFirstCard();
        	
        	Thread.sleep(5000);
        	
        	// Quit
        	writeMsg("quit45678901234567890");
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void playFirstCard() throws IOException {
		byte[] inputLine = new byte[1];
		String input;
		input = new String(inputLine);
		System.out.println("Server received message " + input);
		boolean firstCardPlayed = false;
		boolean onFirstPlayer = false;
		while (!firstCardPlayed) {
			in.read(inputLine, 0, 1);
			input = new String(inputLine);
			System.out.println("Server received message " + input);
			if (input.equals("T")) {
				if (onFirstPlayer) {
					writeMsg("0432E37A831E80" + "453"); // AD played by South
					firstCardPlayed = true;
				}
				else {
					writeMsg("NOCARD");
				}
			}
			else if (input.equals("S")) {
				onFirstPlayer = true;
			}
			else if (input.equals("P")) {
				onFirstPlayer = false;
			}
			else {
				System.out.println("***Antenna changed to " + input + " before done scanning blind hand!");
				System.exit(0);
			}
		}
	}

	private void checkForSwitchToNorth() throws IOException {
		byte[] inputLine = new byte[1];
		in.read(inputLine, 0, 1);
		String input = new String(inputLine);
		System.out.println("Server received message " + input);
		while (input.equals("T") || input.equals("P")) {
			if (input.equals("T")) {
				writeMsg("NOCARD");
			}
			in.read(inputLine, 0, 1);
			input = new String(inputLine);
			System.out.println("Server received message " + input);
		}
		if (!input.equals("S")) {
			System.out.println ("*** Antenna changed to " + input + ".  Expected S");
			System.exit(0);
		}
	}

	private void scanBlindCards() throws IOException {
		byte[] inputLine = new byte[1];
		String currentHand = "N";
		int numScanned = 0;
		boolean onBlindHand = false;
		while (numScanned < 13) {
			in.read(inputLine, 0, 1);
			String input = new String(inputLine);
			System.out.println("Server received message " + input);
			if (input.equals("T")) {
				if (onBlindHand) {
					writeBlindCard(currentHand, numScanned);
					numScanned++;
				}
				else {
					writeMsg("NOCARD");
				}
			}
			else if (input.equals("N")) {
				onBlindHand = true;
			}
			else if (input.equals("P")) {
				onBlindHand = false;
			}
			else {
				System.out.println("***Antenna changed to " + input + " before done scanning blind hand!");
				System.exit(0);
			}
		}
	}

	private void writeBlindCard(String currentHand, int whichCard) throws IOException {
		if (!currentHand.equals("N")) {
			System.out.println("*** Blind hand is " + currentHand + ".  Should be N.");
			System.exit(0);
		}
		writeMsg (blindCards[whichCard] + "451");
	}

	private void writeMsg(String string) throws IOException {
		
		out.write(string.getBytes());
		out.flush();
	}

	public static void main (String[] args) {
		try {
			GameSimulation t = new GameSimulation();
			System.out.println("Server created");
			Thread serverThread = new Thread(t);
			serverThread.start();
			System.out.println("Server started");

			Thread.sleep(2000);
			
			t.initialize();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
