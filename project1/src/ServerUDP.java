import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

/**
 * The ServerUDP demonstrates a UDP server program.
 *
 */
public class ServerUDP {
    private DatagramSocket socket;
    private List<String> listReceived = new ArrayList<String>();
    private Random random;
    private Map<String, String> store = new HashMap<>();

    public ServerUDP() throws SocketException{
        socket = new DatagramSocket();
        random = new Random();
    }

    public static void main(String[] args){
        try{
            ServerUDP server = new ServerUDP();
            server.receiveRequest();
            server.service();
        } catch (SocketException e){
            System.err.println("Socket error: " + e.getMessage());
        } catch (IOException e){
            System.err.println("I/O error: " + e.getMessage());
        }
    }

    private void service() throws IOException{
        while(true){
            // receive a request from client
            byte[] buffer = new byte[512]; // buffer to receive incoming request from client
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            socket.receive(request);
            String sentence = new String(request.getData());
            String[] requestArr = sentence.split(" ");

            // send response to client
            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();
            keyValService(requestArr, clientAddress, clientPort);
        }
    }

    private void keyValService(String[] requestArr, InetAddress clientAddress, int clientPort) throws IOException {
        while (requestArr.length < 2){
            String errMsg = "Syntax: <operation> <parameter1>. For example: get apple";
            sendResult(errMsg, clientAddress, clientPort);
        }
        String action = requestArr[0];
        String key = requestArr[0];
        if (requestArr.length == 3){
            while (!action.equals("put")){
                String errMsg = "For operation of PUT, syntax: <operation> <parameter1> <parameter2>. For example: put apple 10.";
                sendResult(errMsg, clientAddress, clientPort);
            }
        }


        switch(action) {
            case "get":
                String price = store.get(key);
                sendResult(price, clientAddress, clientPort);
                break;
            case "delete":
                if (!store.containsKey(key)) {
                    String errMsg = "Sorry! Store does not carry " + action;
                    sendResult(errMsg, clientAddress, clientPort);
                }
                break;
            case "put":
                if (requestArr.length == 3) {
                    store.put(key, requestArr[2]);
                    break;
                }

                String errMsg = "For operation of PUT, syntax: <operation> <parameter1> <parameter2>. For example: put apple 10.";
                sendResult(errMsg, clientAddress, clientPort);
                break;
            default:
                errMsg = "No operation of " + action;
                sendResult(errMsg, clientAddress, clientPort);
        }
    }

    private void sendResult(String res, InetAddress clientAddress, int clientPort) throws IOException {
        DatagramPacket response = new DatagramPacket(res.getBytes(), res.getBytes().length, clientAddress, clientPort);
        socket.send(response);
    }

}
