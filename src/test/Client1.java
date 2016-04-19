package test;

import java.io.IOException;

import chatroom.Server;

public class Client1 {

	public static void main(String[] args) throws IOException {
		new Thread(new Server(8000)).start();
	}

}
