package com.nedap.go;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Before;
import org.junit.Test;

public class GroupTest {
	private Game game;
	private Player[] players;
	List<Integer> colour1 = new CopyOnWriteArrayList<Integer>();
	List<Integer> colour2 = new CopyOnWriteArrayList<Integer>();
	List<Integer> colour3 = new CopyOnWriteArrayList<Integer>();

	@Before
	public void setUp() {
		players = new Player[2];
		players[0] = new Player("Piet", 1);
		players[1] = new Player("Klaas", 2);
		game = new Game(players, 6, 1);
		game.doMove(7, 1);
		game.doMove(8, 1);
		game.doMove(28, 1);
		game.doMove(34, 1);
		game.doMove(4, 2);
		game.doMove(5, 2);
		game.doMove(24, 2);
		game.doMove(32, 2);
		game.doMove(14, 3);
		game.doMove(16, 3);
		game.doMove(20, 3);
		game.doMove(15, 3);
		game.doMove(22, 3);
		
		colour1.add(7);
		colour1.add(8);
		colour1.add(28);
		colour1.add(34);
		
		colour2.add(4);
		colour2.add(5);
		colour2.add(24);
		colour2.add(32);
		
		colour3.add(14);
		colour3.add(16);
		colour3.add(20);
		colour3.add(15);
		colour3.add(22);
		
	}

	@Test
	public void testgetNeighbours() {
		int[] index14 = {15, 13, 20, 8};
		int[] index5 = {4, 11};
		int[] index18 = {12, 19, 14};
		Arrays.equals(game.getNeighbours(14), index14);
		Arrays.equals(game.getNeighbours(5), index5);
		Arrays.equals(game.getNeighbours(18), index18);
	}
	
	@Test
	public void testgetPointsofColour() {
		assertTrue(colour1.containsAll(game.getPointsofColour(1, game.getBoard())));
		assertTrue(colour2.containsAll(game.getPointsofColour(2, game.getBoard())));
	}
	
	@Test
	public void testdetermineGroups() {
		List<List<Integer>> groupsColour1 = new CopyOnWriteArrayList<List<Integer>>();
		List<Integer> group1Colour1 = Arrays.asList(7, 8);
		List<Integer> group2Colour1 = Arrays.asList(28, 34);
		groupsColour1.add(group1Colour1);
		groupsColour1.add(group2Colour1);
		
		List<List<Integer>> groupsColour2 = new ArrayList<List<Integer>>();
		List<Integer> group1Colour2 = Arrays.asList(4, 5);
		List<Integer> group2Colour2 = Arrays.asList(24);
		List<Integer> group3Colour2 = Arrays.asList(32);
		groupsColour2.add(group1Colour2);
		groupsColour2.add(group2Colour2);
		groupsColour2.add(group3Colour2);
		
		List<List<Integer>> groupsColour3 = new ArrayList<List<Integer>>();
		List<Integer> group1Colour3 = Arrays.asList(14, 16, 20, 15, 22);
		groupsColour3.add(group1Colour3);
		
		System.out.println(game.determineGroups(colour1));
		System.out.println(game.determineGroups(colour2));
		System.out.println(game.determineGroups(colour3));
		
		assertTrue(groupsColour1.containsAll(game.determineGroups(colour1)));
		assertTrue(groupsColour2.containsAll(game.determineGroups(colour2)));
		assertTrue(groupsColour3.containsAll(game.determineGroups(colour3)));
	}
	
	@Test
	public void testgetGroupsNeighbours() {
		List<Integer> group2Colour1 = Arrays.asList(28, 34);
		System.out.println(Arrays.toString(game.getGroupsNeighbours(group2Colour1)));
	}
	
	@Test
	public void testremoveCaptured() {
		game.doMove(18, 1);
		game.doMove(25, 1);
		game.doMove(30, 1);
		System.out.println(game.getTUIboard());
		game.removeCaptured(2, 1, game.getBoard());
		System.out.println(game.getTUIboard());
	}
	
	@Test
	public void testgetScore() {
		game.doMove(18, 1);
		game.doMove(25, 1);
		game.doMove(30, 1);
		game.removeCaptured(2, 1, game.getBoard());
		
		System.out.println(game.getScore(1));
		System.out.println(game.getScore(2));
		System.out.println(game.getScore(3));
	}
}
