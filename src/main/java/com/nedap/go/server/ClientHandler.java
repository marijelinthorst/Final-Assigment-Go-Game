package com.nedap.go.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.io.IOException;

/**
 * ClientHandler.
 * 
 */

public class ClientHandler extends Thread {
	private Server server;
	private BufferedReader in;
	private BufferedWriter out;
	private BlockingQueue<String> queue;

	/**
	 * Constructs a ClientHandler object Initialises both Data streams.
	 */
	public ClientHandler(Server serverArg, Socket sockArg) throws IOException {
		this.server = serverArg;
		this.in = new BufferedReader(new InputStreamReader(sockArg.getInputStream()));
		this.out = new BufferedWriter(new OutputStreamWriter(sockArg.getOutputStream()));
		queue = new ArrayBlockingQueue<String>(5);
	}

// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	/**
	 * These methods takes care of reading messages from the client. The
	 * messages will be put in a queue, which the GameHandler can read. If an 
	 * IOException is thrown while reading the message, the method concludes 
	 * that the socket connection is broken and shutdown() will be called.
	 */
	public void run() {
		try {
			String input;
			while ((input = in.readLine()) != null) {
				queue.add(input);
			}
		} catch (IOException e) {
			queue.add("FAIL");
		}
	}
	
	public String readQueue() {
		try {
			if (!queue.isEmpty()) {
				return queue.take();
			} else {
				return "EmptyQueue";
			}
		} catch (InterruptedException e) {
			System.out.println("InterruptedException of queue");
			e.printStackTrace();
		}
		return "";
	}
	
	// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

	/**
	 * This method can be used to send a message over the socket connection to the
	 * Client. If the writing of a message fails, the method concludes that the
	 * socket connection has been lost and shutdown() is called.
	 */
	public void sendMessage(String msg) {
		try {
			this.out.write(msg);
			this.out.newLine();
			this.out.flush();
		} catch (IOException e) {
			queue.add("FAIL");
		}
	}

	/**
	 * This ClientHandler signs off from the Server and subsequently sends a last
	 * broadcast to the Server to inform that the Client is no longer participating
	 * in the chat.
	 */
	// TODO shutdown does not tell the server
	public void shutdown() {
		server.removeHandler(this);
		System.out.println("A Client(Handler) has disconnected");
	}
}
