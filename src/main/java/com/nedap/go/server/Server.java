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
 * Server
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

	/**
	 * MAIN: Asks for a port number, starts a Server-application and returns the IP
	 * address and port number.
	 */
	public static void main(String[] args) {
		System.out.println("Enter port number: ");
		Scanner in = new Scanner(System.in);
		String sPort = in.hasNextLine() ? in.nextLine() : null;
		in.close();

		try {
			server = new Server(Integer.parseInt(sPort));
		} catch (NumberFormatException e) {
			System.out.println(USAGE);
			System.out.println("Error: " + sPort + " is not an integer");
			System.exit(0);
		}
		try {
			System.out.println("Server is created. IP address: " + InetAddress.getLocalHost().getHostAddress()
					+ ". Port number: " + sPort);
		} catch (UnknownHostException e) {
			System.out.println("ERROR: Localhost is unknown");
			e.printStackTrace();
		}
		server.run();
	}

	/** CONSTRUCTOR: Constructs a new Server object */
	public Server(int portArg) {
		this.port = portArg;
		this.threads = new ArrayList<ClientHandler>();
	}

	// --------------COMMANDS ---------------

	/**
	 * Listens to a port of this Server if there are any Clients that would like to
	 * connect. For every new socket connection a new ClientHandler thread is
	 * started that takes care of the further communication with the Client.
	 */
	public void run() {
		gameIDCount = 1;
		try {
			ServerSocket sSocket = new ServerSocket(this.port);
			while (true) {
				System.out.print("Listening to " + port + "\n");
				Socket localSocket = sSocket.accept();
				ClientHandler user = new ClientHandler(this, localSocket, 0);
				user.announce();
				user.start();
				addHandler(user);

				if (threads.size() % 2 == 0) {

					// Game game = new Game(players, boardsize, gameIDCount);
					gameIDCount++;
					// new game with game id (count, starts with one)
					// Add all clienthandler with game id == 0, to the game, automatic changes game
					// id player.
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printtoGame(String message, int gameid) {
		System.out.println(message);
		for (ClientHandler c : threads) {
			if (c.getGameID() == gameid) {
				c.sendMessage(message);
			}
		}
	}

	/**
	 * Add a ClientHandler to the collection of ClientHandlers.
	 * 
	 * @param handler ClientHandler that will be added
	 */
	public void addHandler(ClientHandler handler) {
		threads.add(handler);
	}

	/**
	 * Remove a ClientHandler from the collection of ClientHanlders.
	 * 
	 * @param handler ClientHandler that will be removed
	 */
	public void removeHandler(ClientHandler handler) {
		threads.remove(handler);
	}
}
