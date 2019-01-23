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

import com.nedap.go.Board;
import com.nedap.go.HumanPlayer;
import com.nedap.go.NaiveCompPlayer;
import com.nedap.go.Player;

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
	private String clientName;
	private Socket sock;
	private Scanner in;
	private BufferedWriter out;
	
	// variables for playing the game
	private String playerType;
	private static Player player = null;
	private int colour;
	private int gameID;
	private String playerName;
	private int size;
	private String gameState;
	private String opponant;
	private Board board;
	
	// booleans which determine status of client
	private boolean isFinished = false;
	private boolean isHandshakeSent = false;
	private boolean isGameConfigured = false;
	private boolean isGameConfigRequested = false;
	private boolean isCurrentPlayer = false;
	private boolean isLeader = false;

	/** MAIN: Starts a Client-application that can connect to a specific server.
	 * Starts the user and server input.
	 */
	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);
		System.out.println("Enter name, IP address and port number");
		String clientName = in.hasNext() ? in.next() : null;
		String sIP = in.hasNext() ? in.next() : null;
		String sPort = in.hasNext() ? in.next() : null;
		in.close();

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
			client.sendMessage(clientName);
			client.startUserInput();
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
		this.clientName = name;
		this.sock = new Socket(host, port);
		System.out.println("Socket created");
		this.in = new Scanner(new BufferedReader(new InputStreamReader(sock.getInputStream())));
		this.out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
	}
	
	//---------------- user input ---------------------------------
	public void startUserInput() {
		Thread userInTread = new Thread() {
			public void run() {
				userEventLoop();
			}
		};
		userInTread.start();
	}
	
	public void userEventLoop() {
		Scanner userIn = new Scanner(System.in);
		while (!isFinished && userIn.hasNext()) {
			showPrompt();
			String inputLine = userIn.nextLine();
			if (inputLine != "") {
				dispatchUILine(inputLine);
			}
		}
		userIn.close();
	}
	
