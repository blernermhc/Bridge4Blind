package model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;

import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Rank;
import lerner.blindBridge.model.Suit;

/**
 * This CardDatabase class keeps track of a Hashtable of Cards by using the id numbers
 * as Keys
 * @author Caden Friedenbach
 *
 */
public class CardDatabase {

	/*Hashtable holding the id to card map*/
	private HashMap<String,Card> cardMap;
	
	/*file holding the card IDs*/
	private static final String FILENAME = "/cardID.txt";

	/**
	 * Creates a new Database
	 */
	public CardDatabase(){
		//reads the File
		readFile();
	}
	
	/**
	 * Reads the file and parses the information into the HashTable
	 */
	private void readFile() {
		//sets up the file
		String fullLine;
		try {
			BufferedReader read = null;
			try {
				read = new BufferedReader (new InputStreamReader (getClass().getResourceAsStream(FILENAME)));

				// first line should be an int with how many cards present
				fullLine = read.readLine();
				assert fullLine != null;
				int numCards = Integer.parseInt(fullLine);
				
				// second line of file should be an int for the number of decks
				fullLine = read.readLine();
				assert fullLine != null;
				int decks = Integer.parseInt(fullLine);

				cardMap = new HashMap<String, Card>(numCards * decks);

				// rest of file should be Card values
				while ((fullLine = read.readLine()) != null) {
					makeCard(fullLine);
				}
			} finally {
				if (read != null) {
					read.close();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Takes in a string and produces a Card 
	 * @param fileLine - should be organized as <ID>.<ID>|<RANK><SUIT>
	 * @return a card with the correct information
	 */
	protected Card makeCard(String fileLine){
		StringTokenizer tokenizer = new StringTokenizer(fileLine, ".|");
		
		//spliting the file line into appropriate chunks
		String idOne = tokenizer.nextToken();
		String idTwo = tokenizer.nextToken();
		
		//finding the apportiate suit and rank
		Rank value = Rank.findValue(fileLine.charAt(fileLine.length()-2));
		Suit suit = Suit.findSuit(fileLine.charAt(fileLine.length()-1));
		
		//Create the card
		Card newCard = new Card(value, suit);
		cardMap.put(idOne, newCard);
		cardMap.put(idTwo, newCard);
		
		return newCard;
	}
	
	/**
	 * Return a Card with a gived ID
	 * @param idNum - the ID number of a Card
	 * @return the card with that ID Number
	 */
	public Card getCard(String idNum){
		Card thisCard = cardMap.get(idNum);
		return thisCard;
	}
}
