import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * The ServerUDP demonstrates a single-threaded UDP server program.
 * The protocol to communicate packet contents for the three request types
 * along with data passed along as part of the requests (e.g. keys, values, etc.) is shown in keyValService().
 *
 */
public class SingleThreadUDPServer implements Runnable{
    /* server information */
    private DatagramSocket      serverSocket;
    private int                 port;
    protected boolean           isStopped = false; // whether the server is terminated.
    protected Thread            runningThread = null;
    private Map<String, String> store = new HashMap<>();

    /* constructor for server */
    public SingleThreadUDPServer(int port){
        this.port = port;
    }


    public void run(){
        // only one thread can access the resource at a given point of time
        synchronized (this){
            // Thread.currentThread() returns a reference to the currently executing thread object.
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();


        // The following while loop does the following:
        // 1. wait for a client request
        // 2. process client request
        // 3. repeat 1.
        while(!isStopped()){
            DatagramPacket request;
            try {
                // wait for a request from client
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
                // display the requests received, and its responses
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
        stop();
    }

    /**
     * openServerSocket() opens the server socket for listening on port specified by the user.
     */
    private void openServerSocket(){
        try{
            // The datagramSocket creates a server that binds to the specific port number
            // so the clients know how to connect to.
            serverSocket = new DatagramSocket(this.port);
            System.out.println("UDP Server is listening on port " + this.port);
        }
        catch (SocketException e){
            // The socket could not be opened, or bind to the specified port or address
            System.err.println("Socket error: " + e.getMessage());
        }
    }

    /**
     * isStopped() checks if the server is running.
     * @return boolean true for running, and false for not running.
     */
    private synchronized boolean isStopped(){
        return this.isStopped;
    }

    /**
     * stop() update server's status of running or not running.
     */
    public synchronized void stop(){
        this.isStopped = true;
        try{
            this.serverSocket.close();
        } catch(Exception e){
            throw new RuntimeException("Error closing server", e);
        }
    }


    /**
     * keyValService() implements protocol for client's request:
     * Client should follow the format: <operation> <key> for get and delete.
     * For example, get apple, delete apple.
     * Client should follow the format: <operation> <parameter> <parameter> for put.
     * For example, put apple 10.
     * @param requestArr String[] client's request sentence.
     * @param clientAddress InetAddress client's IP address.
     * @param clientPort int client's port.
     * @throws IOException
     */
    private void keyValService(String[] requestArr, InetAddress clientAddress, int clientPort) throws IOException {
        if (requestArr.length < 2){
            String errMsg = "Malformed Request from [IP: " + clientAddress + ", Port: "+ clientPort + "]." +
                    " Syntax: <operation> <key> OR <operation> <key> <value>. For example: get apple";
            System.err.println(errMsg + "at time " + System.currentTimeMillis());
            sendResult(errMsg, clientAddress, clientPort);
            return;
        }

        String action = requestArr[0]; // get, put, delete
        String key = requestArr[1];

        switch(action.toLowerCase()) { // normalize operation to lowercase.
            case "get":
                if(store.containsKey(key)){
                    String price = store.get(key);
                    sendResult("Price of " + key + ": " + price + " at time " + System.currentTimeMillis(), clientAddress, clientPort);
                }
                else{
                    String errMsg = key + " not found. Malformed Request from [IP: " + clientAddress + ", " +
                            "Port: " + clientPort + "].";
                    System.err.println(errMsg + " at time " + System.currentTimeMillis());
                    sendResult(errMsg, clientAddress, clientPort);
                }
                break;
            case "delete":
                if (!store.containsKey(key)) {
                    String errMsg =  key + " not found. Malformed Request from [IP: " + clientAddress + ", Port: " + clientPort + "].";
                    System.err.println(errMsg + " at time " + System.currentTimeMillis());
                    sendResult(errMsg, clientAddress, clientPort);
                }
                else{
                    store.remove(key);
                    sendResult("Delete " + key + " succeed. " + "at time " + System.currentTimeMillis(), clientAddress, clientPort);
                }
                break;
            case "put":
                if (requestArr.length == 3) {
                    if (isNumeric(requestArr[2])){
                        store.put(key, requestArr[2]);
                        sendResult("Put [" + key + ", " + requestArr[2] + "] in store succeed. " + "at time " + System.currentTimeMillis(), clientAddress, clientPort);
                    }
                    else{
                        String errMsg = "Value should be numeric.";
                        sendResult(errMsg, clientAddress, clientPort);
                    }
                }
                else{
                    String errMsg = "Malformed Request from [IP: " + clientAddress + ", Port: "+ clientPort + "]." +
                            "Syntax of put: <operation> <key> <value>. For example: put apple 10.";
                    System.err.println(errMsg + " at time " + System.currentTimeMillis());
                    sendResult(errMsg, clientAddress, clientPort);
                }
                break;
            default:
                String errMsg = "Malformed Request from [IP: " + clientAddress + ", Port: "+ clientPort + " ]. " +
                        "Syntax: <operation> <key>...";
                System.err.println(errMsg + "at time " + System.currentTimeMillis());
                sendResult(errMsg, clientAddress, clientPort);
        }
    }

    private void sendResult(String res, InetAddress clientAddress, int clientPort) throws IOException {
        DatagramPacket response = new DatagramPacket(res.getBytes(), res.getBytes().length, clientAddress, clientPort);
        serverSocket.send(response);
    }

    /**
     * isNumeric() checks if user request of "PUT" contains numeric value.
     * For example, "put apple 0" is valid, whereas "put apple zero" is invalid.
     * @param strNum
     * @return
     */
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws SocketException {
        if (args.length < 1) {
            System.out.println("Syntax: SingleThreadUDPServer <port>");
            return;
        }
        SingleThreadUDPServer server;
        try{
            server = new SingleThreadUDPServer(Integer.parseInt(args[0]));
            new Thread(server).start();
        } catch (Exception e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
}