//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//import java.net.SocketException;
//import java.util.*;
//
///**
// * The ServerUDP demonstrates a UDP server program.
// *
// */
//public class ServerUDP {
//    /* server information */
//    private DatagramSocket socket;
//    private int port;
//    private Map<String, String> store = new HashMap<>();
//
//    /* constructor for server */
//    public ServerUDP(int port) throws SocketException{
//        this.port = port;
//        // The datagramSocket below is used to create a server that binds to the specific port number
//        // so the clients know how to connect to.
//        socket = new DatagramSocket(this.port);
//    }
//
//    public static void main(String[] args){
//        if (args.length < 1) {
//            System.out.println("Syntax: ServerUDP <port>");
//            return;
//        }
//        try{
//            ServerUDP server = new ServerUDP(Integer.parseInt(args[0]));
//            System.out.println("Server is listening.");
//            server.service();
//        } catch (SocketException e){ // The socket could not be opened, or bind to the specified port or address
//            System.err.println("Socket error: " + e.getMessage());
//        } catch (IOException e){
//            System.err.println("I/O error: " + e.getMessage());
//        }
//    }
//
//    private void service() throws IOException{
//        while(true){
//            // receive a request from client
//            System.out.println("start of service");
//            byte[] buffer = new byte[512]; // buffer to receive incoming request from client
//            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
//            // blocks until a client request comes in.
//            socket.receive(request);
//            String sentence = new String(request.getData()).trim();
//            System.out.println("Message from " + request.getAddress() + ": " + sentence);
//            String[] requestArr = sentence.split(" ");
//            System.out.println("===== Client: " + sentence);
//
//            // send response to client based on client input
//            InetAddress clientAddress = request.getAddress();
//            int clientPort = request.getPort();
//            keyValService(requestArr, clientAddress, clientPort);
//        }
//    }
//
//    private void keyValService(String[] requestArr, InetAddress clientAddress, int clientPort) throws IOException {
//        if (requestArr.length < 2){
//            String errMsg = "Syntax: <operation> <parameter1>. For example: get apple";
//            sendResult(errMsg, clientAddress, clientPort);
//            return;
//        }
//
//        String action = requestArr[0]; // get, put, delete
//        String key = requestArr[1];
//
//        switch(action) {
//            case "get":
//                if(store.containsKey(key)){
//                    String price = store.get(key);
//                    sendResult("Price of " + key + " :" + price, clientAddress, clientPort);
//                }
//                else{
//                    String errMsg = key + " not found.";
//                    sendResult(errMsg, clientAddress, clientPort);
//                }
//                break;
//            case "delete":
//                if (!store.containsKey(key)) {
//                    String errMsg =  key + " not found.";
//                    sendResult(errMsg, clientAddress, clientPort);
//                }
//                else{
//                    store.remove(key);
//                    sendResult("Delete " + key + " succeed.", clientAddress, clientPort);
//                }
//                break;
//            case "put":
//                if (requestArr.length == 3) {
//                    store.put(key, requestArr[2]);
//                    sendResult("Put [" + key + ", " + requestArr[2] + "] in store succeed.", clientAddress, clientPort);
//                }
//                else{
//                    String errMsg = "Syntax of put: <operation> <parameter1> <parameter2>. For example: put apple 10.";
//                    sendResult(errMsg, clientAddress, clientPort);
//                }
//                break;
//            default:
//                sendResult("Operation or key or value incorrect. Syntax: <operation> <parameter1>...", clientAddress, clientPort);
//        }
//    }
//
//    private void sendResult(String res, InetAddress clientAddress, int clientPort) throws IOException {
//        DatagramPacket response = new DatagramPacket(res.getBytes(), res.getBytes().length, clientAddress, clientPort);
//        socket.send(response);
//    }
//
//}
