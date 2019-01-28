package com.nedap.go.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.nedap.go.Game;
import com.nedap.go.Player;

/**
 * Server.
 * 
 * @author marije.linthorst
 *
 */
public class Server {
	private static final String USAGE = "Usage: " + Server.class.getName() + " <port>";
	private int port;
	private List<ClientHandler> threads;
	private static Server server;
	private int gameIDCount;
	private Player[] players;
	private List<GameHandler> gameHandlers;

	/**
	 * MAIN: Asks for a port number, starts a Server-application and returns the IP
	 * address and port number.
	 */
	public static void main(String[] args) {
		String sPort = null;
		if (args.length == 0) {
			System.out.println("Enter port number: ");
			Scanner in = new Scanner(System.in);
			sPort = in.hasNextLine() ? in.nextLine() : null;
		} else {
			sPort = args[0];
			
		}

		try {
			server = new Server(Integer.parseInt(sPort));
		} catch (NumberFormatException e) {
			System.out.println(USAGE);
			System.out.println("Error: " + sPort + " is not an integer");
			System.exit(0);
		}
		try {
			System.out.println("Server is created. IP address: " + 
					InetAddress.getLocalHost().getHostAddress()
					+ ". Port number: " + sPort);
		} catch (UnknownHostException e) {
			System.out.println("ERROR: Localhost is unknown");
			e.printStackTrace();
		}
		server.runServer();
	}

	/** 
	 *  CONSTRUCTOR: Constructs a new Server object.
	 */
	public Server(int portArg) {
		this.port = portArg;
		this.threads = new ArrayList<ClientHandler>();
		gameHandlers = new ArrayList<GameHandler>();
	}

	// --------------COMMANDS ---------------

	/**
	 * Listens to a port of this Server if there are any Clients that would like to
	 * connect. For every new socket connection a new ClientHandler thread is
	 * started that takes care of the further communication with the Client.
	 */
	public void runServer() {
		gameIDCount = 1;
		try {
			ServerSocket sSocket = new ServerSocket(this.port);
			while (true) {
				System.out.print("Listening to " + port + "\n");
				Socket localSocket = sSocket.accept();		
				System.out.println("client found");
				ClientHandler user = new ClientHandler(this, localSocket);
				user.start();
				
				// TODO: nodig?
				addHandler(user);
				
				boolean foundGame = false;
				if (gameHandlers.isEmpty()) {
					System.out.println("no games");
					GameHandler gameHandler = new GameHandler(gameHandlers.size() + 1);
					gameHandler.addClientHandler(user);
					gameHandler.start();
					gameHandlers.add(gameHandler);
				} else {
					for (int i = 0; i <= gameHandlers.size() && foundGame == false; i++) {
						if (!gameHandlers.get(i).full()) {
							gameHandlers.get(i).addClientHandler(user);
							System.out.println("tweede speler vind game");
							foundGame = true;
						} 	
					}
					if (foundGame == false) {
						System.out.println("tweede speler start nieuwe game");
						GameHandler gameHandler = new GameHandler(gameHandlers.size() + 1);
						gameHandler.addClientHandler(user);
						gameHandler.start();
						gameHandlers.add(gameHandler);
					}
				} 

					// Game game = new Game(players, boardsize, gameIDCount)
					// new game with game id (count, starts with one)
					// Add all clienthandler with game id == 0, to the game, automatic changes game
					// id player.
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// TODO nodig?
	public void printtoAllGames(String message, int gameid) {
		System.out.println(message);
		for (ClientHandler c : threads) {
			c.sendMessage(message);
		}
	}

	/**
	 * Add a ClientHandler to the collection of ClientHandlers.
	 * @param handler ClientHandler that will be added
	 */
	public void addHandler(ClientHandler handler) {
		threads.add(handler);
	}

	/**
	 * Remove a ClientHandler from the collection of ClientHanlders.
	 * @param handler ClientHandler that will be removed
	 */
	public void removeHandler(ClientHandler handler) {
		threads.remove(handler);
	}
}
