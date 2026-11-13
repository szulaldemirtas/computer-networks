public class OCMClientUI {

    public void sendIntroduction() {
        System.out.print("""
                Welcome to the OCM416 Server!
                ----------------------------
                Available Services:
                1. Stations - Find the nearest charging stations
                2. Locations - View available locations
                3. Countries - View supported countries
                4. Go back / Display Menu
                5. Exit
                > """);
    }

    public void sendServiceMenu() {
        System.out.print("""
                Select a service:
                1. Stations - Find the nearest charging stations
                2. Locations - View available locations
                3. Countries - View supported countries
                4. Go back / Display Menu
                5. Exit
                """);
    }

    public void sendStationMenu() {
        System.out.print("""
                Select the method
                a. By coordinates - Enter latitude and longitude
                b. By country - Enter country name
                c. By location - Enter location name
                d. Go back
                > """);
    }






}
