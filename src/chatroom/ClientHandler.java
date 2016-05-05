package chatroom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * The ClientHandler runs a new thread of execution when user connects to 
 * a new client. ClientHandler listens for incoming and outgoing IO from user
 * or the client.
 *  
 * @author Carlos Galdamez
 * @author Jose Rivas
 * @author Eduardo Lopez-Serrano 
 * @version 1.0
 * @since 1.0
 */
public class ClientHandler implements Runnable{
	
	/** List of clients conected to user **/
	private ArrayList<Client> clients;
	/** Client whose ClientHandler instance this is for **/
	private Client client;
	/** Socket connection extablished with this user **/
	private Socket socket;
	/** Stream to send objects to peer **/
	private ObjectOutputStream output;
	/** Stream to recieve objects from peer **/
	private ObjectInputStream input;

	/*------------------------------------------------------------------- CONSTRUCTORS ----------------------------------------------------------*/
	
	/**
	 * This constructor is used when the user is trying to establish a connection with another client.
	 * @param ip IP address of client that user wishes to extablish a connection with
	 * @param port Listening port of client that user wishes to extablish a connection with
	 * @param clients List of clients user is connected to
	 * @throws IOException
	 */
	public ClientHandler(String ip, int port, ArrayList<Client> clients) throws IOException{
		try{
			this.socket = new Socket(ip, port);								/* Try to establish connection with peer at specific port and ip address */
			this.clients = clients;											
			output = new ObjectOutputStream(socket.getOutputStream());		/* Instantiated ObjectOutputStream which will write from the socket stream */
			input = new ObjectInputStream(socket.getInputStream());			/* Instantiated ObjectInputStream which will read from the socket stream */
		}
		catch(UnknownHostException e){
			System.out.println("You did not enter a valid ip address");
		}
	} 
	
	/**
	 * This constructor is used when a client has requested a connection with user and connection was sucessfully established.
	 * @param socket Socket object which connects user with peer
	 * @param clients List of clients user is connected to
	 * @throws IOException
	 */
	public ClientHandler(Socket socket, ArrayList<Client> clients) throws IOException{
		this.socket = socket;
		this.clients = clients;
		output = new ObjectOutputStream(this.socket.getOutputStream());		    /* Instantiated ObjectOutputStream which will write from the socket stream */
		input = new ObjectInputStream(this.socket.getInputStream());			/* Instantiated ObjectInputStream which will read from the socket stream */
	}
	
	/*-----------------------------------------------------------------_SETTERS & GETTERS -----------------------------------------------------------------*/
	
	/**
	 * Returns Socket for this instance of CLientHandler
	 * @return return a Socket
	 * @see Socket
	 */
	public Socket getSocket(){
		return socket;
	}
	
	/**
	 * Gets reference to Client Object to whom this ClientHandler is for.
	 * Also sets connection port.
	 * @param client Client object which contains information about the user for whom this instance of ClientHandler is for
	 * @see Client
	 * @see Socket
	 */
	public void setClient(Client client){
		this.client = client;
		this.client.setConnectionPort(socket.getPort());
	}
	
	/*----------------------------------------------------------------------- SEND METHOD ----------------------------------------------------------------*/
	
	/**
	 * Sends Object to peer who is connect this user in the instance of ClientHandler.
	 * The Objects that can be sent are the following: Client, String, Disconnect
	 * @param object Object being sent to user
	 * @throws IOException
	 * @see Client
	 * @see String
	 * @see Disconnect
	 * @see Object
	 * @see ObjectOutputStream
	 */
	public void send(Object object) throws IOException{
			output.writeObject(object);
			output.flush();							/* ObjectOutputStream need to be flushed so the Object can be sucessfully sent through the socket */
	}

	/*------------------------------------------------------- USED WHEN DISCONNECT IS REQUESTED ----------------------------------------------------------*/
	
	/**
	 * Removes instance of Client from ArrayList of connected clients and closes socket connection.
	 * @throws IOException
	 * @see Client
	 * @see ArrayList
	 * @see Socket
	 */
	private void removeClient() throws IOException{
		
		/* Iterates through list of Clients and removes Client instance that this ClientHandler belongs to */
		for(Client c : clients){
			if(c == client){
				clients.remove(c);
				break;
			}
		}
	}
	/**
	 * Closes socket for this instance of ClientHandler
	 * @throws IOException
	 * @see Socket
	 */
	public void closeConnection() throws IOException{
		socket.close();
	}
	
	/*---------------------------------------------------------- STARTS WHEN NEW THREAD IS CREATED -------------------------------------------------------------------*/
	
	@Override
	public void run() {
		
		/* This loop will run until socket between user and peer is closed.
		 * This loop listens constantly for Objects being sent by peer to the user */
		while(true){
			try {
				Object recv = input.readObject();											/* Attempts to read from the object input stream */
				if(recv instanceof String){													/* Prints message if Object is an instance of String */
					System.out.println(
							"Message received from: " + client.getIP()
							+ "\nSender's Port: " + client.getListeningPort()
							+ "\nMessage: " + (String)recv);
				}
				else if(recv instanceof Disconnect){										/* Removes Client whom this ClientHandler belongs to and closes 
																							   socket if Object is an instance of Disconnect */
					System.out.println((Disconnect)recv);
					removeClient();
					closeConnection();
					break;
				}
				else if(recv instanceof Client){											/* Gets all info of peer who is connected to this user and stores it
				 																				in a Client object*/
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
}
