package my.utar.edu.toothless.wesafe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.Toast;
import android.view.View;
import android.widget.RelativeLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location currentLocation;

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_LOCATION_PERMISSION = 1000;

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
        setupLocationServices();
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

    private static final int REQUEST_REPORT_INCIDENT = 1001;

    private void setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                currentLocation = locationResult.getLastLocation();
            }
        };
        requestLocationUpdates();
    }

    private void setupClickListeners() {
        fabReportIncident.setOnClickListener(v -> {
            // Open incident report activity
            Intent intent = new Intent(MapActivity.this, IncidentReportActivity.class);
            if (currentLocation != null) {
                intent.putExtra("latitude", currentLocation.getLatitude());
                intent.putExtra("longitude", currentLocation.getLongitude());
            }
            startActivityForResult(intent, REQUEST_REPORT_INCIDENT);
        });

        fabMyLocation.setOnClickListener(v -> {
            if (mMap != null) {
                if (currentLocation != null) {
                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                } else {
                    Toast.makeText(this, "Getting location...", Toast.LENGTH_SHORT).show();
                    requestLocationUpdates();
                }
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
        
        // Enable the my-location layer if permission is granted
        enableMyLocation();
        
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
        requestLocationUpdates(); // Request location updates when activity resumes
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_REPORT_INCIDENT && resultCode == RESULT_OK) {
            // Refresh the map or add the new incident marker
            // This will be implemented when the backend is ready
            Toast.makeText(this, "Incident reported successfully", Toast.LENGTH_SHORT).show();
        } else if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                // Handle camera photo
                Toast.makeText(this, "Photo captured successfully", Toast.LENGTH_SHORT).show();
                // TODO: Process the captured photo
                // The photo data is in: data.getExtras().get("data") as a Bitmap
            } else if (requestCode == REQUEST_PICK_IMAGE) {
                // Handle picked image
                Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show();
                // TODO: Process the selected image
                // The image URI is in: data.getData()
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private void enableMyLocation() {
        if (mMap == null) {
            return;
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true); // This enables the blue dot
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                REQUEST_LOCATION_PERMISSION);
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(5000)
            .build();

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Try to enable the location layer again
                enableMyLocation();
                // Start location updates
                requestLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
