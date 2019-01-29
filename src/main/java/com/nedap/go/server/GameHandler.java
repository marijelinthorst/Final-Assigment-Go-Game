package com.nedap.go.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.nedap.go.Game;
import com.nedap.go.Player;

public class GameHandler extends Thread {
	private volatile List<ClientHandler> cHandlers;
	private Player[] players; // players in the game 
	private Game game;
	private int gameID;
	private ClientHandler black; // colour = 1
	private ClientHandler white; // colour = 2
	private final int pass = -1;
	
	// variables to set up game
	private int handshakesCount;
	private int boardsize;
	private String player1; // name of first player to send handshake
	private String player2; // name of second player to send handshake
	private ClientHandler leader;
	private ClientHandler opponant;
	private int prefColour;
	
	// States
	private boolean isFinished;
	private boolean isConfigured;
	private boolean isWaiting;
	
	// lengths commands
	private final int handshakeL = 2;
	private final int setConfigL = 4;
	private final int rematchL = 2;
	private final int moveL = 4;
	private final int exitL = 3;
	
	// errors
	private final String unknown = "UNKNOWN_COMMAND+";
	private final String invalidM = "INVALID_MOVE+";
	
	
	//-------------- Constructor ------------------------------------
	public GameHandler(int gameID) {
		this.gameID = gameID;
		isFinished = false;
		isConfigured = false;
		handshakesCount = 0;
		player1 = "";
		player2 = "";
		cHandlers = new CopyOnWriteArrayList<ClientHandler>();
		isWaiting = false;
	}
	
	//-------------- Commands ---------------------------------------
	public void run() {
		System.out.println("runGame started");
		while (!isFinished && !isConfigured && handshakesCount <= 2) {
			for (ClientHandler player : cHandlers) {
				String input = player.readQueue();
				
				if (input.equals("EmptyQueue")) {
					// TODO: als langer dan ... niet reageert dan eruit
				} else if (input.startsWith("HANDSHAKE")) {
					System.out.println("got handshake");
					this.checkHandshakes(input, player);
				} else if (input.startsWith("SET_CONFIG") && player.equals(leader)) {
					System.out.println("got set config ");
					this.checkSetConfig(input, player);
				} else {
					player.sendMessage("UNKNOWN_COMMAND+Handshake or "
							+ "set_config required");
				}
			}
		}
		System.out.println("hij gaat nu game maken");
		createGame();
		sendAckConfig();
		
		while (!isFinished && isConfigured) {
			if (game.currentPlayer() == 1) {
				String gameInput = black.readQueue();
				if (gameInput != "EmptyQueue") {
					this.handle(gameInput, black);
				}	
			} else {
				String gameInput = white.readQueue();
				if (gameInput != "EmptyQueue") {
					this.handle(gameInput, white);
				}
			}
			isFinished = game.isFinished();
		}
		
		// TODO: REQUEST_REMATCH
		// if (input.startsWith("SET_REMATCH")) {
		//this.handleSetRematch(input);
		//}
		// game.reset --> maak board leeg en current player weer 0
		// stuur ack config
	}
	
	// --------------- Handle user input before game -----------------------------
	public void checkHandshakes(String input, ClientHandler player) {
		String[] handshake = input.split("\\+");
		if (handshake.length == handshakeL) {
			if (handshakesCount == 0) {
				player1 = handshake[1];
				leader = player;
				player.sendMessage("ACKNOWLEDGE_HANDSHAKE+" + gameID + "+" + 1);
				player.sendMessage("REQUEST_CONFIG+Please provide a preferred configuration. "
						+ "(preferred colour and board size");
				System.out.println("config requested");
				handshakesCount++;
			} else if (handshakesCount == 1) {
				player2 = handshake[1];
				opponant = player;
				player.sendMessage("ACKNOWLEDGE_HANDSHAKE+" + gameID + "+" + 0);
				System.out.println("tweede player binnen");
				handshakesCount++;
				if (isWaiting == true) {
					isConfigured = true;
					System.out.println("waiting is klaar, configured is klaar");
				} 
			} 
		} else {
			player.sendMessage(unknown + "Handshake command length is not 2");
		}	
	}
	
	public void checkSetConfig(String input, ClientHandler player) {
		String[] setConfig = input.split("\\+");
		if (player.equals(leader)) {
			if (setConfig.length == setConfigL && setConfig[1].equals(Integer.toString(gameID))) {
				prefColour = Integer.parseInt(setConfig[2]);
				if (prefColour == 1 || prefColour == 2) {
					boardsize = Integer.parseInt(setConfig[3]);
					System.out.println("set config is gedaan");
					if (handshakesCount == 2) {
						isConfigured = true;
					} else {
						isWaiting = true;
						System.out.println("waiting is true");
					}
				} else {
					player.sendMessage(unknown + "preffered colour needs to be 1 or 2");
				}
			} else {
				player.sendMessage(unknown + "Set config command length is not 4 "
						+ "or gameID is incorrect");
			}
		} else {
			player.sendMessage("you are not the leader of this game, wait for opponant"
					+ " to configurate this game");
		}
	}
	
	public void createGame() {
		if (this.full()) {
			players = new Player[2];
			if (prefColour == 1) {
				players[0] = new Player(player1, prefColour);
				players[1] = new Player(player2, 2);
				black = leader;
				white = opponant;
			} else {
				players[0] = new Player(player2, 1);
				players[1] = new Player(player1, prefColour);
				black = opponant;
				white = leader;
			}
			game = new Game(players, boardsize, gameID);
		}
	}
	
