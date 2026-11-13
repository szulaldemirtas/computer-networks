package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class SensorClientHandler {

    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final Random rand = new Random();

    public SensorClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void startSession() throws IOException {
        processRequests();
        closeConnection();
    }

    public void processRequests() throws IOException {
        String line = in.readLine();
        while (line != null) {

            String [] input = line.split(" ");
            if (input.length != 3) {
                out.println("Invalid command format!");
                line = in.readLine();
                continue;
            }

            String sensorType = input[0];
            float rate = Float.parseFloat(input[1]);
            float duration = Float.parseFloat(input[2]);

            if (!sensorType.equals("TEMP")
                    && !sensorType.equals("LIGHT")
                    && !sensorType.equals("PRES")) {
                out.println("Invalid sensor type!");
                line = in.readLine();
                continue;
            }

            int totalReads = (int) (duration / rate);
            for (int i = 0; i < totalReads; i++) {

                float sensorValue = generateSensorValue(sensorType);
                long timestamp = System.currentTimeMillis();
                out.println(sensorType + " " + timestamp + " " + sensorValue);
                try {
                    Thread.sleep((long) rate);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            line = in.readLine();
        }
    }


    public float generateSensorValue(String sensorType) {
        return switch (sensorType) {
            case "TEMP" -> 20 + rand.nextFloat() * 10;
            case "PRES" -> 980 + rand.nextFloat() * 70;
            case "LIGHT" -> rand.nextFloat() * 1000;
            default -> throw new IllegalStateException("Unexpected value: " + sensorType);
        };
    }
    public void closeConnection() throws IOException {
        try {
            in.close();
        } finally {
            try {
                out.close();
            } finally {
                socket.close();
            }
        }
    }
}