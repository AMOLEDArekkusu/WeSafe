package my.utar.edu.toothless.wesafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Main activity of the WeSafe app - serves as the home/dashboard screen
 * Displays current status, quick actions, and safety tips
 */
public class MainActivity extends AppCompatActivity {

    // UI Components
    private TextView tvWelcome, tvLocationStatus, tvWeatherInfo, tvIncidentCount, tvLastUpdate;
    private Button btnViewMap, btnReportIncident, btnEmergencyContacts, btnSettings;
    private FloatingActionButton fabPanicButton;
    private CardView cardQuickActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize toolbar
        setSupportActionBar(findViewById(R.id.toolbar));

        // Initialize UI components
        initializeViews();

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
        // I want to kill myself

        // For now, it just shows a confirmation dialog
        new android.app.AlertDialog.Builder(this)
                .setTitle("Emergency Alert")
                .setMessage("Are you sure you want to trigger an emergency alert? This will notify your emergency contacts and share your location.")
                .setPositiveButton("Yes, Alert Contacts", (dialog, which) -> {
                    // Implement emergency alert functionality
                    // sendEmergencyAlerts();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}