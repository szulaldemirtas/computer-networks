package data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OCMLocationData {

    public static Map<String, Location> LocationsMap = new HashMap<>();
    static {
        LocationsMap.put("koc university", new Location("Koç University", 41.2026952f, 29.0714802f));
        LocationsMap.put("sariyer", new Location("Sariyer", 41.1713436f, 28.695352f));
        LocationsMap.put("hacisoman metro", new Location("Hacisoman Metro", 41.1395095f, 29.0203817f));
        LocationsMap.put("istinyepark", new Location("IstinyePark", 41.1103935f, 29.0229535f));
        LocationsMap.put("besiktas", new Location("Besiktas", 41.0716882f, 28.9417285f));
        LocationsMap.put("levent", new Location("Levent", 41.0824497f, 28.9954444f));
        LocationsMap.put("bebek", new Location("Bebek", 41.0776892f, 29.0337483f));
        LocationsMap.put("sultanahmet", new Location("Sultanahmet", 41.0078663f, 28.9367177f));
        LocationsMap.put("caddebostan", new Location("Caddebostan", 41.0078663f, 28.9367177f));
        LocationsMap.put("bagdat street", new Location("Bağdat Street", 40.9668047f, 29.0217849f));
        LocationsMap.put("ortakoy", new Location("Ortakoy", 40.9589491f, 29.0732007f));
    }

    public static class Location {
        public String locationName;
        public float latitude;
        public float longitude;

        Location(String locationName, float latitude, float longitude) {
            this.locationName = locationName;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public static Map<String, Location> getLocations() {
        return LocationsMap;
    }

    public static List<String> getLocationNames() {
        return LocationsMap.values().stream().map(location -> location.locationName).toList();
    }
}
