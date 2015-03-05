package model;

/** An enumerated type representing the suit of a card.
*
* @author Allison DeJordy
**/

public enum Suit{
	
	CLUBS("C"),  
	DIAMONDS("D"),  
	HEARTS("H"),
	SPADES("S"), 
	NOTRUMP("NT");
	
	private static final String SOUND_FOLDER = "/sounds/suits/";

	private String sound;
	
	private String suitString;
	
	private Suit(String suit){
		suitString = suit;
		sound = SOUND_FOLDER + suitString + ".WAV";
	}
	
	/**
	 * @return the name of the file that contains the sound for this suit
	 */
	public String getSound() {
		return sound;
	}
	
	/**
	 * @return a string representation of the suit
	 */
	@Override
	public String toString(){
		return suitString;
	}

	/**
	 * Finds the appropriate Suit given a letter.  H = Hearts, C = Clubs, S = Spades
	 * D = Diamonds
	 * @param c the letter
	 * @return the Suit.  Returns null if an unexpected code is found.
	 */
	public static Suit findSuit(char c){
		switch(c) {
		case 'H': return HEARTS;
		case 'D': return DIAMONDS;
		case 'S': return SPADES;
		case 'C': return CLUBS;
		}
		
		assert false;
		return null;
	}
	
}