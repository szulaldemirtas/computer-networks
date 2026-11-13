package server;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

public class SSLServer{

    private SSLServerSocket sslServerSocket;
    private SSLServerSocketFactory sslServerSocketFactory;
        private final String SERVER_KEYSTORE_FILE = "keystore.jks";
    private final String SERVER_KEYSTORE_PASSWORD = "aa12345";
    private final String SERVER_KEY_PASSWORD = "aa12345";
    public static final int port = 8001;

    public SSLServer() {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            char ksPass[] = SERVER_KEYSTORE_PASSWORD.toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(SERVER_KEYSTORE_FILE), ksPass);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, SERVER_KEY_PASSWORD.toCharArray());
            sc.init(kmf.getKeyManagers(), null, null);

            sslServerSocketFactory = sc.getServerSocketFactory();
            sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        SSLServer server = new SSLServer();
        System.out.println("SSL server is running on port " + port);
        server.listenAndAccept();
    }

    private void listenAndAccept() {
        while (sslServerSocket != null && !sslServerSocket.isClosed()) {
            try {
                SSLSocket s = (SSLSocket) sslServerSocket.accept();
                System.out.println("SSL connection from " + s.getRemoteSocketAddress());
                SSLServerThread st = new SSLServerThread(s);
                st.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
