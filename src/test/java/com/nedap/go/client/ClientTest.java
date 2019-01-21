package com.nedap.go.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClientTest {
	private Thread myThread;
	private Socket localSocket;
	private ServerSocket testServer;
	private Client client;

	@Before
	public void setUp() {
		myThread = new Thread() {
			public void run() {
				System.out.println("Starting test thread");
				localSocket = null;
				try {
					testServer = new ServerSocket(2727);
					localSocket = testServer.accept();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		myThread.start();

		try {
			client = new Client("Marije", InetAddress.getByName("localhost"), 2727);
			// client.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@After
	public void closedown() {
		client.shutdown();
		try {
			testServer.close();
			localSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			myThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
