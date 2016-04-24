package chatroom;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;


/*NOTE TO SELF: Have to also check whether they put the argumetns in the wrong order*/
public class Server implements Runnable{


	private ServerSocket serverSocket;
	private ArrayList<ClientHandler> clients;	// list of connected rovers
	private int listeningPort;					// listening port
	private String myIP;

	// Will be used to get input from console
	private InputStreamReader cin;
	private StringBuilder message = new StringBuilder();

	// constructor
	public Server(int port) throws IOException{
		this.listeningPort = port;
		this.myIP = Commands.getMyIP();

		// creates a server socket at specified port
		serverSocket = new ServerSocket(port);
		System.out.println("Waiting for clients to connect...");

		cin = new InputStreamReader(System.in);
		clients= new ArrayList<>();

		// begin command listener thread
		commandListener();
	}

	@Override
	public void run() {
		while(true){
			try {
				ClientHandler client = new ClientHandler(serverSocket.accept(),clients);
				Thread newClient = new Thread(client);
				newClient.start();
				clients.add(client);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/***************************************** Methods used for Commands *********************************************************************/

	/* this method starts a new thread that constantly listens to see whether the user has typed something in the console*/
	private void commandListener(){
		Thread commandInput = new Thread(){
			public void run(){
				while(true){
					try {
						if(cin.ready()){
							while(cin.ready())
								message.append((char)cin.read());		// reads one character at a time and appends it to the string builder
							executeCommand(message.toString());
							message.setLength(0); 						// reset StringBuilder
						}
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		// start command thread
		commandInput.start();
	}

	/* method takes in a message, extracts the command and applies the necessary action based on that command */
	private void executeCommand(String message) throws IOException, InterruptedException{
		switch(Commands.getCommand(message)){
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
			String ip = Commands.getDestinationIP(message);
			int port = Commands.getDestinationPort(message);
			if(port < 0){
				System.out.println("You did not enter a valid port number");
				break;
			}
			connect(ip, port);
			break;
		case -4: 
			System.out.println("Could not complete your request make sure your request is in the following form:\n\tconnect <destination ip> <port no>");
			break;
		case 5: 
			showClientList();
			break;
		case 6: 
			int clientId = Commands.getID(message);
			if(clientId < 0 || clientId >= clients.size()){
				System.out.println("You did not enter a valid client id number");
				break;
			}
			terminateConnection(clientId);
			break;
		case -6: 
			System.out.println("Could not complete your request make sure your request is in the following form:\n\tterminate <client id>");
			break;
		case 7: 
			int id = Commands.getID(message);
			String m = Commands.getMesage(message);
			if(id < 0 || id >= clients.size()){
				System.out.println("You did not enter a valid client id number");
				break;
			}
			sendMessage(id,m);
			break;
		case -7: 
			System.out.println("Could not complete your request make sure your request is in the following form:\n\tsend <client id> <message>");
			break;
		case 8: 
			closeAllConnections();
			System.exit(0);
			break;
		default: System.out.println("Not a valid command");  // Might change this later
		}
	}

	// shows help menu on console
	private void showHelp(){
		System.out.println(Commands.BOLD_YELLOW_TEXT + "\n**************************************** COMMANDS ********************************************\n" + Commands.PLAIN_TEXT);
		for(int i = 0; i < Commands.DESCRIPTIONS.length;i++){
			System.out.println(Commands.DESCRIPTIONS[i]);
		}
		System.out.println(Commands.BOLD_YELLOW_TEXT + "**********************************************************************************************\n" + Commands.PLAIN_TEXT);

	}

	private void showMyIP() throws SocketException{
		System.out.println(myIP);
	}

	private void showMyPort(){
		System.out.println(listeningPort);
	}

	private void connect(String destinationIP, int destinationPort) throws IOException {
		try{
			ClientHandler client = new ClientHandler(destinationIP,destinationPort,clients);
			if(client.successfulConnection){
			Thread newClient = new Thread(client);
			newClient.start();
			clients.add(client);
			}
		}catch(ConnectException e){
			System.out.println("Connection could not be extablished please make sure you have the correct ip address and port number.");
		}
	}

	// Client list
	private void showClientList(){
		System.out.println(Commands.BOLD_YELLOW_TEXT + "****************** Connected Clients **********************\n" + Commands.PLAIN_TEXT);
		System.out.printf("%-7s%-20s%-20s%n","\tid:","IP address","Port no.");

		for(int index = 0; index < clients.size(); index++){
			ClientHandler curr = clients.get(index);
			System.out.printf("%-7s%-20s%-20d%n","\t" + index + ": ",curr.getIP(), curr.getPort());
		}
		System.out.println(Commands.BOLD_YELLOW_TEXT + "\n**********************************************************\n" + Commands.PLAIN_TEXT);

	}

	private void terminateConnection(int id) throws IOException{
		ClientHandler client = clients.get(id);
		client.sendDisconnectRequest(new Disconnect(Commands.ITALIC_RED_TEXT + myIP + " has disconnected" + Commands.PLAIN_TEXT));
		clients.get(id).closeConnection();
		clients.remove(client);
	}

	// Send message to specific client
	private void sendMessage(int clientID, String message){
		ClientHandler client = clients.get(clientID);
		client.send(message.toString());
		System.out.println("Message send to " + clientID);
	}

	private void closeAllConnections() throws IOException, InterruptedException{
		while(!clients.isEmpty())
			terminateConnection(0);
	}
}
