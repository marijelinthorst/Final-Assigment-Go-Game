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

/**
 * Client
 * @author marije.linthorst
 *
 */
public class Client extends Thread {
  private static final String USAGE
  = "Usage: " + Client.class.getName() + "<name> <address> <port>";
  private static InetAddress host;
  private static int port;

  private String clientName;
  private Socket sock;
  private BufferedReader in;
  private BufferedWriter out;

  /** MAIN: Starts a Client-application. */
  public static void main(String[] args) {
    
    Scanner in = new Scanner(System.in);
    System.out.println("Enter name, IP address and port number: ");
    String clientName = in.hasNext() ? in.next() : null;
    String sIP = in.hasNext() ? in.next() : null;
    String sPort = in.hasNext() ? in.next() : null;
    in.close();
    
    
    if (clientName ==  null || sIP == null || sPort == null) {
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
      client.start();
      
      do {
        String input = readString();
        client.sendMessage(input);
      } while(true);
      
    } catch (IOException e) {
      print("ERROR: couldn't construct a client object!");
      System.exit(0);
    }
  }

  /**
   * Constructs a Client-object and tries to make a socket connection
   */
  public Client(String name, InetAddress host, int port) throws IOException {
    this.clientName = name;
    this.sock = new Socket(host, port);
    System.out.println("Socket created");
    this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
    this.out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
  }

  /**
   * Reads the messages in the socket connection. Each message will
   * be forwarded to the MessageUI
   */
  public void run() {
    try {
      String line;
      while ((line = in.readLine()) != null) {
        if (line.startsWith("ACKNOWLEDGE_HANDSHAKE")) {
          
        } else if (line.startsWith("REQUEST_CONFIG")) {
          
        } else if (line.startsWith("ACKNOWLEDGE_CONFIG")) {
          
        } else if (line.startsWith("ACKNOWLEDGE_MOVE")) {
          
        } else if (line.startsWith("INVALID_MOVE")) {
          
        } else if (line.startsWith("UPDATE_STATUS")) {
          
        } else {
          // geef server door unknown
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      shutdown();
    }
  }

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

  /** returns the client name */
  public String getClientName() {
    return clientName;
  }

  private static void print(String message){
    System.out.println(message);
  }

  // reads from the system input from this client
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
