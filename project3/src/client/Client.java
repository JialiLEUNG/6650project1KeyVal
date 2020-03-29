/**
 * Client first obtains the stub for the registry by invoking the static LocateRegistry.getRegistry method
 * with the hostname specified on the command line. If no hostname is specified,
 * then null is used as the hostname indicating that the local host address should be used.
 * Next, the client invokes the remote method lookup on the registry stub
 * to obtain the stub for the remote object from the server's registry.
 * Finally, the client invokes the method (as specified in the txt file) on the remote object's stub,
 * which causes the following actions to happen:
 * The client-side runtime opens a connection to the server
 * using the host and port information in the remote object's stub and then serializes the call data.
 * The server-side runtime accepts the incoming call, dispatches the call to the remote object,
 * and serializes the result (the reply string "Hello, world!") to the client.
 * The client-side runtime receives, deserializes, and returns the result to the caller.
 */
package client;
import keyValService.KeyValStoreInterface;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.UUID;


public class Client {

    public static void main(String[] args) throws Exception{

        ClientHelper clientHelper = new ClientHelper();
        clientHelper.setServerPorts(args);
        clientHelper.ParseClientArgsToPorts(args);
        int numOfPorts = clientHelper.serverPorts.length;
        KeyValStoreInterface[] stubs = new KeyValStoreInterface[numOfPorts];
        Registry[] registries = new Registry[numOfPorts];

        try{
            for (int i = 0; i < numOfPorts; i++){
                registries[i] = LocateRegistry.getRegistry("Localhost", clientHelper.serverPorts[i]);
                stubs[i] = (KeyValStoreInterface) registries[i].lookup("keyValService.KeyValStoreInterface");
            }
            for (int i = 0; i < numOfPorts; i++){
                Scanner clientScanner = new Scanner(new File(args[0])); // "./src/client/ClientRequest.txt"

                while(clientScanner.hasNext()){
                    String[] requestArr = clientScanner.nextLine().trim().split(" ");

                    String msg;
                    if (requestArr.length < 2){
                        msg = "----- Error: At least 2 argument needed. Syntax: <operation> <key> OR <operation> <key> <value>. For example: get apple";
                        clientHelper.log(msg);
                    } else if(requestArr.length == 2) {
                        clientHelper.log(stubs[i].clientInputKeyValue(UUID.randomUUID(), requestArr[0], requestArr[1], ""));
                    } else if (requestArr.length == 3) {
                        clientHelper.log(stubs[i].clientInputKeyValue(UUID.randomUUID(), requestArr[0], requestArr[1], requestArr[2]));
                    }
                }
            }
        } catch(Exception e){
            clientHelper.log(e.getMessage());
        }
    }
}
