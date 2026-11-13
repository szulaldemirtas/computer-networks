import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SensorClient {

    private Socket socket;

    String host = "localhost";
    int tcpPort = 8000;
    int sslPort = 8001;

    PrintWriter out;
    BufferedReader in;
    BufferedReader stdIn;

    public SensorClient() {
        stdIn = new BufferedReader(new InputStreamReader(System.in));
    }

    public static void main(String[] args)  {
        new SensorClient().processClientRequests();
    }

    public void processClientRequests() {
        try {
            System.out.println("Select mode:");
            System.out.println("1 - Normal mode");
            System.out.println("2 - Benchmark mode");
            System.out.print("> ");
            String mode = stdIn.readLine().trim();

            if (mode.equals("2")) {
                Benchmark benchmark = new Benchmark();
                benchmark.runBenchmark();
                return;
            }

            System.out.println("Select connection protocol (TCP/SSL): ");
            System.out.print("> ");
            String userInput = stdIn.readLine().trim().toLowerCase();

            switch (userInput.toLowerCase()) {
                case "tcp" -> connectTCP();
                case "ssl" -> connectSSL();
                default -> {
                System.err.println("Invalid protocol selection!");
                    return;
                }
            }

            System.out.println("Available commands: TEMP, LIGHT, PRES");
            System.out.println("Format: Sensor Rate Duration (TEMP 20 160)");

            boolean running = true;
            while (running) {
                System.out.print("> ");
                String command = stdIn.readLine();

                if (command == null) {
                    break;
                }

                String[] input = command.split(" ");
                if (input.length != 3) {
                    System.out.println("Invalid format!");
                    continue;
                }

                float rate;
                float duration;

                try {
                    rate = Float.parseFloat(input[1]);
                    duration = Float.parseFloat(input[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid rate or duration!");
                    continue;
                }

                if (rate <= 0 || duration <= 0) {
                    System.out.println("Rate and duration must be positive!");
                    continue;
                }

                out.println(command);

                int totalReads = (int) (duration / rate);
                for (int i = 0; i < totalReads; i++) {
                    String line = in.readLine();
                    if (line == null) {
                        System.out.println("Server closed the connection.");
                        running = false;
                        break;
                    }
                    System.out.println(line);
                }
            }

            closeConnection();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }


    private void connectTCP() throws IOException {
        socket = new Socket(host, tcpPort);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }


    private void connectSSL() throws IOException {
        System.setProperty("javax.net.ssl.trustStore", "truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "aa12345");

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) factory.createSocket(host, sslPort);

        sslSocket.startHandshake();

        this.socket = sslSocket;

        out = new PrintWriter(socket.getOutputStream(), true);
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private void closeConnection() throws IOException {
        System.out.println("Connection Closed");
        socket.close();
        out.close();
        in.close();
    }
}

