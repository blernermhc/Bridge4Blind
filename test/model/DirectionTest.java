package model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DirectionTest {

	@Test
	public void testFollows() {
		assertTrue(Direction.NORTH.follows(Direction.WEST));
		assertTrue(Direction.WEST.follows(Direction.SOUTH));
		assertTrue(Direction.SOUTH.follows(Direction.EAST));
		assertTrue(Direction.EAST.follows(Direction.NORTH));
		
		assertFalse(Direction.NORTH.follows(Direction.EAST));
		assertFalse(Direction.NORTH.follows(Direction.SOUTH));
	}
	
	@Test
	public void testGetPartner() {
		assertEquals(Direction.NORTH, Direction.SOUTH.getPartner());
		assertEquals(Direction.SOUTH, Direction.NORTH.getPartner());
		assertEquals(Direction.EAST, Direction.WEST.getPartner());
		assertEquals(Direction.WEST, Direction.EAST.getPartner());
	}

	@Test
	public void testGetNextPosition() {
		assertEquals(Direction.NORTH, Direction.WEST.getNextDirection());
		assertEquals(Direction.SOUTH, Direction.EAST.getNextDirection());
		assertEquals(Direction.EAST, Direction.NORTH.getNextDirection());
		assertEquals(Direction.WEST, Direction.SOUTH.getNextDirection());
	}

	@Test
	public void testGetPreviousPosition() {
		assertEquals(Direction.SOUTH, Direction.WEST.getPreviousDirection());
		assertEquals(Direction.NORTH, Direction.EAST.getPreviousDirection());
		assertEquals(Direction.WEST, Direction.NORTH.getPreviousDirection());
		assertEquals(Direction.EAST, Direction.SOUTH.getPreviousDirection());
	}

}
