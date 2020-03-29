/**
 * The Server class implements the remote interface KeyValStoreInterface.
 *
 */
package server;

import keyValService.KeyValStoreInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


public class Server extends Thread implements KeyValStoreInterface {

    private static ServerHelper serverHelper = new ServerHelper();
    private int[] otherServers = new int[4];
    private int myPort;
    private ConcurrentHashMap<UUID, KeyValOp> pendingRequests = new ConcurrentHashMap(new HashMap<UUID, KeyValOp>());
    private ConcurrentHashMap<UUID, Map<Integer, ACK>> pendingPrepareAcks = new ConcurrentHashMap(new HashMap<UUID,Map<Integer,ACK>>());
    private ConcurrentHashMap<UUID, Map<Integer, ACK>> pendingGoAcks = new ConcurrentHashMap(new HashMap<UUID,Map<Integer,ACK>>());
    private ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    ReadWriteLock rwl = new ReadWriteLock();

    /**
     * clientInputKeyValue takes unique messageID, operation, and key-value pair sent by the user.
     * If the operation is a "get", then returns the result of the get request (if key does not exist, error msg pops up)
     * If the operation is a "put" or "delete", do the following:
     * (1) Master server adds the unique messageID, key-value-operation instance (kvop) into the pendingRequests hashmap in the current server,
     * in which the messageID is the key, and the kvop is the value.
     * (2) Master server tells the replica servers to prepare ACK
     * (3) after receiving Yes ACk, Master server tells to go (commit)
     * (4) replica commit.
     *
     * @param messageID a unique identifier for a client's request.
     * @param op operation (get, put, delete)
     * @param key
     * @param value
     * @return
     */
    public String clientInputKeyValue(UUID messageID, String op, String key, String value){
        if(op.equalsIgnoreCase("get")){
            return ServerOutputKeyValue(op, key, value);
        }
        addToTempStorage(messageID, op, key, value);
        tellToPrepare(messageID, op, key, value);
        boolean prepareOK = collectAckPrepare(messageID, op, key, value);
        if (!prepareOK){
            return "Tell to prepare fail. Key-value-operation aborted... ";
        }
        tellToGo(messageID);
        boolean goOK = collectAckGo(messageID);
        if (!goOK){
            return "Tell to go fail. Key-value-operation aborted... ";
        }

        KeyValOp kvOp = this.pendingRequests.get(messageID);
        if (kvOp == null){
            throw new IllegalArgumentException("Error: the message is not in the temporary storage.");
        }
        // ask the current server to "put" or "delete" as requested.
        String msg = this.ServerOutputKeyValue(kvOp.operation, kvOp.key, kvOp.value);
        this.pendingRequests.remove(messageID);
        return msg;
    }

    private String ServerOutputKeyValue(String op, String key, String value){
        String msg;
            switch(op){
                case "get":
                    msg = get(key);
                    return msg;
                case "delete":
                    msg = delete(key);
                    return msg;
                case "put":
                    msg = put(key, value);
                    return msg;
                default:
                    msg = "----- Error: Unknown operation";
                    return msg;
            }
    }



    private String get(String key) {
        serverHelper.log("GET key: " + key + " from client in Master server: " + myPort);
        String msg = "";
        try{
            rwl.lockRead();

            String value = store.get(key);

            if (value != null){
                // we get as many value objects as we need to return to the client
                // (in this case, we select them from the service’s value collection
                // based on whether they’re inside our request getRequest),
                // and write them each in turn to the response observer using its onNext() method.

                msg = "+++++ Succeed: GET key: " + key + ". Value is: " + value; // msg logged to client
                serverHelper.log("+++++ Succeed: GET key: "+ key + "and value: " + value + "at Server " + myPort); // msg logged to server
            } else{
                msg = "----- Fail: GET. Key does not exist.";
                serverHelper.log("----- Fail: Key does not exist. GET request fail " + myPort);
            }
            rwl.unlockRead();
        } catch (InterruptedException e) {
            serverHelper.log(e.getMessage());
        } return msg;
    }

    private String put(String key, String value){
        String msg = "";
        try{
//            serverHelper.log("PUT key: " + key + " and value: " + value + " from client in Master server: " + myPort);
            rwl.lockWrite();

            String result = store.putIfAbsent(key, value);

            if(result == null){
                msg = "+++++ Succeed: PUT key: " + key + " and value: " + value;
                serverHelper.log("+++++ Succeed: PUT key: "+ key + "and value: " + value + "at Server " + myPort);

            } else{
                msg = "----- Fail: Key already exists. PUT request fail";
                serverHelper.log("----- Fail: Key already exists. PUT request fail at Server " + myPort);
            }
            rwl.unlockWrite();
        } catch (Exception e){
            serverHelper.log(e.getMessage());
        } return msg;
    }

    private String delete(String key){
        String msg = "";
        try{
//            serverHelper.log("DELETE key: " + key + " from client in Master server " + myPort);
            rwl.lockWrite();

            String result = store.remove(key);
            if (result != null){
                msg = "+++++ Succeed: DELETE key: " + key;
                serverHelper.log("+++++ Succeed: DELETE key: "+ key + "at Server " + myPort);
            } else{
                msg = "----- Fail: Key does not exist. DELETE request fail";
                serverHelper.log("----- Fail: Key does not exist. DELETE request fail " + myPort);
            }
            rwl.unlockWrite();
        }catch (Exception e){
            serverHelper.log(e.getMessage());
        }
        return msg;
    }



