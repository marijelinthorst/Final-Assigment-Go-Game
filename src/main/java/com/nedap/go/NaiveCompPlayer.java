package com.nedap.go;

import java.util.Random;

public class NaiveCompPlayer extends PlayingPlayer {
	private int gameID;
	private Game game;
	private boolean firstDetermineMove = true;
	

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
		if (firstDetermineMove) {
			Player[] players = new Player[2];
			game = new Game(players, 1, gameID); // boardsize is not important at this point
			firstDetermineMove = false;
		}
		game.updateBoard(board);
		
		boolean valid = false;
		int randomMove = 0;
		while (!valid) {
			int upperRange = game.getBoardSizeN() * game.getBoardSizeN() - 1;
			Random random = new Random();
			randomMove = random.nextInt(upperRange);
			valid = game.isValidMove(randomMove, this.getColour()) == "Move valid";
		}
		return "MOVE" + gameID + "+" + this.getName() + "+" + randomMove;
	}
}
