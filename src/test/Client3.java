package test;

import java.io.IOException;

import chatroom.Server;

public class Client3 {

	public static void main(String[] args) throws IOException {
		new Thread(new Server(8002)).start();
	}

}
