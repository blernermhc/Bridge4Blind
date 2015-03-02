package model;

/**
 * Manages information about a contract:  the suit and number of tricks to win.
 * @author Barbara Lerner
 * @version May 18, 2012
 *
 */
public class Contract {
	public static final int MAX_BID = 7;
	
	// the suit of the contract
	private Suit trumpSuit;
	
	// the number of tricks in the contract
	private int contractNum;
	
	// The player who won the bid
	private Direction bidWinner;

	public void setTrump(Suit suit) {
		trumpSuit = suit;
	}

	public Suit getTrump() {
		return trumpSuit;
	}

	public void setContractNum(int contractNum) {
		assert contractNum >= 1 && contractNum <= MAX_BID;
		this.contractNum = contractNum;
	}

	public int getContractNum() {
		return contractNum;
	}
	
	public void setBidWinner (Direction bidWinner) {
		this.bidWinner = bidWinner;
	}
	
	public Direction getBidWinner() {
		return bidWinner;
	}
	
	public String toString() {
		return "" + contractNum + " " + trumpSuit.toString() + " " + bidWinner;
	}

}
