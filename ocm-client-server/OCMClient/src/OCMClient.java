import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

public class OCMClient {

    int port;
    String host = "localhost";
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    BufferedReader stdIn;
    OCMProtocolHandler protocolHandler;
    OCMClientUI clientUI;

    public OCMClient(int port) throws IOException {
        this.port = port;
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        stdIn = new BufferedReader(new InputStreamReader(System.in));
        protocolHandler = new OCMProtocolHandler();
        clientUI = new OCMClientUI();
        socket.setSoTimeout(60000); // 1 minute timeout
    }

    public static void main(String[] args) throws IOException {

        OCMClient client;
        try {
            // Create a new client with a specified port
            client = new OCMClient(Integer.parseInt(args[0]));
        } catch (Exception e) {
            System.err.println("Invalid port number!");
            return;
        }
        client.processClientRequests();
    }

    public void processClientRequests() throws IOException {
        try {
            boolean running = true;
            clientUI.sendIntroduction();
            String userInput = stdIn.readLine();

            while (running) {
                if (userInput != null) {
                    switch (userInput) {
                        case "1":
                            clientUI.sendStationMenu();
                            String methodInput = stdIn.readLine();
                            handleStationRequest(methodInput);
                            break;

                        case "2":
                            handleLocationRequest();
                            break;

                        case "3":
                            handleCountryRequest();
                            break;

                        case "4":
                            clientUI.sendServiceMenu();
                            break;


                        case "5":
                            running = false;
                            closeConnection();
                            break;

                        default:
                            System.out.println("Invalid entry!");
                    }
                }

                if (userInput.equals("5")) break;

                System.out.print("> ");
                userInput = stdIn.readLine();
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void handleStationRequest(String retrievalMethod) throws IOException {
        switch (retrievalMethod) {
            case "a":
                System.out.print("Enter latitude:\n> ");
                String latitude = stdIn.readLine();
                System.out.print("Enter longitude:\n> ");
                String longitude = stdIn.readLine();
                out.println(protocolHandler.createStationRequest("coord", latitude, longitude));
                break;

            case "b":
                System.out.print("Enter country name:\n> ");
                String country = stdIn.readLine();
                out.println(protocolHandler.createStationRequest("country", country));
                break;

            case "c":
                System.out.print("Enter location name:\n> ");
                String location = stdIn.readLine();
                out.println(protocolHandler.createStationRequest("location", location));
                break;

            case "d":
                clientUI.sendServiceMenu();
                return;
            default:
                System.out.println("Invalid selection! Please enter a, b, c, or d.");
                return;
        }

        try {
        String statusMsg = in.readLine();
        if (statusMsg == null) {
            System.err.println("Server closed the connection.");
            closeConnection();
            return;
        }

            if (statusMsg != null) {
            switch (statusMsg.split(" ")[0]) {
                case "OCM_200":
                    String responseMessage = in.readLine();
                    if (responseMessage.trim().isEmpty()) System.out.println("Stations not found nearby, try another location!");
                    else handleStationResponse(responseMessage);
                    break;
                case "OCM_400":
                    System.out.println("Invalid input!");
                    break;
                case "OCM_404":
                    if ("b".equals(retrievalMethod))
                        System.out.println("Country not found!");
                    else if ("c".equals(retrievalMethod))
                        System.out.println("Location not found or no stations available for that location!");
                        break;
                case "OCM_500":
                    System.out.println("Something unexpected happened on the server side. Please try again later.");
                    break;
            }
        }} catch (SocketTimeoutException e) {
            System.out.println("Server response timed out. Closing connection...");
            closeConnection();
            return;
        }


    }

    public void handleCountryRequest() throws IOException {
        out.println(protocolHandler.createCountryRequest());

        try {
            String statusMsg = in.readLine();

            switch (statusMsg.split(" ")[0]) {
                case "OCM_200" -> {
                    String responseMessage = in.readLine();
                    handleLocationAndCountryResponse(responseMessage);
                }
                case "OCM_404" -> {
                    System.out.println("No data found!");
                }
                case "OCM_500" -> {
                    System.out.println("Something unexpected happened on the server side. Please try again later.");
                }
            }
        }
        catch (SocketTimeoutException e) {
            System.out.println("Server response timed out. Closing connection...");
            closeConnection();
            return;
        }
    }

    public void handleLocationRequest() throws IOException {
        out.println(protocolHandler.createLocationRequest());

        try {
            String statusMsg = in.readLine();
            switch (statusMsg.split(" ")[0]) {
                case "OCM_200" -> {
                    String responseMessage = in.readLine();
                    handleLocationAndCountryResponse(responseMessage);
                }
                case "OCM_404" -> {
                    System.out.println("No data found!");
                }
                case "OCM_500" -> {
                    System.out.println("Something unexpected happened on the server side. Please try again later.");
                }
            }
        }
        catch (SocketTimeoutException e) {
            System.out.println("Server response timed out. Closing connection...");
            closeConnection();
            return;
        }
    }

    public void closeConnection() throws IOException {
        System.out.println("Connection Closed");
        out.println(protocolHandler.createExitRequest());
        socket.close();
        out.close();
        in.close();
        stdIn.close();
    }

    public void handleStationResponse(String responseMessage) {
        String[] response = responseMessage.split(" , ");
        System.out.println("\n----- Charging Stations -----\n");
        for (String line: response) {
            line = line.replace("\"", "");
            System.out.println(line);
            System.out.println("------------------------------------------------------------------------");
        }
    }

    public void handleLocationAndCountryResponse (String responseMessage) {
        String[] response = responseMessage.split(" , ");
        response = Arrays.stream(response).sorted().toArray(String[]::new);

        for (int i = 0; i < response.length; i++) {
            if (i % 4 == 0) System.out.print("    ");
            System.out.printf("%-35s", response[i]);
            if ((i + 1) % 4 == 0 || i == response.length - 1) System.out.println();
        }
    }
}

