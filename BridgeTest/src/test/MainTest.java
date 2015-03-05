package test;

import java.io.IOException;
import java.net.UnknownHostException;

import gui.GameGUI;
import audio.AudibleGameListener;
import model.Card;
import model.Direction;
import model.Game;
import model.Rank;
import model.Suit;

public class MainTest {
	

	private Game game;
	
	public MainTest() throws UnknownHostException, IOException{
		
		game = new Game();
		game.activateAntennas();
		//game.setBlindPosition(BLIND_DIRECTION);
		//game.initPlayingPhase(BLIND_DIRECTION);
		
		GameGUI gui = new GameGUI(game);
		game.addListener(new AudibleGameListener());
		game.addListener(gui);
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		new MainTest() ;
		
	}

}
