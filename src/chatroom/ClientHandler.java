package chatroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ClientHandler {

	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;


	public ClientHandler(String ip, int port) throws IOException{
		this.socket = new Socket(ip, port);
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

	public ClientHandler(Socket socket) throws IOException{
		this.socket = socket;
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


	// Will be used to send messages to other rovers
	public void send(String message){
		output.println(message);
	}

	public InetAddress getIP(){
		return socket.getInetAddress();
	}

	public int getPort(){
		return socket.getPort();
	}
}