	public void sendAckConfig() {
		//ACKNOWLEDGE_CONFIG+$PLAYER_NAME+$COLOR+$SIZE+$GAME_SATE+$OPPONENT
		String gameState = "PLAYING" + ";" + game.currentPlayer() + ";" + game.getCurrentBoard();
		String messageblack = "ACKNOWLEDGE_CONFIG+" + players[0].getName() + "+" + "1+" 
				+ game.getBoardSizeN() + "+" + gameState + "+" + players[1].getName();
		String messagewhite = "ACKNOWLEDGE_CONFIG+" + players[1].getName() + "+" + "2+" 
				+ game.getBoardSizeN() + "+" + gameState + "+" + players[0].getName();
		
		black.sendMessage(messageblack);
		white.sendMessage(messagewhite);
	}
	
	
	// ---------------- Handle user input when game is started -------------- 
	public void handle(String input, ClientHandler colour) {
		if (input.startsWith("MOVE")) {
			this.handleMove(input, colour);
		} else if (input.startsWith("EXIT")) {
			this.handleExit(input, colour);
		} else {
			System.out.println(unknown + "Please enter MOVE, EXIT command");
		}
	}
	
	public void handleMove(String input, ClientHandler colour) {
		//MOVE+$GAME_ID+$PLAYER_NAME+$TILE_INDEX
		Player playingPlayer;
		int capturedColour;
		if (colour.equals(black)) {
			playingPlayer = players[0];
			capturedColour = players[1].getColour();
		} else {
			playingPlayer = players[1];
			capturedColour = players[0].getColour();
		}
		
		String[] move = input.split("\\+");
		if (move.length == moveL) {
			int moveInt = Integer.parseInt(move[3]);
			// TODO: try
			if (move[1].equals(Integer.toString(gameID)) && 
					move[2].equals(playingPlayer.getName())) {
				if (move[2].equals(Integer.toString(pass))) {
					game.doPass();
					sendAckMove(pass, playingPlayer.getColour());
				} else {
					if (game.isValidMove(moveInt, playingPlayer.getColour()).equals("Move valid")) {
						game.doMove(moveInt, playingPlayer.getColour());
						game.removeCaptured(capturedColour, playingPlayer.getColour());
						// need to check both ways since you can suicide a group and don't recreate
						// a previous board state (since the other player did moves)
						game.removeCaptured(playingPlayer.getColour(), capturedColour);
						sendAckMove(moveInt, playingPlayer.getColour());
					} else {
						colour.sendMessage(invalidM + 
								game.isValidMove(moveInt, playingPlayer.getColour()));
					}
				}
			} else {
				colour.sendMessage(invalidM + "GameID is not correct or "
						+ "you are not the current player");
			}
		} else {
			System.out.println(unknown + "Move command length is not 4");
		}
		
	}
	
	public void sendAckMove(int move, int colour) {
		// ACKNOWLEDGE_MOVE+$GAME_ID+$MOVE+$GAME_STATE
		String status;
		if (game.isFinished()) {
			status = "FINISHED";
		} else {
			status = "PLAYING";
		}
		String gameState = status + ";" + game.currentPlayer() + ";" + game.getCurrentBoard();
		String message = "ACKNOWLEDGE_MOVE+" + gameID + "+" + move + ";" + colour + "+" + gameState;
		black.sendMessage(message);
		white.sendMessage(message);
		if (game.isFinished()) {
			sendGameFinished(game.determineWinner(), "Game ended");
		}
	}
	
	public void sendGameFinished(String winner, String reason) {
		// GAME_FINISHED+$GAME_ID+$WINNER+$SCORE+$MESSAGE
		String message = "GAME_FINISHED+" + gameID + "+" + winner + "+"
				+ game.getScore(1) + ";" + game.getScore(2) + "+" + reason;
		black.sendMessage(message);
		white.sendMessage(message);
		this.isFinished = true;
	}
	
	public void handleExit(String input, ClientHandler colour) {
		// EXIT+$GAME_ID+$PLAYER_NAME
		Player playingPlayer;
		Player winner;
		if (colour.equals(black)) {
			playingPlayer = players[0];
			winner = players[1];
		} else {
			playingPlayer = players[1];
			winner = players[0];
		}
		
		String[] exit = input.split("\\+");
		if (exit.length == exitL) {
			if (exit[1].equals(Integer.toString(gameID)) && 
					exit[2].equals(playingPlayer.getName())) {
				// TODO: sluit af
				sendGameFinished(winner.getName(), playingPlayer.getName() + "left the game");
			} else {
				System.out.println(unknown + "GameID and/or player name is not correct");
			}
		} else {
			System.out.println(unknown + "Exit command length is not 3");
		}
	}
	
	// -------------------- Handle rematch -------------------------
	
	// TODO: send rematch request
	
	public void handleSetRematch(String input) {
		String[] setRematch = input.split("\\+");
		if (setRematch.length == rematchL) {
			// do something
			// move -1 is pass
		} else {
			System.out.println(unknown + "Set rematch command length is not 3");
		}
	}
	
	// -------------- Queries ---------------------------------------
	public List<ClientHandler> getClientHandlers() {
		return cHandlers;
	}
	
	public boolean full() {
		return cHandlers.size() == 2;
	}
	
	public void addClientHandler(ClientHandler player) {
		cHandlers.add(player);
	}
	
	public void removeClientHandler(ClientHandler player) {
		cHandlers.remove(player);
		handshakesCount--;
		// TODO: zorg ervoor opnieuw run, gameconfig enzo;
	}
}
