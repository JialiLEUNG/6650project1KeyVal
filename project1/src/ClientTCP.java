/**
 * ClientTCP implements a client by establishing a socket connection and communication.
 * The client connects to TCP port 32000, writes a line of text,
 * and then reads the echoed text back from the server.
 * This client will detect network timeouts, and exit gracefully, rather than stalling.
 *
 * Start server and the run by typing "java ClientTCP.java 127.0.0.1 32000".
 *
 * The following steps are applied for the client's communication with the server:
 * 1. The client initiates connection to a server specified by hostname/IP address and port number.
 * 2. Send data to the server using an OutputStream.
 * 3. Read data from the server using an InputStream.
 * 4. Close the connection.
 */

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.net.InetAddress;
import java.net.Socket;

public class ClientTCP {

    public static void main(String[] args){
        if(args.length < 2) {
            System.out.println("Syntax: ClientTCP <IP> <port>");
            return;
        }
        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        try(Socket socket = new Socket(ip, port)){
            // set the socket timeout for 5 seconds
            socket.setSoTimeout(5000);
            // Send data to the server using an OutputStream
            OutputStream output = socket.getOutputStream();
            // Read data from the server using an InputStream
            InputStream input = socket.getInputStream();

            Scanner clientScanner = new Scanner(System.in);

            while(socket.isConnected()){
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String received = reader.readLine();
                System.out.println("===== Server: " + received);

                // get client input from console
                String sent = clientScanner.nextLine();
                // The argument true below indicates that
                // the writer flushes the data after each method call (auto flush).
                PrintWriter writer = new PrintWriter(output, true);
                writer.println(sent);

                if (sent.equals("Exit")){
                    System.out.println("===== Closing the connection: " + socket);
                    socket.close();
                    System.out.println("===== Connection is closed.");
                    break;
                }

                Thread.sleep(1000);
            }
        } catch (SocketTimeoutException e){ // Socket timed out
            System.err.println("===== Remote host timed out during read operation: " + e.getMessage());
        } catch (UnknownHostException e){
            System.err.println("===== Server not found: " + e.getMessage());
        } catch (IOException e){
            System.err.println("===== I/O error" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