// needs some other prompts for unknown command, invalid move, update status	
	public void showPrompt() {
		if (!isHandshakeSent) {
            System.out.println("Please enter player name and \"Human\" or \"Comp\""
            		+ ", seperated by a \",\"");
		} else if (!isGameConfigured) {
			if (isLeader && isGameConfigRequested) {
				System.out.println("Please enter the preferred colour and "
						+ "desired width of the game board, seperated by a \",\"");
			} else {
				System.out.println("Waiting on other player to configurate the game");
			}
            
        } else if (isCurrentPlayer && playerType.equals("human")) {
        	System.out.println(board.toTUIString());
            System.out.println("Please enter your next move. Type \"MOVE\" followed by an index,"
            		+ "\"PASS\" or \"EXIT\"");
        }
	}
	
	public void dispatchUILine(String inputLine) {
		if (!isHandshakeSent) {
            dispatchHandshakeLine(inputLine);
        } else if (!isGameConfigured && isGameConfigRequested) {
            dispatchGameConfigurationLine(inputLine);
        } else if (isCurrentPlayer) {
            dispatchGamePlayLine(inputLine);
        }
	}
	
	public void dispatchHandshakeLine(String line) {
		String[] input = line.split(",");
		if (input.length == 2 && input[1].equals("comp") || input[1].equals("human")) {
			playerName = input[0];
			playerType = input[1];
			this.sendMessage("HANDSHAKE+" + playerName);
		} else {
			System.out.println("Handshake requires 2 arguments, "
					+ "second argument needs to be human or comp)");
		}
    }
	
	public void dispatchGameConfigurationLine(String line) {
		String[] input = line.split(",");
		if (input.length == 2) {
			try {
				int prefColour = Integer.parseInt(input[0]);
				int prefBoardSize = Integer.parseInt(input[1]);
				this.sendMessage("SET_CONFIG+" + gameID + "+" + prefColour + "+" + prefBoardSize);
			} catch (NumberFormatException e) {
// log something
				System.out.println("Preferred colour and board size need to be integers");
			}
		} else {
			System.out.println("Game configuration requires 2 arguments");
		}
	}
    
	
	public void dispatchGamePlayLine(String line) {
		if (player instanceof HumanPlayer) {
			String move = ((HumanPlayer) player).determineMove(line);
			if (!move.equals("")) {
// send to server or queue
				// inputMoveQueue.put(Move.parseMove(line));
			} else {
				System.out.println("Invalid move");
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
	public void dispatchServerLine(String inputLine) {
		if (inputLine.startsWith("ACKNOWLEDGE_HANDSHAKE")) {
			System.out.println("Handshake acknowledged");
			this.ackHandshake(inputLine);
		} else if (inputLine.startsWith("REQUEST_CONFIG")) {
			this.isGameConfigRequested = true;
		} else if (inputLine.startsWith("ACKNOWLEDGE_CONFIG")) {
			this.ackConfig(inputLine);
			isGameConfigured = true;
		} else if (inputLine.startsWith("ACKNOWLEDGE_MOVE")) {
			this.ackMove(inputLine);
		} else if (inputLine.startsWith("INVALID_MOVE")) {
			this.invalidMove(inputLine);
		} else if (inputLine.startsWith("UPDATE_STATUS")) {
			this.updateStatus(inputLine);
		} else if (inputLine.startsWith("GAME_FINISHED")) {
			this.gameFinished(inputLine);
			isFinished = true;
		} else {
			System.out.println("Something is wrong with server");
			System.out.println("Command is unknown (" + inputLine + ")");
		}
	}
	
	// ----------- dealing with server commands --------------------------
	public void ackHandshake(String line) {
		String[] input = line.split("+");
		if (input.length == 3) {
			try {
				gameID = Integer.parseInt(input[1]);
				isLeader = Integer.parseInt(input[2]) == 1;
			} catch (NumberFormatException e) {
// log something
				System.out.println("GameID and/or leader is/are no integer(s)."
						+ " Something is wrong with server");
			}
		} else {
			System.out.println("Command is unknown (" + line + ")");
		}
	}

	public void ackConfig(String line) {
		String[] input = line.split("+");
		if (input.length == 6) {
			playerName = input[1];
			gameState = input[4];
			opponant = input[5];
			try {
				colour = Integer.parseInt(input[2]);
				size  = Integer.parseInt(input[3]);
			} catch (NumberFormatException e) {
// log something
				System.out.println("Colour and/or size is/are no integer(s)." + 
						"Something is wrong with server");
			}
			this.makePlayer();
			this.doMove(gameState);

			System.out.println("Configuration succesful.");
			System.out.println("Playername = " + playerName);
			System.out.println("Colour = " + colour);
			System.out.println("Board size = " + size);
			System.out.println("Game state  = " + gameState);
			System.out.println("Opponant's name = " + opponant);	
		} else {
			System.out.println("Command is unknown (" + line + ")");
		}
	}
	
	public void makePlayer() {
		if (playerType.equals("human")) {
			player = new HumanPlayer(playerName, colour, gameID);
		} else if (playerType.equals("comp")) {
			player = new NaiveCompPlayer(playerName, colour, gameID);
		}
	}
	
// doMove need adjustment
	public void doMove(String gameStatus) {
		// check gameID
		String[] status = gameStatus.split(";");
		String move = "";
		if (status.length == 3) {
			this.board = new Board(status[2]);
			if (player instanceof NaiveCompPlayer) {
				((NaiveCompPlayer) player).addToHistory(status[2]);
			}

			if (status[1].equals(Integer.toString(colour))) {
				this.isCurrentPlayer = true;
// human player is already handled by changing status of client. 
// Sends move to queue, not implemented yet 
				if (player instanceof HumanPlayer) {
					//move = ((HumanPlayer) player).determineMove(board);
				} else if (player instanceof NaiveCompPlayer) {
					move = ((NaiveCompPlayer) player).determineMove(board);
				}
				this.sendMessage(move);
			}
		} else {
			System.out.println("Something is wrong with server");
			System.out.println("Gamestate is incorrect");
		}
	}

	public void ackMove(String line) {
		String[] input = line.split("+");
		if (input.length == 4) {
			try {
				if (Integer.parseInt(input[1]) == gameID) {
					gameState = input[3];
					this.doMove(gameState);
				} else {
					System.out.println("Something is wrong with server");
					System.out.println("GameID is incorrect");
				}
			} catch (NumberFormatException e) {
// log something
				System.out.println("Something is wrong with server");
				System.out.println("GameID is no integer");
			}
		} else {
			System.out.println("Something is wrong with server");
			System.out.println("Command is unknown (" + line + ")");
		}
	}	

	public void invalidMove(String line) {
		String[] input = line.split("+");
		if (input.length == 2) {
			System.out.println(input[1]);
		} else {
			System.out.println(line);
		}
	}

// HIER
	public void updateStatus(String line) {
		Scanner serverIn = new Scanner(line);
		serverIn.useDelimiter("\\+");
		
		String state = serverIn.next();
		this.gameState = state;
		String[] status = gameState.split(";");
		
		if (status.length == 3) {
			this.isCurrentPlayer = status[1].equals(Integer.toString(colour));
			this.board = new Board(status[2]);

			if (player instanceof NaiveCompPlayer) {
				((NaiveCompPlayer) player).addToHistory(status[2]);
			}
			System.out.println("New game state  = " + this.gameState);
		} else {
			System.out.println("Something is wrong with server");
			System.out.println("Gamestate is incorrect");
		}
		serverIn.close();
	}

//not yet implemented
	public void gameFinished(String line) {
		Scanner serverIn = new Scanner(line);
		serverIn.useDelimiter("\\+");
		
		serverIn.close();
	}
	
	// ----------------------------------------------------
	/** send a message to a ClientHandler. */
	public void sendMessage(String msg) {
		try {
			this.out.write(msg);
			this.out.newLine();
			this.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			shutdown();
		}
	}
	
// should be used if "quit"
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

	/** returns the client name. 
	public String getClientName() {
		return clientName;
	}
	*/

	/** prints message to console of this client. 
	private static void print(String message) {
		System.out.println(message);
	}
	*/

}
