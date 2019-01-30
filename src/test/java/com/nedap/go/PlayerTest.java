package com.nedap.go;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PlayerTest {
	private Player one;
	private Player two;

	@Before
	public void setUp() {
		one = new Player("Klaas", 1);
		two = new Player("Piet", 2);
	}

	@Test
	public void testgetName() {
		assertEquals(one.getName(), "Klaas");
		assertEquals(two.getName(), "Piet");
	}

	@Test
	public void testgetColour() {
		assertEquals(one.getColour(), 1);
		assertEquals(two.getColour(), 2);
	}
}
