package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import controller.AntennaHandler;

public class StubCSharpServer implements Runnable {
	
	private InputStream in;
	private OutputStream out;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	
	public StubCSharpServer () throws IOException {
		
		try {
	        serverSocket = new ServerSocket(6666);
	    } catch (IOException e) {
	        System.err.println("Could not listen on port: 6666.");
	        System.exit(1);
	    }
	    
	    
	    System.out.println("Socket opened");

	}

	@Override
	public void run() {
		
        try {
            clientSocket = serverSocket.accept();
            System.out.println("Client connected");

        	in = clientSocket.getInputStream();
        	out = clientSocket.getOutputStream();
		
        	byte[] inputLine = new byte[1];

        	in.read(inputLine, 0, 1);
        	String input = new String(inputLine);
    		System.out.println("Message " + input + " received");

    		// No card
        	writeMsg("NOCARD");

        	String currentHand = "N";
        	while (!input.startsWith("q")) {
        		if (input.equals("T")) {
        			writeCard(currentHand);
        		}
        		else {
        			currentHand = input;
        		}
        		
            	in.read(inputLine, 0, 1);
            	input = new String(inputLine);
        		System.out.println("Server received message " + input);

        	}
        	
        	// Quit
        	writeMsg("quit45678901234567890");
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void writeCard(String currentHand) throws IOException {
		if (currentHand.equals("N")) {
        	// Two of hearts on north antenna
			writeMsg("041B797A831E80451");
		}
		else if (currentHand.equals("E")) {
			writeMsg("04A3837A831E80452");
		}
		else if (currentHand.equals("S")) {
			writeMsg("04665F7A831E80453");
		}
		else if (currentHand.equals("W")) {
			writeMsg("0455FB7A831E80454");
		}
		else if (currentHand.equals("P")) {
        	writeMsg("042E657A831E8045C");
			
		}
	}

	private void writeMsg(String string) throws IOException {
		
		out.write(string.getBytes());
		out.flush();
	}
	
}
