import java.io.IOException;
import java.net.*;

/**
 * ClientUDP demonstrates how to implement a UDP client program
 * that requests an echo service.
 *
 * To run this client program, type the following command:
 * java ClientUDP.java 127.0.0.1 32000
 *
 */
public class ClientUDP {
    public static void main(String[] args){
        if (args.length < 2){
            System.out.println("===== Syntax: ClientUDP <IP> <port>");
            return;
        }

        String ip = args[0];

        int port = Integer.parseInt(args[1]);

        try {
            InetAddress address = InetAddress.getByName(ip);
            DatagramSocket socket = new DatagramSocket();

            while (true) {
                DatagramPacket sent = new DatagramPacket(new byte[1], 1, address, port);
                socket.send(sent);

                byte[] buffer = new byte[512];
                DatagramPacket received = new DatagramPacket(buffer, buffer.length);
                socket.receive(received);

                String message = new String(buffer, 0, received.getLength());

                System.out.println("===== Server: " + message);
            }
        } catch (SocketException e) {
            System.err.println("===== Client error: " + e.getMessage());
            e.printStackTrace();
        } catch (UnknownHostException e) {
            System.err.println("===== Client error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("===== Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
