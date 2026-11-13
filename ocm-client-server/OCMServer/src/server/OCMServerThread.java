package server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class OCMServerThread extends Thread {


    protected BufferedReader in;
    protected PrintWriter out;
    protected Socket socket; // Thread will open a socket per client
    private String lines = "";

    public OCMServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("A connection was established with a client on the address of " + socket.getRemoteSocketAddress());
        OCMClientHandler handler = null;
        try {
            handler = new OCMClientHandler(socket);
            handler.startSession();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
