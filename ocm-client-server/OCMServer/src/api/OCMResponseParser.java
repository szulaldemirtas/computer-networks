package api;

import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import data.OCMCountryData;
import data.OCMStationData;

public class OCMResponseParser {

    Gson gson;
    public OCMResponseParser() {
        gson = new Gson();
    }

    public OCMCountryData.Country[] parseCountries(String json) {

        OCMCountryData data = gson.fromJson(json, OCMCountryData.class);
        return data.Countries;
    }

    public String parseStations(String json) {

        OCMStationData[] data = gson.fromJson(json, OCMStationData[].class);
        data = Arrays.copyOfRange(data, 0, Math.min(10, data.length));

        return Arrays.stream(data)
                .map(station -> String.format(
                        "\"%s\",\"%s\",\"https://www.google.com/maps?q=%.4f,%.4f\",\"%d\"",
                        station.AddressInfo.Title,
                        station.AddressInfo.AddressLine1,
                        station.AddressInfo.Latitude,
                        station.AddressInfo.Longitude,
                        station.NumberOfPoints)
                )
                .collect(Collectors.joining(" , "));
    }
}
