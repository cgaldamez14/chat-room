package chatroom;

import java.io.IOException;
import java.io.Serializable;

/**
 * The Client class is a model of a client. This class stores information
 * about a specific client that is connected to the user and has dirrect access to the
 * client handler for this specific client to simplify the process of sending messages to
 * this client. This class need to implement Serializable because we have to be able
 * to send the users information through the socket.
 *  
 * @author Carlos Galdamez
 * @author Jose Rivas
 * @author Eduardo Lopez-Serrano 
 * @version 1.0
 * @since 1.0
 */
public class Client implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/** Client's id number **/
	private int id;
	/** Client's listening port **/
	private int listeningPort;
	/** Client's connection port **/
	private int connectionPort;
	/** Client's IP address **/
	private String iPAddress;
	/** Client handler that will take care of receiving and sending messages to this client **/
	private ClientHandler handler;
	
	public boolean messageSentSuccessfully = true;
	
	
	/*------------------------------------------------------------------- CONSTRUCTORS ----------------------------------------------------------*/
	
	/**
	 * When Client is instantiated using this Constructor the id is set to the current number of clients
	 * and the current number of clients is them increased by one.
	 */
	public Client(int id){
		this.id = id;
	}
	
	/**
	 * Constructor in which two fields are set for this client. The client's IP address and the client's listening port.
	 * @param iPAddress IP address of peer that is connecting or that user is connecting to
	 * @param listeningPort Port number of the peer that is connecting or that user is connecting to
	 */
	public Client(String iPAddress, int listeningPort){
		this.listeningPort = listeningPort;
		this.iPAddress = iPAddress;
	}
	
	/**
	 * Constructor in which three fields are set for this client. The client id, the client's IP address and the client's listening port.
	 * @param iPAddress IP address of peer that is connecting or that user is connecting to
	 * @param listeningPort Port number of the peer that is connecting or that user is connecting to
	 * @param id Client id
	 */
	public Client(String iPAddress, int listeningPort, int id){
		this.listeningPort = listeningPort;
		this.iPAddress = iPAddress;
		this.id = id;
	}
	
	/*------------------------------------------------------------ GETTERS ---------------------------------------------------------------------- */
	
	/**
	 * Returns id of the client
	 * @return returns the id of this client
	 */
	public int getID(){
		return id;
	}
	
	/**
	 * Returns IP address of this client
	 * @return returns IP address of this client
	 */
	public String getIP(){
		return iPAddress;
	}
	
	/**
	 * Returns port this user is using to listen to connection requests
	 * @return returns listening port number
	 */
	public int getListeningPort(){
		return listeningPort;
	}
	
	/**
	 * Returns the connection port of this client
	 * @return returns the connection port of this client
	 */
	public int getConnectionPort(){
		return connectionPort;
	}
	
	
	/*------------------------------------------------------------ SETTERS ---------------------------------------------------------------------- */

	
	/**
	 * Sets client's IP address
	 * @param iPAddress Clients IP address
	 */
	public void setIP(String iPAddress){
		this.iPAddress = iPAddress;
	}
	
	/**
	 * Set client's listening port number
	 * @param listeningPort Port number on which this client is listening for incoming connections
	 */
	public void setListeningPort(int listeningPort){
		this.listeningPort = listeningPort;
	}
	
	/**
	 * Sets client's connection port number
	 * @param port Port number generated when connection was established witht his client
	 */
	public void setConnectionPort(int port){
		this.connectionPort = port;
	}
	
	/**
	 * Sets the client handler for this IO which will be responsible for managing the IO for this client. This
	 * method also start the ClientHandler thread.
	 * @param handler ClientHandler object that will be used to keep track of client's IO
	 */
	public void setClientHandler(ClientHandler handler){
		this.handler = handler;									
		this.handler.setClient(this);
		new Thread(this.handler).start();						/* Start the ClientHandler thread */
	}
	
	
	/**
	 * Sends object to this client by using its ClientHandler
	 * @param object Object being sent through the socket. This Object can be one of the following:
	 * 					<ul>
	 * 						<li>Client Object - first thing that is sent after connection is established</li>
	 * 						<li>String Object - messages being sent to peer</li>
	 * 						<li>Disconnect Object - object that lets peer know that the connection is being terminated</li>
	 * 					</ul>
	 * @throws IOException
	 * @see Client
	 * @see String
	 * @see Disconnect
	 * @see ClientHandler
	 * @see Object
	 */
	public void send(Object object) throws IOException{
		try {
			handler.send(object);
			messageSentSuccessfully = true;
		} catch (IOException e) {
			System.out.println(getIP() + " has disconnected. Message could not be sent");
			handler.removeClient();
			handler.closeConnection();
			messageSentSuccessfully = false;
		}
	}
	
	
	/**
	 * Obtains reference to socket from ClientHandler class and closes the socket.
	 * @throws IOException
	 */
	public void closeConnection() throws IOException{
		handler.getSocket().close();
	}
	
	public String toString(){
		return getIP() + " on port " + getListeningPort();
	}
}
