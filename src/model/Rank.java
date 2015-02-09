package model;

/** An enumerated type representing the rank of a card.
*
* @author Allison DeJordy
**/

public enum Rank{
	
	DEUCE("2"),
	THREE("3"), 
	FOUR("4"), 
	FIVE("5"), 
	SIX("6"), 
	SEVEN("7"), 
	EIGHT("8"), 
	NINE("9"), 
	TEN("10"), 
	JACK("J"), 
	QUEEN("Q"), 
	KING("K"), 
	ACE("A");
	
	
	private static final String SOUND_FOLDER = "/sounds/ranks/";
	private final String rankValue;
	private String sound;
	
	private Rank(String rank){
		this.rankValue = rank;
		sound = SOUND_FOLDER + rank + ".WAV";
	}
	
	/**
	 * @return the name of the sound file for this rank
	 */
	public String getSound() {
		return sound;
	}
	
	/**
	 * @return a text description of the rank
	 */
	@Override
	public String toString() {
		return rankValue;
	}

	/**
	 * Given a letter find the appropriate Value.  '2' for two, etc.  'T' for ten, 
	 * 'J', 'Q', 'K' for face cards, 'A' for ace.
	 * @param c the Letter 
	 * @return the Rank.  Returns null if an unexpected character is passed in.
	 */
	public static Rank findValue(char c){
		switch(c) {
		case '2': return DEUCE;
		case '3': return THREE;
		case '4': return FOUR;
		case '5': return FIVE;
		case '6': return SIX;
		case '7': return SEVEN;
		case '8': return EIGHT;
		case '9': return NINE;
		case 'T': return TEN;
		case 'J': return JACK;
		case 'Q': return QUEEN;
		case 'K': return KING;
		case 'A': return ACE;
		}
		assert false;
		return null;
	}
	
}