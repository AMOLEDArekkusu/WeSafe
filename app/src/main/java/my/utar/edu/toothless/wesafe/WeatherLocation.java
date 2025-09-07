package my.utar.edu.toothless.wesafe;

public class WeatherLocation {
    private final double latitude;
    private final double longitude;

    public WeatherLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public static WeatherLocation fromAndroidLocation(android.location.Location location) {
        return new WeatherLocation(location.getLatitude(), location.getLongitude());
    }
}
