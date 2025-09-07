package my.utar.edu.toothless.wesafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

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
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
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
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
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
        
        // Add padding to prevent controls from overlapping with FABs
        // Left padding for zoom controls, bottom padding for FABs
        mMap.setPadding(16, 0, 160, 250);
        
        // Move zoom controls to bottom-left
        if (mapFragment != null && mapFragment.getView() != null) {
            View zoomControls = mapFragment.getView().findViewById(0x1);
            if (zoomControls != null) {
                zoomControls.setVisibility(View.VISIBLE);
                zoomControls.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT));
            }
        }

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
        hideStatusBar(); // Re-hide status bar when activity resumes
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
