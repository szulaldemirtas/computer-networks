package server;

import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    private ServerSocket tcpServerSocket;
    public static final int port = 8000;

    public TCPServer() {
        try {
            tcpServerSocket = new ServerSocket(port);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        TCPServer server = new TCPServer();
        System.out.println("TCP server is running on port " + port);
        server.listenAndAccept();
    }

    private void listenAndAccept() {
        while (tcpServerSocket != null && !tcpServerSocket.isClosed()) {
            try {
                Socket clientSocket = tcpServerSocket.accept();
                TCPServerThread st = new TCPServerThread(clientSocket);
                st.start();
            } catch (Exception e) {
                System.err.println(e.getMessage());

                if (tcpServerSocket == null || tcpServerSocket.isClosed()) break;
            }
        }
    }
}