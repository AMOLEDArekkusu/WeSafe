package my.utar.edu.toothless.wesafe;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherManager {
    private static final String TAG = "WeatherManager";
    private static final String API_KEY = "AIzaSyAfb2cWOSNwvnb_-qFWt20z4hBrXKmHbEI"; // TODO: Replace with your WeatherAPI.com key
    private static final String BASE_URL = "https://api.weatherapi.com/v1/current.json";
    private static final long UPDATE_INTERVAL = TimeUnit.MINUTES.toMillis(5); // Update every 5 minutes

    private final OkHttpClient client;
    private final Handler handler;
    private WeatherUpdateListener listener;
    private final Runnable updateRunnable;
    private WeatherLocation lastLocation;

    public interface WeatherUpdateListener {
        void onWeatherUpdated(double tempCelsius, String condition);
        void onWeatherError(String error);
    }

    public WeatherManager() {
        client = new OkHttpClient();
        handler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (lastLocation != null) {
                    fetchWeather(lastLocation);
                }
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
    }

    public void setListener(WeatherUpdateListener listener) {
        this.listener = listener;
    }

    public void startUpdates(android.location.Location location) {
        lastLocation = WeatherLocation.fromAndroidLocation(location);
        stopUpdates(); // Stop any existing updates
        fetchWeather(lastLocation); // Fetch immediately
        handler.postDelayed(updateRunnable, UPDATE_INTERVAL); // Schedule periodic updates
    }

    public void stopUpdates() {
        handler.removeCallbacks(updateRunnable);
    }

    public void updateLocation(android.location.Location location) {
        lastLocation = WeatherLocation.fromAndroidLocation(location);
        fetchWeather(lastLocation);
    }

    private void fetchWeather(WeatherLocation location) {
        String url = String.format("%s?key=%s&q=%f,%f&aqi=no",
                BASE_URL, API_KEY, location.getLatitude(), location.getLongitude());

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "WeatherAPI call failed", e);
                notifyError("Could not fetch weather data");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        notifyError("WeatherAPI service error: " + response.code());
                        return;
                    }

                    String jsonData = response.body().string();
                    JSONObject json = new JSONObject(jsonData);

                    // Parse WeatherAPI.com response format
                    JSONObject current = json.getJSONObject("current");
                    
                    // Get temperature in Celsius
                    double temp = current.getDouble("temp_c");
                    
                    // Get weather condition from WeatherAPI format
                    JSONObject condition = current.getJSONObject("condition");
                    String conditionText = condition.getString("text");
                    
                    // Convert to standard format
                    String standardCondition = convertWeatherAPICondition(conditionText);

                    // Format temperature to one decimal place
                    DecimalFormat df = new DecimalFormat("#.#");
                    double roundedTemp = Double.parseDouble(df.format(temp));

                    notifyUpdate(roundedTemp, standardCondition);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error for WeatherAPI", e);
                    notifyError("Could not parse weather data");
                }
            }
        });
    }

    /**
     * Convert WeatherAPI.com condition to standard format
     */
    private String convertWeatherAPICondition(String apiCondition) {
        if (apiCondition == null) return "Unknown";
        
        String condition = apiCondition.toLowerCase();
        
        // Map WeatherAPI conditions to standard format for consistency
        if (condition.contains("clear") || condition.contains("sunny")) {
            return "Clear";
        } else if (condition.contains("cloud") || condition.contains("overcast") || condition.contains("partly cloudy")) {
            return "Clouds";
        } else if (condition.contains("rain") || condition.contains("drizzle") || condition.contains("shower")) {
            return "Rain";
        } else if (condition.contains("snow") || condition.contains("blizzard")) {
            return "Snow";
        } else if (condition.contains("thunder") || condition.contains("storm")) {
            return "Thunderstorm";
        } else if (condition.contains("mist") || condition.contains("fog") || condition.contains("haze")) {
            return "Mist";
        } else if (condition.contains("wind")) {
            return "Windy";
        } else {
            // Return original condition with proper capitalization
            return apiCondition.substring(0, 1).toUpperCase() + apiCondition.substring(1);
        }
    }

    private void notifyUpdate(final double tempCelsius, final String condition) {
        if (listener != null) {
            handler.post(() -> listener.onWeatherUpdated(tempCelsius, condition));
        }
    }

    private void notifyError(final String error) {
        if (listener != null) {
            handler.post(() -> listener.onWeatherError(error));
        }
    }
}
