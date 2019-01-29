package com.nedap.go;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class for maintaining the Go game. Returns current player, number of passes,
 * score of colour, check if game is finished, gives current status of board do
 * move, pass, remove stone
 * 
 * valid move (game his, rules)
 * 
 * @author marije.linthorst
 */
public class Game {
	private Board board;
	private int countPasses; // how many players have passed
	private Player[] players; // players in the game
	private int current; // index current player
	private List<String> history;
	private int gameID;
	private List<List<Integer>> groups = new CopyOnWriteArrayList<List<Integer>>();

	// -- Constructor -----------------------------------------------

	/**
	 * Creates a new Game object.
	 * 
	 * @param s0 the first player
	 * @param s1 the second player
	 */
	public Game(Player[] players, int boardsize, int gameID) {
		board = new Board(boardsize);
		this.players = players;
		this.gameID = gameID;
		current = 0;
		history = new ArrayList<String>();
	}

	// ---- Queries ------------------------------------------------

	public int getGameID() {
		return gameID;
	}

	public int currentPlayer() {
		return players[current].getColour();
	}

	public int numberOfPasses() {
		return countPasses;
	}

	public int getBoardSizeN() {
		return board.getBoardSizeN();
	}
	
	public String getCurrentBoard() {
		return board.getCurrentStringBoard();
	}
	
	public Board getBoard() {
		return this.board;
	}

	public double getScore(int colour) {
		int score = 0;
		score = score + this.getPointsofColour(colour, this.board).size();
		
		List<Integer> emptyStones = this.getPointsofColour(0, this.board);
		List<List<Integer>> emptyGroups = this.determineGroups(emptyStones);
		for (List<Integer> group : emptyGroups) {
			int count = 0;
			int[] groupsNeighbours = this.getGroupsNeighbours(group);
			for (int indexToCheck : groupsNeighbours) {
				if (board.getPoint(indexToCheck) != colour) {
					count++;
				}
			}
			if (count == 0) {
				score = score + group.size();
			}
		}
		if (colour != 1) {
			return score + 0.5;
		} else {
			return score;
		}
	}

