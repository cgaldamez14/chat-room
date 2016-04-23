package chatroom;

//import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler {

	private Socket socket;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	//private PrintWriter output;
	//private BufferedReader input;
	private ArrayList<ClientHandler> clients;


	public ClientHandler(String ip, int port, ArrayList<ClientHandler> clients) throws IOException{
		this.socket = new Socket(ip, port);
		this.clients = clients;
		output = new ObjectOutputStream(socket.getOutputStream());
		output.flush();
		input = new ObjectInputStream(socket.getInputStream());
		//input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		//output = new PrintWriter(this.socket.getOutputStream(), true);

		Thread read = new Thread(){
			public void run() {
				while(true){
					try {
						//if(input.available() > 0){			// Checks it there are any bytes to be read
							Object recv = input.readObject();
							if(recv instanceof String){
								System.out.println("Message received from " + getIP()
									+ "\nSender's Port: " + getPort()
									+ "\nMessage: " + (String)recv);
							}
							else if(recv instanceof Disconnect){
								System.out.println((Disconnect)recv);
								removeClient();
								break;
							}
						//}
					} catch (IOException | ClassNotFoundException e) {
						continue;
					}
				}
			}
		};
		read.start();
	} 
	
	private void removeClient() throws IOException{
		for(ClientHandler c : clients){
			if(c == this){
				clients.remove(c);
				break;
			}
		}
		socket.close();
	}

	public ClientHandler(Socket socket, ArrayList<ClientHandler> clients) throws IOException{
		this.socket = socket;
		this.clients = clients;
		output = new ObjectOutputStream(this.socket.getOutputStream());
		output.flush();
		input = new ObjectInputStream(this.socket.getInputStream());
		//input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		//output = new PrintWriter(this.socket.getOutputStream(), true);

		Thread read = new Thread(){
			public void run() {
				while(true){
					try {
						//if(input.available() > 0){			// Checks it there are any bytes to be read
							Object recv = input.readObject();
							if(recv instanceof String){
								System.out.println("Message received from " + getIP()
									+ "\nSender's Port: " + getPort()
									+ "\nMessage: " + (String)recv);
							}
							else if(recv instanceof Disconnect){
								System.out.println((Disconnect)recv);
								removeClient();
								break;
							}
						//}
					} catch (IOException | ClassNotFoundException e) {
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
			output.writeObject(message);
			output.flush();
		}catch(Exception e) {
			for(ClientHandler c : clients){
				if(c.getIP().equals(getIP())){
					clients.remove(c);
					break;
				}
			}
		}
	}
	
	public void sendDisconnectRequest(Disconnect request){
		try{
			output.writeObject(request);
			output.flush();
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
