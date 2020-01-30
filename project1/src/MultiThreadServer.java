/**
 * The MultiThreadServer implements a Multithreaded server in Java.
 * The server offers an echo service to multiple clients.
 * It includes timeout handling to prevent server threads from blocking if a client is stalled
 *
 * What separates the single threaded server from a multithreaded server is that
 * the single threaded server processes the incoming requests in the same thread that
 * accepts the client connection.
 * A multithreaded server passes the connection on to a worker thread that processes the request.
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadServer implements Runnable{
    /* Server information */
    protected int           port = 32000;
    protected ServerSocket  serverSocket = null;
    protected boolean       isStopped = false; // whether the server is terminated.
    protected Thread        runningThread = null;
    protected int           clientNo = 0;

    // constructor
    public MultiThreadServer(int port){
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
                clientSocket = this.serverSocket.accept();
                System.out.println("Client " + clientNo + " connected...");
            } catch (IOException e){
                if (isStopped()){
                    System.out.println("Server Stopped.");
                    return;
                }
                throw new RuntimeException( "Error accepting client connection", e);
            }

            // Compared to SingleThreadedServer,
            // Rather than processing the incoming requests in the same thread that
            // accepts the client connection, the connection is handed off to a worker thread.
            // That way, the main thread listens for incoming requests and
            // spends as much as possible in the serverSocket.accept() call.
            // The risk of clients being denied access to the server is minimized
            // because the listening thread (main thread) is not inside the accept() call.
            new Thread(
                    new WorkerRunnable(
                            clientSocket, clientNo)).start();
            clientNo++; // count the number of clients coming in.
        }
        System.out.println("Server Stopped.");
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
            System.out.println("Server is listening on port " + port);
        } catch (IOException e){
            throw new RuntimeException("Cannot open port" + port, e);
        }
    }

    public class WorkerRunnable implements Runnable{
        protected Socket clientSocket = null;
        protected int clientNo;

        public WorkerRunnable(Socket clientSocket, int clientNo){
            this.clientSocket = clientSocket;
            this.clientNo = clientNo;
        }

        public void run(){
            try{
                processClientRequest(clientSocket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void processClientRequest(Socket clientSocket) throws Exception{
            OutputStream output = clientSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println("Please type something and press Enter: \n");


            InputStream input = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader((input)));
            while(clientSocket.isConnected()){
                String res = reader.readLine().trim();
                System.out.println("===== Client " + clientNo + ": " + res); // print message from client.

                // echo client message to client.
                writer.println("Request processed at: " + new java.util.Date() + "." + clientNo + " typed:" + res + " \n");
            }
        }
    }

    public static void main(String args[]) throws Exception{
        MultiThreadServer server = new MultiThreadServer(32000);
        new Thread(server).start();

        try{
            Thread.sleep(1000*1000);
        } catch(InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("Stopping Server");
        server.stop();
    }
}
