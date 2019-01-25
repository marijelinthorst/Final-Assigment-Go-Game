package com.nedap.go;

/**
 * Class for creating a player with name and colour. Can determine the score of
 * this player
 * 
 * @author marije.linthorst
 */
public class Player {
	private int colour;
	private String name;

	// -- Constructors -----------------------------------------------

	/**
	 * Creates a new Player object with name and colour.
	 * 
	 */
	public Player(String name, int colour) {
		this.name = name;
		this.colour = colour;
	}

	// -- Queries ----------------------------------------------------

	/**
	 * Returns the name of the player.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the colour of the player.
	 */
	public int getColour() {
		return colour;
	}

	/**
	 * Returns the colour of the player.
	 */
	public double getScore(Game game) {
		return game.getScore(colour);
	}
}
