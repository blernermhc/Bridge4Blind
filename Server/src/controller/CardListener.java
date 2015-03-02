package controller;

import model.Card;

/**The CardListener interface listens for cards to be detected on the antennae.
*
* @author Allison DeJordy
**/
public interface CardListener {
	
	/**Indicates that a card has been found on one of the antennae.
	 * @param c The card that was detected.
	 */
	public void cardFound(Card c);
	
	
}