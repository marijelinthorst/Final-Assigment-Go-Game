package com.nedap.go.client;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClientTest {
	private Thread myThread;
	private Socket localSocket;
	private ServerSocket testServer;
	private Client client;
	private Scanner in;
	private Client client2;

	@Before
	public void setUp() {
		myThread = new Thread() {
			public void run() {
				System.out.println("Starting test thread");
				localSocket = null;
				try {
					testServer = new ServerSocket(2727);
					localSocket = testServer.accept();
					
					in = new Scanner(new BufferedReader(
							new InputStreamReader(localSocket.getInputStream())));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		myThread.start();

		try {
			client = new Client("Marije", InetAddress.getByName("localhost"), 2727);
			client.startUserInput();
			client.startServerInput();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	/**
	@Test
	public void testdispatchHandshakeLine() {
		client.dispatchHandshakeLine("huma");
		client.dispatchHandshakeLine("human");
		assertTrue("Marije".equals(in.nextLine()));
	}
	
	@Test
	public void testdispatchGameConfigurationLine() {
		client.dispatchGameConfigurationLine("1,7");
		assertTrue("Marije".equals(in.nextLine()));
	}
	*/

//	@Test
//	public void test() {
//		while (in.hasNext()) {
//			System.out.println(in.nextLine());
//		}
//	}

//	@After
//	public void closedown() {
//		client.shutdown();
//		try {
//			testServer.close();
//			localSocket.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		try {
//			myThread.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}

}
