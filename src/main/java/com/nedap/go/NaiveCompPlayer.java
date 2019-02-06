package com.nedap.go;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NaiveCompPlayer extends PlayingPlayer {
	private int gameID;
	private Game game;
	private boolean firstDetermineMove = true;
	private List<Integer> possibleMoves;
	

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
		possibleMoves = new ArrayList<Integer>();
		if (firstDetermineMove) {
			Player[] players = new Player[2];
			game = new Game(players, 1, gameID); // boardsize is not important at this point
			firstDetermineMove = false;
		}
		game.updateBoard(board);
		
		for (int i = 0; i < game.getBoardSizeN() * game.getBoardSizeN(); i++) {
			if (game.getBoard().getPoint(i) == 0) {
				possibleMoves.add(i);
			}
		}
		
		
		boolean valid = false;
		int randomMove = 0;
		while (!valid && !possibleMoves.isEmpty()) {
			int moveIndex = (int) (Math.random() * possibleMoves.size());
			randomMove = possibleMoves.get(moveIndex);
			valid = game.isValidMove(randomMove, this.getColour()) == "Move valid";
			possibleMoves.remove(moveIndex);
		}
		
		if (!valid || possibleMoves.isEmpty()) {
			return "MOVE+" + gameID + "+" + this.getName() + "+" + "-1";
		}
		
		return "MOVE+" + gameID + "+" + this.getName() + "+" + randomMove;
	}
}
