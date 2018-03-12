// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.gameController;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/***********************************************************************
 * Represents a playing card
 ***********************************************************************/
public class CardLibrary
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(CardLibrary.class.getName());

	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------
	
	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------
	
	/**
	 * Map from RFID id (hex string) to a Card.
	 * You can add multiple decks into one library, so you can interchange cards, if necessary.
	 * So, there may be multiple IDs that map to the same Card object.
	 */
	private static Map<String, Card>	m_cardLibrary = new HashMap<>();

	/**
	 * Map from a Card, to the list of RFID id that map to the card
	 */
	private static SortedMap<Card, List<String>>	m_cardLibraryIds = new TreeMap<>();

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------

	/***********************************************************************
	 * Reads card ids from a file.
	 * @param p_cardFile		the full path to the card file
	 * @throws FileNotFoundException if the file cannot be found
	 * @throws IOException if there is an error reading the file
	 * @throws IllegalArgumentException if there are problems parsing a line in the file
	 ***********************************************************************/
	public static void readCardFile (String p_cardFile)
		throws FileNotFoundException, IOException
	{
		//sets up the file
		String fullLine;

		BufferedReader read = null;
		try
		{
			FileInputStream fstream = new FileInputStream(p_cardFile);
			read = new BufferedReader(new InputStreamReader(fstream));

			// each line should contain some number of period-separated ids
			// followed by a vertical bar and the card abbreviation (e.g, 9S, TH, JC, AD)
			// for backward compatibility with an older implementation, other lines are ignored.

			while ((fullLine = read.readLine()) != null)
			{
				makeCard(fullLine);
			}
		}
		finally
		{
			if (read != null)
			{
				read.close();
			}
		}
	}
	
	/***********************************************************************
	 * Adds ids to the library from a line in the cardFile.
	 * Ignores lines without a vertical bar.
	 * @param p_line		the line to parse
	 * @throws IllegalArgumentException if the card abbreviation is not valid
	 ***********************************************************************/
	private static void makeCard (String p_line)
	{
		String[] parts = p_line.split("\\|");
		if (parts.length != 2) return;		// ignore incorrectly formatted lines
		
		Card card = new Card(parts[1]);
		
		String[] ids = parts[0].split("\\.");
		
		for (String id : ids)
		{
			m_cardLibrary.put(id, card);
			List<String> cardIds = m_cardLibraryIds.get(card);
			if (cardIds == null)
			{
				cardIds = new ArrayList<>();
				m_cardLibraryIds.put(card, cardIds);
			}
			cardIds.add(id);
		}
	}

	/***********************************************************************
	 * Returns the card corresponding to the given cardId
	 * @param p_cardId	card ID to look up
	 * @return the card, or null, if not found
	 ***********************************************************************/
	public static Card findCard (String p_cardId)
	{
		return (m_cardLibrary.get(p_cardId));
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 * 
	 * Outputs library in a format that can be read with readCardFile.
	 */
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		out.append("CardLibrary");
		
		m_cardLibraryIds.keySet();
		
		for (Card card : m_cardLibraryIds.keySet())
		{
			List<String> ids = m_cardLibraryIds.get(card);
			if (ids != null)
			{
				String sep = "\n";
				for (String id : ids)
				{
					out.append(sep);
					out.append(id);
					sep = ".";
				}
				out.append("|");
				out.append(card.abbreviation());
			}
		}
		out.append("\n");

		return out.toString();
	}


	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

}
