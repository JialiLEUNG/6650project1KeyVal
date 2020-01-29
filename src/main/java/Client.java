/**
 * This is the client side of the implementation of a TCP client and server.
 * The TCP client/server will communicate over the network and exchange data.
 * The message sent by client will be reversed, and cases are switched.
 */
import java.net.*;
import java.io.*;

public class Client {
    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;
    private int msgLength = 80;


    // constructor to put ip address and port number;
    public Client(String address, int port){
        // establish a connection
        try{
            socket = new Socket(address, port);
            System.out.println("Connected");

            // take client input from terminal
            input = new DataInputStream(System.in);
            // The client starts and contacts the server (on a given IP address and port number).
            // The client will pass the server a string (eg: “network”) up to 80 characters in length.
            out = new DataOutputStream(socket.getOutputStream());
        }
        catch(UnknownHostException u){
            System.out.println(u);
        }
        catch(IOException i){
            System.out.println(i);
        }

        // string to read message from input
        String line ="";

        // keep reading until enter is hit.
        while (line.isEmpty()){
            try{
//                if (line.length() > msgLength){
//                    System.out.println("Length of Character greater than 80. Please try again.");
//                }
                line = input.readLine();
                out.writeUTF(line);
                InputStream message = socket.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(message));
                String returnMessage = br.readLine();
                if (returnMessage.length() > msgLength){
                    // if string length is greater than 80,
                    // return the first 80 characters.
                    returnMessage = returnMessage.substring(0, 80);
                }
                System.out.println("Response from Server is: " + returnMessage);
            }
            catch(IOException i){
                System.out.println(i);
            }
        }

        // close the connection
        try{
            input.close();
            out.close();
            socket.close();
            System.out.println("Client exits. Connection Closes.");
        }
        catch(IOException i) {
            System.out.println(i);
        }
    }
    public static void main(String args[]){
        Client client = new Client(args[0], Integer.parseInt(args[1]));
    }
}
