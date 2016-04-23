package chatroom;

import java.io.Serializable;

public class Disconnect implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
	
	public Disconnect(String message){
		this.message = message;
	}
	
	public String toString(){
		return message;
	}
}
