package chatroom;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * The Server class takes care of creating a listening socket and
 * binding it to a specific port and ip. This class starts multiple threads that are
 * reponsible to listening to IO from the user and peers. The Server class provides the
 * functionality of all of the following available commands available to the user.
 *   
 * @author Carlos Galdamez
 * @author Jose Rivas
 * @author Eduardo Lopez-Serrano 
 * @version 1.0
 * @since 1.0
 */
public class Server implements Runnable{

	private Commands commands;
	/** List of clients connected to user **/
	private ArrayList<Client> clients;
	/** Client object that contains users' IP address and listening port number **/
	private Client myInfo;		
	/** ServerSocket object that will be used as a listening socket for incoming connection requests **/
	private ServerSocket serverSocket;			

	/** Input stream from console **/
	private InputStreamReader cin;
	/** Message generated from appending characters from input stream **/
	private StringBuilder message;

	private int numberOfClients = -1;

	/*------------------------------------------------------------------- CONSTRUCTOR ----------------------------------------------------------*/

	/**
	 * 	Constructor instantiates a socket and binds it to the process ip and specified port
	 *  number. The socket that is created in the constructor will be used to listen to 
	 *  incoming connection request. 
	 *  @param port any positive integer value between 1 and 65500. Cannot be port number
	 * 		       that has already been standardized.
	 *  @see StringBuilder
	 *  @see ArrayList
	 *  @see ServerSocket
	 *  @see Client
	 *  @see InputStreamReader
	 *  @throws IOException
	 *  
	 */
	public Server(int port) throws IOException{
		myInfo = new Client(Commands.getMyIP(),port);
		numberOfClients = numberOfClients + 1;
		clients= new ArrayList<>();
		commands = new Commands(clients);
		/* Instantiates server socket and binds it to machine IP and specified port number */
		serverSocket = new ServerSocket(myInfo.getListeningPort());

		System.out.println("The program runs on port number: " + myInfo.getListeningPort());
		System.out.println("Waiting for clients to connect...");

		cin = new InputStreamReader(System.in);
		message = new StringBuilder();

		commandListener();
	}

	/*------------------------------------------------------------ METHODS FOR COMMANDS -----------------------------------------------------------------*/

