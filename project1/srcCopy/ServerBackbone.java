import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerBackbone {

    public static void main(String args[]) throws Exception{
        System.out.println("Server is Running...");
        ServerSocket ss = new ServerSocket(32000);

        while(true){
            Socket s = ss.accept();

            BufferedReader reader = new BufferedReader(new InputStreamReader((s.getInputStream())));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

            writer.write("*** Welcome to the Server ***\r\n");
            writer.write("*** Please type in the 1st number and press Enter : \n");
            writer.flush();

            String data1 = reader.readLine().trim();

            writer.write("*** Please type in the 2nd number and press Enter : \n");
            writer.flush();

            String data2 = reader.readLine().trim();

            int num1 = Integer.parseInt(data1);
            int num2 = Integer.parseInt(data2);

            int res = num1 + num2;
            System.out.println("Addition operation done.");

            writer.write("\r\n === Result is:" + res);
            writer.flush();

            s.close();

        }
    }
}
