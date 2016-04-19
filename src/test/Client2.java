package test;

import java.io.IOException;

import chatroom.Server;

public class Client2 {

	public static void main(String[] args) throws IOException {
		new Thread(new Server(8001)).start();
	}

}
