package server;

import java.io.IOException;

import model.Card;
import model.CardDatabase;
import model.Direction;
import controller.AntennaHandler;
import controller.CardListener;

/**
 * Expected output:
 * 
Server started
Starting antenna handler thread
Connecting...
Client connected
Connected to localhost in port 6666
Message T received
SwitchHand called; switching to EAST
SwitchHand called; switching to SOUTH
SwitchHand called; switching to WEST
Switching to ID
Switching to player antenna WEST
Switching to ID
Switching to player antenna WEST
Switching to ID
Switching to player antenna WEST
Everything is closed

 * @author Barbara Lerner
 * @version Jul 12, 2012
 *
 */
public class AntennaHandlerTest {
	public static void main(String[] args) throws IOException{
		// Stub for the C# server that controls the antennas
		StubCSharpServer t = new StubCSharpServer();
		System.out.println("Server created");
		Thread serverThread = new Thread(t);
		serverThread.start();
		System.out.println("Server started");

		// Client's antenna handler
		AntennaHandler a = new AntennaHandler(new CardDatabase());
		a.addIdListener(new CardListener() {

			@Override
			public void cardFound(Card c) {
				if (!c.toString().equals("AH")) {
					System.out.println("Unexpected card on id antenna:  " + c.toString());
				}
			}
			
		});
		
		a.addHandListener(new CardListener() {
			@Override
			public void cardFound(Card c) {
				if (!c.toString().equals("2H")) {
					System.out.println("Unexpected card on north antenna:  " + c.toString());
				}
			}
			
		}, Direction.NORTH);

		a.addHandListener(new CardListener() {
			@Override
			public void cardFound(Card c) {
				if (!c.toString().equals("3H")) {
					System.out.println("Unexpected card on east antenna:  " + c.toString());
				}
			}
			
		}, Direction.EAST);

		a.addHandListener(new CardListener() {
			@Override
			public void cardFound(Card c) {
				if (!c.toString().equals("4H")) {
					System.out.println("Unexpected card on south antenna:  " + c.toString());
				}
			}
			
		}, Direction.SOUTH);

		a.addHandListener(new CardListener() {
			@Override
			public void cardFound(Card c) {
				if (!c.toString().equals("5H")) {
					System.out.println("Unexpected card on west antenna:  " + c.toString());
				}
			}
			
		}, Direction.WEST);

		System.out.println("Starting antenna handler thread");
		Thread handlerThread = new Thread(a);
		handlerThread.start();
		
		try {
			Thread.sleep(100);
			a.switchHand(Direction.EAST);
			Thread.sleep(100);
			a.switchHand(Direction.SOUTH);
			Thread.sleep(100);
			a.switchHand(Direction.WEST);
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		a.quitServer();

	}
	

}
