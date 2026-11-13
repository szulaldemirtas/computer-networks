import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Benchmark {

    String host = "localhost";
    int tcpPort = 8000;
    int sslPort = 8001;

    int rate = 20;
    int duration = 10000;

    int tcpRuns = 10;
    int sslRuns = 10;

    private static class Result {
        long timeFirstMessage;
        long timeLastMessage;
        long avgGapBetweenMessages;

        Result(long timeFirstMessage, long timeLastMessage, long avgGapBetweenMessages) {
            this.timeFirstMessage = timeFirstMessage;
            this.timeLastMessage = timeLastMessage;
            this.avgGapBetweenMessages = avgGapBetweenMessages;
        }
    }


    public void runBenchmark() {
        try {
            long totalFirstReceivedTCP = 0;
            long totalArrivalTCP = 0;

            long totalFirstReceivedSSL = 0;
            long totalArrivalSSL = 0;

            for (int i = 0; i < tcpRuns; i++) {
                System.out.println("TCP Run " + (i + 1));
                Result res = performRun(false);
                totalFirstReceivedTCP += res.timeFirstMessage;
                totalArrivalTCP += res.avgGapBetweenMessages;
            }

            System.out.println();

            for (int i = 0; i < sslRuns; i++) {
                System.out.println("SSL Run " + (i + 1));
                Result res = performRun(true);
                totalFirstReceivedSSL += res.timeFirstMessage;
                totalArrivalSSL += res.avgGapBetweenMessages;
            }

            long avgFirstReceivedTCP = totalFirstReceivedTCP / tcpRuns;
            long avgArrivalTCP = totalArrivalTCP / tcpRuns;

            long avgFirstReceivedSSL = totalFirstReceivedSSL / sslRuns;
            long avgArrivalSSL = totalArrivalSSL / sslRuns;

            System.out.println("\n ~~ Benchmark Results ~~");
            System.out.println("TCP average time until first message: " + avgFirstReceivedTCP + " ms");
            System.out.println("TCP average delay between messages: " + avgArrivalTCP+ " ms");
            System.out.println("SSL average time until first message: " + avgFirstReceivedSSL + " ms");
            System.out.println("SSL average delay between messages: " + avgArrivalSSL + " ms");


        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private Result performRun(boolean isSSL) throws IOException {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            if (isSSL) {
                socket = connectSSL();
            } else {
                socket = new Socket(host, tcpPort);
            }

            out = new PrintWriter(socket.getOutputStream(), true);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("TEMP " + rate + " " + duration);

            List<Long> arrivalTimes = new ArrayList<>();

            long start = System.currentTimeMillis();
            long deadline = start + duration;

            long timeFirstMessage = -1;
            long timeLastMessage  = -1;

            while (System.currentTimeMillis() < deadline) {
                String line = in.readLine();
                if (line == null) break;

                long now = System.currentTimeMillis() - start;
                arrivalTimes.add(now);

                if (arrivalTimes.size() == 1)
                    timeFirstMessage = now;

                timeLastMessage = now;
            }

            if (arrivalTimes.size() < 2) {
                return new Result(0, 0, 0);
            }

            long gapTotal = 0;
            for (int i = 1; i < arrivalTimes.size(); i++) {
                gapTotal += (arrivalTimes.get(i) - arrivalTimes.get(i - 1));
            }
            long avgGapBetweenMessages = gapTotal / (arrivalTimes.size() - 1);

            return new Result(timeFirstMessage, timeLastMessage, avgGapBetweenMessages);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new Result(0, 0, 0);
        } finally {
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }
    }
    private SSLSocket connectSSL() throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "aa12345");

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) factory.createSocket(host, sslPort);
        sslSocket.startHandshake();
        return sslSocket;
        }
}