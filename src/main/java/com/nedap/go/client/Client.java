package com.nedap.go.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.nedap.go.Board;
import com.nedap.go.HumanPlayer;
import com.nedap.go.NaiveCompPlayer;
import com.nedap.go.PlayingPlayer;
import com.nedap.go.gui.GoGuiIntegrator;

/**
 * Client.
 * @author marije.linthorst
 *
 */
public class Client extends Thread {
	// variables to connect to server
	private static final String USAGE = "Usage: " + Client.class.getName() 
			+ "<name> <address> <port>";
	private static InetAddress host;
	private static int port;
	private static String clientName;
	private Socket sock;
	private Scanner in;
	private BufferedWriter out;
	
	// variables for playing the game
	private String playerType;
	private static PlayingPlayer player = null;
	private int gameID;
	private int size;
	private String opponant;
	private Board board;
	private BlockingQueue<String> queue;
	private GoGuiIntegrator gui;
	
	// booleans which determine status of client
	private boolean isFinished = false;
	private boolean isHandshakeSent = false;
	private boolean isGameConfigured = false;
	private boolean isGameConfigRequested = false;
	private boolean isCurrentPlayer = false;
	private boolean isLeader = false;
	private boolean serverReady = true;
	private boolean hasReadConfiguration = false;
	private boolean rematchRequested = false;
	
	// variables for command length
	private final int ackHand = 3;
	private final int reqConfig = 2;
	private final int ackConfig = 6;
	private final int ackMove = 4;
	private final int invMove = 2;
	private final int unCom = 2;
	private final int gameFin = 5;	
	private final int gameSt = 3;
	private final int moveL = 2;
	private final int ackRematch = 2;
	
	// common error messages
	private String serverError = "Something is wrong with the server";
	private String gameIDError = "GameID is incorrect";
	private String commandError = "Command is unknown: ";

	/** MAIN: Starts a Client-application that can connect to a specific server.
	 * Starts the user and server input.
	 */
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		String sIP = null;
		String sPort = null;
		
		if (args.length == 0) {
			System.out.println("Enter name, IP address and port number");
			clientName = in.hasNext() ? in.next() : null;
			sIP = in.hasNext() ? in.next() : null;
			sPort = in.hasNext() ? in.next() : null;
		} else {
			clientName = args[0];
			sIP = args[1];
			sPort = args[2];
		}

		if (clientName == null || sIP == null || sPort == null) {
			System.out.println(USAGE);
			System.exit(0);
		}

		try {
			host = InetAddress.getByName(sIP);
		} catch (UnknownHostException e) {
			System.out.println("ERROR: no valid hostname!");
			System.exit(0);
		}

		try {
			port = Integer.parseInt(sPort);
		} catch (NumberFormatException e) {
			System.out.println("ERROR: no valid portnummer!");
			System.exit(0);
		}

