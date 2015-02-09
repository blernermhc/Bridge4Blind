package controller;

import java.io.IOException;

import model.Card;
import model.CardDatabase;
import model.Direction;
import controller.AntennaHandler;
import controller.CardListener;

public class ServerTest extends AntennaHandler {
	public ServerTest(CardDatabase cardDatabase) {
		super(cardDatabase);
	}

	public void run()
	{
		try{
			for (int i = 0; i < 1000; i++) {
				// Works well!
//				switchHand("N");
//				switchHand("E");
//				switchHand("S");
//				switchHand("W");
				
				// Following includes cycling to the id antenna
				switchHand(Direction.NORTH);
				Thread.sleep(200);
				switchHand(Direction.EAST);
				Thread.sleep(200);
				switchHand(Direction.SOUTH);
				Thread.sleep(200);
				switchHand(Direction.WEST);
				Thread.sleep(200);
			}
			quitServer();
			
			in.close();
			out.close();
			requestSocket.close();
			System.out.println("Everything is closed");
			System.exit(0);
		}
		
		catch(IOException ioException){
			System.err.println("Lost connection to the server!");
			ioException.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println("Antenna server was interrupted");
			e.printStackTrace();
		}
	}

	private void startNotifyingThread() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					byte[] messageRec = new byte[3000];

					while (true) {
						String message = requestCard(messageRec);
						if (!message.startsWith("NOCARD")) {
							System.out.println("Received " + message.substring(0, MESSAGE_LENGTH));
						}
						Thread.sleep(400);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}).start();
	}

	public static void main(String[] args) throws IOException{
		// Client's antenna handler
		ServerTest a = new ServerTest(new CardDatabase());
		a.connect();
		a.startNotifyingThread();

		new Thread(a).start();

	}

	

}
