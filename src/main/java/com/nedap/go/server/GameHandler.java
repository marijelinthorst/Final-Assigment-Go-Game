package com.nedap.go.server;

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
	
	private int rematchCount;
	private int setRematch;
	
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
		rematchCount = 0;
		player1 = "";
		player2 = "";
		cHandlers = new CopyOnWriteArrayList<ClientHandler>();
		isWaiting = false;
	}
	
	//-------------- Run ---------------------------------------
	public void run() {
		System.out.println("runGame started");
		while (!isFinished) {
			for (ClientHandler player : cHandlers) {
				String input = player.readQueue();
				this.handleInput(input, player);
			}
		}
		// TODO: als iemand weg gaat, gameFinished try en verwijder diegene uit lijst
	}
	
	private void handleInput(String line, ClientHandler player) {
		if (line.startsWith("EXIT") || line.equals("FAIL")) {
			this.handleExitAndDisconnect(line, player);
		} else if (!isConfigured && handshakesCount <= 2) {
			this.configGame(line, player);
		} else if (isConfigured) {
			this.playGame(line, player);
		} else {
			System.out.println("No input expected, but received: " + line);
		}
	}
	
	// --------------- Handle user input before game -----------------------------
	
	
	private void configGame(String input, ClientHandler player) {
		if (input.equals("EmptyQueue")) {
			// TODO: als langer dan ... niet reageert dan eruit
		} else if (input.startsWith("HANDSHAKE")) {				
			System.out.println("Server received handshake");
			this.checkHandshakes(input, player);
		} else if (input.startsWith("SET_CONFIG") && player.equals(leader)) {
			System.out.println("Server received set configuration ");
			this.checkSetConfig(input, player);
		} else {
			player.sendMessage("UNKNOWN_COMMAND+Handshake or "
					+ "set_config required");
		}
	}
	
	public void checkHandshakes(String input, ClientHandler player) {
		String[] handshake = input.split("\\+");
		if (handshake.length == handshakeL) {
			if (handshakesCount == 0) {
				player1 = handshake[1];
				leader = player;
				player.sendMessage("ACKNOWLEDGE_HANDSHAKE+" + gameID + "+" + 1);
				player.sendMessage("REQUEST_CONFIG+Please provide a preferred configuration. "
						+ "(preferred colour and board size");
				System.out.println("Config requested");
				handshakesCount++;
			} else if (handshakesCount == 1) {
				player2 = handshake[1];
				opponant = player;
				player.sendMessage("ACKNOWLEDGE_HANDSHAKE+" + gameID + "+" + 0);
				System.out.println("Second player found");
				handshakesCount++;
				if (isWaiting == true) {
					createGame();
					sendAckConfig();
					isConfigured = true;
					System.out.println("No longer waiting on second player, configuration is done");
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
					System.out.println("Set config is received");
					if (handshakesCount == 2) {
						createGame();
						sendAckConfig();
						isConfigured = true;
						System.out.println("Ack config is send");
					} else {
						isWaiting = true;
						System.out.println("Waiting on second player");
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
	
	private void playGame(String input, ClientHandler player) {
		if (game.currentPlayer() == 1 && player.equals(black)) {
			if (input != "EmptyQueue") {
				this.handleGame(input, player);
			}
		} else if (game.currentPlayer() == 2 && player.equals(white)) {
			if (input != "EmptyQueue") {
				this.handleGame(input, player);
			}
		} else {
			//TODO: het is niet jouw beurt
		}

		if (game.isFinished()) {
			System.out.println("Game is finished");
			sendGameFinished(game.determineWinner(), "Game ended");
			handleRequestRematch();
		}			
	}
	
	public void handleGame(String input, ClientHandler colour) {
		if (input.startsWith("MOVE")) {
			this.handleMove(input, colour);
		} else {
			colour.sendMessage((unknown + "Please enter MOVE or EXIT command"));
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
				if (moveInt == pass) {
					game.doPass();
					sendAckMove(pass, playingPlayer.getColour());
				} else {
					if (game.isValidMove(moveInt, playingPlayer.getColour()).equals("Move valid")) {
						game.doMove(moveInt, playingPlayer.getColour());
						game.removeCaptured(capturedColour, playingPlayer.getColour(), game.getBoard());
						// need to check both ways since you can suicide a group and don't recreate
						// a previous board state (since the other player did moves)
						game.removeCaptured(playingPlayer.getColour(), capturedColour, game.getBoard());
						game.addCurrentBoardToHistory();
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
	}
	
	public void sendGameFinished(String winner, String reason) {
		// GAME_FINISHED+$GAME_ID+$WINNER+$SCORE+$MESSAGE
		String message = "GAME_FINISHED+" + gameID + "+" + winner + "+"
				+ game.getScore(1) + ";" + game.getScore(2) + "+" + reason;
		for (ClientHandler player: cHandlers) {
			player.sendMessage(message);
		}
		// TODO: dit moet niet hier?
		// this.isFinished = true;
	}
	
	// --------------------- exit -------------------------
	public void handleExitAndDisconnect(String input, ClientHandler colour) {
		// EXIT+$GAME_ID+$PLAYER_NAME
		System.out.println("An exit or disconnect is received");
		Player exitPlayer;
		Player winner;
		if (colour.equals(black)) {
			exitPlayer = players[0];
			winner = players[1];
		} else {
			exitPlayer = players[1];
			winner = players[0];
		}
		
		System.out.println("Determined winner");
		
		if (input.startsWith("EXIT")) {
			String[] exit = input.split("\\+");
			if (exit.length == exitL && exit[1].equals(Integer.toString(gameID)) && 
					exit[2].equals(exitPlayer.getName())) {
				System.out.println("EXIT correct");
				colour.shutdown();
				System.out.println("Shutdown clienthandler");
				this.removeClientHandler(colour);
				System.out.println("Removed clienthandler");
				sendGameFinished(winner.getName(), (exitPlayer.getName() + "left the game"));
				System.out.println("Send game finished");
				this.handleRequestRematch();
			} else {
				System.out.println(unknown + "Exit command length, GameID and/or player "
						+ "name is not correct");
			}
		} else if (input.equals("FAIL")) {
			colour.shutdown();
			this.removeClientHandler(colour);
			sendGameFinished(winner.getName(), exitPlayer.getName() + " left the game");
			this.handleRequestRematch();
		}	
	}
	
	// -------------------- Handle rematch -------------------------
	
	public void handleRequestRematch() {
		this.sendRequestRematch();
		System.out.println("Request for rematch send");
		
		setRematch = 0;
		while (setRematch != this.cHandlers.size()) {	
			this.readSetRematch();	
			
		}
		
		if (rematchCount == this.cHandlers.size()) {
			this.resetGame();
		} else {
			for (ClientHandler player: cHandlers) {
				player.sendMessage("ACKNOWLEDGE_REMATCH+0");
				System.out.println("No rematch");
				System.out.println("Acknowledge rematch send to player");
				player.shutdown();
				this.removeClientHandler(player);
			}
			isFinished = true;
		}
	}
	
	public void sendRequestRematch() {
		for (ClientHandler player: cHandlers) {
			player.sendMessage("REQUEST_REMATCH");
		}
	}
	
	public void readSetRematch() {
		for (ClientHandler player: cHandlers) {
			String input = player.readQueue();
			if (!input.equals("EmptyQueue")) { 
				this.handleSetRematch(input);
				setRematch++;
				System.out.println("A set rematch received");
			}
		}
	}
	
	public void resetGame() {
		System.out.println("Game will now be reset");
		for (ClientHandler player: cHandlers) {
			player.sendMessage("ACKNOWLEDGE_REMATCH+1");
		}
		System.out.println("Acknowledge rematch sent to players");
		
		isFinished = false;
		isConfigured = false;
		handshakesCount = 0;
		rematchCount = 0;
		player1 = "";
		player2 = "";
		isWaiting = false;
	}
	
	public void handleSetRematch(String input) {
		String[] setRematchInput = input.split("\\+");
		if (setRematchInput.length == rematchL) {
			if (setRematchInput[1].equals("1")) {
				rematchCount++;
			}
		} else {
			System.out.println(unknown + "Set rematch command length is not 2");
		}
	}
	
	// -------------- Queries ---------------------------------------
	public List<ClientHandler> getClientHandlers() {
		return cHandlers;
	}
	
	public boolean full() {
		return cHandlers.size() == 2;
	}
	
	public boolean empty() {
		return cHandlers.size() == 0;
	}
	
	public void addClientHandler(ClientHandler player) {
		cHandlers.add(player);
	}
	
	public void removeClientHandler(ClientHandler player) {
		cHandlers.remove(player);
	}
}
