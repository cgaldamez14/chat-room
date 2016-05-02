package chatroom;

import java.net.InetAddress;

public class Client {
	
	
	private int listeningPort;
	private int connectionPort;
	private InetAddress iPAddress;
	
	
	public Client(int listeningPort, InetAddress iPAddress){
		this.listeningPort = listeningPort;
		this.iPAddress = iPAddress;
	}
	
	public String getIP(){
		return iPAddress.toString().split("/")[1];
	}
	
	public int getListeningPort(){
		return listeningPort;
	}
	
	public int getConnectionPort(){
		return connectionPort;
	}
	
	public void setConnectionPort(int port){
		this.connectionPort = port;
	}
	
	public String toString(){
		return getIP() + " on port " + getListeningPort();
	}
}
