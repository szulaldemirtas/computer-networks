package server;

import java.net.ServerSocket;
import java.net.Socket;

public class OCMServer {

    private ServerSocket serverSocket;
    int port;

    public OCMServer(int port) {
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            OCMServer server = new OCMServer(port);
            server.listenAndAccept();
        } catch (Exception e) {
            System.err.println("Please enter a valid port.");
        }
    }

    private void listenAndAccept()
    {
        System.out.println("OCM Server started on port: " + port);
        while (serverSocket != null && !serverSocket.isClosed()) try {
            Socket clientSocket = serverSocket.accept();
            OCMServerThread st = new OCMServerThread(clientSocket);
            st.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());

            if (serverSocket == null || serverSocket.isClosed()) {
                break;
            }
        }
    }
}
