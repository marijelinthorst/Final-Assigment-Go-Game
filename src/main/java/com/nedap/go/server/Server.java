package com.nedap.go.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
  private static final String USAGE
    = "usage: " + Server.class.getName() + " <port>";
  private int port;
  private List<ClientHandler> threads;
  private static Server server;

  
  /** MAIN: Asks for a port number and starts a Server-application. */
  public static void main(String[] args) {
    System.out.println("Enter port number: ");
    Scanner in = new Scanner(System.in);
    String sPort = in.hasNextLine() ? in.nextLine() : null;
    in.close();
    
    try {
      server = new Server(Integer.parseInt(sPort));
    } catch(NumberFormatException e) {
      System.out.println(USAGE);
      System.out.println("Error: " + sPort + " is not an integer");
      System.exit(0);
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
   * Listens to a port of this Server if there are any Clients that 
   * would like to connect. For every new socket connection a new
   * ClientHandler thread is started that takes care of the further
   * communication with the Client.
   */
  public void run() {
    try {
      ServerSocket sSocket = new ServerSocket(this.port);
      while (true) {
        System.out.print("Listening to " + port + "\n");
        Socket localSocket = sSocket.accept();
        ClientHandler user = new ClientHandler(this, localSocket);
        user.announce();
        user.start();
        addHandler(user);
      }                  
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void print(String message){
    System.out.println(message);
  }

  /**
   * Sends a message using the collection of connected ClientHandlers
   * to all connected Clients.
   * @param msg message that is send
   */
  public void broadcast(String msg) {
    print(msg);
    for (ClientHandler c:threads) {
      c.sendMessage(msg);
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
