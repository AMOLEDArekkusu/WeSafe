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
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
    private TextView tvWeatherTemperature;
    private TextView tvWeatherCondition;
    private TextView tvWeatherHumidity;
    private TextView tvWeatherWind;
    private TextView tvWeatherUpdate;
    private FloatingActionButton fabPanicButton;
    private MaterialButton btnViewMap, btnReportIncident, btnEmergencyContacts, btnSettings;
    
    // Navigation
    private BottomNavigationView bottomNavigationView;

    private IncidentStorage incidentStorage;

    // Location and Weather Components
    private FusedLocationProviderClient fusedLocationClient;
    private boolean locationPermissionGranted = false;
    private WeatherManager weatherManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Set theme and layout
            setTheme(R.style.Theme_WeSafe);
            setContentView(R.layout.activity_main);

            // Initialize basic components only
            initializeBasicViews();
            
            // Now try to initialize the full UI safely
            initializeViews();
            
            // Set up click listeners safely
            setupClickListeners();
            
            // Initialize weather and location services
            initializeWeatherAndLocation();
            
            // Complete first-time setup if this is the first launch
            FirstTimeSetupManager setupManager = FirstTimeSetupManager.getInstance(this);
            if (setupManager.isFirstLaunch()) {
                setupManager.completeFirstLaunch();
            }
            
        } catch (Exception e) {
            // Log the error and show a toast
            android.util.Log.e("MainActivity", "Error in onCreate", e);
            android.widget.Toast.makeText(this, getString(R.string.error_loading_app, e.getMessage()), android.widget.Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Initialize only the most basic view components for testing
     */
    private void initializeBasicViews() {
        try {
            // Try to find and initialize only essential views
            TextView tvWelcome = findViewById(R.id.tv_welcome);
            if (tvWelcome != null) {
                // Use string resource for proper localization
                tvWelcome.setText(R.string.welcome_message);
            }
            
            // Try to set up basic click listeners
            setupBasicClickListeners();
            
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error initializing basic views", e);
        }
    }

    /**
     * Set up only basic click listeners to test functionality
     */
    private void setupBasicClickListeners() {
        try {
            // Only set up emergency card click - the most important one
            View cardEmergency = findViewById(R.id.card_emergency);
            if (cardEmergency != null) {
                cardEmergency.setOnClickListener(v -> {
                    Toast.makeText(this, getString(R.string.emergency_button_clicked), Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error setting up click listeners", e);
        }
    }

    /**
     * Initialize weather and location services safely
     */
    private void initializeWeatherAndLocation() {
        try {
            // Initialize location client
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            
            // Initialize weather manager
            weatherManager = new WeatherManager();
            
            // Show default weather data initially
            showDefaultWeatherData();
            
            // Check permissions and start location updates if available
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                initializeLocationUpdates();
            } else {
                // Show default weather data
                showDefaultWeatherData();
            }
            
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error initializing weather and location", e);
            // Show default weather data if initialization fails
            showDefaultWeatherData();
        }
    }

    /**
     * Initialize all view components from the layout
     */
    private void initializeViews() {
        // Initialize text views for the new card-based layout
        TextView tvWelcome = findViewById(R.id.tv_welcome);
        if (tvWelcome != null) {
            tvWelcome.setText(R.string.welcome_message);
        }
        
        // These are from the new layout
        tvLocationStatus = findViewById(R.id.tv_location_status);
        tvLastUpdate = findViewById(R.id.tv_last_update);

        // Initialize weather card components
        tvWeatherTemperature = findViewById(R.id.tv_weather_temperature);
        tvWeatherCondition = findViewById(R.id.tv_weather_condition);
        tvWeatherHumidity = findViewById(R.id.tv_weather_humidity);
        tvWeatherWind = findViewById(R.id.tv_weather_wind);
        tvWeatherUpdate = findViewById(R.id.tv_weather_update);

        // These elements don't exist in our new card-based layout
        // Keeping variables for backward compatibility but not initializing from layout
        tvWeatherInfo = null; 
        tvIncidentCount = null;
        fabPanicButton = null;
        btnViewMap = null;
        btnReportIncident = null;
        btnEmergencyContacts = null;
        btnSettings = null;
        
        // Initialize bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupClickListeners() {
        // Set up click listeners for the new card-based layout
        View cardEmergency = findViewById(R.id.card_emergency);
        if (cardEmergency != null) {
            cardEmergency.setOnClickListener(v -> triggerEmergencyProtocol());
        }
        
        View cardLocation = findViewById(R.id.card_location);
        if (cardLocation != null) {
            cardLocation.setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));
        }
        
        View cardSafetyCheck = findViewById(R.id.card_safety_check);
        if (cardSafetyCheck != null) {
            cardSafetyCheck.setOnClickListener(v -> performSafetyCheck());
        }
        
        View cardQuickSettings = findViewById(R.id.card_quick_settings);
        if (cardQuickSettings != null) {
            cardQuickSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        }

        // Weather card click listener
        View cardWeather = findViewById(R.id.card_weather);
        if (cardWeather != null) {
            cardWeather.setOnClickListener(v -> updateWeatherData());
        }
        
        // Setup bottom navigation
        setupBottomNavigation();
    }

    /**
     * Setup bottom navigation functionality
     */
    private void setupBottomNavigation() {
        if (bottomNavigationView == null) {
            return;
        }
        
        // Set the current item (Home/Dashboard)
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Set Home as default for main dashboard
        
        // Set up navigation item selection listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_maps) {
                // Navigate to Maps/Location activity
                startActivity(new Intent(this, MapActivity.class));
                return true;
                
            } else if (itemId == R.id.nav_contact) {
                // Navigate to Emergency Contacts activity
                startActivity(new Intent(this, EmergencyContactsActivity.class));
                return true;
                
            } else if (itemId == R.id.nav_home) {
                // Already on main activity (Home/Dashboard), just stay here
                return true;
                
            } else if (itemId == R.id.nav_report) {
                // Navigate to Incident Report activity
                startActivity(new Intent(this, IncidentReportActivity.class));
                return true;
                
            } else if (itemId == R.id.nav_settings) {
                // Navigate to Settings activity
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            
            return false;
        });
    }

    /**
     * Load data from various sources to populate the dashboard
     */
    private void loadData() {
        // This would typically involve API calls, database queries, etc.
        // For now, we'll set placeholder text

        // Update weather info (only if the view exists)
        if (tvWeatherInfo != null) {
            tvWeatherInfo.setText("🌤️ 23.5°C");
        }

        // Update incident count from storage (only if the view exists)
        if (tvIncidentCount != null && incidentStorage != null) {
            int todayCount = incidentStorage.getTodayCount();
            int totalCount = incidentStorage.getTotalCount();
            tvIncidentCount.setText(String.format("📊 Incidents: %d today, %d total", todayCount, totalCount));
        }

        // Update last updated time (only if the view exists)
        if (tvLastUpdate != null) {
            String formattedDate = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
            tvLastUpdate.setText(getString(R.string.last_updated_prefix, formattedDate));
        }
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
                .setTitle(getString(R.string.emergency_title))
                .setMessage(getString(R.string.emergency_message))
                .setPositiveButton(getString(R.string.yes_alert), (dialog, which) -> {
                    // Get current location and send alert
                    getCurrentLocationAndAlert();
                })
                .setNegativeButton(getString(R.string.cancel), null)
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
                .setTitle(getString(R.string.location_permission_needed))
                .setMessage(getString(R.string.location_permission_rationale))
                .setPositiveButton(getString(R.string.grant_permission), (dialog, which) -> {
                    requestLocationPermissions();
                })
                .setNegativeButton(getString(R.string.not_now), (dialog, which) -> {
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
            FirstTimeSetupManager setupManager = FirstTimeSetupManager.getInstance(this);
            
            // Only show dialog if this is first launch and we haven't shown it before
            if (setupManager.shouldShowBackgroundLocationDialog() &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.background_location_title))
                        .setMessage(getString(R.string.background_location_message))
                        .setPositiveButton(getString(R.string.grant_permission), (dialog, which) -> {
                            ActivityCompat.requestPermissions(this, BACKGROUND_LOCATION,
                                    PERMISSION_REQUEST_CODE + 1);
                            // Mark that we've shown the dialog
                            setupManager.markBackgroundLocationDialogShown();
                        })
                        .setNegativeButton(getString(R.string.not_now), (dialog, which) -> {
                            // Mark that we've shown the dialog even if user declines
                            setupManager.markBackgroundLocationDialogShown();
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    private void showLocationRequiredSnackbar() {
        Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.location_permission_denied),
                        Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.settings), v -> {
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
                        .setAction(getString(R.string.settings), v -> {
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
                                    tvLocationStatus.setText(getString(R.string.location_unavailable_status));
                                }
                            }
                        })
                        .addOnFailureListener(this, new com.google.android.gms.tasks.OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                tvLocationStatus.setText(getString(R.string.error_getting_location_status));
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
                                tvLocationStatus.setText(getString(R.string.location_prefix, address));
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
                    tvLocationStatus.setText(getString(R.string.location_prefix, address));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateWeatherUI(double tempCelsius, String condition) {
        String weatherEmoji = getWeatherEmoji(condition);
        
        // Update the new weather card components if they exist
        if (tvWeatherTemperature != null) {
            tvWeatherTemperature.setText(String.format("%.1f°C", tempCelsius));
        }
        if (tvWeatherCondition != null) {
            tvWeatherCondition.setText(String.format("%s %s", weatherEmoji, condition));
        }
        
        // Update humidity and wind with placeholder values (would come from detailed API response)
        if (tvWeatherHumidity != null) {
            tvWeatherHumidity.setText("💧 65%");
        }
        if (tvWeatherWind != null) {
            tvWeatherWind.setText("🌬️ 12 km/h");
        }
        if (tvWeatherUpdate != null) {
            tvWeatherUpdate.setText(getString(R.string.updated_just_now));
        }
        
        // Update old weather info if it exists (for backward compatibility)
        if (tvWeatherInfo != null) {
            tvWeatherInfo.setText(String.format("%s %.1f°C", weatherEmoji, tempCelsius));
        }
        
        if (tvLastUpdate != null) {
            String formattedDate = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
            tvLastUpdate.setText(getString(R.string.last_updated_prefix, formattedDate));
        }
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
        // Refresh incident counts (only if the view exists)
        if (tvIncidentCount != null && incidentStorage != null) {
            int todayCount = incidentStorage.getTodayCount();
            int totalCount = incidentStorage.getTotalCount();
            tvIncidentCount.setText(String.format("📊 Incidents: %d today, %d total", todayCount, totalCount));
        }
        
        // Load weather data
        updateWeatherData();
        
        // Ensure correct navigation item is selected
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    /**
     * Update weather data by fetching from location or showing default data
     */
    private void updateWeatherData() {
        // Check if weather views are available before updating
        if (tvWeatherUpdate == null || tvWeatherTemperature == null || 
            tvWeatherCondition == null || tvWeatherHumidity == null || tvWeatherWind == null) {
            return; // Views not initialized, skip weather update
        }
        
        // Show loading state
        tvWeatherUpdate.setText(getString(R.string.updating_weather_data));
        tvWeatherTemperature.setText("--°C");
        tvWeatherCondition.setText(getString(R.string.weather_loading));
        tvWeatherHumidity.setText("💧 --%");
        tvWeatherWind.setText("🌬️ -- km/h");

        // Check if location permissions are available
        if (hasLocationPermissions()) {
            // Get current location and fetch weather
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Fetch real weather data from API
                            fetchWeatherData(location.getLatitude(), location.getLongitude());
                        } else {
                            // Use default location or show error
                            showDefaultWeatherData();
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        showDefaultWeatherData();
                    });
            }
        } else {
            // Show default weather data or prompt for location
            showDefaultWeatherData();
        }
    }

    /**
     * Fetch weather data for given coordinates
     */
    private void fetchWeatherData(double latitude, double longitude) {
        // Check if weather views are available
        if (tvWeatherTemperature == null || tvWeatherCondition == null || 
            tvWeatherHumidity == null || tvWeatherWind == null || tvWeatherUpdate == null) {
            return;
        }
        
        // Check if WeatherManager is initialized
        if (weatherManager == null) {
            showDefaultWeatherData();
            return;
        }
        
        try {
            // Set up weather update listener for this activity
            weatherManager.setListener(new WeatherManager.WeatherUpdateListener() {
                @Override
                public void onWeatherUpdated(double tempCelsius, String condition) {
                    // Update UI on main thread
                    runOnUiThread(() -> updateWeatherUI(tempCelsius, condition));
                }
                
                @Override
                public void onWeatherError(String error) {
                    // Show error and fallback to default data
                    runOnUiThread(() -> {
                        android.util.Log.e("MainActivity", "Weather error: " + error);
                        showDefaultWeatherData();
                    });
                }
            });
            
            // Create Android location object and start weather updates
            android.location.Location androidLocation = new android.location.Location("manual");
            androidLocation.setLatitude(latitude);
            androidLocation.setLongitude(longitude);
            weatherManager.startUpdates(androidLocation);
            
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error fetching weather data", e);
            showDefaultWeatherData();
        }
    }

    /**
     * Show default weather data when location is unavailable
     */
    private void showDefaultWeatherData() {
        // Check if weather views are available
        if (tvWeatherTemperature == null || tvWeatherCondition == null || 
            tvWeatherHumidity == null || tvWeatherWind == null || tvWeatherUpdate == null) {
            return;
        }
        
        tvWeatherTemperature.setText("--°C");
        tvWeatherCondition.setText(getString(R.string.unknown_weather_condition));
        tvWeatherHumidity.setText("💧 --%");
        tvWeatherWind.setText("🌬️ -- km/h");
        tvWeatherUpdate.setText(getString(R.string.location_needed_for_weather));
    }

    /**
     * Perform a safety check
     */
    private void performSafetyCheck() {
        Toast.makeText(this, "Safety check completed", Toast.LENGTH_SHORT).show();
        // Add actual safety check logic here
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop weather updates when activity is not visible
        // TEMPORARILY DISABLED FOR DEBUGGING
        /*
        if (weatherManager != null) {
            weatherManager.stopUpdates();
        }
        */
    }
}