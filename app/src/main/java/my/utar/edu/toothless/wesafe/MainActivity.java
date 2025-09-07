package my.utar.edu.toothless.wesafe;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

/**
 * Main activity of the WeSafe app - serves as the home/dashboard screen
 * Displays current status, quick actions, and safety tips
 */
public class MainActivity extends AppCompatActivity {

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

    // UI Components
    private TextView tvWelcome, tvLocationStatus, tvWeatherInfo, tvIncidentCount, tvLastUpdate;
    private Button btnViewMap, btnReportIncident, btnEmergencyContacts, btnSettings;
    private FloatingActionButton fabPanicButton;
    private CardView cardQuickActions;

    // Location Components
    private FusedLocationProviderClient fusedLocationClient;
    private boolean locationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        setContentView(R.layout.activity_main);

        // Initialize toolbar
        setSupportActionBar(findViewById(R.id.toolbar));

        // Initialize UI components
        initializeViews();

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check and request permissions
        checkAndRequestPermissions();

        // Set up click listeners
        setupClickListeners();

        // Load data (this would be implemented based on your data sources)
        loadData();
    }

    /**
     * Initialize all view components from the layout
     */
    private void initializeViews() {
        tvWelcome = findViewById(R.id.tv_welcome);
        tvLocationStatus = findViewById(R.id.tv_location_status);
        tvWeatherInfo = findViewById(R.id.tv_weather_info);
        tvIncidentCount = findViewById(R.id.tv_incident_count);
        tvLastUpdate = findViewById(R.id.tv_last_update);

        btnViewMap = findViewById(R.id.btn_view_map);
        btnReportIncident = findViewById(R.id.btn_report_incident);
        btnEmergencyContacts = findViewById(R.id.btn_emergency_contacts);
        btnSettings = findViewById(R.id.btn_settings);

        fabPanicButton = findViewById(R.id.fab_panic_button);
        cardQuickActions = findViewById(R.id.card_quick_actions);
    }

    /**
     * Set up click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Map button - opens MapActivity
        btnViewMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        // Report incident button - opens IncidentReportActivity
        btnReportIncident.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, IncidentReportActivity.class);
            startActivity(intent);
        });

        // Emergency contacts button - opens EmergencyContactsActivity
        btnEmergencyContacts.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EmergencyContactsActivity.class);
            startActivity(intent);
        });

        // Settings button - opens SettingsActivity
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Panic button - triggers emergency actions
        fabPanicButton.setOnClickListener(v -> triggerEmergencyProtocol());
    }

    /**
     * Load data from various sources to populate the dashboard
     */
    private void loadData() {
        // This would typically involve API calls, database queries, etc.
        // For now, we'll set placeholder text

        // Update location status (this would use Location Services in a real implementation)
        tvLocationStatus.setText("📍 Location: San Francisco, CA");

        // Update weather info (this would use a weather API in a real implementation)
        tvWeatherInfo.setText("🌤️ Weather: 72°F, Clear");

        // Update incident count (this would come from your database/API)
        tvIncidentCount.setText("📊 Nearby Incidents: 3 reported today");

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
        if (!hasLocationPermissions()) {
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
                return false;
            }
        }
        return true;
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
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                updateLocationUI(location);
                            }
                        });
            }
        }
    }

    private void getCurrentLocationAndAlert() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // TODO: Implement emergency alert sending
                            Toast.makeText(this, "Emergency alert sent with location: " +
                                    location.getLatitude() + ", " + location.getLongitude(),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Unable to get current location",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateLocationUI(Location location) {
        if (location != null) {
            tvLocationStatus.setText(String.format("📍 Location: %.6f, %.6f",
                    location.getLatitude(), location.getLongitude()));
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

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                     | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                     | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar(); // Re-hide the status bar when activity resumes
        if (!hasLocationPermissions()) {
            showLocationRequiredSnackbar();
        }
    }
}