		try {
			Client client = new Client(clientName, host, port);
			client.startUserInput(in);
			client.startServerInput();

		} catch (IOException e) {
			System.out.println("ERROR: couldn't construct a client object!");
			System.exit(0);
		}
	}

	/**
	 * Constructs a Client-object and tries to make a socket connection.
	 */
	public Client(String name, InetAddress host, int port) throws IOException {
		clientName = name;
		this.sock = new Socket(host, port);
		System.out.println("Socket created");
		this.in = new Scanner(new BufferedReader(new InputStreamReader(sock.getInputStream())));
		this.out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		queue = new ArrayBlockingQueue<String>(5);
	}
	
	//---------------- user input ---------------------------------
	public void startUserInput(Scanner userIn) {
		Thread userInTread = new Thread() {
			public void run() {
				userEventLoop(userIn);
			}
		};
		userInTread.start();
	}
	
	public void userEventLoop(Scanner userIn) {
		while (!isFinished) {
			//System.out.println("Server is " + (serverReady ? "": "not") + " ready");
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) { // has slept a bit
			}
			
			boolean hasInput = false;
			try {
				hasInput = System.in.available() > 0;
			} catch (IOException e) { // has no input :) 
			}
			
			if (shouldAskInput() || hasInput) {			
				showPrompt();
				if (userIn.hasNext()) {
					String inputLine = userIn.nextLine();
					dispatchUILine(inputLine);
				}	
			}
		}
		userIn.close();
	}

	private boolean shouldAskInput() {
		return serverReady && (isCurrentPlayer || stillNeedConfiguration() || !isHandshakeSent 
				|| rematchRequested);
	}
	
	public void showPrompt() {
		System.out.println("Going to show prompt");
		if (!isHandshakeSent) {
            System.out.println("Please enter \"human\" or \"comp\"");
		} else if (!isGameConfigured) {
			if (stillNeedConfiguration()) {
				System.out.println("Please enter the preferred colour and "
						+ "desired width of the game board, seperated by a \",\"");
			} 
        } else if (rematchRequested) {
        	System.out.println("Would you like a rematch? Answer \"yes\" or \"no.\"");
        } else if (isCurrentPlayer && playerType.equals("human")) {
            System.out.println("Please enter your next move. Type \"MOVE\" followed "
            		+ "by an index and separated by a \",\", \"PASS\" or \"EXIT\"");
            System.out.println("Current board:");
            System.out.println(board.toTUIString());
            this.updateGUI();
        }
	}

	private boolean stillNeedConfiguration() {
		return (isLeader || isGameConfigRequested) && !hasReadConfiguration;
	}
	
	public void dispatchUILine(String inputLine) {
		if (!isHandshakeSent) {
            dispatchHandshakeLine(inputLine);
        } else if (stillNeedConfiguration()) {
            dispatchGameConfigurationLine(inputLine);
        } else if (rematchRequested) {
        	dispatchSetRematchLine(inputLine);
        } else if (isCurrentPlayer) {
            dispatchGamePlayLine(inputLine);
        }
	}
	
	public void dispatchHandshakeLine(String line) {
		if (line.equals("comp") || line.equals("human")) {
			playerType = line;
			this.sendMessage("HANDSHAKE+" + clientName);
			isHandshakeSent = true;
		} else {
			System.out.println("Handshake requires the argument to be human or comp");
		}
    }
	
	public void dispatchGameConfigurationLine(String line) {
		String[] input = line.split(",");
		if (input.length == reqConfig) {
			try {
				int prefColour = Integer.parseInt(input[0]);
				int prefBoardSize = Integer.parseInt(input[1]);
				this.sendMessage("SET_CONFIG+" + gameID + "+" + prefColour + "+" + prefBoardSize);
				hasReadConfiguration = true;
			} catch (NumberFormatException e) {
				System.out.println("Preferred colour and board size need to be integers");
			}
		} else {
			System.out.println("Game configuration requires 2 arguments");
		}
	}
    
	public void dispatchSetRematchLine(String line) {
		if (line.equals("yes")) {
			this.sendMessage("SET_REMATCH+1");
			isCurrentPlayer = false;
			isLeader = false;
			rematchRequested = false;
		} else if (line.equals("no")) {
			this.sendMessage("SET_REMATCH+2");
			System.out.println("Thank you for playing! Bye.");
			isFinished = true;
		} else {
			System.out.println("Please answer \"yes\" or \"no.\"");
		}
	}
	
	public void dispatchGamePlayLine(String line) {
		// EXIT+$GAME_ID+$PLAYER_NAME
		// MOVE+$GAME_ID+$PLAYER_NAME+$TILE_INDEX
		if (playerType.equals("human")) {
			if (line.equals("EXIT")) {
				queue.add(line);
				// TODO: implement quit
			} else if (line.equals("PASS")) {
				queue.add(line);
			} else if (line.startsWith("MOVE")) {
				String[] input = line.split("\\,");
				if (input.length == 2) {
					queue.add(line);
					System.out.println("Move is added to queue");
				} else {
					System.out.println("Move requires 2 arguments");
				}
			} else {
				System.out.println("Command unknown: choose move, pass or exit");
			}
		}
    }
	
	// ---------------- server input ---------------------
	public void startServerInput() {
		Thread serverInTread = new Thread() {
			public void run() {
				serverEventLoop();
			}
		};
		serverInTread.start();
	}
	
	
	public void serverEventLoop() {
		while (!isFinished && in.hasNextLine()) {
			String inputLine = in.nextLine();
			dispatchServerLine(inputLine);
		}
	}
	
	/**
	 * Reads the messages in the socket connection. Each message will command will
	 * be dealt with in the next section
	 */
	// TODO: implement set_rematch
	public void dispatchServerLine(String inputLine) {
		if (inputLine.startsWith("ACKNOWLEDGE_HANDSHAKE")) {
			System.out.println("Handshake acknowledged");
			this.ackHandshake(inputLine);
		} else if (inputLine.startsWith("REQUEST_CONFIG")) {
			System.out.println("Config requested");
			this.isGameConfigRequested = true;
		} else if (inputLine.startsWith("ACKNOWLEDGE_CONFIG")) {
			this.ackConfig(inputLine);
			isGameConfigured = true;
		} else if (inputLine.startsWith("ACKNOWLEDGE_MOVE")) {
			this.ackMove(inputLine);
		} else if (inputLine.startsWith("INVALID_MOVE")) {
			this.invalidMove(inputLine);
		} else if (inputLine.startsWith("GAME_FINISHED")) {
			this.gameFinished(inputLine);
		} else if (inputLine.equals("REQUEST_REMATCH")) {
			rematchRequested = true;
		} else if (inputLine.startsWith("ACKNOWLEDGE_REMATCH")) {
			this.ackRematch(inputLine);
		} else if (inputLine.startsWith("UNKNOWN_COMMAND")) {
			this.unknownCommand(inputLine);
		} else {
			System.out.println(serverError);
			System.out.println("Command is unknown (" + inputLine + ")");
		}
		serverReady = true;
	}
	
	// ----------- dealing with server commands --------------------------
	public void ackHandshake(String line) {
		String[] input = line.split("\\+");
		if (input.length == ackHand) {
			try {
				gameID = Integer.parseInt(input[1]);
				isLeader = Integer.parseInt(input[2]) == 1;
				System.out.println("isLeader=" + isLeader);
			} catch (NumberFormatException e) {
				//TODO: log something
				System.out.println("GameID and/or leader is/are no integer(s).");
				System.out.println(serverError);
			}
		} else {
			System.out.println(commandError + line);
			System.out.println(serverError);
		}
	}

	public void ackConfig(String line) {
		String[] input = line.split("\\+");
		if (input.length == ackConfig) {
			clientName = input[1];
			String gameState = input[4];
			opponant = input[5];
			try {
				int colour = Integer.parseInt(input[2]);
				size  = Integer.parseInt(input[3]);
				
				this.makePlayer(colour);
				this.startQueue();
				gui = new GoGuiIntegrator(true, true, size);
				gui.startGUI();
				gui.clearBoard();
				this.doMove(gameState);

				System.out.println("Configuration succesful.");
				System.out.println("Playername = " + clientName);
				System.out.println("Colour = " + colour);
				System.out.println("Board size = " + size);
				System.out.println("Game state  = " + gameState);
				System.out.println("Opponant's name = " + opponant);
				
				this.isGameConfigured = true;	
			} catch (NumberFormatException e) {
				// TODO: log something
				System.out.println("Colour and/or size is/are no integer(s).");
				System.out.println(serverError);
			}	
		} else {
			System.out.println(commandError + line);
			System.out.println(serverError);
		}
	}
	
	public void makePlayer(int colour) {
		if (playerType.equals("human")) {
			player = new HumanPlayer(clientName, colour, gameID);
		} else if (playerType.equals("comp")) {
			player = new NaiveCompPlayer(clientName, colour, gameID);
		}
	}
	
	public void doMove(String gameStatus) {
		String[] status = gameStatus.split("\\;");
		if (status.length == gameSt) {
			this.board = new Board(status[2]);
			if (status[1].equals(Integer.toString(player.getColour()))) {
				this.isCurrentPlayer = true;
				queue.add("DOMOVE");				
			} else {
				System.out.println("Waiting on other player. Current board:");
				System.out.println(board.toTUIString());
				this.updateGUI();
				this.isCurrentPlayer = false;
			}
		} else {
			System.out.println("Gamestate is incorrect");
			System.out.println(serverError);
		}
	}

	public void ackMove(String line) {
		// TODO: status 2 = move/pass
		String[] input = line.split("\\+");
		if (input.length == ackMove) {
			try {
				if (Integer.parseInt(input[1]) == gameID) {
					String gameState = input[3];
					this.doMove(gameState);
				} else {
					System.out.println(gameIDError);
					System.out.println(serverError);
				}
			} catch (NumberFormatException e) {
				// TODO: log something
				System.out.println(gameIDError);
				System.out.println(serverError);
			}
		} else {
			System.out.println(commandError + line);
			System.out.println(serverError);
		}
	}	

	public void invalidMove(String line) {
		String[] input = line.split("\\+");
		if (input.length == invMove) {
			System.out.println(input[1]);
			queue.add("DOMOVE");
		} else {
			System.out.println(line);
		}
	}

	public void gameFinished(String line) {
		String[] input = line.split("\\+");
		if (input.length == gameFin) {
			if (input[1].equals(Integer.toString(gameID))) {
				String[] scores = input[3].split("\\;");
				System.out.println("The winner is " + input[2]);
				System.out.println("Black scored " + scores[0] + " points");
				System.out.println("White scored " + scores[1] + " points");
				System.out.println(input[4]);
			} else {
				System.out.println(gameIDError);
				System.out.println(serverError);
			}
		} else {
			System.out.println(commandError + line);
			System.out.println(serverError);
		}
	}
	
	public void ackRematch(String line) {
		String[] input = line.split("\\+");
		if (input.length == ackRematch) {
			if (input[1].equals("1")) {
				isFinished = false;
				isHandshakeSent = false;
				isGameConfigured = false;
				isGameConfigRequested = false;
				serverReady = true;
				hasReadConfiguration = false;
			} else if (input[1].equals("0")) {
				System.out.println("There will be no rematch. Thank you for playing! Bye.");
				isFinished = true;
			} else {
				System.out.println(commandError + line);
				System.out.println(serverError);
			}
		} else {
			System.out.println(commandError + line);
			System.out.println(serverError);
		}
	}
	
	public void unknownCommand(String line) {
		String[] input = line.split("\\+");
		if (input.length == unCom) {
			System.out.println(input[1]);
		} else {
			System.out.println(line);
		}
	}
	//------------------ queue ----------------------------
	public void startQueue() {
		Thread startQueue = new Thread() {
			public void run() {
				queueEventLoop();
			}
		};
		startQueue.start();
	}
	
	// TODO: should have minimum of 1 second before sending to server
	public void queueEventLoop() {
		while (!isFinished) {
			String nextMove = "";
			try {
				nextMove = queue.take();
				if (nextMove.equals("DOMOVE")) {
					if (playerType.equals("human")) {
						nextMove = queue.take();
						String humanMove = player.determineMove(nextMove);
						if (humanMove != "INVALID") {
							this.sendMessage(humanMove);
						} else {
							System.out.println("Invalid move, try again");
							System.out.println(nextMove);
							System.out.println(humanMove);
						}	
					} else {
						String compMove = player.determineMove(board.getCurrentStringBoard());
						this.sendMessage(compMove);
					}
				}  
				
			} catch (InterruptedException e) {
				System.out.println("Nothing in queue, interrupted");
				// TODO: log something
			}	
		}
	}
	
	// ----------------------------------------------------
	/** send a message to a ClientHandler. */
	public void sendMessage(String msg) {
		try {
			serverReady = false;
			this.out.write(msg);
			this.out.newLine();
			this.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			shutdown();
		}
	}
	
	// TODO: should be used if "quit"
	/** close the socket connection. */
	public void shutdown() {
		System.out.println("Closing socket connection...");
		try {
			sock.close();
		} catch (IOException e) {
			System.out.println("Error during closing of socket");
			e.printStackTrace();
		}
	}
	
	// ----------------- GUI ---------------------
	
	public void updateGUI() {
		gui.clearBoard();
		for (int i = 0; i < board.getBoardSizeN() * board.getBoardSizeN(); i++) {
			if (board.getPoint(i) != 0) {
				boolean white = board.getPoint(i) == 2;
				int x = i % board.getBoardSizeN();
				int y = i / board.getBoardSizeN();
				gui.addStone(x, y, white);
			}
		}
	}
}
