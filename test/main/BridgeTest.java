package main;

import java.io.IOException;

import controller.TestHandler;
import gui.GameGUI;
import model.CardDatabase;
import model.Game;
import audio.AudibleGameListener;

public class BridgeTest implements BridgeMode {
	
	public BridgeTest() {

		start() ;
	}

	@Override
	public void start() {
		
		try {
			
			Game game = new Game(new TestHandler(new CardDatabase()), true);			
			game.activateAntennas();
			GameGUI gui = new GameGUI(game);
			game.addListener(new AudibleGameListener());
			game.addListener(gui);
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}

	}
	
	public static void main (String[] args){
		
		new BridgeTest() ;
	}

}