	/**
	 *  Starts a new thread which listens for the user's input on the console.
	 *  This method is called when Server class in instantiated.
	 *  @see Thread
	 *  @see InputStreamReader
	 *  @see StringBuilder
	 */
	private void commandListener(){
		Thread commandInput = new Thread(){
			public void run(){
				while(true){
					try {
						if(cin.ready()){								/* Checks if the stream is ready to be read */
							while(cin.ready())							/* While stream has something to be read each character will be read one by one */
								message.append((char)cin.read());		/* Read one character at a time and append it to the StringBuilder */
							executeCommand(message.toString());
							message.setLength(0); 						/* Clear StringBuilder for further use */
						}
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		commandInput.start();
	}

	/** Determines what method to call based on the input string received from the console
	 * 	@param input Input string from console 
	 *  @see Commands
	 * 	@throws IOException
	 *  @throws InterruptedException
	 * */
	private void executeCommand(String input) throws IOException, InterruptedException{
		/* Chooses method based on integer received from getCommand(input) method in Command class */
		switch(commands.getCommand(input)){
		case 1: 
			showHelp();
			break;
		case 2: 
			showMyIP();
			break;
		case 3: 
			showMyPort();
			break;
		case 4: 
			String ip = Commands.getDestinationIP(input);
			int port = Commands.getDestinationPort(input);
			connect(ip, port);
			break;
		case 5: 
			showClientList();
			break;
		case 6: 
			int clientId = commands.getID(input);
			terminateConnection(clientId);
			break;
		case 7: 
			int id = commands.getID(input);
			String m = Commands.getMesage(input);
			sendMessage(id,m);
			break;
		case 8: 
			closeAllConnections();
			System.exit(0);
			break;
		default: /* Does nothing if number is anything else */
		}
	}

	/** Displays help menu on the console 
	 * @see Commands
	 * */
	private void showHelp(){
		System.out.println("\n---------------------------------------- COMMANDS -----------------------------------------------\n");
		/* Iterates through list of descriptions in Commands class and prints each command with its description on the console */
		for(int i = 0; i < Commands.DESCRIPTIONS.length;i++){
			System.out.println(Commands.DESCRIPTIONS[i]);
		}
		System.out.println("\n------------------------------------------------------------------------------------------------\n");

	}

	/** Displays user's IP address on the console 
	 * @see Commands
	 * @throws SocketException
	 * */
	private void showMyIP() throws SocketException{
		System.out.println(myInfo.getIP());
	}

	/** Displays users' listening port on the console 
	 * @see Commands
	 * */
	private void showMyPort(){
		System.out.println(myInfo.getListeningPort());
	}

	/** Creates socket, connecting user to peer at the destination IP address and port
	 * 	specified. Once the connection is established new ClientHandler thread is started and
	 *  new client is added to the client list.
	 * @param destinationIP IP address of peer user wants to connect to
	 * @param destinationPort Listening port of peer user wants to connect to
	 * @see Commands
	 * @see ClientHandler
	 * @see Thread
	 * @see ArrayList
	 * @throws IOException
	 * */
	private void connect(String destinationIP, int destinationPort) throws IOException {
		try{
			/* Check for self connections and exisisting connections first  */
			if(destinationIP.equals("127.0.0.1") || destinationIP.equals(myInfo.getIP())){
				System.out.println("ERROR: You cannot establish a connection with yourself.");
				return;
			}
			for(Client c: clients)
				if(destinationIP.equals(c.getIP()) && destinationPort == c.getListeningPort()){
					System.out.println("ERROR: You are already connected to this client. Type 'list' to see a list of connected clients.");
					return;
				}

			ClientHandler handler = new ClientHandler(destinationIP,destinationPort,clients);
			if(handler.successfulConnection){
				Client client = new Client(numberOfClients - 1);
				numberOfClients = numberOfClients + 1;
				client.setClientHandler(handler);
				client.send(myInfo);
				clients.add(client);
			}
		}catch(ConnectException e){
			System.err.println("ERROR: Connection could not be extablished please make sure you have the correct ip address and port number.");
		}
	}

	/** Displays list of connected clients on the console
	 * @see ClientHandler
	 * */
	private void showClientList(){
		System.out.println("\n----------------------------- CLIENTS ------------------------------\n");
		System.out.printf("%-7s%-20s%-20s%n","\tid:","IP address","Port no.");

		for(int index = 0; index < clients.size(); index++){
			Client curr = clients.get(index);
			System.out.printf("%-7s%-20s%-20d%n","\t" + curr.getID() + ": ",curr.getIP(), curr.getListeningPort());
		}
		System.out.println("\n-------------------------------------------------------------------\n");

	}

	/** Terminates connection with client
	 * @param id Client's id that is displayed when user enters <code>list</code> command;
	 * @see ClientHandler
	 * @see ArrayList
	 * @see Disconnect
	 * @throws IOException
	 * */
	private void terminateConnection(int id) throws IOException{
		for(Client c : clients)
			if (c.getID() == id){
				System.out.println("You have disconnected from " + c.getIP() + " on port " + c.getListeningPort());
				c.send(new Disconnect(myInfo.getIP() + " has disconnected"));
				c.closeConnection();
				clients.remove(c);
				break;
			}
	}

	/** Sends message to client
	 * @param id Client's id that is displayed when user enters <code>list</code> command;
	 * @param message String message that will be sent to peer. String can be up to 100 characters including spaces.
	 * @throws IOException 
	 * @see ClientHandler
	 * */
	private void sendMessage(int id, String message) throws IOException{
		/* Gets client at the specified id and uses ClientHandler's send method to send message*/
		for(Client c : clients)
			if (c.getID() == id){
				c.send(message);
				break;
			}
		System.out.println("Message sent to " + id);
	}

	/** Terminates all connections and closes application
	 * @see ArrayList
	 * @throws IOException
	 * @throws InterruptedException
	 * */
	private void closeAllConnections() throws IOException, InterruptedException{
		/* Iterates through list of connected clients and calls the terminate connection function for each one */
		for(Client c: clients){
			c.send(new Disconnect(myInfo.getIP() + " has disconnected"));
			c.closeConnection();
		}
		clients.clear();
	}

	/*---------------------------------------------------------- STARTS WHEN NEW THREAD IS CREATED -------------------------------------------------------------------*/

	@Override
	public void run() {

		/* Loop will run until program is shut down, this loop is used to listen for incoming connection requests */
		while(true){
			try {
				/* Instantiates a new client handler every time a connect request is received.
				 *  Connection socket is created and thread that listens for incoming messages is started.
				 *  ClientHandler is also added to the ArrayList of clients */
				Client client = new Client(numberOfClients);
				numberOfClients = numberOfClients + 1;
				client.setClientHandler(new ClientHandler(serverSocket.accept(),clients));
				client.send(myInfo);
				clients.add(client);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
