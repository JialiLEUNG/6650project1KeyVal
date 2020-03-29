/**
 * keyValService is an interface which will provide the description of the methods
 * that can be invoked by remote clients.
 * This interface should extend the Remote interface
 * and the method prototype within the interface should throw the RemoteException.
 */
package keyValService;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;
import server.ACKType;

public interface KeyValStoreInterface extends Remote {
    // declare the methods prototypes.
    String clientInputKeyValue(UUID messageID, String op, String key, String value) throws RemoteException;
    void ackMe(UUID messageID, int callBackServer, ACKType type) throws RemoteException;
    void go(UUID messageID, int callBackServer) throws RemoteException;
    void prepareKeyValue(UUID messageID, String op, String key, String value, int callBackServer) throws RemoteException;
    void setServerInfo(int[] otherServerPorts, int yourPorts) throws RemoteException;
}
