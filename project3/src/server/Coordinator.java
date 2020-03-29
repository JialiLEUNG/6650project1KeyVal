/**
 * This project is inspired by
 * https://github.com/ronak14329/Replicated-using-2-Phase-Commit-Multi-threaded-Key-Value-Store-using-RPC.
 *
 * The Coordinator class instantiates 5 servers.
 * For each server, it does not extend UnicastRemoteObject.
 * Instead, it implements the remote interface and call UnicastRemoteObject.exportObject() to export the remote object.
 */
package server;

import keyValService.KeyValStoreInterface;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Coordinator extends Thread{
    private static ServerHelper serverHelper = new ServerHelper();
    private static Server[] servers = new Server[5];

    public static void main(String args[]) throws Exception{
        serverHelper.ParseServerArgsToPorts(args);

        for (int i = 0; i < serverHelper.serverPortNumbers.length; i++){
            try{
                // server needs to create the remote object that provides the service.
                servers[i] = new Server();
                // Additionally, the remote object must be exported to the Java RMI runtime so that it may receive incoming remote calls.
                // The static method UnicastRemoteObject.exportObject exports the supplied remote object
                // to receive incoming remote method invocations on an anonymous TCP port
                // and returns the stub for the remote object to pass to clients.
                // As a result of the exportObject call,
                // the runtime may begin to listen on a new server socket
                // or may use a shared server socket to accept incoming remote calls for the remote object.
                // The returned stub implements the same set of remote interfaces as the remote object's class
                // and contains the host name and port over which the remote object can be contacted.
                int port = serverHelper.serverPortNumbers[i];
                KeyValStoreInterface stub = (KeyValStoreInterface) UnicastRemoteObject.exportObject(servers[i], port);
                // Creates and exports a Registry instance on the local host that accepts requests on the specified port.
                Registry registry = LocateRegistry.createRegistry(port);
                registry.rebind("keyValService.KeyValStoreInterface", stub);
                registerServerInfo(serverHelper.serverPortNumbers, port);
                serverHelper.log("Server " + i + " is running at port " + port);
            } catch (RemoteException e) {
                System.err.println("server exception. " + e.getMessage());
            }
            Thread serverThread = new Thread();
            serverThread.start();
        }
    }


    /**
     * registerServerInfo does two things:
     * (1) Client looks up the object by name, obtains a remote object reference, and then invokes remote methods on the object.
     * (2) Set serverPortNumbers information by current server vs. other serverPortNumbers:
     * @param servers
     * @param port
     */
    private static void registerServerInfo(int[] servers, int port){
        try{
            Registry registry = LocateRegistry.getRegistry(port);
            KeyValStoreInterface stub = (KeyValStoreInterface) registry.lookup("keyValService.KeyValStoreInterface");

            int j = 0;
            int[] others = new int[servers.length - 1];
            for (int i = 0; i < servers.length; i++){
                if (servers[i] != port){
                    others[j] = servers[i];
                    j++;
                }
            }
            stub.setServerInfo(others, port);
        } catch (AccessException e) {
            serverHelper.log(e.getMessage());
        } catch (RemoteException e) {
            serverHelper.log(e.getMessage());
        } catch (NotBoundException e) {
            serverHelper.log(e.getMessage());
        }
    }


















}
