package server;

import javax.net.ssl.SSLSocket;
import java.io.*;

public class SSLServerThread extends Thread
{

    private final String SERVER_ACK_MESSAGE = "server_ack";
    private SSLSocket sslSocket;
    private String line = new String();
    private BufferedReader is;
    private PrintWriter os;
    public SSLServerThread(SSLSocket s)
    {
        sslSocket = s;
    }

    @Override
    public void run() {
        System.out.println("A connection was established with a client on " +
                sslSocket.getRemoteSocketAddress());
        server.SensorClientHandler handler = null;

        try {
            handler = new server.SensorClientHandler(sslSocket);
            handler.startSession();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
