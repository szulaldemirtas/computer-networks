package api;

import data.OCMCountryData;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class OCMApiHandler {
    String apiKey = System.getenv("OCM_API_KEY");
    HttpClient client = HttpClient.newHttpClient();
    String baseUrl = "https://api.openchargemap.io/v3";
    OCMResponseParser responseParser = new OCMResponseParser();
    HashMap<String, String> countriesMap = new HashMap<>();

    public OCMApiHandler()  {
        countriesMap = getCountryData();
    }

    public HashMap<String, String> getCountryData() {
        if (countriesMap.isEmpty()) {
            String url = baseUrl + "/referencedata";

            HttpRequest request = HttpRequest.newBuilder()
                    .header("Accept", "application/json")
                    .header("X-API-Key", apiKey)
                    .uri(URI.create(url))
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                OCMCountryData.Country[] countryList = responseParser.parseCountries(response.body());
                for (OCMCountryData.Country country : countryList) {
                    countriesMap.put(country.Title, country.ISOCode);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return new HashMap<>();            }
        }

        return countriesMap;
    }

    public String getStations(float latitude, float longitude, String country) {

        List<String> parameters = new ArrayList<>();
        String url = baseUrl + "/poi/?";

        if (!Float.isNaN(latitude)) {
            parameters.add(String.format("latitude=%f", latitude));
        }
        if (!Float.isNaN(longitude)) {
            parameters.add(String.format("longitude=%f",    longitude));
        }
        if (!country.isEmpty()) {
            String countryCode = countriesMap.entrySet().stream()
                    .filter(entry -> entry.getKey().trim().toLowerCase().equals(country.trim().toLowerCase()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse("");

            if (!countryCode.isEmpty()) parameters.add("countrycode=" + countryCode);
            else return "";
        }

        if (!parameters.isEmpty()) {
            url += String.join("&", parameters);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .header("X-API-Key",  apiKey)
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
                return null;

            String body = response.body().trim();
            if (body.isEmpty() || body.equals("[]"))
                return "";


            return responseParser.parseStations(body);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return null;
    }
}
