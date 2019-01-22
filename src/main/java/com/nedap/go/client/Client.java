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
 * 
 * @author marije.linthorst
 *
 */
public class Client extends Thread {
	private static final String USAGE = "Usage: " + Client.class.getName() 
			+ "<name> <address> <port>";
	private static InetAddress host;
	private static int port;

	private static Player player;
	private int colour;
	private int gameID;
	private int leader; // 0 = false, 1 = true
	private String playerName;
	private int size;
	private String gameState;
	private String opponant;
	private Board board;

	private String clientName;
	private Socket sock;
	private Scanner in;
	private BufferedWriter out;
	
	// booleans which determine status of client
	private boolean isFinished = false;
	private boolean isHandshakeSent = false;
	private boolean isGameConfigured = false;
	private boolean isGameConfigRequested = false;
	private boolean isCurrentPlayer = false;

	/** MAIN: Starts a Client-application. */
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
			print("ERROR: no valid hostname!");
			System.exit(0);
		}

		try {
			port = Integer.parseInt(sPort);
		} catch (NumberFormatException e) {
			print("ERROR: no valid portnummer!");
			System.exit(0);
		}

		try {
			Client client = new Client(clientName, host, port);
			client.sendMessage(clientName);
			client.startUserInput();
			client.startServerInput();

		} catch (IOException e) {
			print("ERROR: couldn't construct a client object!");
			System.exit(0);
		}
	}

	/**
	 * Constructs a Client-object and tries to make a socket connection.
	 */
	public Client(String name, InetAddress host, int port) throws IOException {
		this.clientName = name;
		player = null;
		this.sock = new Socket(host, port);
		System.out.println("Socket created");
		this.in = new Scanner(new BufferedReader(new InputStreamReader(sock.getInputStream())));
		this.out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
	}

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
			if (inputLine!="") {
				handleUILine(inputLine);
			}
		}
	}
