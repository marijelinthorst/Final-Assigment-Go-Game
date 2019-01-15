package com.nedap.go.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.io.IOException;

/**
 * ClientHandler.
 * 
 */

public class ClientHandler extends Thread{
  private Server server;
  private BufferedReader in;
  private BufferedWriter out;
  private String clientName;

  /**
   * Constructs a ClientHandler object
   * Initialises both Data streams.
   */
  public ClientHandler(Server serverArg, Socket sockArg) throws IOException{
    this.server = serverArg;
    this.in = new BufferedReader(new InputStreamReader(sockArg.getInputStream()));
    this.out = new BufferedWriter(new OutputStreamWriter(sockArg.getOutputStream()));
  }

  /**
   * Reads the name of a Client from the input stream and sends 
   * a broadcast message to the Server to signal that the Client
   * is participating in the chat. 
   */
  public void announce() throws IOException {
    clientName = in.readLine();
    server.broadcast("[" + clientName + " has entered]");
    
  }

  /**
   * This method takes care of sending messages from the Client.
   * Every message that is received, is preprended with the name
   * of the Client, and the new message is offered to the Server
   * for broadcasting. If an IOException is thrown while reading
   * the message, the method concludes that the socket connection is
   * broken and shutdown() will be called. 
   */
  public void run() {
    try {
      String input;
      while ((input = in.readLine()) != null) {
          server.broadcast(clientName + ": " + input);
      }
      shutdown();
    } catch (IOException e) {
        e.printStackTrace();
        shutdown();
    }
  }

  /**
   * This method can be used to send a message over the socket
   * connection to the Client. If the writing of a message fails,
   * the method concludes that the socket connection has been lost
   * and shutdown() is called.
   */
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
  
  /**
   * This ClientHandler signs off from the Server and subsequently
   * sends a last broadcast to the Server to inform that the Client
   * is no longer participating in the chat. 
   */
  private void shutdown() {
      server.removeHandler(this);
      server.broadcast("[" + clientName + " has left]");
  }

}
