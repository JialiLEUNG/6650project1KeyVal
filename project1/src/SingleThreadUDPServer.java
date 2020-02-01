import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * The ServerUDP demonstrates a single-threaded UDP server program.
 *
 */
public class SingleThreadUDPServer implements Runnable{
    /* server information */
    private DatagramSocket      serverSocket;
    private int                 port;
    protected boolean           isStopped = false; // whether the server is terminated.
    protected Thread            runningThread = null;
    private static final int    timeout_length = 60000; // 1 minute
    private Map<String, String> store = new HashMap<>();

    /* constructor for server */
    public SingleThreadUDPServer(int port) throws SocketException{
        this.port = port;
        // The datagramSocket creates a server that binds to the specific port number
        // so the clients know how to connect to.
        serverSocket = new DatagramSocket(this.port);
    }


    public void run(){
        // only one thread can access the resource at a given point of time
        synchronized (this){
            // Thread.currentThread() returns a reference to the currently executing thread object.
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();

        while(!isStopped()){
            DatagramPacket request;
            try {
                // wait for a request from client
                System.out.println("Server listening for request.");
                byte[] buffer = new byte[512]; // buffer to receive incoming request from client
                request = new DatagramPacket(buffer, buffer.length);
                // blocks until a client request comes in.
                serverSocket.receive(request);
                System.out.println("New client connected and message received...");
            } catch (IOException e){
                if (isStopped()){
                    System.out.println("Server Stopped.");
                    return;
                }
                throw new RuntimeException( "Error accepting client connection", e);
            }

            try{
                String sentence = new String(request.getData()).trim();
                String[] requestArr = sentence.split(" ");
                System.out.println("===== Client: " + sentence);

                // send response to client based on client input
                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();
                keyValService(requestArr, clientAddress, clientPort);
            } catch (IOException e){
                e.printStackTrace();
                // log exception and go onto the next request;
            }
        }
    }

    private void openServerSocket(){
        try{
            SingleThreadUDPServer server = new SingleThreadUDPServer(this.port);
            System.out.println("Server is listening on port " + this.port);
            this.serverSocket.setSoTimeout(timeout_length);
        }
        catch (SocketException e){ // The socket could not be opened, or bind to the specified port or address
            System.err.println("Socket error: " + e.getMessage());
        }
    }

    private synchronized boolean isStopped(){
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        this.serverSocket.close();
    }

    private void keyValService(String[] requestArr, InetAddress clientAddress, int clientPort) throws IOException {
        if (requestArr.length < 2){
            String errMsg = "Syntax: <operation> <parameter1>. For example: get apple";
            sendResult(errMsg, clientAddress, clientPort);
            return;
        }

        String action = requestArr[0]; // get, put, delete
        String key = requestArr[1];

        switch(action) {
            case "get":
                if(store.containsKey(key)){
                    String price = store.get(key);
                    sendResult("Price of " + key + " :" + price, clientAddress, clientPort);
                }
                else{
                    String errMsg = key + " not found.";
                    sendResult(errMsg, clientAddress, clientPort);
                }
                break;
            case "delete":
                if (!store.containsKey(key)) {
                    String errMsg =  key + " not found.";
                    sendResult(errMsg, clientAddress, clientPort);
                }
                else{
                    store.remove(key);
                    sendResult("Delete " + key + " succeed.", clientAddress, clientPort);
                }
                break;
            case "put":
                if (requestArr.length == 3) {
                    store.put(key, requestArr[2]);
                    sendResult("Put [" + key + ", " + requestArr[2] + "] in store succeed.", clientAddress, clientPort);
                }
                else{
                    String errMsg = "Syntax of put: <operation> <parameter1> <parameter2>. For example: put apple 10.";
                    sendResult(errMsg, clientAddress, clientPort);
                }
                break;
            default:
                sendResult("Operation or key or value incorrect. Syntax: <operation> <parameter1>...", clientAddress, clientPort);
        }
    }

    private void sendResult(String res, InetAddress clientAddress, int clientPort) throws IOException {
        DatagramPacket response = new DatagramPacket(res.getBytes(), res.getBytes().length, clientAddress, clientPort);
        serverSocket.send(response);
    }

    public static void main(String[] args) throws SocketException {
        if (args.length < 1) {
            System.out.println("Syntax: SingleThreadUDPServer <port>");
            return;
        }

        SingleThreadUDPServer server = new SingleThreadUDPServer(Integer.parseInt(args[0]));
        new Thread(server).start();

        try{
            Thread.sleep(60*1000);
        } catch(InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("Stopping Server");
        server.stop();

    }

}
