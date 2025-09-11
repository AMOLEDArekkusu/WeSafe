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
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MapActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FloatingActionButton fabReportIncident, fabMyLocation;
    private BottomNavigationView bottomNavigation;
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
        // Remove back arrow - no longer needed with bottom navigation

        initializeViews();
        setupLocationServices();
        setupMap();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initializeViews() {
        fabReportIncident = findViewById(R.id.fab_report_incident);
        fabMyLocation = findViewById(R.id.fab_my_location);
        bottomNavigation = findViewById(R.id.bottom_navigation);
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
                Location newLocation = locationResult.getLastLocation();
                if (newLocation != null) {
                    boolean isFirstLocation = (currentLocation == null);
                    currentLocation = newLocation;
                    
                    // Move camera to current location if this is the first location update
                    // and the map is ready
                    if (isFirstLocation && mMap != null) {
                        moveToCurrentLocation();
                    }
                }
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
                    Toast.makeText(this, getString(R.string.getting_location_toast), Toast.LENGTH_SHORT).show();
                    requestLocationUpdates();
                }
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_maps);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(MapActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_contact) {
                startActivity(new Intent(MapActivity.this, EmergencyContactsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_maps) {
                // Already on maps activity
                return true;
            } else if (itemId == R.id.nav_report) {
                startActivity(new Intent(MapActivity.this, IncidentReportActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(MapActivity.this, SettingsActivity.class));
                finish();
                return true;
            }
            return false;
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
        
        // Try to get current location immediately
        getCurrentLocation();
        
        // Add padding to prevent controls from overlapping with FABs and bottom navigation
        // Left padding for zoom controls, bottom padding for bottom navigation and FABs
        mMap.setPadding(16, 0, 160, 320);
        
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
                .title(getString(R.string.sample_incident))
                .snippet(getString(R.string.incident_description, "30 mins")));

        // Move camera to current location if available, otherwise use sample location
        moveToCurrentLocation();

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

    private void moveToCurrentLocation() {
        if (mMap == null) {
            return;
        }
        
        if (currentLocation != null) {
            // Move camera to current location
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
        } else {
            // Try to get last known location
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = location;
                        if (mMap != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                        }
                    } else {
                        // If no last known location, use sample location as fallback
                        LatLng sampleLocation = new LatLng(37.7749, -122.4194);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sampleLocation, 12));
                    }
                })
                .addOnFailureListener(this, e -> {
                    // If location retrieval fails, use sample location as fallback
                    LatLng sampleLocation = new LatLng(37.7749, -122.4194);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sampleLocation, 12));
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar(); // Re-hide status bar when activity resumes
        requestLocationUpdates(); // Request location updates when activity resumes
        
        // Move to current location if map is ready
        if (mMap != null) {
            getCurrentLocation();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_REPORT_INCIDENT && resultCode == RESULT_OK) {
            // Refresh the map or add the new incident marker
            // This will be implemented when the backend is ready
            Toast.makeText(this, getString(R.string.incident_reported), Toast.LENGTH_SHORT).show();
        } else if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                // Handle camera photo
                Toast.makeText(this, getString(R.string.photo_captured), Toast.LENGTH_SHORT).show();
                // TODO: Process the captured photo
                // The photo data is in: data.getExtras().get("data") as a Bitmap
            } else if (requestCode == REQUEST_PICK_IMAGE) {
                // Handle picked image
                Toast.makeText(this, getString(R.string.image_selected), Toast.LENGTH_SHORT).show();
                // TODO: Process the selected image
                // The image URI is in: data.getData()
            }
        }
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
                Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Try to enable the location layer again
                enableMyLocation();
                // Start location updates
                requestLocationUpdates();
            } else {
                Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
