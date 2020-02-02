///**
// * The SingleThreadServer implements a single-threaded server in Java.
// * The server offers an echo service to clients.
// * It includes timeout handling to prevent server threads from blocking if a client is stalled
// *
// * What separates the single threaded server from a multithreaded server is that
// * the single threaded server processes the incoming requests in the same thread that
// * accepts the client connection.
// * A multithreaded server passes the connection on to a worker thread that processes the request.
// *
// * Type "Exit" to exit the program.
// */
//
//import java.io.*;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.net.SocketTimeoutException;
//
//public class SingleThreadServer implements Runnable{
//
//    /* Server information */
//    protected int           port = 32000;
//    protected ServerSocket  serverSocket = null;
//    protected boolean       isStopped = false; // whether the server is terminated.
//    protected Thread        runningThread = null;
//    private static final int timeout_length = 60000; // 1 minute
//
//    // constructor
//    public SingleThreadServer(int port){
//        this.port = port;
//    }
//
//    public void run(){
//        // only one thread can access the resource at a given point of time
//        synchronized (this){
//            // Thread.currentThread() returns a reference to the currently executing thread object.
//            this.runningThread = Thread.currentThread();
//        }
//        openServerSocket();
//
//        // The following while loop does the following:
//        // 1. wait for a client request
//        // 2. process client request
//        // 3. repeat 1.
//        while(!isStopped()){
//            Socket clientSocket = null;
//            try{
//                // listen for connection
//                clientSocket = this.serverSocket.accept();
//                System.out.println("New client connected...");
//            } catch (IOException e){
//                if (isStopped()){
//                    System.out.println("Server Stopped.");
//                    return;
//                }
//                throw new RuntimeException( "Error accepting client connection", e);
//            }
//            try{
//                System.out.println("Server starts to process request...");
//
//                processClientRequest(clientSocket);
//            } catch (Exception e){
//                e.printStackTrace();
//                // log exception and go onto the next request;
//            }
//        }
//        System.out.println("Server Stopped.");
//    }
//
//    private void processClientRequest(Socket clientSocket) throws Exception{
//        // once client is connected, use socket stream to send a prompt message to client
//        OutputStream output = clientSocket.getOutputStream();
//        PrintWriter writer = new PrintWriter(output, true);
//        // Prompt for client to enter something.
//        writer.println("Please type something and press Enter: \n");
//
//        // Create a InputStream  and BufferedReader for reading from socket
//        InputStream input = clientSocket.getInputStream();
//        BufferedReader reader = new BufferedReader(new InputStreamReader((input)));
//        while(clientSocket.isConnected()){
//            String res = reader.readLine().trim();
//            System.out.println("===== Client: " + res); // print message from client.
//
//            // echo client message to client.
//            writer.println("Request processed at: " + new java.util.Date() + ". You typed:" + res + " \n");
//        }
//    }
//
//    private synchronized boolean isStopped(){
//        return this.isStopped;
//    }
//
//    public synchronized void stop(){
//        this.isStopped = true;
//        try{
//            this.serverSocket.close();
//        } catch(IOException e){
//            throw new RuntimeException("Error closing server", e);
//        }
//    }
//
//    private void openServerSocket(){
//        try{
//            this.serverSocket = new ServerSocket(this.port);
//            System.out.println("Server is listening on port " + port);
//            // set timeout to prevent stalled connections.
//            this.serverSocket.setSoTimeout(timeout_length);
//        } catch (SocketTimeoutException e){
//            System.err.println("No data arrives within 5s. " + e.getMessage());
//
//        } catch (IOException e){
//            throw new RuntimeException("Cannot open port " + port, e);
//        }
//    }
//
//    public static void main(String args[]) throws Exception{
//        SingleThreadServer server = new SingleThreadServer(32000);
//        new Thread(server).start();
//
//        try{
//            Thread.sleep(60*1000);
//        } catch(InterruptedException e){
//            e.printStackTrace();
//        }
//        System.out.println("Stopping Server");
//        server.stop();
//    }
//}
