package com.nedap.go;

public class pseudocode {
	/* Merk op: dit is pseudocode, het compileert niet. Het bevat net genoeg structuur om aan
	   te geven hoe je zou kunnen werken met de beschrijving die ik je probeerde te geven.
	*/
	import java.net.InetSocketAddress;
	import java.net.URI;
	import java.util.Scanner;
	import java.util.concurrent.BlockingQueue;
	 
	 
	public class Client {
	    ConcurrentQueue<Move> inputMoveQueue = new BlockingQueue<Move>();
	 
	    public static void main(String[] args) {
	        Client client = new Client();
	        client.startUI();
	    }
	 
	    public void startUI() {
	        Thread tuiThread = new Thread {
	            public void run() {
	                this.eventLoop();
	            }
	        }
	    }
	 
	    public void eventLoop() {
	        Scanner stdin = new Scanner(System.in);
	 
	        while(!finished && stdin.hasNext()) {
	            showPrompt();
	            String inputLine = stdin.nextLine();
	            dispatchUILine(inputLine);
	        }
	    }
	 
	    public void showPrompt() {
	        if (!isServerConfigured) {
	            System.out.println("Please enter your name, an at sign (@), "+
	                    "the server's name, a colon (:) and the server's port, "+
	                    "e.g. Martijn Vis@goserver:1234");
	        } else if (!isGameConfigured && needToConfigureGame) {
	            System.out.println("Please enter the desired width of the game board");
	        } else {
	            System.out.println("Please enter your next move");
	        }
	    }
	 
	    public void dispatchUILine(String line) {
	        if (!isServerConfigured) {
	            dispatchServerConfigurationLine(line);
	        } else if (!isGameConfigured) {
	            dispatchGameConfigurationLine(line);
	        } else {
	            dispatchGamePlayLine(line);
	        }
	    }
	 
	    public void dispatchServerConfigurationLine(String line) {
	        Scanner nameServer = new Scanner(line).useDelimiter("@");
	        String name = nameServer.next();
	        String server = nameServer.next();
	 
	        URI serverUri = new URI("go://" + server);
	        InetSocketAddress address = new InetSocketAddress(serverUri.getHost(), serverUri.getPort());
	 
	        Thread serverThread = new Thread {
	            public void run() {
	                this.serverConnection(address);
	            }
	        }
	    }
	 
	    public void dispatchGamePlayLine(String line) {
	        if (!line.startsWith("exit") && !line.startsWith("quit")) {
	            inputMoveQueue.put(Move.parseMove(line));
	        }
	    }
	 
	    public void serverConnection(InetSocketAddress address) {
	        doConnect(address);
	        isServerConfigured = true;
	 
	        Scanner serverIn = new Scanner(serverSocket);
	 
	        while(!finished && serverIn.hasNext()) {
	            inputLine = serverIn.nextLine();
	            dispatchServerLine(inputLine);
	        }
	    }
	 
	    public void dispatchServerLine(String line) {
	        if (line.startsWith("REQUEST_CONFIG")) {
	            needToConfigureGame = true;
	        } else if (line.startsWith("ACKNOWLEDGE_CONFIG")) {
	            doApplyGameConfiguration();
	            isGameConfigured = true;
	        } else if (line.startsWith("GAME_END")) {
	            finished = true;
	        }
	    }
	 
	    public void doApplyGameConfiguration() {
	        Thread playThread = new Thread {
	            public void run () {
	                while (!finished) {
	                    Move nextMove;
	                    if (playAsComputer) {
	                        nextMove = calculateMove();
	                    } else {
	                        nextMove = inputMoveQueue.take();
	                    }
	                    playMove(nextMove);
	                }
	            }
	        }
	    }
	}

}
