package com.nedap.go;

import java.util.Scanner;

public class HumanPlayer extends Player {
	private int gameID;
	private String command = null;
	private int move = 0;
	private Scanner clientIn;

	public HumanPlayer(String name, int colour, int gameID) {
		super(name, colour);
		this.gameID = gameID;
	}

	/**
	 * Determines the field for the next move.
	 * 
	 * @param board: the current game board
	 * @return the player's choice
	 */
	public String determineMove(Board board) {
		return askUserMove();
	}

	private String askUserMove() {
		clientIn = new Scanner(System.in);
		String message = "> " + this.getName() + " (" + this.getColour() + "),"
				+ ", what is your choice? Type MOVE, PASS or EXIT";

		do {
			System.out.print(message);
			if (clientIn.hasNext()) {
				command = clientIn.next();
			}
		} while (command.equals("PASS") || command.startsWith("MOVE") || command.equals("EXIT"));

		if (command.equals("PASS")) {
			return this.pass();
		} else if (command.startsWith("MOVE")) {
			do {
				System.out.println("Please enter index");
				if (clientIn.hasNextInt()) {
					move = clientIn.nextInt();
				}
			} while (!clientIn.hasNextInt());
			return this.move();
		} else if (command.equals("EXIT")) {
			return this.exit();
		} else {
			return "";
		}
	}

	// -------- CREATING COMMANDS---------------------------------
	private String move() {
		
		return "MOVE+" + gameID + "+" + this.getName() + "+" + move;
	}

	private String pass() {
		return "MOVE+" + gameID + "+" + this.getName() + "+" + "-1";
	}

	private String exit() {
		return "EXIT+" + gameID + "+" + this.getName();
	}
}
