package chatroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler {

	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	private ArrayList<ClientHandler> clients;


	public ClientHandler(String ip, int port, ArrayList<ClientHandler> clients) throws IOException{
		this.socket = new Socket(ip, port);
		this.clients = clients;
		input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		output = new PrintWriter(this.socket.getOutputStream(), true);

		Thread read = new Thread(){
			public void run() {
				while(true){
					try {
						if(input.ready())
							System.out.println(input.readLine());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		read.start();
	} 

	public ClientHandler(Socket socket, ArrayList<ClientHandler> clients) throws IOException{
		this.socket = socket;
		this.clients = clients;
		input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		output = new PrintWriter(this.socket.getOutputStream(), true);

		Thread read = new Thread(){
			public void run() {
				while(true){
					try {
						if(input.ready()){
							System.out.println(input.readLine());
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		};
		read.start();
	} 


	// Will be used to send messages to other rovers
	public void send(String message){
		try{
			output.println(message);
		}catch(Exception e) {
			for(ClientHandler c : clients){
				if(c.getIP().equals(getIP())){
					clients.remove(c);
					break;
				}
			}
		}
	}

	public InetAddress getIP(){
		return socket.getInetAddress();
	}

	public int getPort(){
		return socket.getPort();
	}
	
	public void closeConnection() throws IOException{
		socket.close();
	}
}
