import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The ServerUDP demonstrates a UDP server program.
 *
 */
public class ServerUDP {
    private DatagramSocket socket;
    private List<String> listReceived = new ArrayList<String>();
    private Random random;

    public ServerUDP(int port) throws SocketException{
        socket = new DatagramSocket(port);
    }

}
