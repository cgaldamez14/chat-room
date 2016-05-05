chat room project

Developed By : Carlos , Jose, Eduardo

Objective: Develop a simple chat application for message exchange among remote peers.

Starting the Program:
1. Have the .jar file in a location that is easy to access
2. Be sure to have your Java Compiler Up-to-date
3. At the Command Prompt, change directories to the .jar file 
4. Use the command "java -jar chat.jar <port number>"
5. If all steps properly done, a message of "Waiting for clients to connect..." should appear

Using the Program:

-the program is always waiting for keywords to come into the command prompt
-the prompt are to be taken all in lowercase otherwise it will give you a "Not a valid command" error message

---------------------------HELP----------------------------------------------------
-help
-Will display all of the possible commands
-Gives a small description with each command

---------------------------MYIP-----------------------------------------------------
-myip
-Displays IP address of the Machine (computer)
-Uses .Net library's .getHostaddress() to retrieve the IP

---------------------------MYPORT---------------------------------------------------
-myport
-Displays the port number that the client is using to listen 
-This is the port that is given to the program before it can

---------------------------CONNECT--------------------------------------------------
-connect <connection IP> port #
-Connects to another client using a Destination and a port number
-The destination is the IP address of the computer

---------------------------LIST-----------------------------------------------------
-list
-Display a number list of all the connections this process is part of.
-The output displays the IP and port number of the connections

---------------------------TERMINATE------------------------------------------------
-terminate <connection id>
-Will terminate the connection listed under the specified number when LIST is used to display all connections.
-An error message is displayed if a valid connection does not exist
-If a remote machine terminates one of your connections, you should also display a message.

---------------------------SEND------------------------------------------------------
-send <connection id> <message>
-This will send the message to the host on the connection that is designated
-The message to be sent can be up-to 100 characters long, including blank spaces
-On successfully executing the command, the sender should display “Message sent to <connection id>” on the screen
-On receiving any message from the peer, the receiver should display the received message along with the sender information.

---------------------------EXIT-------------------------------------------------------
-exit
-Close all connections the client uses and terminate process
-The other peers should also update their connection list by removing the peer that exits.

Resources Used: 
1. Socket Programming: Beej Socket Guide: http://beej.us/guide/bgnet
2. Java Library
3. .Net Library 
4. Eclipse

