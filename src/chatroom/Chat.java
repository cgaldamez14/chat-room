package chatroom;
import java.io.IOException;

/**
 * Chat is the main class that runs the chat application. To start chat application user is required 
 * to enter a listening port number. If running a .jar file from terminal the command used would be 
 * the following:
 * 
 *  	<br><br><code>java -jar chat.jar &lt;listeningPortNumber&gt;</code><br><br>
 *  
 *  When the application is running the user has the following commands at his/her disposal:
 * 		
 * 		<h1>COMMANDS</h1>
 *  	<ul>
 *  		<code>
 *  			<li>help</li>
 *  			<li>myip</li>
 *  			<li>myport</li>
 *  			<li>connect &lt;destinationIP&gt; &lt;destinationListeningPort&gt;</li>
 *  			<li>list</li>
 *  			<li>terminate &lt;clientID&gt;</li>
 *  			<li>send &lt;clientID&gt; &lt;message&gt;</li>
 *  			<li>exit</li>
 *  		</code>
 *  	</ul>
 *  
 *  The <code>help</code> command will give user more information on what each command does.
 *  
 * @author Carlos Galdamez
 * @author Jose Rivas
 * @author Eduardo Lopez-Serrano 
 * @version 1.0
 * @since 1.0
 */
public class Chat {

	/**
	 * Starts chat application if port number was entered and is valid;
	 * @param args Argument list from console
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String args[]) throws InterruptedException, IOException{
		int listeningPort;
		if (args.length > 0) {
			if(!Commands.validPort(args[0])){
				System.err.println("ERROR: You did not enter a valid port number");
				System.exit(1);
			}
			listeningPort = Integer.parseInt(args[0]);
			new Thread(new Server(listeningPort)).start();

		}else{
			System.err.println("ERROR: You did not enter a port number");					/* Port number was not entered */
			System.exit(1);
		}
	}
}
