package chatroom;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


/*NOTE TO SELF: Have to also check whether they put the argumetns in the wrong order*/
public class Server implements Runnable{

	/************************** created a static map for the commands ****************************/

	private static final Map<String,Integer> COMMANDS;
	static {
		Map<String, Integer> map = new HashMap<>();
		map.put("help",1);
		map.put("myip",2);
		map.put("myport",3);
		map.put("connect",4);
		map.put("list",5);
		map.put("terminate",6);
		map.put("send",7);
		map.put("exit",8);
		COMMANDS = Collections.unmodifiableMap(map);
	}
	/********************************************************************************************/

	/**************** created a static array for the command descriptions ***********************/

	private static final String PLAIN_TEXT = "\033[0;0m";
	private static final String BOLD_TEXT = "\033[0;1m";
	private static final String BOLD_YELLOW_TEXT = "\033[33;1m";
	private static final String ITALIC_RED_TEXT = "\033[31;3m";

	private static final String DESCRIPTIONS[] = {
			BOLD_YELLOW_TEXT + " myip : " + PLAIN_TEXT + " displays ip address of machine\n", 										
			BOLD_YELLOW_TEXT + " myport : " + PLAIN_TEXT + "displays port number listening for incoming connections\n",													
			BOLD_YELLOW_TEXT + " connect <destination> <port no.> : " + PLAIN_TEXT + "extablishes TCP connection to the specified <destination> and the specified <port no.>\n",
			BOLD_YELLOW_TEXT + " list : " + PLAIN_TEXT + "displays a numbered list of all the connections this process is part of\n",									
			BOLD_YELLOW_TEXT + " terminate <connection id> : " + PLAIN_TEXT + "this command terminates the connection listed under the specified id when \"list\" is used to display all connections\n",
			BOLD_YELLOW_TEXT + " send <connection id> <message> :  " + PLAIN_TEXT + "will send the message to the host on the connection that is designated by the id when command \"list\" is used. The message to be sent can be up-to 100 characters long, including blank spaces.\n",
			BOLD_YELLOW_TEXT + " exit : " + PLAIN_TEXT + "closes all connections and terminates this process.\n"
	};

	/***********************************************************************************************/

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
		this.myIP = getMyIP();

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

	// only called once in Constructor
	private String getMyIP() throws SocketException{
		boolean gotAddress = false;
		String address = null;
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()){
			NetworkInterface current = interfaces.nextElement();
			if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
			Enumeration<InetAddress> addresses = current.getInetAddresses();
			while (addresses.hasMoreElements()){
				InetAddress current_addr = addresses.nextElement();
				if (current_addr.isLoopbackAddress()) continue;
				if (current_addr instanceof Inet4Address){
					address = current_addr.getHostAddress();
					gotAddress = true;
					break;
				}
			}
			if(gotAddress)
				break;
		}
		return address;
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
		switch(getCommand(message)){
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
			String ip = getDestinationIP(message);
			int port = getDestinationPort(message);
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
			int clientId = getID(message);
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
			int id = getID(message);
			String m = getMesage(message);
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
		System.out.println(BOLD_YELLOW_TEXT + "\n**************************************** COMMANDS ********************************************\n" + PLAIN_TEXT);
		for(int i = 0; i < DESCRIPTIONS.length;i++){
			System.out.println(DESCRIPTIONS[i]);
		}
		System.out.println(BOLD_YELLOW_TEXT + "**********************************************************************************************\n" + PLAIN_TEXT);

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
		System.out.println(BOLD_YELLOW_TEXT + "****************** Connected Clients **********************\n" + PLAIN_TEXT);
		System.out.printf("%-7s%-20s%-20s%n","\tid:","IP address","Port no.");

		for(int index = 0; index < clients.size(); index++){
			ClientHandler curr = clients.get(index);
			System.out.printf("%-7s%-20s%-20d%n","\t" + index + ": ",curr.getIP(), curr.getPort());
		}
		System.out.println(BOLD_YELLOW_TEXT + "\n**********************************************************\n" + PLAIN_TEXT);

	}

	private void terminateConnection(int id) throws IOException{
		ClientHandler client = clients.get(id);
		client.sendDisconnectRequest(new Disconnect(ITALIC_RED_TEXT + getMyIP() + " has disconnected" + PLAIN_TEXT));
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

	/************************************************ Methods used for parsing input *********************************************************/ 

	private String getMesage(String input) {
		//ip number will be the third item in the input
		return input.split(" ",3)[2];	// Split will only be applied three times
	}

	private int getID(String input) {
		try{
		//id number will be the second item in the input
		return Integer.parseInt(input.split(" ")[1].trim());
		}
		catch(NumberFormatException e){
			return -1;
		}
	}

	private String getDestinationIP(String input) {
		//ip number will be the second item in the input
		return input.split(" ")[1];
	}

	private int getDestinationPort(String input) {
		try{
		//Port number will be the third item in the input
		return Integer.parseInt(input.split(" ")[2].trim()); // need to trim or you get an exception
		}
		catch(NumberFormatException e){
			return -1;
		}
		// Need to create an exception in case Integer.parseInt does not work...when users dont enter a valid port number
	}

	// Will be used to extract command from input
	private int getCommand(String input){
		String command = input.split(" ")[0].trim();
		Integer commandIndex = COMMANDS.get(command);
		if (commandIndex == null)								// order here matter do not move around
			return -1;
		switch(commandIndex){
		case 4:	
			if(input.split(" ").length != 3)
				return -4;
			break;
		case 6:
			if(input.split(" ").length != 2)
				return -6;
			break;

		case 7:
			if(input.split(" ").length < 3)
				return -7;
			break;

		}
		if(validCommand(command))
			return commandIndex;
		return -1;
	}

	// checks if a valid command was entered by checking if the command exists in the command map
	private boolean validCommand(String command){
		return COMMANDS.containsKey(command);
	}
}
