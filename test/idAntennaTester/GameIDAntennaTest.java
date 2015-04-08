package idAntennaTester ;

import java.io.IOException;
import java.net.UnknownHostException;

import controller.CardIdentifier;
import controller.HandAntenna;
import controller.Handler;
import model.Direction;
import model.Game;

public class GameIDAntennaTest extends Game{

	public GameIDAntennaTest(Handler handler, boolean isTestMode) {
		super(handler, isTestMode);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void activateAntennas() throws UnknownHostException, IOException {
		
		System.out.println("activaeAntenna for the ID Hand test");
		
//		HandAntenna[] handAntennas = new HandAntenna[NUM_PLAYERS];
//		Direction[] directions = Direction.values();
//		for (int i = 0; i < players.length; i++) {
//			handAntennas[i] = new HandAntenna(directions[i], this);
//		}

		// construct the card identifier
		CardIdentifier id = new CardIdentifier(this);
		handler.connect();

//		// add the listeners for the antenna handler
//		handler.addHandListener(handAntennas[Direction.NORTH.ordinal()],
//				Direction.NORTH);
//		handler.addHandListener(handAntennas[Direction.EAST.ordinal()],
//				Direction.EAST);
//		handler.addHandListener(handAntennas[Direction.SOUTH.ordinal()],
//				Direction.SOUTH);
//		handler.addHandListener(handAntennas[Direction.WEST.ordinal()],
//				Direction.WEST);
		handler.addIdListener(id);
	}
	
}