package server;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerHelper {
    public int[] serverPortNumbers = new int[5];

    public void ParseServerArgsToPorts(String[] args) throws Exception{
        if (args.length < 5){
            throw new IllegalArgumentException("Five ports needed for serverPortNumbers.");
        }
        for (int i = 0; i < args.length; i++){
            serverPortNumbers[i] = Integer.parseInt(args[i]);
        }
    }

    public static String currentTime(){
        return " at time: " + new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z").format(new Date());
    }

    public static void log(String msg){
        System.out.println(msg + currentTime());
    }

}


