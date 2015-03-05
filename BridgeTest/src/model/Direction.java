package model;

/**The Direction class is an enumerated type corresponding
 * to a cardinal direction.
 * 
 * @author Allison DeJordy
 */

public enum Direction {
	
	NORTH,
	EAST,
	SOUTH,
	WEST;

	public boolean follows(Direction otherDirection) {
		int thisPosition = ordinal();
		int otherPosition = otherDirection.ordinal();
		return thisPosition == (otherPosition + 1) % 4;
	}

	public Direction getPartner() {
		int thisPosition = ordinal();
		int partnerPosition = (thisPosition + 2) % 4;
		return values()[partnerPosition];
	}

	public Direction getNextDirection() {
		int thisPosition = ordinal();
		int nextPosition = (thisPosition + 1) % 4;
		return values()[nextPosition];
	}

	public Direction getPreviousDirection() {
		int thisPosition = ordinal();
		int nextPosition = (thisPosition + 3) % 4;
		return values()[nextPosition];
	}
	
}
