package com.nedap.go;

import java.util.List;
import java.util.Random;

public class NaiveCompPlayer extends PlayingPlayer {
	private List<String> history;
	private int gameID;
	

	public NaiveCompPlayer(String name, int colour, int gameID) {
		super(name, colour);
		this.gameID = gameID;
	}

	/**
	 * Determines the field for the next move.
	 * 
	 * @param board: the current game board
	 * @return the player's choice
	 */
	public String determineMove(String board) {
		Board currentBoard = new Board(board);
		this.addToHistory(board);
		boolean valid = false;
		int randomMove = 0;
		while (!valid) {
			int upperRange = currentBoard.getBoardSizeN() * currentBoard.getBoardSizeN() - 1;
			Random random = new Random();
			randomMove = random.nextInt(upperRange);

			String validMove = this.isValidMove(randomMove, this.getColour(), currentBoard);
			valid = validMove == "Move valid";
		}
		return "MOVE" + gameID + "+" + this.getName() + "+" + randomMove;
	}

	public String isValidMove(int index, int colour, Board board) {
		// check for captures and remove stuff if needed
		// check if its the right colour to doMove
		if (!board.onBoard(index)) {
			return "Move invalid: not on board";
		}
		if (!board.isEmpty(index)) {
			return "Move invalid: point not empty";
		}
		Board copy = board.deepCopy();
		copy.setPoint(index, colour);
		if (history.contains(copy.getCurrentStringBoard())) {
			return "Move invalid: creates a previous board state";
		}
		// rules: check rules etc
		return "Move valid";
	}

	private void addToHistory(String board) {
		history.add(board);
	}
}
