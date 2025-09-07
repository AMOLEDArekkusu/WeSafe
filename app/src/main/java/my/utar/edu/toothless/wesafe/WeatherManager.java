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
    private static final String API_KEY = "ce755dabd89b8062b6f6c72a2908a9a0"; // TODO: Replace with your API key
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
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
        String url = String.format("%s?lat=%f&lon=%f&appid=%s&units=metric",
                BASE_URL, location.getLatitude(), location.getLongitude(), API_KEY);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Weather API call failed", e);
                notifyError("Could not fetch weather data");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        notifyError("Weather service error: " + response.code());
                        return;
                    }

                    String jsonData = response.body().string();
                    JSONObject json = new JSONObject(jsonData);

                    // Get temperature in Celsius (already in Celsius because of units=metric)
                    double temp = json.getJSONObject("main").getDouble("temp");
                    
                    // Get weather condition
                    String condition = json.getJSONArray("weather")
                            .getJSONObject(0)
                            .getString("main");

                    // Format temperature to one decimal place
                    DecimalFormat df = new DecimalFormat("#.#");
                    double roundedTemp = Double.parseDouble(df.format(temp));

                    notifyUpdate(roundedTemp, condition);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error", e);
                    notifyError("Could not parse weather data");
                }
            }
        });
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
