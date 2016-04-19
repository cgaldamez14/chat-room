package chatroom;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable{
		// Rover name and listerning port
		private final static String NAME = "ROVER_03";
		private final static int PORT = 8000;

		// List of connected rovers
		private List<ClientHandler> clients;


		private ServerSocket serverSocket;

		// Will be used to get input from console
		private InputStreamReader cin;
		private StringBuilder message = new StringBuilder();

		public Server() throws IOException{
			// Creates a server socket at specified port
			serverSocket = new ServerSocket(PORT);

			System.out.println("ROVER_03 server online...");
			System.out.println("Waiting for other rovers to connect...");

			cin = new InputStreamReader(System.in);
			clients= new ArrayList<>();

			// Begin messaging thread
			sendMessage();
		}

		@Override
		public void run() {
			while(true){
				try {
					ClientHandler client = new ClientHandler(serverSocket.accept(), NAME);
					clients.add(client);
					getClientList();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void sendMessage(){
			Thread messages = new Thread(){
				public void run(){
					while(true){
						try {
							if(cin.ready())
								if(!clients.isEmpty()){

									while(cin.ready()){
										message.append((char)cin.read());
										System.out.println(cin.ready());
									}

									for(ClientHandler c : clients){
										System.out.println(message.toString());
										System.out.println("sending to " + c.getPort() + " message: " + message );
										c.send(message.toString());
									}
									message.setLength(0); // Reset StringBuilder
								}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			};
			messages.start();
		}

		public void getClientList(){
			System.out.println("****************** Rover List **********************");
			for(ClientHandler c : clients){
				System.out.println("Address: " + c.getIP() + " , Port:" + c.getPort());
			}
		}

		public static void main(String args[]) throws IOException{
			Thread newThread = new Thread(new Server());
			newThread.start();
		}
}
