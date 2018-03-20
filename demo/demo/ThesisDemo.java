package demo;

import java.io.IOException;

import model.CardDatabase;
import model.Game;
import audio.AudioPlayer;
import controller.AntennaHandler;
import controller.CardIdentifier;
import controller.Handler;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Direction;

/**The ThesisDemo class is a small demonstration for my thesis defense.
* @author Allison DeJordy
* Spring 2012
**/

public class ThesisDemo extends Game{
	
	private CardDatabase data;
	private AntennaHandler ah;
	private CardIdentifier cardID;
	private AudioPlayer ap;
	
	public ThesisDemo () throws IOException {
		
		super(new AntennaHandler(new CardDatabase()), false) ;
		
		data = new CardDatabase();
		ah = new AntennaHandler(data);
		cardID = new CardIdentifier(this);
		ah.addIdListener(cardID);
		ap = new AudioPlayer();
		new Thread(ah).start();
	}
	
	public void start() throws IOException, InterruptedException {
		
		ah.switchHand(Direction.NORTH);
	}
	
	@Override
	public void cardIded(Card c){
		
		ap.init(c.getSound());
		ap.play();
		
	}
	
	public Handler getHandler() {
		
		return ah;
		
	}
	
}