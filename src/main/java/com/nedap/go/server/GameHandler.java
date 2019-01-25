package com.nedap.go.server;

import java.util.List;

import com.nedap.go.Game;
import com.nedap.go.Player;

public class GameHandler {
	private List<ClientHandler> cHandlers;
	private Player[] players; // players in the game 
	private Game game;
	private int gameID;
	private int handshakesCount;
	private int boardsize;
	
	// States
	private boolean isFinished;
	private boolean isConfigured;
	
	
	//-------------- Constructor ------------------------------------
	public GameHandler(int gameID) {
		this.gameID = gameID;
		isFinished = false;
		isConfigured = false;
		handshakesCount = 0;
	}
	
	//-------------- Commands ---------------------------------------
	public void runGame() {
		
		while (!isFinished && !isConfigured && handshakesCount != 2) {
			this.checkHandshakes();
			this.checkSetConfig();
		}
	
		createGame(boardsize);
		
		while (!isFinished && isConfigured) {
			String gameInput = cHandlers.get(game.currentPlayer()).readQueue();
			this.handle(gameInput);
		}	
	}
	
	
	
	
	
	
	public void checkHandshakes() {
		for (ClientHandler lijst : cHandlers) {
			String input = lijst.readQueue();
			
			if (input.startsWith("HANDSHAKE")) {
				// ack handshake
				// request config
				handshakesCount++;
			} else {
				cHandlers.get(0).sendMessage("UNKNOWN_COMMAND+Handshake required");
			}
		}
	}
	
	public void checkSetConfig() {
		for (ClientHandler lijst : cHandlers) {
			String input = lijst.readQueue();
			
			if (input.startsWith("SET_CONFIG")) {
				// do something
				// determine boardsize
				// remember size for row/col to index
				isConfigured = true; 
			} else {
				cHandlers.get(0).sendMessage("UNKNOWN_COMMAND+Set_config required");
			}
		}
	}
	
	
	public void addClientHandler(ClientHandler player) {
		cHandlers.add(player);
	}
	
	public void createGame(int boardsize) {
		if (this.full()) {
			players = new Player[2];
			players[0] = new Player(cHandlers.get(0).getName(), 1);
			players[1] = new Player(cHandlers.get(1).getName(), 2);
			game = new Game(players, boardsize, gameID);
		}
	}
	
	// -------------- Queries ---------------------------------------
	public List<ClientHandler> getClientHandlers() {
		return cHandlers;
	}
	
	public boolean full() {
		return cHandlers.size() == 2;
	}
	
	// --------------- handle user input --------------
	
	public void handle(String input) {
		if (input.startsWith("MOVE")) {
			// do something
			// move -1 is pass
		} else if (input.startsWith("EXIT")) {
			// do something
		} else if (input.startsWith("SET_GAME")){
			// do something
		} else {
			System.out.println("Unknown command. Known commands: " + 
					" HANDSHAKE, SET_CONFIG, MOVE, EXIT");
			// plus wat er bij moet
		}
	}
}
