package chatroom;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
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

	private static final String DESCRIPTIONS[] = {"myip : displays ip address of machine", 										
			"myport : displays port number listening for incoming connections",													
			"connect <destination> <port no.> : extablishes TCP connection to the specified <destination> "						
					+ "and the specified <port no.>",
					"list : displays a numbered list of all the connections this process is part of",									
					"terminate <connection id> : this command terminates the connection listed under the "								
							+ "specified id when \"list\" is used to display all connections",
							"send <connection id> <message> :  will send the message to the host on the connection that"						
									+ " is designated by the id when command \"list\" is used. The message to be sent can be up-to 100 characters"
									+ " long, including blank spaces. ",
	"exit : closes all connections and terminates this process."};

	/***********************************************************************************************/

	// Rover name and listerning port
	private int listeningPort;
	private String myIP;

	// List of connected rovers
	private ArrayList<ClientHandler> clients;


	private ServerSocket serverSocket;

	// Will be used to get input from console
	private InputStreamReader cin;
	private StringBuilder message = new StringBuilder();

	public Server(int port) throws IOException{
		this.listeningPort = port;
		this.myIP = getMyIP();

		// Creates a server socket at specified port
		serverSocket = new ServerSocket(port);
		System.out.println("Waiting for clients to connect...");

		cin = new InputStreamReader(System.in);
		clients= new ArrayList<>();

		// Begin command listener thread
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

	private boolean validCommand(String command){
		return COMMANDS.containsKey(command);
	}

	
	// Only called once in Constructor
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
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		// start command thread
		commandInput.start();
	}

	/* method takes in a message, extracts the command and applies the necessary action based on that command */
	private void executeCommand(String message) throws IOException{
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
			connect(ip, port);
			break;
		case 5: 
			showClientList();
			break;
		case 6: 
			int clientId = getID(message);
			terminateConnection(clientId);
			break;
		case 7: 
			int id = getID(message);
			String m = getMesage(message);
			sendMessage(id,m);
			break;
		case 8: 
			//closeAllConnections();
			break;
		default: System.out.println("Not a valid command");  // Might change this later
		}
	}

	// shows help menu on console
	private void showHelp(){													
		for(int i = 0; i < DESCRIPTIONS.length;i++){
			System.out.println(DESCRIPTIONS[i]);
		}
	}

	private void showMyIP() throws SocketException{
		System.out.println(myIP);
	}

	private void showMyPort(){
		System.out.println(listeningPort);
	}
	
	private void connect(String destinationIP, int destinationPort) throws IOException {
		ClientHandler client = new ClientHandler(destinationIP,destinationPort,clients);
		Thread newClient = new Thread(client);
		newClient.start();
		clients.add(client);
	}
	
	// Client list
	private void showClientList(){
		System.out.println("****************** Connected Clients **********************");
		System.out.println("id: IP address        Port no. ");

		for(int index = 0; index < clients.size(); index++){
			ClientHandler curr = clients.get(index);
			System.out.println(index+ ": " + curr.getIP() + " " + curr.getPort());
		}
	}
	
	private void terminateConnection(int id) throws IOException{
		ClientHandler client = clients.get(id);
		client.sendDisconnectRequest(new Disconnect(getMyIP() + "has disconnected"));
		clients.get(id).closeConnection();
		clients.remove(client);
	}
	
	// Send message to specific client
	private void sendMessage(int clientID, String message){
		ClientHandler client = clients.get(clientID);
		client.send(message.toString());
		System.out.println("Message send to " + clientID);
	}
	
	/************************************************ Methods used for parsing input *********************************************************/ 

	private String getMesage(String input) {
		//ip number will be the third item in the input
		return input.split(" ",3)[2];	// Split will only be applied three times
	}

	private int getID(String input) {
		//id number will be the second item in the input
		return Integer.parseInt(input.split(" ")[1].trim());
	}

	private String getDestinationIP(String input) {
		//ip number will be the second item in the input
		return input.split(" ")[1];
	}

	private int getDestinationPort(String input) {
		//Port number will be the third item in the input
		return Integer.parseInt(input.split(" ")[2].trim()); // need to trim or you get an exception

		// Need to create an exception in case Integer.parseInt does not work...when users dont enter a valid port number
	}
	
	// Will be used to extract command from input
	private int getCommand(String input){
		String command = input.split(" ")[0].trim();
		if(validCommand(command))
			return COMMANDS.get(command);
		return -1;
	}
}
