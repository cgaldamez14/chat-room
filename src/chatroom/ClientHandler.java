package chatroom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

	private Socket socket;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ArrayList<ClientHandler> clients;
	public boolean successfulConnection = false;


	@Override
	public void run() {
		while(true){
			try {
				Object recv = input.readObject();
				if(recv instanceof String){
					System.out.println(
							"\033[35;3m Message received from: \033[0;0m" + getIP()
							+ "\n\033[35;3m Sender's Port: \033[0;0m" + getPort()
							+ "\n\033[35;3m Message: \033[0;0m" + (String)recv);
				}
				else if(recv instanceof Disconnect){
					System.out.println((Disconnect)recv);
					removeClient();
					closeConnection();
					break;
				}
			} catch (IOException | ClassNotFoundException e) {
				continue;
			}
		}
	}
	public ClientHandler(String ip, int port, ArrayList<ClientHandler> clients) throws IOException{
		try{
			this.socket = new Socket(ip, port);
			this.clients = clients;
			output = new ObjectOutputStream(socket.getOutputStream());
			input = new ObjectInputStream(socket.getInputStream());
			System.out.println("\033[34;3m You are now connected to " + getIP() + " on port " + getPort() + "\033[0;0m \n");
			successfulConnection = true;
		}
		catch(UnknownHostException e){
			System.out.println("You did not enter a valid ip address");
		}
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
		input = new ObjectInputStream(this.socket.getInputStream());
		System.out.println("\033[34;3m \nYou are now connected to " + getIP() + "on port " + getPort() + "\033[0;0m \n");

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

	public String getIP(){
		return socket.getInetAddress().toString().split("/")[1];
	}

	public int getPort(){
		return socket.getPort();
	}

	public void closeConnection() throws IOException{
		socket.close();
	}
}
