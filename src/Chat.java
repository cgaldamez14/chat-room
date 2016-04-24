import java.io.IOException;

import chatroom.Server;

public class Chat {
public static void main(String args[]) throws InterruptedException, IOException{
	int listeningPort;
	if (args.length > 0) {
	    try {
	        listeningPort = Integer.parseInt(args[0]);
	        new Thread(new Server(listeningPort)).start();
	    } catch (NumberFormatException e) {
	        System.err.println("You did not enter a valid port number");
	        System.exit(1);
	    }
	}else{
    System.err.println("You did not enter a port number");
	System.exit(1);
	}
}
}
