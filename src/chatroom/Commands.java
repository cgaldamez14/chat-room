package chatroom;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Commands {
	
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

	public static final String PLAIN_TEXT = "\033[0;0m";
	public static final String BOLD_TEXT = "\033[0;1m";
	public static final String BOLD_YELLOW_TEXT = "\033[33;1m";
	public static final String ITALIC_RED_TEXT = "\033[31;3m";
	
	public static final String DESCRIPTIONS[] = {
			BOLD_YELLOW_TEXT + " myip : " + PLAIN_TEXT + " displays ip address of machine\n", 										
			BOLD_YELLOW_TEXT + " myport : " + PLAIN_TEXT + "displays port number listening for incoming connections\n",													
			BOLD_YELLOW_TEXT + " connect <destination> <port no.> : " + PLAIN_TEXT + "extablishes TCP connection to the specified <destination> and the specified <port no.>\n",
			BOLD_YELLOW_TEXT + " list : " + PLAIN_TEXT + "displays a numbered list of all the connections this process is part of\n",									
			BOLD_YELLOW_TEXT + " terminate <connection id> : " + PLAIN_TEXT + "this command terminates the connection listed under the specified id when \"list\" is used to display all connections\n",
			BOLD_YELLOW_TEXT + " send <connection id> <message> :  " + PLAIN_TEXT + "will send the message to the host on the connection that is designated by the id when command \"list\" is used. The message to be sent can be up-to 100 characters long, including blank spaces.\n",
			BOLD_YELLOW_TEXT + " exit : " + PLAIN_TEXT + "closes all connections and terminates this process.\n"
	};

	/***********************************************************************************************/
	
	
	// checks if a valid command was entered by checking if the command exists in the command map
	public static boolean validCommand(String command){
		return COMMANDS.containsKey(command);
	}
	
	// Will be used to extract command from input
	public static int getCommand(String input){
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
	
	public static String getMyIP() throws SocketException{
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
	
	/************************************************ Methods used for parsing input *********************************************************/ 

	public static String getMesage(String input) {
		//ip number will be the third item in the input
		return input.split(" ",3)[2];	// Split will only be applied three times
	}

	public static int getID(String input) {
		try{
		//id number will be the second item in the input
		return Integer.parseInt(input.split(" ")[1].trim());
		}
		catch(NumberFormatException e){
			return -1;
		}
	}

	public static String getDestinationIP(String input) {
		//ip number will be the second item in the input
		return input.split(" ")[1];
	}

	public static int getDestinationPort(String input) {
		try{
		//Port number will be the third item in the input
		return Integer.parseInt(input.split(" ")[2].trim()); // need to trim or you get an exception
		}
		catch(NumberFormatException e){
			return -1;
		}
		// Need to create an exception in case Integer.parseInt does not work...when users dont enter a valid port number
	}
	
}
