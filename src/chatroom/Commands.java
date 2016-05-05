package chatroom;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * The Commands class is a helper class that parses user console input to extract
 * arguments and commands. This class is also in charge of some input validation 
 * to make sure that the correct number of arguments were entered by the user and
 * correct values were entered for each argument.
 *   
 * @author Carlos Galdamez
 * @author Jose Rivas
 * @author Eduardo Lopez-Serrano 
 * @version 1.0
 * @since 1.0
 */
public class Commands {

	/** Command map, with each command correspoding to a unique integer value **/
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

	/** Array of command descriptions **/
	public static final String DESCRIPTIONS[] = {
			" myip : displays ip address of machine\n", 										
			" myport : displays port number listening for incoming connections\n",													
			" connect <destination> <port no.> : extablishes TCP connection to the specified <destination> and the specified <port no.>\n",
			" list : displays a numbered list of all the connections this process is part of\n",									
			" terminate <connection id> : this command terminates the connection listed under the specified id when \"list\" is used to display all connections\n",
			" send <connection id> <message> : will send the message to the host on the connection that is designated by the id when command \"list\" is used. The message to be sent can be up-to 100 characters long, including blank spaces.\n",
			" exit : closes all connections and terminates this process.\n"
	};

	/** List of connected peers **/
	private ArrayList<Client> clients;

	/**
	 * Constructor 
	 * @param clients List of clients that the user is connected to
	 */
	public Commands(ArrayList<Client> clients){
		this.clients = clients;
	}

	/**
	 * Searches through command map and returns corresponding Integer value for command. Most of the argument validation is done here.
	 * @param input String representation of user input on console
	 * @return returns the Integer value corresponding to the command was entered. If an issue was encountered a -1 will be returned.
	 */
	public int getCommand(String input){
		String command = input.split(" ")[0].trim();
		/* Checks first if valid command was entered */
		if (!validCommand(command)){
			System.err.println("ERROR: You did not enter a valid command. Type 'help' to see a list of valid commands.");
			return -1;
		}

		Integer commandIndex = COMMANDS.get(command);

		/* This switch statement does some argument validation */
		switch(commandIndex){
		case 4:	
			if(input.split(" ").length != 3){			/* Checks if user put the correct number of arguments*/
				System.err.println("ERROR: Could not complete your request make sure your request is in the following form:\n\tconnect <destination ip> <port no>");
				return -1;
			}else if(getDestinationPort(input) == -1){
				System.err.println("ERROR: You did not enter a valid port number.");
				return -1;
			}
			break;
		case 6:
			if(input.split(" ").length != 2){			/* Checks if user put the correct number of arguments*/
				System.err.println("ERROR: Could not complete your request make sure your request is in the following form:\n\tterminate <client id>");
				return -1;
			}else if(getID(input) == -1){
				System.err.println("ERROR: You did not enter a valid client ID number. Type 'list' to see a list of available peers.");
				return -1;
			}
			break;
		case 7: 
			if(input.split(" ").length < 3){			    /* Checks if user put the correct number of arguments*/
				System.err.println("ERROR: Could not complete your request make sure your request is in the following form:\n\tsend <client id> <message>");
				return -1;
			}
			else if(getID(input) == -1){
				System.err.println("ERROR: You did not enter a valid client ID number. Type 'list' to see a list of available peers.");
				return -1;
			}
			break;
		}

		return commandIndex;
	}

	/**
	 * Iterates through a series of NetworkInterface elements to figure out the users IP address
	 * @return returns String representation of user's IP address
	 * @throws SocketException
	 * @see Enumeration
	 * @see NetworkInterface
	 * @see InetAddress
	 * @see Inet4Address
	 */
	public static String getMyIP() throws SocketException{
		boolean gotAddress = false;																	/* Checks if Inet4Address was found*/	
		String address = null;
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();			/* Generates a series of Network Interface element to iterate through */
		while (interfaces.hasMoreElements()){
			NetworkInterface current = interfaces.nextElement();									/* Gets the next Network Interface element */
			if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
			Enumeration<InetAddress> addresses = current.getInetAddresses();						/* There is a series of Inet Address element inside of each Network Interface, this creates another series of elements to iterate through */
			while (addresses.hasMoreElements()){
				InetAddress current_addr = addresses.nextElement();
				if (current_addr.isLoopbackAddress()) continue;										/* Skips loop back IP address which is of the form: 127.0.0.1 */
				if (current_addr instanceof Inet4Address){											/* We will get the first instance of an IPv4 address and return that */
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

	/*------------------------------------------------------------ COMMAND ARGUMENT EXTRACTORS --------------------------------------------------------------------*/ 


	/**
	 * Extracts the id argument from the user's input
	 * @param input User's input from the console
	 * @return returns id number of peer that user entered if it is valid otherwise it returns -1
	 */
	public int getID(String input) {
		//id number will be the second item in the input
		String id = input.split(" ")[1].trim();
		if(isNumber(id)){
			int idNum = Integer.parseInt(id);
			if(idExist(idNum))
				return idNum;
		}
		return -1;
	}

	/**
	 * Extracts the IP address argument from the user's input
	 * @param input User's input from console
	 * @return a String that only contains the IP address entered by the user
	 */
	public static String getDestinationIP(String input) {
		//ip number will be the second item in the input
		return input.split(" ")[1];
	}

	/**
	 * Extracts the port argument from the user's input
	 * @param input User's input from console
	 * @return returns port number of peer that user entered if it is valid otherwise it returns -1
	 */
	public static int getDestinationPort(String input) {
		/* Port number will be the third item in the input */
		String port = input.split(" ")[2].trim();
		if(validPort(port))
			return Integer.parseInt(port);
		return -1;
	}

	/**
	 * Extracts the message from the user's input
	 * @param input User's input from console
	 * @return returns a String that only contains the message entered by the user
	 */
	public static String getMesage(String input) {
		/* Message will be the third item in the input */
		return input.split(" ",3)[2];
	}


	/*----------------------------------------------------------- SUPPORT BOOLEANS -----------------------------------------------------------------*/

	/**
	 * Verifies if command that was entered was a valid command by checking if the command map cotains the command that was entered by the user
	 * @param command A command entered by the user
	 * @return returns true if command is in the command map, otherwise it returns false
	 */
	public static boolean validCommand(String command){
		return COMMANDS.containsKey(command);
	}

	/**
	 * Verifies that the port number that is supposed to be entered by the user is a valid port number
	 * @param port String represenation of the port number entered by the user
	 * @return returns true if user input for port number is a valid port number otherwise it returns false
	 */
	public static boolean validPort(String port){
		if(isNumber(port)){
			int portNum = Integer.parseInt(port);
			if(portNum >= 1024 && portNum <= 65500)	/* Check ASCII number representation of number character to verify that only numbers were enterd for the port */
				return true;
		}
		return false;
	}

	/**
	 * Iterates through every client object to see if specified ID exist
	 * @param id ID input by user as a parameter for one of the commands
	 * @return return true if the id entered is a client ID for a connected client otherwise false
	 */
	private boolean idExist(int id){
		if(clients != null && !clients.isEmpty())
			for(Client c : clients){
				if (id == c.getID())
					return true;
			}
		return false;
	}

	/**
	 * Verifies whether a giver string can be parsed into an Integer
	 * @param s String entered by application user
	 * @return returns true if the input String can be converted into a integer otherwise false
	 */
	private static boolean isNumber(String s){
		for(int i = 0; i < s.length(); i++){
			int curr = (int)s.charAt(i);
			if(curr < 48 || curr > 57 )
				return false;
		}
		return true;
	}

}