    @Override
    public void ackMe(UUID messageID, int callBackServer, ACKType type) throws RemoteException {

        if (type == ACKType.AckGo){
            this.pendingGoAcks.get(messageID).get(callBackServer).isAcked = true;
            serverHelper.log("GO ACK sent from callBackServer: " + callBackServer);
        }
        else if (type == ACKType.AckPrepare){
            this.pendingPrepareAcks.get(messageID).get(callBackServer).isAcked = true;
            serverHelper.log("PREPARE ACK sent from callBackServer: " + callBackServer);
        }

    }

    @Override
    public void go(UUID messageID, int callBackServer) throws RemoteException {
        KeyValOp kvOp = this.pendingRequests.get(messageID);
        if (kvOp == null){
            throw new IllegalArgumentException("The message is not in the temporary storage.");
        }
        this.ServerOutputKeyValue(kvOp.operation, kvOp.key, kvOp.value);
        this.pendingRequests.remove(messageID);
        this.sendAck(messageID, callBackServer, ACKType.AckGo);
    }

    @Override
    public void prepareKeyValue(UUID messageID, String op, String key, String value, int callBackServer) throws RemoteException {
        if(this.pendingRequests.containsKey(messageID)){
            sendAck(messageID, callBackServer, ACKType.AckPrepare);
        }
        this.addToTempStorage(messageID, op, key, value);
        sendAck(messageID, callBackServer, ACKType.AckPrepare);
    }

    @Override
    public void setServerInfo(int[] otherServerPorts, int yourPorts) throws RemoteException {
        this.otherServers = otherServerPorts;
        this.myPort = yourPorts;

    }

    private void addToTempStorage(UUID messageID, String op, String key, String value){
        KeyValOp kvOp = new KeyValOp();
        kvOp.operation = op;
        kvOp.key = key;
        kvOp.value = value;
        this.pendingRequests.put(messageID, kvOp);
    }

    /**
     * tell the replica servers to prepare ACK
     * @param messageID
     * @param op
     * @param key
     * @param value
     */
    private void tellToPrepare(UUID messageID, String op, String key, String value){
        this.pendingPrepareAcks.put(messageID, new ConcurrentHashMap(new HashMap<Integer, ACK>()));
        for(int server : this.otherServers){
            callPrepare(messageID, op, key, value, server);
        }
    }

    /**
     *
     * @param messageID
     * @param op
     * @param key
     * @param value
     * @param server
     */
    private void callPrepare(UUID messageID, String op, String key, String value, int server){
        try{
            ACK a = new ACK();
            a.isAcked = false;
            this.pendingPrepareAcks.get(messageID).put(server, a);
            Registry registry = LocateRegistry.getRegistry(server);
            KeyValStoreInterface stub = (KeyValStoreInterface) registry.lookup("keyValService.KeyValStoreInterface");
            stub.prepareKeyValue(messageID, op, key, value, myPort);
        }catch (Exception e){
            serverHelper.log("Send ACK fail, removing data from temporary storage.");
        }
        serverHelper.log("Call prepare succeed. Target server: " + server);

    }

    private boolean collectAckPrepare(UUID messageID, String op, String key, String value){
        int areAllAck = 0;
        int retry = 3;

        while(retry != 0){
            pauseThread();
            areAllAck = 0;
            retry--;
            Map<Integer, ACK> map = this.pendingPrepareAcks.get(messageID);
            for(int server : this.otherServers){
                if(map.get(server).isAcked){
                    areAllAck++;
                }
                else{
                    callPrepare(messageID, op, key, value, server);
                }
            }
            if (areAllAck == 4){
                return true;
            }
        }
        return false;
    }

    private void tellToGo(UUID messageID){
        this.pendingGoAcks.put(messageID, new ConcurrentHashMap(new HashMap<Integer, ACK>()));
        for (int server : this.otherServers){
            callGo(messageID, server);
        }
    }

    private void callGo(UUID messageID, int server){
        try{
            ACK a = new ACK();
            a.isAcked = false;
            this.pendingGoAcks.get(messageID).put(server, a);
            Registry registry = LocateRegistry.getRegistry(server);
            KeyValStoreInterface stub = (KeyValStoreInterface) registry.lookup("keyValService.KeyValStoreInterface");
            stub.go(messageID, myPort);
        }catch (Exception e){
            serverHelper.log("Send go fail, removing data from temporary storage");
        }
        serverHelper.log("Call go succeed. Target server: " + server);
    }

    private boolean collectAckGo(UUID messageID){
        int areAllAck = 0;
        int attemps = 3;

        while(attemps != 0){
            pauseThread();

            areAllAck = 0;
            attemps--;
            Map<Integer, ACK> map = this.pendingGoAcks.get(messageID);

            for (int server : this.otherServers){
                if(map.get(server).isAcked){
                    areAllAck++;
                }
                else{
                    callGo(messageID, server);
                }
            }
            if (areAllAck == 4){
                return true;
            }
        }
        return false;
    }

    private void sendAck(UUID messageID, int otherServerDestination, ACKType type){
        try{
            Registry registry = LocateRegistry.getRegistry(otherServerDestination);
            KeyValStoreInterface stub = (KeyValStoreInterface) registry.lookup("keyValService.KeyValStoreInterface");
            stub.ackMe(messageID, myPort, type);
        }catch (Exception e){
            serverHelper.log("Send ACK fail, removing data from temporary storage");
            this.pendingRequests.remove(messageID);
        }
        serverHelper.log("Send ACK succeed.");
    }


    private static void pauseThread(){
        try{
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(50, 100));
        } catch(InterruptedException e){
            serverHelper.log("Thread sleeping fail");
            Thread.currentThread().interrupt();
            throw new RuntimeException();
        }
    }
}

class ACK{
    public boolean isAcked;
}

class KeyValOp{
    String operation;
    String key;
    String value;
}