// needs some other prompts for unknown command, invalid move, update status	
	public void showPrompt() {
		if (!isHandshakeSent) {
            System.out.println("Please enter player name");
		} else if (!isGameConfigured) {
			if (leader == 1 && isGameConfigRequested) {
				System.out.println("Please enter the preferred colour and "
						+ "desired width of the game board");
			} else {
				System.out.println("Waiting on other player to configurate the game");
			}
            
        } else if (isCurrentPlayer) {
            System.out.println("Please enter your next move");
        }
	}
	
	public void handleUILine(String inputLine) {
		if (!isHandshakeSent) {
            dispatchHandshakeLine(inputLine);
        } else if (!isGameConfigured && isGameConfigRequested) {
            dispatchGameConfigurationLine(inputLine);
        } else {
            dispatchGamePlayLine(inputLine);
        }
	}
	
	public void dispatchHandshakeLine(String line) {
       //  something
    }
	
	public void dispatchGameConfigurationLine(String line) {
        // something
    }
	
	public void dispatchGamePlayLine(String line) {
        // something
		if (!line.startsWith("exit") && !line.startsWith("quit")) {
            inputMoveQueue.put(Move.parseMove(line));
        }
    }
	
	
	
	public void startServerInput() {
		Thread serverInTread = new Thread() {
			public void run() {
				
			}
		};
		serverInTread.start();
	}
	
	
	
	
	
	
	
	/**
	 * Reads the messages in the socket connection. Each message will command will
	 * be dealt with in the next section
	 */
	public void run() {
		String line;
		Scanner clientIn = new Scanner(System.in);
		this.handshake();

		while (in.hasNextLine()) {
			line = in.nextLine();
			
			if (line.startsWith("ACKNOWLEDGE_HANDSHAKE")) {
				this.ackHandshake(line);
			} else if (line.startsWith("REQUEST_CONFIG")) {
				this.config(clientIn);
			} else if (line.startsWith("ACKNOWLEDGE_CONFIG")) {
				//Object configuration = parseConfiguration(in);
				//this.ackConfig(configuration);
			} else if (line.startsWith("ACKNOWLEDGE_MOVE")) {
				this.ackMove();
			} else if (line.startsWith("INVALID_MOVE")) {
				this.invalidMove();
			} else if (line.startsWith("UPDATE_STATUS")) {
				this.updateStatus();
			} else {
				System.out.println("Something is wrong with server");
				System.out.println("Command is unknown (" + line + ")");
			}
		}
		in.close();
		clientIn.close();
	}

	// --------- COMMANDS ----------------------------------------------------
	public void handshake() {
		this.sendMessage("HANDSHAKE+" + clientName);
	}

	public void ackHandshake(String line) {
		Scanner serverIn = new Scanner(line);
		serverIn.useDelimiter("\\+");
		
		if (serverIn.hasNextInt()) {
			gameID = serverIn.nextInt();
		}
		if (serverIn.hasNextInt()) {
			leader = serverIn.nextInt();
		}

		print("Server acknowledged handshake. GameID = " + gameID);
		if (leader == colour) {
			print("You are the leader of this game");
		} else {
			print("Opponant (" + opponant + ") is leader of this game");
		}
		serverIn.close();
	}

	public void config(Scanner in) {
		System.out.println(this.in.next());
		System.out.println("Please enter preferred colour and boardsize");
		int prefColour = in.nextInt();
		int prefBoardSize = in.nextInt();
		this.sendMessage("SET_CONFIG+" + gameID + "+" + prefColour + "+" + prefBoardSize);
	}

	public void ackConfig(Scanner in) {
		playerName = this.in.next();
		colour = this.in.nextInt();
		size = this.in.nextInt();
		gameState = this.in.next();
		opponant = this.in.next();

		do {
			System.out.println("Make human player or computer player? " + 
					"Type \"human\" or \"comp\".");
			String suggestType = in.next();

			if (suggestType.equals("human")) {
				player = new HumanPlayer(playerName, colour, gameID);
			} else if (suggestType.equals("comp")) {
				player = new NaiveCompPlayer(playerName, colour, gameID);
			}
		} while (player == null);

		this.doMove(gameState);

		System.out.println("Configuration succesful.");
		System.out.println("Playername = " + playerName);
		System.out.println("Colour = " + colour);
		System.out.println("Board size = " + size);
		System.out.println("Game state  = " + gameState);
		System.out.println("Opponant's name = " + opponant);
	}

	public void doMove(String gameState) {
		String[] state = gameState.split(";");
		String move = "";
		if (state.length == 3) {
			this.board = new Board(state[2]);
			if (player instanceof NaiveCompPlayer) {
				((NaiveCompPlayer) player).addToHistory(state[2]);
			}

			if (state[1].equals(Integer.toString(colour))) {
				if (player instanceof HumanPlayer) {
					move = ((HumanPlayer) player).determineMove(board);
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

	public void ackMove() {
		if (this.in.nextInt() == gameID) {
			this.in.next(); // skip move
			String gameState = this.in.next();
			this.doMove(gameState);
		} else {
			System.out.println("Something is wrong with server");
			System.out.println("GameID is incorrect");
		}
	}

	public void invalidMove() {
		System.out.println("INVALID MOVE");
	}

	public void updateStatus() {
		String state = this.in.next();
		this.gameState = state;
		String[] status = gameState.split(";");
		if (status.length == 3) {
			this.board = new Board(status[2]);

			if (player instanceof NaiveCompPlayer) {
				((NaiveCompPlayer) player).addToHistory(status[2]);
			}
			System.out.println("New game state  = " + this.gameState);
		} else {
			System.out.println("Something is wrong with server");
			System.out.println("Gamestate is incorrect");
		}
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

	/** close the socket connection. */
	public void shutdown() {
		print("Closing socket connection...");
		try {
			sock.close();
		} catch (IOException e) {
			System.out.println("Error during closing of socket");
			e.printStackTrace();
		}
	}

	/** returns the client name. */
	public String getClientName() {
		return clientName;
	}

	/** prints message to console of this client. */
	private static void print(String message) {
		System.out.println(message);
	}

	/** reads from the system input from this client. */
	public static String readString() {
		String antw = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			antw = in.readLine();
		} catch (IOException e) {
		}
		return (antw == null) ? "" : antw;
	}
}
