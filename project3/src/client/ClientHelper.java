package client;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHelper {
    public int[] serverPorts;

    public void ParseClientArgsToPorts(String[] args) throws Exception{
        if (args[0].equalsIgnoreCase("quit")){
            System.out.println("Client exits.");
            System.exit(serverPorts[0]);
        }
        else{
            if (args.length < 2){
                String msg = "At least 2 arguments needed. Please specified as follows: [client request file path] [port1] [port2] ....\n";
                throw new IllegalArgumentException(msg);
            }
            for (int i = 0; i < args.length - 1; i++){
                serverPorts[i] = Integer.parseInt(args[i]);
            }
        }
    }

    public void setServerPorts(String[] args){
        serverPorts = new int[args.length - 1];
    }

    public static String currentTime(){
        return " at time: " + new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z").format(new Date());
    }

    public void log(String msg){
        System.out.println(msg + currentTime());
    }
}
