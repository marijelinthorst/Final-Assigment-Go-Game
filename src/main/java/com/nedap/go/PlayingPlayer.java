package com.nedap.go;

public abstract class PlayingPlayer extends Player {

	public PlayingPlayer(String name, int colour) {
		super(name, colour);
	}
	
	public abstract String determineMove(String board);
	
}
