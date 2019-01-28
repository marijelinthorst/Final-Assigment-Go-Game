package com.nedap.go;

public class HumanPlayer extends PlayingPlayer {
	private int gameID;

	public HumanPlayer(String name, int colour, int gameID) {
		super(name, colour);
		this.gameID = gameID;
	}

	//TODO; hint functie
	/**
	 * Determines the field for the next move.
	 * 
	 * @param board: the current game board
	 * @return the player's choice
	 */
	public String determineMove(String line) {
		if (line.equals("EXIT")) {
			return this.exit();
		} else if (line.equals("PASS")) {
			return this.pass();
		} else if (line.startsWith("MOVE")) {
			return this.move(line);
		} else {
			return "INVALID";
		}
	}

	// -------- CREATING COMMANDS---------------------------------
	private String move(String line) {
		String[] input = line.split("\\,");
		if (input.length == 2) {
			String intMove = input[1];
			return "MOVE+" + gameID + "+" + this.getName() + "+" + intMove;
		} else {
			return "";
		}
		
	}

	private String pass() {
		return "MOVE+" + gameID + "+" + this.getName() + "+" + "-1";
	}

	private String exit() {
		return "EXIT+" + gameID + "+" + this.getName();
	}
}
