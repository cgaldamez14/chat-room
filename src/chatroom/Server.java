package chatroom;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server implements Runnable{

	//Command map
	private Map<String,Integer> commands = new HashMap<>();

	// Rover name and listerning port
	private int port;

	// List of connected rovers
	private List<ClientHandler> clients;


	private ServerSocket serverSocket;

	// Will be used to get input from console
	private InputStreamReader cin;
	private StringBuilder message = new StringBuilder();

	public Server(int port) throws IOException{
		commands.put("help",1);
		commands.put("myip",2);
		commands.put("myport",3);
		commands.put("connect",4);
		commands.put("list",5);
		commands.put("terminate",6);
		commands.put("send",7);
		commands.put("exit",8);
		
		this.port = port;

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
				ClientHandler client = new ClientHandler(serverSocket.accept());
				clients.add(client);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void showMyIP() throws SocketException{
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()){
		    NetworkInterface current = interfaces.nextElement();
		    if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
		    Enumeration<InetAddress> addresses = current.getInetAddresses();
		    while (addresses.hasMoreElements()){
		        InetAddress current_addr = addresses.nextElement();
		        if (current_addr.isLoopbackAddress()) continue;
		        if (current_addr instanceof Inet4Address)
		        	  System.out.println(current_addr.getHostAddress());
		    }
		}
	}
	
	// Changing send mesage to a getInput method
	private void commandListener(){
		Thread commandInput = new Thread(){
			public void run(){
				while(true){
					try {
						if(cin.ready()){
							while(cin.ready())
								message.append((char)cin.read());
							executeCommand(message.toString());
							message.setLength(0); // Reset StringBuilder
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		commandInput.start();
	}

	// Will be used to extract command from input
	private int getCommand(String input){
		String command = input.split(" ")[0].trim();
		if(validCommand(command))
			return commands.get(command);
		return -1;
	}

	private boolean validCommand(String command){
		return commands.containsKey(command);
	}

	private void executeCommand(String message) throws IOException{
		switch(getCommand(message)){
		case 1: showHelp();
				break;
		case 2: showMyIP();
				break;
		case 3: showMyPort();
		break;
		case 4: String ip = getDestinationIP(message);
		int port = getDestinationPort(message);
		connect(ip, port);
		break;
		case 5: showClientList();
		break;
		//case 6: terminateConnection(clientId);
		//		break;
		case 7: int id = getID(message);
		String m = getMesage(message);
		sendMessage(id,m);
		break;
		//case 8: closeAllConnections();
		//		break;
		default: System.out.println("Not a valid command");  // Might change this later
		}
	}
	
	private void showHelp(){
		String descriptions[] = {"myip : displays ip address of machine", 										// myip
		"myport : displays port number listening for incoming connections",										// myport
		"connect <destination> <port no.> : extablishes TCP connection to the specified <destination> "			// connect
				+ "and the specified <port no.>",
		"list : displays a numbered list of all the connections this process is part of",						// list
		"terminate <connection id> : this command terminates the connection listed under the "					// terminate
				+ "specified id when \"list\" is used to display all connections",
		"send <connection id> <message> :  will send the message to the host on the connection that"			// send
				+ " is designated by the id when command \"list\" is used. The message to be sent can be up-to 100 characters"
				+ " long, including blank spaces. ",
		"exit : closes all connections and terminates this process."};											// exit							
		
		for(int i = 0; i < descriptions.length;i++){
			System.out.println(descriptions[i]);
		}
	}

	private String getMesage(String input) {
		//ip number will be the third item in the input
		return input.split(" ",3)[2];	// Split will only be applied three times
	}

	private int getID(String input) {
		//id number will be the second item in the input
		return Integer.parseInt(input.split(" ")[1]);
	}

	private void connect(String destinationIP, int destinationPort) throws IOException {
		ClientHandler client = new ClientHandler(destinationIP,destinationPort);
		clients.add(client);
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

	// port will be set when application is initiated
	private int showMyPort(){
		return port;
	}
	
	// Send message to specific client
	public void sendMessage(int clientID, String message){
		ClientHandler client = clients.get(clientID);
		client.send(message.toString());
		System.out.println("Message send to " + clientID);
	}

	// Client list
	public void showClientList(){
		System.out.println("****************** Connected Clients **********************");
		System.out.println("id: IP address        Port no. ");

		for(int index = 0; index < clients.size(); index++){
			ClientHandler curr = clients.get(index);
			System.out.println(index+ ": " + curr.getIP() + " " + curr.getPort());
		}
	}
}
