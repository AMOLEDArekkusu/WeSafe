package my.utar.edu.toothless.wesafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FloatingActionButton fabReportIncident, fabMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupMap();
        setupClickListeners();
    }

    private void initializeViews() {
        fabReportIncident = findViewById(R.id.fab_report_incident);
        fabMyLocation = findViewById(R.id.fab_my_location);
    }

    private void setupMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupClickListeners() {
        fabReportIncident.setOnClickListener(v -> {
            // Open incident report activity
            Intent intent = new Intent(MapActivity.this, IncidentReportActivity.class);
            startActivity(intent);
        });

        fabMyLocation.setOnClickListener(v -> {
            // Center map on user's current location
            if (mMap != null) {
                // This would use location services to get current location
                // For now, using a placeholder location
                LatLng currentLocation = new LatLng(37.7749, -122.4194); // San Francisco
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Configure map settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false); // Using our own FAB

        // Add a sample marker (in real app, this would load from database/API)
        LatLng sampleIncident = new LatLng(37.7749, -122.4194);
        mMap.addMarker(new MarkerOptions()
                .position(sampleIncident)
                .title("Sample Incident")
                .snippet("Traffic accident reported 30 mins ago"));

        // Move camera to the sample location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sampleIncident, 12));

        // Set marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            // Show incident details when marker is clicked
            showIncidentDetails(marker.getTitle(), marker.getSnippet());
            return true;
        });
    }

    private void showIncidentDetails(String title, String snippet) {
        // Create and show a dialog with incident details
        IncidentDetailDialog dialog = new IncidentDetailDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("snippet", snippet);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "IncidentDetailDialog");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
