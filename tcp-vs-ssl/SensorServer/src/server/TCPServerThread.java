package server;

import java.net.Socket;

public class TCPServerThread extends Thread {

    private Socket socket;

    public TCPServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("A connection was established with a client on " +
                socket.getRemoteSocketAddress());
        server.SensorClientHandler handler;

        try {
            handler = new server.SensorClientHandler(socket);
            handler.startSession();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}