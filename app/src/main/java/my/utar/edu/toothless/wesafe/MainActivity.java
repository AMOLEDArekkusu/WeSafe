package my.utar.edu.toothless.wesafe;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

/**
 * Main activity of the WeSafe app - serves as the home/dashboard screen
 * Displays current status, quick actions, and safety tips
 */
public class MainActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
    };
    private static final String[] BACKGROUND_LOCATION = {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };
    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES
    };

    private TextView tvLocationStatus;
    private TextView tvWeatherInfo;
    private TextView tvIncidentCount;
    private TextView tvLastUpdate;
    private FloatingActionButton fabPanicButton;
    private MaterialButton btnViewMap, btnReportIncident, btnEmergencyContacts, btnSettings;

    private IncidentStorage incidentStorage;

    // Location and Weather Components
    private FusedLocationProviderClient fusedLocationClient;
    private boolean locationPermissionGranted = false;
    private WeatherManager weatherManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set theme and layout
        setTheme(R.style.Theme_WeSafe);
        setContentView(R.layout.activity_main);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("WeSafe");
        }

        // Initialize UI components
        initializeViews();
        setupClickListeners();

        // Initialize incident storage
        incidentStorage = new IncidentStorage(this);

        // Initialize location services - FIXED: Correct class name
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize weather manager
        weatherManager = new WeatherManager();
        weatherManager.setListener(new WeatherManager.WeatherUpdateListener() {
            @Override
            public void onWeatherUpdated(double tempCelsius, String condition) {
                updateWeatherUI(tempCelsius, condition);
            }

            @Override
            public void onWeatherError(String error) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Check and request permissions
        checkAndRequestPermissions();

        // Load data (this would be implemented based on your data sources)
        loadData();
    }

    /**
     * Initialize all view components from the layout
     */
    private void initializeViews() {
        // Initialize text views
        // UI Components
        TextView tvWelcome = findViewById(R.id.tv_welcome);
        tvWelcome.setText(R.string.welcome_message);
        tvLocationStatus = findViewById(R.id.tv_location_status);
        tvLocationStatus.setText(R.string.getting_location);
        tvWeatherInfo = findViewById(R.id.tv_weather_info);
        tvWeatherInfo.setText(R.string.loading_weather);
        tvIncidentCount = findViewById(R.id.tv_incident_count);
        tvIncidentCount.setText(R.string.loading_incidents);
        tvLastUpdate = findViewById(R.id.tv_last_update);

        // Initialize buttons
        fabPanicButton = findViewById(R.id.fab_panic_button);
        btnViewMap = findViewById(R.id.btn_view_map);
        btnReportIncident = findViewById(R.id.btn_report_incident);
        btnEmergencyContacts = findViewById(R.id.btn_emergency_contacts);
        btnSettings = findViewById(R.id.btn_settings);
    }

    private void setupClickListeners() {
        // Set up click listeners for quick action buttons
        btnViewMap.setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));
        btnReportIncident.setOnClickListener(v -> startActivity(new Intent(this, IncidentReportActivity.class)));
        btnEmergencyContacts.setOnClickListener(v -> startActivity(new Intent(this, EmergencyContactsActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        // Panic button click listener
        fabPanicButton.setOnClickListener(v -> triggerEmergencyProtocol());
    }

    /**
     * Load data from various sources to populate the dashboard
     */
    private void loadData() {
        // This would typically involve API calls, database queries, etc.
        // For now, we'll set placeholder text

        // Update weather info (this would use a weather API in a real implementation)
        tvWeatherInfo.setText("🌤️ 23.5°C");

        // Update incident count from storage
        int todayCount = incidentStorage.getTodayCount();
        int totalCount = incidentStorage.getTotalCount();
        tvIncidentCount.setText(String.format("📊 Incidents: %d today, %d total", todayCount, totalCount));

        // Update last updated time
        tvLastUpdate.setText("Last updated: " + java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));
    }

    /**
     * Emergency protocol triggered by the panic button
     * This would alert emergency contacts and potentially authorities
     */
    private void triggerEmergencyProtocol() {
        if (!locationPermissionGranted) {
            showLocationNeededDialog();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Emergency Alert")
                .setMessage("Are you sure you want to trigger an emergency alert? This will notify your emergency contacts and share your location.")
                .setPositiveButton("Yes, Alert Contacts", (dialog, which) -> {
                    // Get current location and send alert
                    getCurrentLocationAndAlert();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkAndRequestPermissions() {
        if (hasLocationPermissions()) {
            showLocationPermissionRationale();
        } else {
            locationPermissionGranted = true;
            // Check for background location if needed
            checkBackgroundLocationPermission();
            // Initialize location updates
            initializeLocationUpdates();
        }

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE + 1);
        }

        // Check storage permissions
        checkStoragePermissions();
    }

    private boolean hasLocationPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    private void showLocationPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Required")
                .setMessage("WeSafe needs location permission to:\n\n" +
                        "• Send your location to emergency contacts\n" +
                        "• Show nearby incidents on the map\n" +
                        "• Provide accurate emergency response\n\n" +
                        "Please grant location permission to use these safety features.")
                .setPositiveButton("Grant Permission", (dialog, which) -> {
                    requestLocationPermissions();
                })
                .setNegativeButton("Not Now", (dialog, which) -> {
                    showLocationRequiredSnackbar();
                })
                .setCancelable(false)
                .show();
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    private void checkStoragePermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above, only need READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE + 2);
            }
        } else {
            // For Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        PERMISSION_REQUEST_CODE + 2);
            }
        }
    }

    private void checkBackgroundLocationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Check if we've shown the dialog before
            SharedPreferences prefs = getSharedPreferences("WeSafePrefs", MODE_PRIVATE);
            boolean hasShownLocationDialog = prefs.getBoolean("hasShownBackgroundLocationDialog", false);

            if (!hasShownLocationDialog &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                new AlertDialog.Builder(this)
                        .setTitle("Background Location Access")
                        .setMessage("To provide emergency assistance even when the app is closed, " +
                                "please allow WeSafe to access your location all the time.")
                        .setPositiveButton("Grant Permission", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this, BACKGROUND_LOCATION,
                                    PERMISSION_REQUEST_CODE + 1);
                            // Mark that we've shown the dialog
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("hasShownBackgroundLocationDialog", true);
                            editor.apply();
                        })
                        .setNegativeButton("Not Now", (dialog, which) -> {
                            // Mark that we've shown the dialog even if user declines
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("hasShownBackgroundLocationDialog", true);
                            editor.apply();
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    private void showLocationRequiredSnackbar() {
        Snackbar.make(findViewById(android.R.id.content),
                        "Location permission is required for safety features",
                        Snackbar.LENGTH_INDEFINITE)
                .setAction("Settings", v -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                locationPermissionGranted = true;
                checkBackgroundLocationPermission();
                initializeLocationUpdates();
            } else {
                showLocationRequiredSnackbar();
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE + 2) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                Snackbar.make(findViewById(android.R.id.content),
                                "Storage permission is required for uploading images",
                                Snackbar.LENGTH_LONG)
                        .setAction("Settings", v -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .show();
            }
        }
    }

    private void initializeLocationUpdates() {
        if (locationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                // Add proper error handling
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new com.google.android.gms.tasks.OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    updateLocationUI(location);
                                } else {
                                    // Handle case where location is null
                                    tvLocationStatus.setText("📍 Location unavailable");
                                }
                            }
                        })
                        .addOnFailureListener(this, new com.google.android.gms.tasks.OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                tvLocationStatus.setText("📍 Error getting location");
                                Log.e("Location", "Error getting location", e);
                            }
                        });
            }
        }
    }

    private void getCurrentLocationAndAlert() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // FIXED: Added proper permission check
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new com.google.android.gms.tasks.OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // TODO: Implement emergency alert sending
                                Toast.makeText(MainActivity.this, "Emergency alert sent with location: " +
                                                location.getLatitude() + ", " + location.getLongitude(),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Unable to get current location",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void updateLocationUI(Location location) {
        if (location != null) {
            // Update location display with coordinates
            tvLocationStatus.setText(String.format("📍 %.6f, %.6f",
                    location.getLatitude(), location.getLongitude()));

            // Start weather updates when we get a location
            weatherManager.startUpdates(location);

            // Get address from coordinates (geocoding)
            getAddressFromLocation(location);
        }
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1,
                    new Geocoder.GeocodeListener() {
                        @Override
                        public void onGeocode(List<Address> addresses) {
                            if (!addresses.isEmpty()) {
                                String address = addresses.get(0).getLocality() + ", " +
                                        addresses.get(0).getAdminArea();
                                tvLocationStatus.setText("📍 " + address);
                            }
                        }
                    }
            );
        } else {
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1
                );
                if (!addresses.isEmpty()) {
                    String address = addresses.get(0).getLocality() + ", " +
                            addresses.get(0).getAdminArea();
                    tvLocationStatus.setText("📍 " + address);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateWeatherUI(double tempCelsius, String condition) {
        String weatherEmoji = getWeatherEmoji(condition);
        tvWeatherInfo.setText(String.format("%s %.1f°C", weatherEmoji, tempCelsius));
        tvLastUpdate.setText("Last updated: " + java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));
    }

    private String getWeatherEmoji(String condition) {
        switch (condition.toLowerCase()) {
            case "clear": return "☀️";
            case "clouds": return "☁️";
            case "rain": return "🌧️";
            case "drizzle": return "🌦️";
            case "thunderstorm": return "⛈️";
            case "snow": return "🌨️";
            case "mist":
            case "fog":
            case "haze": return "🌫️";
            default: return "🌤️";
        }
    }

    private void showLocationNeededDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Location Required")
                .setMessage("Location permission is required to send your location during emergencies. Please enable location permissions to use this feature.")
                .setPositiveButton("Enable", (dialog, which) -> {
                    checkAndRequestPermissions();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getColor(R.color.colorPrimary));

            // Make status bar icons dark if we're using a light theme
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasLocationPermissions()) {
            showLocationRequiredSnackbar();
        } else {
            initializeLocationUpdates(); // Refresh location and weather
        }
        // Refresh incident counts
        int todayCount = incidentStorage.getTodayCount();
        int totalCount = incidentStorage.getTotalCount();
        tvIncidentCount.setText(String.format("📊 Incidents: %d today, %d total", todayCount, totalCount));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop weather updates when activity is not visible
        weatherManager.stopUpdates();
    }
}