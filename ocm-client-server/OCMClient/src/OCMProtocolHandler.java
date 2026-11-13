public class OCMProtocolHandler {
    public final String protocolName = "OCM416";

    public String createStationRequest(String type, String... args) {
        return switch (type) {
            case "coord" -> "%s STATIONS -lat %s -long %s".formatted(protocolName, args[0], args[1]);
            case "country" -> "%s STATIONS -country %s".formatted(protocolName, args[0]);
            case "location" -> "%s STATIONS -location %s".formatted(protocolName, args[0]);
            default -> "";
        };
    }

    public String createCountryRequest() {
        return "%s CTS".formatted(protocolName);
    }

    public String createLocationRequest() {
        return "%s LOCS".formatted(protocolName);
    }

    public String createExitRequest() {
        return "%s EXT".formatted(protocolName);
    }

}
