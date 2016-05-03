package chatroom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

	
	private Client client;
	private Socket socket;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ArrayList<Client> clients;
	public boolean successfulConnection = false;

	public ClientHandler(String ip, int port, ArrayList<Client> clients) throws IOException{
		try{
			this.socket = new Socket(ip, port);
			this.clients = clients;
			output = new ObjectOutputStream(socket.getOutputStream());
			input = new ObjectInputStream(socket.getInputStream());
			successfulConnection = true;
		}
		catch(UnknownHostException e){
			System.out.println("You did not enter a valid ip address");
		}
	} 
	
	public ClientHandler(Socket socket, ArrayList<Client> clients) throws IOException{
		this.socket = socket;
		this.clients = clients;
		output = new ObjectOutputStream(this.socket.getOutputStream());
		input = new ObjectInputStream(this.socket.getInputStream());
	}

	public void send(Object o) throws IOException{
			output.writeObject(o);
			output.flush();
	}

	public void closeConnection() throws IOException{
		socket.close();
	}
	
	public void setClient(Client client){
		this.client = client;
		this.client.setConnectionPort(socket.getPort());
	}
	
	@Override
	public void run() {
		while(true){
			try {
				Object recv = input.readObject();
				if(recv instanceof String){
					System.out.println(
							"Message received from: " + client.getIP()
							+ "\nSender's Port: " + client.getListeningPort()
							+ "\nMessage: " + (String)recv);
				}
				else if(recv instanceof Disconnect){
					System.out.println((Disconnect)recv);
					removeClient();
					closeConnection();
					break;
				}
				else if(recv instanceof Client){
					client.setIP(((Client)recv).getIP());
					client.setListeningPort(((Client)recv).getListeningPort());
					client.setConnectionPort(socket.getPort());
					System.out.println(" The connection to peer " + client + " is successfully established.");
				}
			} catch (IOException | ClassNotFoundException e) {
				continue;
			}
		}
	}
	
	private void removeClient() throws IOException{
		for(Client c : clients){
			if(c == client){
				clients.remove(c);
				break;
			}
		}
		socket.close();
	}
	
	public Socket getSocket(){
		return socket;
	}
}
