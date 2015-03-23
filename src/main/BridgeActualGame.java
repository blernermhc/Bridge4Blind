package main;

import gui.GameGUI;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;

import controller.AntennaHandler;
import model.CardDatabase;
import model.Game;
import audio.AudibleGameListener;

public class BridgeActualGame implements BridgeMode{
	
	public BridgeActualGame() {

		start();
	}

	@Override
	public void start() {

		try {
			Game game = new Game(new AntennaHandler(new CardDatabase()), false);
			game.activateAntennas();
			GameGUI gui = new GameGUI(game);
			game.addListener(new AudibleGameListener());
			game.addListener(gui);

			gui.debugMsg("main run");
			
		} catch (UnknownHostException e) {
			System.err.println("Could not connect to server.  Host unknown.");
		}
		catch (ConnectException connectExc) {
			System.err.println("The server is not running!");
		}
		catch (SocketException socketEsc) {
			System.err.println("Check that there is no virus scanner blocking IRC connections.");
			socketEsc.printStackTrace();
		} 
		catch (IOException e) {
			System.err.println("Could not connect to server.");
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args){
		
		new BridgeActualGame() ;
	}

}
