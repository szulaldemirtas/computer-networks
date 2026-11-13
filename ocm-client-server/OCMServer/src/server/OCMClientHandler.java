package server;

import api.OCMApiHandler;
import data.OCMLocationData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OCMClientHandler {

    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final OCMApiHandler apiHandler;

    public enum Method {
        STATIONS,
        LOCS,
        CTS,
        EXT
    }

    public enum StatusCode {
        OCM_200,
        OCM_400,
        OCM_404,
        OCM_500
    }

    public Map<StatusCode, String> statusCodes = Map.of(
            StatusCode.OCM_200, "Success",
            StatusCode.OCM_400, "Bad Request",
            StatusCode.OCM_404, "Not Found",
            StatusCode.OCM_500, "Internal Server Error"
    );

    public OCMClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.apiHandler = new OCMApiHandler();
        socket.setSoTimeout(60000); // 1 minute timeout
    }

    public void startSession() throws IOException {
        processRequests();
        closeConnection();
    }

    public void processRequests() throws IOException {

        while (true) {
            try {
                String request = in.readLine();

                if (request == null) {
                    System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
                    closeConnection();
                    return;
                }

                if (request.trim().isEmpty()) {
                    sendStatusMessage(StatusCode.OCM_400);
                    continue;
                }

                String[] lines = request.trim().split("\\s+");

                if (lines[0].equalsIgnoreCase("OCM416")) {

                    Method method;
                    try {
                        method = Method.valueOf(lines[1].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        sendStatusMessage(StatusCode.OCM_400);
                        continue;
                    }

                    switch (method) {
                        case STATIONS -> handleStations(lines);
                        case LOCS -> handleLocations();
                        case CTS -> handleCountries();
                        case EXT -> {
                            sendStatusMessage(StatusCode.OCM_200);
                            closeConnection();
                            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
                            return;
                        }
                    }
                }
            }
            catch(SocketTimeoutException e) {
                System.err.println("Client timed out.");
                closeConnection();
                return;
            }
            catch (Exception e) {
                sendStatusMessage(StatusCode.OCM_500);
            }
        }
    }

    public void handleStations(String[] input) {
        float latitude = Float.NaN;
        float longitude = Float.NaN;
        String country = "";

        boolean usedCoordinates = false;
        boolean usedCountry = false;
        boolean usedLocation = false;

        HashMap<String, String> countryData = apiHandler.getCountryData();
        if (countryData == null || countryData.isEmpty()) {
            sendStatusMessage(StatusCode.OCM_500);
            return;
        }

        for (int i = 0; i < input.length; i++) {
            switch (input[i].toLowerCase()) {
                case "-lat" -> {
                    usedCoordinates = true;
                    if (usedCountry || usedLocation) {
                        sendStatusMessage(StatusCode.OCM_400);
                        return;
                    }
                    try {
                        latitude = Float.parseFloat(parseArguments(input, i + 1));
                    } catch (NumberFormatException e) {
                        sendStatusMessage(StatusCode.OCM_400);
                        return;
                    }

                    // Latitude out of bounds
                    if (latitude < -90 || latitude > 90) {
                        sendStatusMessage(StatusCode.OCM_400);
                        return;
                    }
                }
                case "-long" -> {
                    usedCoordinates = true;
                    if (usedCountry || usedLocation) {
                        sendStatusMessage(StatusCode.OCM_400);
                        return;
                    }
                    try {
                        longitude = Float.parseFloat(parseArguments(input, i + 1));
                    } catch (NumberFormatException e) {
                        sendStatusMessage(StatusCode.OCM_400);
                        return;
                    }

                    // Longitude out of bounds
                    if (longitude < -180 || longitude > 180) {
                        sendStatusMessage(StatusCode.OCM_400);
                        return;
                    }
                }
                case "-country" -> {
                    usedCountry = true;

                    // Only one flag is allowed
                        if (usedCoordinates || usedLocation) {
                        sendStatusMessage(StatusCode.OCM_400);
                            return;
                        }

                    try {
                        country = parseArguments(input, i+1);
                    } catch (Exception e) {
                        sendStatusMessage(StatusCode.OCM_400);
                        return;
                    }

                    String countryTrimmed = country.trim().toLowerCase();
                    boolean countryExists = apiHandler.getCountryData().keySet().stream()
                            .anyMatch(c -> c.equalsIgnoreCase(countryTrimmed));

                    if (!countryExists) {
                        sendStatusMessage(StatusCode.OCM_404);
                        return;
                    }

                    if (country.isEmpty()) {
                        sendStatusMessage(StatusCode.OCM_400);
                        return;
                    }
                }
                case "-location" -> {
                    usedLocation = true;

                    // Only one flag is allowed
                    if (usedCoordinates || usedCountry) {
                        sendStatusMessage(StatusCode.OCM_400);
                        return;
                    }

                    // Parse argument and get the coordinates of the location
                    try {
                        String location = parseArguments(input, i+1).toLowerCase();
                        if (location.isEmpty()) {
                            sendStatusMessage(StatusCode.OCM_400);
                            return;
                        }

                        OCMLocationData.Location matchedLocation = OCMLocationData.getLocations()
                                .values()
                                .stream()
                                .filter(loc -> loc.locationName.equalsIgnoreCase(location))
                                .findFirst()
                                .orElse(null);

                        if (matchedLocation == null) {
                            sendStatusMessage(StatusCode.OCM_404);
                            return;
                        }

                        latitude = matchedLocation.latitude;
                        longitude = matchedLocation.longitude;

                    } catch (Exception e) {
                        sendStatusMessage(StatusCode.OCM_400);
                        return;
                    }

                    // Check bounds
                    if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                        sendStatusMessage(StatusCode.OCM_400);
                        return;
                    }
                }
                default -> {}
            }
        }

        // Getting stations from API
        String stations = apiHandler.getStations(latitude, longitude, country);

        if (stations == null) {
            sendStatusMessage(StatusCode.OCM_500);
            return;
        }

        sendStatusMessage(StatusCode.OCM_200);
        out.println(stations);
    }

    private String parseArguments(String[] input, int j) {
        String inputStr = "";
        while (j < input.length && !input[j].startsWith("-")) {
            inputStr = inputStr.concat(input[j] + " ");
            j++;
        }
        inputStr = inputStr.trim();
        return inputStr;
    }

    public void handleLocations() {
        String locationNames = OCMLocationData.getLocations().values().stream()
                                    .map(loc -> loc.locationName)
                                    .collect(Collectors.joining(" , "));

        sendStatusMessage(StatusCode.OCM_200);
        out.println(locationNames.isEmpty() ? "[]" : locationNames);
    }

    public void handleCountries() {
        HashMap<String, String> countriesMap = apiHandler.getCountryData();
        String countryNames = String.join(" , ", countriesMap.keySet());

        sendStatusMessage(StatusCode.OCM_200);
        out.println(countryNames.isEmpty() ? "[]" : countryNames);

    }

    public void sendStatusMessage(StatusCode code) {
        String message = code.name() + " " + statusCodes.get(code);
        out.println(message);
    }

    public void closeConnection() throws IOException {
        socket.close();
        out.close();
        in.close();
    }
}
