package chatroom;

import java.io.Serializable;

/**
 * Disconnect class contains a custom message that will be displayed when client receives
 * the disconnect object as a disconnect request.
 *   
 * @author Carlos Galdamez
 * @author Jose Rivas
 * @author Eduardo Lopez-Serrano 
 * @version 1.0
 * @since 1.0
 */
public class Disconnect implements Serializable{
	
	private static final long serialVersionUID = 1L;
	/** Disconnect message **/
	private String message;
	
	/**
	 * Constructor
	 * @param message Message that will be show to peer when Disconnect request is received
	 */
	public Disconnect(String message){
		this.message = message;
	}
	
	public String toString(){
		return message;
	}
}
