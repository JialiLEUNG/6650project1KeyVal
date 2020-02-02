/**
 * ClientUDP demonstrates how to implement a UDP client program
 * that requests an echo service.
 *
 * To run this client program, type the following command:
 * java ClientUDP.java localhost 32000
 * The current client runs on a file that is named as ClientRequestUDP.txt, the path is hard coded
 * so that once the key-value store is populated, the client can do at least five of each operation: 5 PUTs, 5 GETs, 5 DELETEs.
 * The final command is "Exit" which is to exit the program.
 */

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;


public class ClientUDP {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private Scanner clientScanner;

    // ClientUDP constructor
    private ClientUDP(String address, int port) throws IOException{
        this.address = InetAddress.getByName(address);
        this.port = port;
        socket = new DatagramSocket();
        clientScanner = new Scanner(new File("./ClientRequestUDP.txt"));
    }


    private void readyToSendReceivePacket() {
        while (true) {
            try {
                while (clientScanner.hasNext()){
                    String clientInput = clientScanner.nextLine();
                    if (clientInput.equals("Exit")){
                        System.out.println("===== Closing the connection: " + socket);
                        socket.close();
                        System.out.println("===== Connection is closed.");
                        break;
                    }

                    // send a UDP message to server
                    DatagramPacket sent = new DatagramPacket(clientInput.getBytes(), clientInput.getBytes().length, address, port);
                    socket.send(sent);

                    // receive a response from server
                    byte[] buffer = new byte[512]; // buffer to receive incoming data from server
                    DatagramPacket received = new DatagramPacket(buffer, buffer.length);
                    socket.receive(received);

                    String message = new String(buffer, 0, received.getLength());
                    System.out.println("===== Server: " + message);

                    Thread.sleep(1000);
                }
                clientScanner.close();
            } catch (SocketException e) {
                System.err.println("===== Client error: " + e.getMessage());
                e.printStackTrace();
            } catch (UnknownHostException e) {
                System.err.println("===== Client error: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("===== Client error: " + e.getMessage());
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws NumberFormatException, IOException {
        if (args.length < 2) {
            System.out.println("===== Syntax: ClientUDP <IP> <port>");
            return;
        }

        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        ClientUDP client = new ClientUDP(ip, port);
        System.out.println("===== Running UDP client at IP address of : " + ip);
        client.readyToSendReceivePacket();
    }
}
