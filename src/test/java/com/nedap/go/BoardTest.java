package com.nedap.go;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BoardTest {
	public Board board3;
	public Board board5;
	public Board boardString3;

	@Before
	public void setUp() throws Exception {
		board3 = new Board(3);
		board5 = new Board(5);
		boardString3 = new Board("000000000");
	}

	@Test
	public void testgetPoint() {
		assertEquals(board3.getPoint(5), 0);
		board3.setPoint(5, 1);
		assertEquals(board3.getPoint(5), 1);

		assertEquals(boardString3.getPoint(5), 0);
		boardString3.setPoint(5, 1);
		assertEquals(boardString3.getPoint(5), 1);
	}

	@Test
	public void testisEmpty() {
		assertTrue(board3.isEmpty(3));
		board3.setPoint(3, 1);
		assertFalse(board3.isEmpty(3));

		assertTrue(boardString3.isEmpty(3));
		boardString3.setPoint(3, 1);
		assertFalse(boardString3.isEmpty(3));
	}

	@Test
	public void testonBoard() {
		assertTrue(board3.onBoard(5));
		assertFalse(board3.onBoard(12));
		assertFalse(board3.onBoard(-1));
		assertTrue(boardString3.onBoard(5));
		assertFalse(boardString3.onBoard(12));
		assertFalse(boardString3.onBoard(-1));
	}

	@Test
	public void testisFull() {
		assertFalse(board3.isFull());
		board3.setPoint(0, 1);
		board3.setPoint(1, 1);
		board3.setPoint(2, 1);
		board3.setPoint(3, 1);
		board3.setPoint(4, 1);
		board3.setPoint(5, 1);
		board3.setPoint(6, 1);
		board3.setPoint(7, 1);
		board3.setPoint(8, 1);
		assertTrue(board3.isFull());

		assertFalse(boardString3.isFull());
		boardString3.setPoint(0, 1);
		boardString3.setPoint(1, 1);
		boardString3.setPoint(2, 1);
		boardString3.setPoint(3, 1);
		boardString3.setPoint(4, 1);
		boardString3.setPoint(5, 1);
		boardString3.setPoint(6, 1);
		boardString3.setPoint(7, 1);
		boardString3.setPoint(8, 1);
		assertTrue(boardString3.isFull());
	}

	@Test
	public void testgetBoardSize() {
		assertEquals(board3.getBoardSizeN(), 3);
		assertEquals(board5.getBoardSizeN(), 5);
		assertEquals(boardString3.getBoardSizeN(), 3);
	}

	@Test
	public void testgetCurrentStringBoard() {
		String current = "000100000";
		board3.setPoint(3, 1);
		assertEquals(current, board3.getCurrentStringBoard());
		boardString3.setPoint(3, 1);
		assertEquals(current, boardString3.getCurrentStringBoard());
	}

	@Test
	public void testgetCurrentIntBoard() {
		int[] current = {0, 0, 0, 1, 0, 0, 0, 0, 0};
		board3.setPoint(3, 1);
		assertArrayEquals(current, board3.getCurrentIntBoard());
		boardString3.setPoint(3, 1);
		assertArrayEquals(current, boardString3.getCurrentIntBoard());
	}

	@Test
	public void testdeepCopy() {
		Board copy = board3.deepCopy();
		copy.setPoint(5, 1);
		assertFalse(copy.getPoint(5) == board3.getPoint(5));
	}

	@Test
	public void testsetPoint() {
		board3.setPoint(5, 1);
		assertEquals(board3.getPoint(5), 1);
		boardString3.setPoint(5, 1);
		assertEquals(boardString3.getPoint(5), 1);
	}

	// --------- For TUI test ------------
	@Test
	public void testtoTUIString() {
		System.out.print(board3.toTUIString());
		System.out.print(board5.toTUIString());
		System.out.print(boardString3.toTUIString());
	}
}
