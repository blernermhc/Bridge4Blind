package audio;

import model.Card;
import model.Rank;
import model.Suit;

public class SoundTest {
	
	public SoundTest() {
		Card c = new Card (Rank.NINE, Suit.CLUBS);
		System.out.println(c.getSound());
		AudioPlayer ap = new AudioPlayer();
		if (ap.init(c.getSound())) {
			ap.play();			
		}
		else {
			System.out.println(ap.error);
		}
		
		
	}
	
	public static void main (String[] args){
		
		new SoundTest();
		
	}
	
}