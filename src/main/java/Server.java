/**
 * This is the server side of the implementation of a TCP client and server.
 * The TCP client/server will communicate over the network and exchange data.
 * The message sent by client will be reversed, and cases are switched.
 */

import java.net.*;
import java.io.*;


public class Server {
    // initialize socket and input stream
    private Socket socket = null;
    private ServerSocket server = null;
    private DataInputStream in = null;

    // constructor with port
    public Server(int port){
        // start server and waits for a connection
        try{
            // The server will start in passive mode listening for a transmission from the client.
            server = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client");

            socket = server.accept();
            System.out.println("Client accepted");

            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            String returnMessage = "";
            while (returnMessage.isEmpty()){
                try{
                    returnMessage = reverseString(in.readUTF());
                    if (returnMessage.length() > 80){
                        break;
                    }
                }
                catch(IOException i) {
                    System.out.println(i);
                }
            }

            // send the response (return message) back to the client:
            OutputStream os = socket.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            bw.write(returnMessage);
            System.out.println("Message sent back to the client is: " + returnMessage);
            bw.flush();

            // close connection
            System.out.println("Server exits. Connection closes.");
            socket.close();
            in.close();
            }
            catch(IOException i){
            System.out.println(i);
        }
    }

    /**
     * reverseString reverses the input string
     * and switches the cases of each character.
     * @param str the client's input message
     * @return a reverse string with cases switched.
     */
    public String reverseString(String str){
        String[] res = new String[str.length()];

        int i = str.length() - 1;
        for (Character s : str.toCharArray()){
            if ( s >= 65 && s <= 90){
                s = Character.toLowerCase(s);
            }
            else if (s >= 97 && s <= 122){
                s = Character.toUpperCase(s);
            }
            res[i--] = s.toString();
        }
        String ans = "";
        for (String s : res){
            ans+=s;
        }
        return ans;
    }

    public static void main(String args[]){

        Server server = new Server(Integer.parseInt(args[0]));
    }
}
