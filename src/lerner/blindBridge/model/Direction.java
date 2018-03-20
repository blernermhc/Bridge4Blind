package lerner.blindBridge.model;

/***********************************************************************
 * Represents a player's position (e.g., North, South, etc.)
 ***********************************************************************/
public enum Direction
{
	NORTH
	, EAST
	, SOUTH
	, WEST
	;

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Determines if the given direction immediately follows this direction.
	 * @param otherDirection		the direction to test
	 * @return true if immediately follows
	 ***********************************************************************/
	public boolean follows (Direction otherDirection)
	{
		int thisPosition = ordinal();
		int otherPosition = otherDirection.ordinal();
		return thisPosition == (otherPosition + 1) % 4;
	}

	/***********************************************************************
	 * Returns the partner of this position
	 * @return the partner
	 ***********************************************************************/
	public Direction getPartner()
	{
		int thisPosition = ordinal();
		int partnerPosition = (thisPosition + 2) % 4;
		return values()[partnerPosition];
	}

	/***********************************************************************
	 * Returns the player position following this position
	 * @return the position
	 ***********************************************************************/
	public Direction getNextDirection()
	{
		int thisPosition = ordinal();
		int nextPosition = (thisPosition + 1) % 4;
		return values()[nextPosition];
	}

	/***********************************************************************
	 * Returns the player position before this position
	 * @return the position
	 ***********************************************************************/
	public Direction getPreviousDirection()
	{
		int thisPosition = ordinal();
		int nextPosition = (thisPosition + 3) % 4;
		return values()[nextPosition];
	}
	
	/***********************************************************************
	 * Converts from a string to a player position, allows full names or one-letter abbreviations.
	 * @param p_directionShortHand	name to lookup (a letter like N, S)
	 * @return position direction
	 * @throws IllegalArgumentException if no match found
	 ***********************************************************************/
	public static Direction fromString (String p_directionShortHand)
		throws IllegalArgumentException
	{
		String directionShortHand = p_directionShortHand.toUpperCase();
		if (directionShortHand.length() == 1)
		{
			Direction direction;
			switch (directionShortHand)
			{
				case "N":	direction = NORTH; break;
				case "E":	direction = EAST; break;
				case "S":	direction = SOUTH; break;
				case "W":	direction = WEST; break;
				default:		throw new IllegalArgumentException("Unknown direction name: " + p_directionShortHand);
			}
			return direction;
		}
		
		return Direction.valueOf(directionShortHand);
	}
}
