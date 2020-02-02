/**
 * The SingleThreadServer implements a single-threaded server.
 * The server offers key-value storing and checking service to clients.
 * It includes timeout handling to prevent server threads from blocking if a client is stalled
 *
 * Run the client by typing "java SingleThreadTCPServer.java 32000".
 *
 * What separates the single threaded server from a multithreaded server is that
 * the single threaded server processes the incoming requests in the same thread that
 * accepts the client connection.
 * A multithreaded server passes the connection on to a worker thread that processes the request.
 *
 */

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

public class SingleThreadTCPServer implements Runnable{

    /* Server information */
    protected int           port;
    protected ServerSocket  serverSocket = null;
    protected boolean       isStopped = false; // whether the server is terminated.
    protected Thread        runningThread = null;
//    private static final int timeout_length = 60000; // 1 minute
    private Map<String, String> store = new HashMap<>();

    // constructor
    public SingleThreadTCPServer(int port){
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
            Socket clientSocket = null;
            try{
                // listen for connection
                clientSocket = this.serverSocket.accept();
                System.out.println("New client connected...");
            } catch (IOException e){
                if (isStopped()){
                    System.out.println("Server Stopped.");
                    return;
                }
                throw new RuntimeException( "Error accepting client connection", e);
            }
            try{
                System.out.println("Server starts to process request...");
                processClientRequest(clientSocket);
            } catch (Exception e){
                e.printStackTrace();
                // log exception and go onto the next request;
            }
        }
        System.out.println("Server Stopped.");
        stop();
    }

    private void processClientRequest(Socket clientSocket) throws Exception{
        // once client is connected, use socket stream to send a prompt to client
        OutputStream output = clientSocket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        // Prompt for client to enter something.
        writer.println("Please type your request and enter: \n");

        // Create a InputStream  and BufferedReader for reading from socket
        InputStream input = clientSocket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader((input)));
        while(clientSocket.isConnected()){
            String res = reader.readLine().trim();
            System.out.println("===== Client: " + res); // print message from client.

            // echo client message to client.
            InetAddress clientAddress = clientSocket.getInetAddress();
            int clientPort = clientSocket.getPort();
            String[] requestArr = res.split(" ");
            keyValService(writer, requestArr, clientAddress, clientPort);
        }
    }

    /**
     * keyValService() implements protocol for client's request:
     * Client should follow the format: <operation> <key> for get and delete.
     * For example, get apple, delete apple.
     * Client should follow the format: <operation> <parameter> <parameter> for put.
     * For example, put apple 10.
     * @param writer socket stream for sending message to client.
     * @param requestArr String[] client's request sentence.
     * @param clientAddress InetAddress client's IP address.
     * @param clientPort int client's port.
     * @throws IOException
     */
    private void keyValService(PrintWriter writer, String[] requestArr, InetAddress clientAddress, int clientPort) throws IOException {
        if (requestArr.length < 2){
            String errMsg = "Malformed Request from [IP: " + clientAddress + ", Port: "+ clientPort + "]." +
                    " Syntax: <operation> <key> OR <operation> <key> <value>. For example: get apple";
            System.err.println(errMsg + "at time " + System.currentTimeMillis());
            writer.println(errMsg + " at time: " + System.currentTimeMillis());
            return;
        }

        String action = requestArr[0]; // get, put, delete
        String key = requestArr[1];

        switch(action.toLowerCase()) { // normalize operation to lowercase.
            case "get":
                if(store.containsKey(key)){
                    String price = store.get(key);
                    writer.println("Price of " + key + ": " + price + " at time " + System.currentTimeMillis());
                }
                else{
                    String errMsg = key + " not found. Malformed Request from [IP: " + clientAddress + ", " +
                            "Port: " + clientPort + "].";
                    System.err.println(errMsg + " at time " + System.currentTimeMillis());
                    writer.println(errMsg + " at time: " + System.currentTimeMillis());
                }
                break;
            case "delete":
                if (!store.containsKey(key)) {
                    String errMsg =  key + " not found. Malformed Request from [IP: " + clientAddress + ", Port: " + clientPort + "].";
                    System.err.println(errMsg + " at time " + System.currentTimeMillis());
                    writer.println(errMsg + " at time: " + System.currentTimeMillis());
                }
                else{
                    store.remove(key);
                    writer.println("Delete " + key + " succeed. " + "at time " + System.currentTimeMillis());
                }
                break;
            case "put":
                if (requestArr.length == 3) {
                    if (isNumeric(requestArr[2])){
                        store.put(key, requestArr[2]);
                        writer.println("Put [" + key + ", " + requestArr[2] + "] in store succeed. " + "at time " + System.currentTimeMillis());
                    }
                    else{
                        String errMsg = "Value should be numeric.";
                        writer.println(errMsg + " at time: " + System.currentTimeMillis());
                    }
                }
                else{
                    String errMsg = "Malformed Request from [IP: " + clientAddress + ", Port: "+ clientPort + "]." +
                            "Syntax of put: <operation> <key> <value>. For example: put apple 10.";
                    System.err.println(errMsg + " at time " + System.currentTimeMillis());
                    writer.println(errMsg + " at time: " + System.currentTimeMillis());
                }
                break;
            default:
                String errMsg = "Malformed Request from [IP: " + clientAddress + ", Port: "+ clientPort + " ]. " +
                        "Syntax: <operation> <key>...";
                System.err.println(errMsg + "at time " + System.currentTimeMillis());
                writer.println(errMsg + " at time: " + System.currentTimeMillis());
        }
    }

    private synchronized boolean isStopped(){
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try{
            this.serverSocket.close();
        } catch(IOException e){
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket(){
        try{
            this.serverSocket = new ServerSocket(this.port);
            System.out.println("TCP Server is listening on port " + port);
            // set timeout to prevent stalled connections.
//            this.serverSocket.setSoTimeout(timeout_length);
        } catch (SocketTimeoutException e){
            System.err.println("No data arrives within 5s. " + e.getMessage());

        } catch (IOException e){
            throw new RuntimeException("Cannot open port " + port, e);
        }
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

    public static void main(String args[]) throws Exception{
        if (args.length < 1) {
            System.out.println("Syntax: SingleThreadTCPServer <port>");
            return;
        }

        try{
            SingleThreadTCPServer server;
            server = new SingleThreadTCPServer(Integer.parseInt(args[0]));
            new Thread(server).start();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