	public boolean isFinished() {
		if (countPasses == players.length || board.isFull()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * returns message about the validity of the move.
	 * 
	 * @param index  of the move
	 * @param colour of the player
	 * @return
	 */
	public String isValidMove(int index, int colour) {
		if (!board.onBoard(index)) {
			return "Move invalid: not on board";
		}
		if (!board.isEmpty(index)) {
			return "Move invalid: point not empty";
		}
		
		Board copy = board.deepCopy();
		copy.setPoint(index, colour);
		int capturedColour = 10;
		if (colour == 1) {
			capturedColour = 2;
		} else if (colour == 2) {
			capturedColour = 1;
		}
		this.removeCaptured(capturedColour, colour, copy);
		this.removeCaptured(colour, capturedColour, copy);
		
		if (history.contains(copy.getCurrentStringBoard())) {
			return "Move invalid: creates a previous board state";
		}
		return "Move valid";
	}

	/**
	 * Returns player that has won. <br>
	 */
	public String determineWinner() {
		Player winner = players[0];
		for (int i = 1; i < players.length; i++) {
			if (winner.getScore(this) < players[i].getScore(this)) {
				winner = players[i];
			}
		}
		return winner.getName();
	}

	/**
	 * returns the game situation as a String.
	 */
	public String update() {
		return board.getCurrentStringBoard();
	}

	// -- Commands ---------------------------------------------------

	/**
	 * Places stone on point, resets passes and changes current player to the next
	 * player. Adds board, after move, to the history
	 */
	public void doMove(int index, int colour) {
		board.setPoint(index, colour);
		Board copy = board.deepCopy(); 
		// TODO: is copy nodig?
		history.add(copy.getCurrentStringBoard());
		countPasses = 0;
		current++;
		if (current == players.length) {
			current = 0;
		}
	}

	/**
	 * Counts how often the method is called and changes the current player to the
	 * next player.
	 */
	public void doPass() {
		countPasses++;
		current++;
		if (current == players.length) {
			current = 0;
		}
	}

	/**
	 * removes stone from board (e.g. when captured)
	 * 
	 * @param index
	 */
	public void removeStone(int index) {
		board.setPoint(index, 0);
	}
	
	public void removeGroup(List<Integer> group) {
		for (int i: group) {
			board.setPoint(i, 0);
		}
	}
	
	public void removeCaptured(int capturedColour, int otherColour, Board board) {	
		List<Integer> stonesOfColour = this.getPointsofColour(capturedColour, board);
		List<List<Integer>> groupsOfColour = this.determineGroups(stonesOfColour);
		for (List<Integer> group : groupsOfColour) {
			int count = 0;
			int[] groupsNeighbours = this.getGroupsNeighbours(group);
			for (int indexToCheck : groupsNeighbours) {
				if (board.getPoint(indexToCheck) != otherColour) {
					count++;
				}
			}
			if (count == 0) {
				this.removeGroup(group);
			}
		}
	}
	
	public String getTUIboard() {
		return board.toTUIString();
	}
	
	//------------------------- make groups ---------------------------------
	/**
	 * returns a integer array with the indexes of the neighbours of the given index.
	 * This function is necessary for making groups of the same colour (or empty, 0)
	 * 
	 * @param index
	 * @return integer array
	 */
	public int[] getNeighbours(int index) {
		int[] neighbours;
		int n = this.getBoardSizeN();
		
		// check the corners first, then edges and then the rest
		if (index == 0) {
			neighbours = new int[] {index + 1, index + n};
		} else if (index == n - 1) {
			neighbours = new int[] {index - 1, index + n};
		} else if (index == n * n - 1) {
			neighbours = new int[] {index - 1, index - n};
		} else if (index == n * n - n) {
			neighbours = new int[] {index + 1, index - n};
		} else if (index < n) {
			neighbours = new int[] {index + 1, index - 1, index + n};	
		} else if (index >= n * n - n) {
			neighbours = new int[] {index + 1, index - 1, index - n};
		} else if (index % n == 0) {
			neighbours = new int[] {index + 1, index + n, index - n};
		} else if (index % n == n - 1) {
			neighbours = new int[] {index - 1, index + n, index - n};
		} else {
			neighbours = new int[] {index + 1, index - 1, index + n, index - n};
		}
		return neighbours;
	}
	
	/**
	 * returns a integer list with the indexes of all the stones of given colour.
	 * These are the stones to check for making groups
	 * 
	 * @param colour
	 * @return List<Integer>
	 */
	public List<Integer> getPointsofColour(int colour, Board board) {
		int n = this.getBoardSizeN();
		List<Integer> points = new ArrayList<Integer>();
		for (int i = 0; i < n * n; i++) {
			if (board.getPoint(i) == colour) {
				points.add(i);
			}
		}
		return points;
	}
	
	public List<List<Integer>> determineGroups(List<Integer> listToCheck) {
		if (listToCheck.isEmpty()) {
			groups.clear();
		} else {
			int index = listToCheck.get(0);
			listToCheck.remove(0);
			this.determineGroups(listToCheck);
			if (!this.findGroup(index)) {
				List<Integer> tempGroup = new CopyOnWriteArrayList<Integer>();
				tempGroup.add(index);
				groups.add(tempGroup);
			}
			if (groups.size() > 1) {
				this.joinGroups(index);	
			}	
		}
		return groups;
	}
	
	public boolean findGroup(int index) {
		int count = 0;
		for (List<Integer> group : groups) {
			for (int i : group) {
				int[] neighbours = this.getNeighbours(i);
				for (int neighbour : neighbours) {
					if (index == neighbour) {
						group.add(index);
						count++;
					}
				}
			}
		}
		return count > 0;
	}
	
	public void joinGroups(int index) {
		List<List<Integer>> temp = new CopyOnWriteArrayList<List<Integer>>();
		for (List<Integer> group : groups) {
			if (group.contains(index)) {
				temp.add(group);
				groups.remove(group);
			}
		}
		if (temp.size() > 1) {
			List<Integer> result = new CopyOnWriteArrayList<Integer>();
			for (List<Integer> group : temp) {
				for (int i : group) {
					if (!result.contains(i)) {
						result.add(i);
					}
				}
			}
			groups.add(result);
		} else if (temp.size() == 1) {
			groups.add(temp.get(0));
		}
	}
	
	public int[] getGroupsNeighbours(List<Integer> group) {
		int[] allNeighbours = null;
		Set<Integer> set = new LinkedHashSet<Integer>();
	
		for (int i : group) {
			int[] neighbours = this.getNeighbours(i);
			if (allNeighbours == null) {					
				allNeighbours = neighbours;
			} else {
				int[] result = new int[allNeighbours.length + neighbours.length];
				System.arraycopy(neighbours, 0, result, 0, neighbours.length);
				System.arraycopy(allNeighbours, 0, result, neighbours.length, 
						allNeighbours.length);
				allNeighbours = result;
			}
		}
		
		for (int i : allNeighbours) {
			set.add(i);
		}
		for (int i: group) {
			set.remove(i);
		}
		int[] groupsNeighbours = new int[set.size()];
		int i = 0;
		for (Integer index : set) {
			groupsNeighbours[i] = index;
			i++;
		}
		
		return groupsNeighbours;
	}
	
	// ------------- for comp player -------------
	public void updateBoard(String board) {
		history.add(board);
		this.board = new Board(board);
	}
}
